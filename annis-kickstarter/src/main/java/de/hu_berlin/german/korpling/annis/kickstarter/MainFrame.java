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

import annis.AnnisBaseRunner;
import annis.administration.CorpusAdministration;
import annis.service.internal.AnnisServiceRunner;
import annis.utils.Utils;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class MainFrame extends javax.swing.JFrame
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    MainFrame.class);

  private class MainFrameWorker extends SwingWorker<String, String>
    implements Serializable
  {

    @Override
    protected String doInBackground() throws Exception
    {
      setProgress(1);
      try
      {
        startService();
        setProgress(2);
        startJetty();
      }
      catch (Exception ex)
      {
        return ex.getLocalizedMessage();
      }
      return "";
    }

    private void startService() throws Exception
    {

      // starts RMI service at bean creation
      AnnisServiceRunner runner = new AnnisServiceRunner();
      runner.setUseAuthentification(false);
      runner.start(true);
    }

    private void startJetty() throws Exception
    {
      // disable jetty logging
      org.eclipse.jetty.util.log.Log.setLog(new JettyNoLogger());
      
      Server jetty = new Server(8080);
      // add context for our bundled webapp
      WebAppContext context = new WebAppContext("./webapp/", "/annis-gui");
      context.setInitParameter("managerClassName",
        "annis.security.TestSecurityManager");
      String webxmlOverrride = System.getProperty("annis.home")
        + "/conf/override-web.xml";//ClassLoader.getSystemResource("webxmloverride.xml").toString();
      List<String> listWebXMLOverride = new LinkedList<String>();
      listWebXMLOverride.add(webxmlOverrride);
      context.setOverrideDescriptors(listWebXMLOverride);
      
      
      // Exclude some jersey classes explicitly from the web application classpath.
      // If they still exists some automatic dependency resolution of Jersey will
      // fail.
      // Whenever we add new dependencies on jersey classes for the service but 
      // not for the GUI and "Missing dependency" errors occur, add the classes
      // to the server class list
      context.addServerClass("com.sun.jersey.json.");
      context.addServerClass("com.sun.jersey.server.");
      
      jetty.setHandler(context);

      // start
      jetty.start();
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
  } //end MainFrameWorker class
  private CorpusAdministration corpusAdministration;
  private SwingWorker<String, String> serviceWorker;
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


    // init corpusAdministration
    System.setProperty("annis.home", ".");
    this.corpusAdministration =
      (CorpusAdministration) AnnisBaseRunner.getBean("corpusAdministration",
      true, "file:"
      + Utils.getAnnisFile("conf/spring/Admin.xml").getAbsolutePath());

    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception ex)
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
        new ExceptionDialog(ex).setVisible(true);
      }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
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
          .addComponent(btExit, javax.swing.GroupLayout.Alignment.TRAILING))
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
        .addComponent(btExit)
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
      catch (Exception ex)
      {
        new ExceptionDialog(this, ex).setVisible(true);
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
  private javax.swing.JLabel lblStatusService;
  private javax.swing.JProgressBar pbStart;
  // End of variables declaration//GEN-END:variables
}
