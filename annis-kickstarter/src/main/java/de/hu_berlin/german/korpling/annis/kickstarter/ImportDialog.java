/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.hu_berlin.german.korpling.annis.kickstarter;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import annis.administration.CorpusAdministration;
import annis.administration.ImportStatus;
import annis.administration.StatementController;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;

/**
 *
 * @author thomas
 */
public class ImportDialog extends javax.swing.JDialog
{

  private static final org.slf4j.Logger log =
    LoggerFactory.getLogger(ImportDialog.class);

  private File confFile;

  private Properties confProps;

  private transient StatementController statementController;


  private static class StatementControllerImpl implements StatementController
  {

    PreparedStatement statement = null;

    Boolean isCancelled = false;

    @Override
    public void registerStatement(PreparedStatement statement)
    {

      this.statement = statement;
      if (isCancelled)
      {
        cancelStatements();
      }
    }

    @Override
    public void cancelStatements()
    {
      isCancelled = true;
      if (statement != null)
      {
        try
        {
          log.info("cancel statement");
          statement.cancel();
        }
        catch (SQLException ex)
        {
          log.error("problems with interrupting statement", ex);
        }
      }

    }

    @Override
    public boolean isCancelled()
    {
      return isCancelled;
    }
  }

  private class ImportDialogWorker extends SwingWorker<ImportStatus, Void>
  {

    StatementController statementController;

    public ImportDialogWorker(StatementController statementController)
    {
      this.statementController = statementController;
    }

    @Override
    protected ImportStatus doInBackground() throws Exception
    {
      StringBuilder errorMessages = new StringBuilder();

      if (corpora == null)
      {
        try
        {
          SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              lblCurrentCorpus.setText("import " + StringUtils.abbreviateMiddle(
                txtInputDir.getText(), "...", 50));
              pbCorpus.setMaximum(1);
              pbCorpus.setMinimum(0);
              pbCorpus.setValue(0);
            }
          });

          corpusAdministration.getAdministrationDao().registerGUICancelThread(
            statementController);

