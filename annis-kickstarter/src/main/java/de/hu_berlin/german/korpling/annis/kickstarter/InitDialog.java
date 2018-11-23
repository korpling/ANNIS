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

import annis.administration.CorpusAdministration;
import annis.administration.ImportStatus;
import com.google.common.base.Charsets;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author thomas
 */
public class InitDialog extends javax.swing.JDialog
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    InitDialog.class);
  private CorpusAdministration corpusAdministration;
  private SwingWorker<String, Void> initWorker;
  private Frame parentFrame;

  private class InitDialogWorker extends SwingWorker<String, Void>
    implements Serializable
  {

    private InitDialog parent;
    private List<Map<String, Object>> corpora;

    public InitDialogWorker(InitDialog parent, List<Map<String, Object>> corpora)
    {
      this.parent = parent;
      this.corpora = corpora;
    }

    @Override
    protected String doInBackground() throws Exception
    {
      // get the values from the installation
      File propFile = new File(System.getProperty("annis.home") + "/conf",
        "database.properties");
      try(InputStream propStream = new FileInputStream(propFile))
      {
        Properties prop = new Properties();
        try
        (InputStreamReader propReader = new InputStreamReader(propStream, Charsets.UTF_8)) 
        {
          prop.load(propReader);
        }
        
        String rawDataSourceURI = prop.getProperty("datasource.url", 
          "jdbc:postgresql://localhost:5432/anniskickstart").trim();
        
        URI uri = new URI(rawDataSourceURI.substring("jdbc:".length()));
        
        corpusAdministration.initializeDatabase(
          uri.getHost(), "" + uri.getPort(),
          uri.getPath().substring(1), // remove / at beginning
          prop.getProperty("datasource.username", "anniskickstart").trim(), 
          prop.getProperty("datasource.password", "annisKickstartPassword").trim(), 
          "postgres",
          txtAdminUsername.getText(), 
          new String(txtAdminPassword.getPassword()), 
          prop.getProperty("datasource.ssl", "false").trim().equalsIgnoreCase("true"),
          prop.getProperty("datasource.schema", "public"));
        
        // also perform a cleanup of the data directory
        // when using kickstarter you are either using just the default one instance
        // or you can change the service settings defining where to put the data files to
        corpusAdministration.cleanupData();

        return "";
      }
      catch (DataAccessException | IOException | URISyntaxException ex)
      { 
        parent.setVisible(false);
        ImportStatus importStatus = corpusAdministration.getAdministrationDao().initImportStatus();
        importStatus.addException("init database exception:", ex);
        ExceptionDialog dlg = new ExceptionDialog(parent, importStatus);
        dlg.setVisible(true);
      }

      return "ERROR";
    }
    @Override
    protected void done()
    {
      pbInit.setIndeterminate(false);
      btOk.setEnabled(true);
      btCancel.setEnabled(true);
      try
      {
        if ("".equals(this.get()))
        {
          pbInit.setValue(100);
          if (corpora != null && corpora.size() > 0)
          {
            setVisible(false);
            // migrate corpora
            ImportDialog importDlg = new ImportDialog(parentFrame, true,
              corpusAdministration, corpora);
            importDlg.setVisible(true);
          }
          else
          {
            
            JOptionPane.showMessageDialog(null, "Database initialized.", "INFO",
              JOptionPane.INFORMATION_MESSAGE);
            setVisible(false);

          }

        }
        else
        {
          pbInit.setValue(0);
        }
      }
      catch (InterruptedException ex)
      {
        log.error(null, ex);
      }
      catch (ExecutionException ex)
      {
        log.error(null, ex);
      }
    }
  }

  /**
   * Creates new form InitDialog
   */
  public InitDialog(java.awt.Frame parent, boolean modal,
    final CorpusAdministration corpusAdministration)
  {
    super(parent, modal);

    this.parentFrame = parent;
    initComponents();

    getRootPane().setDefaultButton(btOk);
    this.corpusAdministration = corpusAdministration;

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
    jLabel2 = new javax.swing.JLabel();
    btOk = new javax.swing.JButton();
    btCancel = new javax.swing.JButton();
    txtAdminUsername = new javax.swing.JTextField();
    txtAdminPassword = new javax.swing.JPasswordField();
    pbInit = new javax.swing.JProgressBar();
    cbMigrate = new javax.swing.JCheckBox();

    fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Init - ANNIS Kickstarter");
    setLocationByPlatform(true);

    jLabel1.setText("Postgres-Admin username:");

    jLabel2.setText("Postgres-Admin password:");

    btOk.setMnemonic('o');
    btOk.setText("Ok");
    btOk.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btOkActionPerformed(evt);
      }
    });

    btCancel.setMnemonic('c');
    btCancel.setText("Cancel");
    btCancel.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btCancelActionPerformed(evt);
      }
    });

    txtAdminUsername.setText("postgres");

    cbMigrate.setText("migrate already imported corpora (will take same time as re-import)");
    cbMigrate.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbMigrateActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(pbInit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(txtAdminUsername))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel2)
            .addGap(18, 18, 18)
            .addComponent(txtAdminPassword))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(btCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btOk, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(cbMigrate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(txtAdminUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(txtAdminPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(cbMigrate)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(pbInit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(btOk)
          .addComponent(btCancel))
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btCancelActionPerformed
    {//GEN-HEADEREND:event_btCancelActionPerformed

      setVisible(false);

    }//GEN-LAST:event_btCancelActionPerformed

  private void checkCorpusSourcePathExists(Map<String, Object> corpus)
  {
    if (!corpus.containsKey("source_path"))
    {
      // show dialog to the user 
      int result = JOptionPane.showConfirmDialog(this,
        "No source directory given for "
        + "corpus \"" + corpus.get("name")
        + "\". Do you want to manually select a directory?",
        "Cannot find \"" + corpus.get("name") + "\"",
        JOptionPane.YES_NO_OPTION);

      if (result == JOptionPane.YES_OPTION)
      {
        // show directory chooser
        int fileChooseResult = fileChooser.showDialog(this, "Select");
        if (fileChooseResult == JFileChooser.APPROVE_OPTION)
        {
          corpus.put("source_path", fileChooser.getSelectedFile().
            getAbsolutePath());
        }
      }
    }

  }

    private void btOkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btOkActionPerformed
    {//GEN-HEADEREND:event_btOkActionPerformed

      pbInit.setIndeterminate(true);
      btOk.setEnabled(false);
      btCancel.setEnabled(false);

      // collect all necessary information for migration
      List<Map<String, Object>> existingCorpora = new LinkedList<Map<String, Object>>();
      if (cbMigrate.isSelected())
      {
        // catch all existing corpora
        try
        {
          existingCorpora = corpusAdministration.listCorpusStats();
        }
        catch(Exception ex)
        {
          log.warn("Could not get existing corpus list for migration, migrating "
          + "the corpora will be disabled.", ex);
          JOptionPane.showMessageDialog(parentFrame, 
            "Could not get existing corpus list for migration, migrating "
          + "the corpora will be disabled.");
        }
        // check if any of these corpora needs more information from the user
        for (Map<String, Object> corpus : existingCorpora)
        {
          checkCorpusSourcePathExists(corpus);
        }
      }

      initWorker = new InitDialogWorker(this, existingCorpora);

      // do the actual work
      initWorker.execute();

    }//GEN-LAST:event_btOkActionPerformed

  private void cbMigrateActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbMigrateActionPerformed
  {//GEN-HEADEREND:event_cbMigrateActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_cbMigrateActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btCancel;
  private javax.swing.JButton btOk;
  private javax.swing.JCheckBox cbMigrate;
  private javax.swing.JFileChooser fileChooser;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JProgressBar pbInit;
  private javax.swing.JPasswordField txtAdminPassword;
  private javax.swing.JTextField txtAdminUsername;
  // End of variables declaration//GEN-END:variables
}
