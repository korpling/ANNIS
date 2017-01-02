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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.LoggerFactory;

import annis.AnnisBaseRunner;
import annis.administration.CorpusAdministration;
import annis.administration.ImportStatus;
import annis.utils.Utils;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import javax.swing.UIDefaults;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

/**
 *
 * @author thomas
 */
public class MainFrame extends javax.swing.JFrame
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    MainFrame.class);

  private class MainFrameWorker extends SwingWorker<String, String>
  {
    
    private final KickstartRunner delegate = new KickstartRunner();

    @Override
    protected String doInBackground() throws Exception
    {
      delegate.resetRunner();
      setProgress(1);
      try
      {
        delegate.startService();
        setProgress(2);
        delegate.startJetty();
      }
      catch (Exception ex)
      {
        return ex.getLocalizedMessage();
      }
      return "";
    }


    @Override
    protected void done()
    {
      try 
      {
        final String result = get();
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            handleServiceStartResult(result);
          }
        });
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
    
    public void setTimeoutDisabled(boolean disabled)
    {
      delegate.setTimeoutDisabled(disabled);
    }
    
  } //end MainFrameWorker class
  private CorpusAdministration corpusAdministration;
  private MainFrameWorker serviceWorker;
  private boolean wasStarted = false;
  
  
  /**
   * Creates new form MainFrame
   */
  public MainFrame()
  {
    Integer[] sizes = new Integer[]
    {
      192, 128, 64, 48, 32, 16, 14
    };
    List<Image> allImages = new LinkedList<Image>();

    for (int s : sizes)
    {
      try
      {
        BufferedImage imgIcon = ImageIO.read(MainFrame.class.getResource(
          "logo/annis_" + s + ".png"));
        allImages.add(imgIcon);
      }
      catch (IOException ex)
      {
        log.error(null, ex);
      }
    }
    this.setIconImages(allImages);

    // find the location of the kickstarter
    if(System.getProperty("annis.home") == null)
    {
      try
      {
       URL classLocation = getClass().getProtectionDomain().getCodeSource().getLocation();
       File jarFile = new File(classLocation.toURI());
       // check if this is an actual jar file or only a folder
       if(jarFile.isFile())
       {
         System.setProperty("annis.home", jarFile.getParent());
       }
       else
       {
         // fallback to current working directory
         System.setProperty("annis.home", ".");
       }
      }
      catch(SecurityException | URISyntaxException ex)
      {
        log.warn("Could not reliable get the location of ANNIS Kickstarter, fallback to working directory is used.", ex);
        // fallback to current working directory
        System.setProperty("annis.home", ".");
      }
    }
    

    // init corpusAdministration
    this.corpusAdministration =
      (CorpusAdministration) AnnisBaseRunner.getBean("corpusAdministration",
      true, "file:"
      + Utils.getAnnisFile("conf/spring/Admin.xml").getAbsolutePath());

    try
    {
      UIManager.setLookAndFeel(new NimbusLookAndFeel()
      {
        @Override
        public UIDefaults getDefaults()
        {
          UIDefaults defaults = super.getDefaults();
          GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
          if(gd.getDisplayMode().getWidth() > 2000)
          {
            defaults.put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 18));
          }
          return defaults;
        }
        
      });
    }
    catch (UnsupportedLookAndFeelException ex)
    {
      log.error(null, ex);
    }

    initComponents();


    serviceWorker = new MainFrameWorker();
    serviceWorker.addPropertyChangeListener(new PropertyChangeListener()
    {

      public void propertyChange(PropertyChangeEvent evt)
      {
        if (serviceWorker.getProgress() == 1)
        {
          pbStart.setIndeterminate(true);
          lblStatusService.setText("<html>Starting ANNIS...</html>");
          lblStatusService.setIcon(
            new javax.swing.ImageIcon(
            getClass().getResource(
            "/de/hu_berlin/german/korpling/annis/kickstarter/crystal_icons/quick_restart.png")));
        }
      }
    });


    if (isInitialized() && !serviceWorker.isDone())
    {

      btImport.setEnabled(true);
      btList.setEnabled(true);

      serviceWorker.execute();

    }
  }
  
  private void handleServiceStartResult(String result)
  {
    try
      {
        wasStarted = true;
        pbStart.setIndeterminate(false);
        pbStart.setValue(100);
        if ("".equals(result))
        {
          lblStatusService.setText("<html>ANNIS started</html>");
          lblStatusService.setIcon(
            new javax.swing.ImageIcon(
            MainFrame.class.getResource(
            "/de/hu_berlin/german/korpling/annis/kickstarter/crystal_icons/button_ok.png")));
          btLaunch.setEnabled(true);
          btLaunch.setForeground(Color.blue);
          cbDisableTimeout.setEnabled(true);
        }
        else
        {
          lblStatusService.setText("<html>ANNIS start failed:<br>" + result + "</html>");
          lblStatusService.setIcon(
            new javax.swing.ImageIcon(
            getClass().getResource(
            "/de/hu_berlin/german/korpling/annis/kickstarter/crystal_icons/no.png")));
        }
      }
      catch (Exception ex)
      {
        ImportStatus importStatus = corpusAdministration
          .getAdministrationDao().initImportStatus();
        importStatus.addException("unknown exception", ex);
        new ExceptionDialog(importStatus).setVisible(true);
      }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    btInit = new javax.swing.JButton();
    btImport = new javax.swing.JButton();
    btList = new javax.swing.JButton();
    lblStatusService = new javax.swing.JLabel();
    btLaunch = new javax.swing.JButton();
    pbStart = new javax.swing.JProgressBar();
    btExit = new javax.swing.JButton();
    cbDisableTimeout = new javax.swing.JCheckBox();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("ANNIS Kickstarter");
    setLocationByPlatform(true);

    btInit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/german/korpling/annis/kickstarter/crystal_icons/db_comit.png"))); // NOI18N
    btInit.setMnemonic('d');
    btInit.setText("Init database");
    btInit.setToolTipText("<html>\nBefore you can use ANNIS the very first time<br>\nyou have to initialize the database. <br><br>\n\nPlease note that if you initialize a database that was<br>\nalready in use you will delete all imported corpora<br>\nof this database.\n</html>");
    btInit.setName("btInit"); // NOI18N
    btInit.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btInitActionPerformed(evt);
      }
    });

    btImport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/german/korpling/annis/kickstarter/crystal_icons/db_add.png"))); // NOI18N
    btImport.setMnemonic('i');
    btImport.setText("Import corpus");
    btImport.setToolTipText("<html>\nImport a new corpus to ANNIS.\n</html>");
    btImport.setEnabled(false);
    btImport.setName("btImport"); // NOI18N
    btImport.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btImportActionPerformed(evt);
      }
    });

    btList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/german/korpling/annis/kickstarter/crystal_icons/month.png"))); // NOI18N
    btList.setMnemonic('l');
    btList.setText("List imported corpora");
    btList.setToolTipText("<html>\nList all existing corpora of the database.<br>\nYou can delete copora here as well.\n</html>");
    btList.setEnabled(false);
    btList.setName("btList"); // NOI18N
    btList.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btListActionPerformed(evt);
      }
    });

    lblStatusService.setFont(new java.awt.Font("DejaVu Sans", 0, 18)); // NOI18N
    lblStatusService.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblStatusService.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/german/korpling/annis/kickstarter/crystal_icons/no.png"))); // NOI18N
    lblStatusService.setText("<html>Annis stopped</html>");
    lblStatusService.setName("lblStatusService"); // NOI18N

    btLaunch.setForeground(java.awt.Color.lightGray);
    btLaunch.setMnemonic('u');
    btLaunch.setText("<html><u>Launch ANNIS frontend</u></html>");
    btLaunch.setActionCommand("Launch Frontend");
    btLaunch.setEnabled(false);
    btLaunch.setName("btLaunch"); // NOI18N
    btLaunch.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mouseEntered(java.awt.event.MouseEvent evt)
      {
        btLaunchMouseEntered(evt);
      }
      public void mouseExited(java.awt.event.MouseEvent evt)
      {
        btLaunchMouseExited(evt);
      }
    });
    btLaunch.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btLaunchActionPerformed(evt);
      }
    });

    pbStart.setName("pbStart"); // NOI18N

    btExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/german/korpling/annis/kickstarter/crystal_icons/exit.png"))); // NOI18N
    btExit.setMnemonic('e');
    btExit.setText("Exit");
    btExit.setToolTipText("<html>\nThis will terminate the application.\n</html>");
    btExit.setName("btExit"); // NOI18N
    btExit.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btExitActionPerformed(evt);
      }
    });

    cbDisableTimeout.setText("disable query timeout");
    cbDisableTimeout.setToolTipText("If this checkbox is active no query timeout will be applied. Please note the only way to abort the query is either the automatic timeout or by closing the ANNIS kickstarter.");
    cbDisableTimeout.setEnabled(false);
    cbDisableTimeout.setName("cbDisableTimeout"); // NOI18N
    cbDisableTimeout.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbDisableTimeoutActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(btInit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
          .addComponent(btImport, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
          .addComponent(btList, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
          .addComponent(lblStatusService, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
          .addComponent(pbStart, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
          .addComponent(btLaunch, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(cbDisableTimeout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btExit)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(btInit)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btImport)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btList)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(lblStatusService, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(pbStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btLaunch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(btExit)
          .addComponent(cbDisableTimeout))
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void btInitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btInitActionPerformed
    {//GEN-HEADEREND:event_btInitActionPerformed

      InitDialog dlg = new InitDialog(this, true, corpusAdministration);
      dlg.setVisible(true);

      if (!wasStarted && isInitialized() && !serviceWorker.isDone())
      {
        btImport.setEnabled(true);
        btList.setEnabled(true);
        serviceWorker.execute();
      }

    }//GEN-LAST:event_btInitActionPerformed

    private void btImportActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btImportActionPerformed
    {//GEN-HEADEREND:event_btImportActionPerformed

      ImportDialog dlg = new ImportDialog(this, true, corpusAdministration);
      dlg.setVisible(true);

    }//GEN-LAST:event_btImportActionPerformed

    private void btListActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btListActionPerformed
    {//GEN-HEADEREND:event_btListActionPerformed

      ListDialog dlg = new ListDialog(this, true, corpusAdministration);
      dlg.setVisible(true);

    }//GEN-LAST:event_btListActionPerformed

    private void btLaunchActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btLaunchActionPerformed
    {//GEN-HEADEREND:event_btLaunchActionPerformed
      try
      {
        Desktop.getDesktop().browse(new URI(
          "http://localhost:8080/annis-gui/"));
      }
      catch (IOException | URISyntaxException ex)
      {
       ImportStatus importStatus = corpusAdministration
          .getAdministrationDao().initImportStatus();
        importStatus.addException("unknown exception", ex);
        new ExceptionDialog(importStatus).setVisible(true);
      }

    }//GEN-LAST:event_btLaunchActionPerformed

    private void btExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btExitActionPerformed
    {//GEN-HEADEREND:event_btExitActionPerformed

      System.exit(0);

    }//GEN-LAST:event_btExitActionPerformed

    private void btLaunchMouseEntered(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btLaunchMouseEntered
    {//GEN-HEADEREND:event_btLaunchMouseEntered

      this.setCursor(new Cursor(Cursor.HAND_CURSOR));

    }//GEN-LAST:event_btLaunchMouseEntered

    private void btLaunchMouseExited(java.awt.event.MouseEvent evt)//GEN-FIRST:event_btLaunchMouseExited
    {//GEN-HEADEREND:event_btLaunchMouseExited

      this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

    }//GEN-LAST:event_btLaunchMouseExited

  private void cbDisableTimeoutActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbDisableTimeoutActionPerformed
  {//GEN-HEADEREND:event_cbDisableTimeoutActionPerformed
    
    if(serviceWorker != null)
    {
      serviceWorker.setTimeoutDisabled(cbDisableTimeout.isSelected());
    }
    
  }//GEN-LAST:event_cbDisableTimeoutActionPerformed


  private boolean isInitialized()
  {
    if(corpusAdministration.checkDatabaseSchemaVersion() == false)
    {
      btInit.setText("Init to update your database");
      return false;
    }
      
    // hack, just try to list corpora
    try
    {
      corpusAdministration.listCorpusStats();
    }
    catch (Exception ex)
    {
      return false;
    }

    return true;
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {

      public void run()
      {
        MainFrame frame = new MainFrame();
        frame.setVisible(true);
      }
    });
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btExit;
  private javax.swing.JButton btImport;
  private javax.swing.JButton btInit;
  private javax.swing.JButton btLaunch;
  private javax.swing.JButton btList;
  private javax.swing.JCheckBox cbDisableTimeout;
  private javax.swing.JLabel lblStatusService;
  private javax.swing.JProgressBar pbStart;
  // End of variables declaration//GEN-END:variables
}