          importStatus.add(corpusAdministration.importCorporaSave(
            jCheckBox1.isSelected(), null, null, false, txtInputDir.getText()));

        }
        catch (Exception ex)
        {
          importStatus.addException("unknown error: ", ex);
          return importStatus;
        }
      }
      else
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            pbCorpus.setMaximum(corpora.size());
            pbCorpus.setMinimum(0);
            pbCorpus.setValue(0);
          }
        });

        int i = 0;
        for (Map<String, Object> corpus : corpora)
        {
          if (isCancelled())
          {
            return importStatus;
          }

          if (corpus.containsKey("source_path"))
          {
            final String path = (String) corpus.get("source_path");

            final int finalI = i;
            SwingUtilities.invokeLater(new Runnable()
            {
              @Override
              public void run()
              {
                lblCurrentCorpus.setText("import "
                  + StringUtils.abbreviateMiddle(path, "...", 40)
                  + " [" + (finalI + 1) + "/" + corpora.size() + "]");
                pbCorpus.setValue(finalI);
              }
            });

            try
            {

              corpusAdministration.getAdministrationDao().
                registerGUICancelThread(statementController);

              importStatus.add(corpusAdministration.importCorporaSave(
                jCheckBox1.isSelected(), null, null, false, path));
            }
            catch (Exception ex)
            {
              log.error("could not import corpus", ex);
              errorMessages.append("[").append(path).append("]\n");
              errorMessages.append(ex.getMessage()).append(path).append("\n\n");
            }
          }
          i++;
        }
      }

      return importStatus;
    }

    @Override
    protected void done()
    {
      isImporting = false;
      btOk.setEnabled(true);
      btSearchInputDir.setEnabled(true);
      txtInputDir.setEnabled(true);
      lblCurrentCorpus.setText("");
      pbCorpus.setValue(pbCorpus.getMaximum());
      pbImport.setIndeterminate(false);

      try
      {
        if (!isCancelled())
        {
          ImportStatus importStatus = this.get();
          if (importStatus.getStatus())
          {
            JOptionPane.showMessageDialog(null,
              "Corpus imported.", "INFO", JOptionPane.INFORMATION_MESSAGE);
            setVisible(false);
          }
          else
          {
            new ExceptionDialog(importStatus, "Import failed").setVisible(true);
            setVisible(false);
          }
        }
      }
      catch (HeadlessException | InterruptedException | ExecutionException ex)
      {
        log.error(null, ex);
      }
    }
  }
  private transient CorpusAdministration corpusAdministration;

  private ImportStatus importStatus;

  private transient SwingWorker<ImportStatus, Void> worker;

  private boolean isImporting;

  private List<Map<String, Object>> corpora;

  public ImportDialog(java.awt.Frame parent, boolean modal,
    CorpusAdministration corpusAdmin)
  {
    this(parent, modal, corpusAdmin, null);
  }

  /**
   * Creates new form ImportDialog
   */
  public ImportDialog(java.awt.Frame parent, boolean modal,
    CorpusAdministration corpusAdmin, List<Map<String, Object>> corpora)
  {
    super(parent, modal);
    initTransients();
    
    this.corpusAdministration = corpusAdmin;
    this.importStatus =  this.corpusAdministration.getAdministrationDao()
      .initImportStatus();
    this.corpora = corpora;

    confProps = new Properties();
    confFile = new File(System.getProperty("user.home")
      + "/.annis/kickstart.properties");
    try
    {
      if (!confFile.exists())
      {
        if (!confFile.getParentFile().mkdirs())
        {
          log.warn("Cannot create directory " + confFile.getAbsolutePath());
        }
        if (!confFile.createNewFile())
        {
          log.warn("Cannot create file " + confFile.getAbsolutePath());
        }
      }
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }

    initComponents();
    
    this.fileChooser.setFileFilter(new FileNameExtensionFilter("ZIP file (*.zip) or directory", "zip"));



    loadProperties();

    getRootPane().setDefaultButton(btOk);

    isImporting = false;
    
    addAppender();

    // directly start import if we were called from outside
    if (this.corpora != null)
    {
      startImport();
    }

  }
  
  private void initTransients()
  {
    statementController = new StatementControllerImpl();
    worker = new ImportDialogWorker(statementController);
  }
  
  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    initTransients();
  }

  private void storeProperties()
  {
    confProps.put("last-directory", txtInputDir.getText());
    try(FileOutputStream oStream = new FileOutputStream(confFile))
    {
      confProps.store(oStream, "");
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
  }

  private void loadProperties()
  {
    try(FileInputStream iStream = new FileInputStream(confFile);)
    {
      confProps.load(iStream);
      String lastDirectory = confProps.getProperty("last-directory");
      if (lastDirectory != null)
      {
        txtInputDir.setText(lastDirectory);
      }

    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }

  }

  private void addAppender()
  {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(lc);


    Appender<ILoggingEvent> appender = new AppenderBase<ILoggingEvent>()
    {
      @Override
      protected void append(ILoggingEvent event)
      {
        if (event.getLevel().isGreaterOrEqual(Level.INFO))
        {
          lblStatus.setText(event.getFormattedMessage());
        }
      }
    };
    ch.qos.logback.classic.Logger rootLogger = lc.getLogger(
      Logger.ROOT_LOGGER_NAME);
    rootLogger.addAppender(appender);
    appender.start();
  }

  private void startImport()
  {
    btOk.setEnabled(false);
    btSearchInputDir.setEnabled(false);
    txtInputDir.setEnabled(false);

    pbImport.setIndeterminate(true);

    isImporting = true;
    worker.execute();

  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    fileChooser = new javax.swing.JFileChooser();
    jLabel1 = new javax.swing.JLabel();
    txtInputDir = new javax.swing.JTextField();
    btCancel = new javax.swing.JButton();
    btOk = new javax.swing.JButton();
    btSearchInputDir = new javax.swing.JButton();
    pbImport = new javax.swing.JProgressBar();
    jLabel2 = new javax.swing.JLabel();
    lblStatus = new javax.swing.JLabel();
    pbCorpus = new javax.swing.JProgressBar();
    lblCurrentCorpus = new javax.swing.JLabel();
    jCheckBox1 = new javax.swing.JCheckBox();

    fileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Import - ANNIS Kickstarter");
    setLocationByPlatform(true);

    jLabel1.setText("Directory to import:");

    btCancel.setMnemonic('c');
    btCancel.setText("Cancel");
    btCancel.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btCancelActionPerformed(evt);
      }
    });

    btOk.setMnemonic('o');
    btOk.setText("Ok");
    btOk.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btOkActionPerformed(evt);
      }
    });

    btSearchInputDir.setText("...");
    btSearchInputDir.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btSearchInputDirActionPerformed(evt);
      }
    });

    jLabel2.setText("status:");

    lblStatus.setText("...");

    lblCurrentCorpus.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblCurrentCorpus.setText("Please select corpus for import!");

    jCheckBox1.setText("overwrite");
    jCheckBox1.setMnemonic('w');
    jCheckBox1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jCheckBox1ActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(pbImport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(txtInputDir, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btSearchInputDir, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(jCheckBox1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(lblCurrentCorpus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(pbCorpus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(btCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btOk, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(txtInputDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(btSearchInputDir))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(pbCorpus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(lblCurrentCorpus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox1)))
        .addGap(6, 6, 6)
        .addComponent(pbImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(lblStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(btCancel)
          .addComponent(btOk))
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btCancelActionPerformed
    {//GEN-HEADEREND:event_btCancelActionPerformed

      if (isImporting)
      {
        worker.cancel(true);
        statementController.cancelStatements();
      }
      setVisible(false);
    }//GEN-LAST:event_btCancelActionPerformed

    private void btOkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btOkActionPerformed
    {//GEN-HEADEREND:event_btOkActionPerformed

      startImport();
    }//GEN-LAST:event_btOkActionPerformed

    private void btSearchInputDirActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btSearchInputDirActionPerformed
    {//GEN-HEADEREND:event_btSearchInputDirActionPerformed

      if (!"".equals(txtInputDir.getText()))
      {
        File f = new File(txtInputDir.getText());
        if (f.exists() && (f.isDirectory() || "zip".equals(Files.getFileExtension(f.getName()))))
        {
          fileChooser.setSelectedFile(f);
        }
      }

      if (fileChooser.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION)
      {
        File f = fileChooser.getSelectedFile();
        txtInputDir.setText(f.getAbsolutePath());
        storeProperties();
      }

    }//GEN-LAST:event_btSearchInputDirActionPerformed

  private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
  }//GEN-LAST:event_jCheckBox1ActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btCancel;
  private javax.swing.JButton btOk;
  private javax.swing.JButton btSearchInputDir;
  private javax.swing.JFileChooser fileChooser;
  private javax.swing.JCheckBox jCheckBox1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel lblCurrentCorpus;
  private javax.swing.JLabel lblStatus;
  private javax.swing.JProgressBar pbCorpus;
  private javax.swing.JProgressBar pbImport;
  private javax.swing.JTextField txtInputDir;
  // End of variables declaration//GEN-END:variables
}
