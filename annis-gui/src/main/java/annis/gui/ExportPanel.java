/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui;

import java.io.File;

import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import annis.gui.components.HelpButton;
import annis.gui.controlpanel.QueryPanel;
import annis.gui.controlpanel.SearchOptionsPanel;
import annis.gui.converter.CommaSeperatedStringConverterList;
import annis.gui.objects.QueryUIState;
import annis.libgui.AnnisBaseUI;
import annis.libgui.PluginSystem;
import annis.libgui.exporter.ExporterPlugin;
import net.sf.ehcache.CacheException;
import net.xeoh.plugins.base.util.PluginManagerUtil;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ExportPanel extends GridLayout
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExportPanel.class);

  private final ComboBox cbLeftContext;

  private final ComboBox cbRightContext;

  private final TextField txtAnnotationKeys;

  private final TextField txtParameters;

  private final BeanItemContainer<Class> exporterClassContainer = new BeanItemContainer<>(Class.class);

  private final ComboBox cbExporter;

  private final Button btDownload;

  private final Button btExport;

  private final Button btCancel;

  private final QueryPanel queryPanel;

  private File tmpOutputFile;

  private final ProgressBar progressBar;

  private final Label progressLabel;

  private FileDownloader downloader;

  private final transient EventBus eventBus;

  private transient Stopwatch exportTime = Stopwatch.createUnstarted();

  private final QueryController controller;

  private UI ui;
  private final QueryUIState state;

  private final FormLayout formLayout;
  private final Label lblHelp;
  
  private final PluginSystem ps;
  
  private final CheckBox cbAlignmc;

  public ExportPanel(QueryPanel queryPanel, QueryController controller, QueryUIState state, PluginSystem ps)
  {
    super(2, 3);
    this.queryPanel = queryPanel;
    this.controller = controller;
    this.state = state;
    this.ps = ps;

    this.eventBus = new EventBus();
    this.eventBus.register(ExportPanel.this);

    this.formLayout = new FormLayout();
    formLayout.setWidth("-1px");

    setWidth("99%");
    setHeight("-1px");

    setColumnExpandRatio(0, 0.0f);
    setColumnExpandRatio(1, 1.0f);
    
    cbExporter = new ComboBox("Exporter");
    cbExporter.setNewItemsAllowed(false);
    cbExporter.setNullSelectionAllowed(false);
    cbExporter.setImmediate(true);
    cbExporter.setPropertyDataSource(controller.getState().getExporter());
    cbExporter.setContainerDataSource(exporterClassContainer);
  
    
    cbExporter.addValueChangeListener(new ExporterSelectionHelpListener());

    formLayout.addComponent(cbExporter);
    addComponent(formLayout, 0, 0);

    lblHelp = new Label();
    lblHelp.setContentMode(ContentMode.HTML);
    addComponent(lblHelp, 1, 0);
    

    cbLeftContext = new ComboBox("Left Context");
    cbRightContext = new ComboBox("Right Context");

    cbLeftContext.setNullSelectionAllowed(false);
    cbRightContext.setNullSelectionAllowed(false);

    cbLeftContext.setNewItemsAllowed(true);
    cbRightContext.setNewItemsAllowed(true);

    cbLeftContext.addValidator(
        new IntegerRangeValidator("must be a number", Integer.MIN_VALUE, Integer.MAX_VALUE));
    cbRightContext.addValidator(
        new IntegerRangeValidator("must be a number", Integer.MIN_VALUE, Integer.MAX_VALUE));

    for (Integer i : SearchOptionsPanel.PREDEFINED_CONTEXTS)
    {
      cbLeftContext.addItem(i);
      cbRightContext.addItem(i);
    }

    cbLeftContext.setValue(5);
    cbRightContext.setValue(5);

    formLayout.addComponent(cbLeftContext);
    formLayout.addComponent(cbRightContext);

    txtAnnotationKeys = new TextField("Annotation Keys");
    txtAnnotationKeys.setDescription("Some exporters will use this comma "
        + "seperated list of annotation keys to limit the exported data to these "
        + "annotations.");
    formLayout.addComponent(new HelpButton<String>(txtAnnotationKeys));

    txtParameters = new TextField("Parameters");
    txtParameters.setDescription("You can input special parameters "
        + "for certain exporters. See the description of each exporter "
        + "(‘?’ button above) for specific parameter settings.");
    formLayout.addComponent(new HelpButton<String>(txtParameters));
    
  //check box for match-with-context exporter
    cbAlignmc = new CheckBox("align matches" +"<br/>" + "by node number");
    cbAlignmc.setCaptionAsHtml(true);
    cbAlignmc.setDescription("Click here to align export result by node number.");
    cbAlignmc.setEnabled(true);
    formLayout.addComponent(cbAlignmc);

    btExport = new Button("Perform Export");
    btExport.setIcon(FontAwesome.PLAY);
    btExport.setDisableOnClick(true);
    btExport.addClickListener(new ExportButtonListener());

    btCancel = new Button("Cancel Export");
    btCancel.setIcon(FontAwesome.TIMES_CIRCLE);
    btCancel.setEnabled(false);
    btCancel.addClickListener(new CancelButtonListener());
    

    btDownload = new Button("Download");
    btDownload.setDescription("Click here to start the actual download.");
    btDownload.setIcon(FontAwesome.DOWNLOAD);
    btDownload.setDisableOnClick(true);
    btDownload.setEnabled(false);
    
    

    HorizontalLayout layoutExportButtons = new HorizontalLayout(btExport, btCancel, btDownload);
    addComponent(layoutExportButtons, 0, 1, 1, 1);

    VerticalLayout vLayout = new VerticalLayout();
    addComponent(vLayout, 0, 2, 1, 2);

    progressBar = new ProgressBar();
    progressBar.setVisible(false);
    progressBar.setIndeterminate(true);
    vLayout.addComponent(progressBar);

    progressLabel = new Label();
    vLayout.addComponent(progressLabel);

    if (state != null)
    {
      cbLeftContext.setPropertyDataSource(state.getLeftContext());
      cbRightContext.setPropertyDataSource(state.getRightContext());
      cbExporter.setPropertyDataSource(state.getExporter());

      txtAnnotationKeys.setConverter(new CommaSeperatedStringConverterList());
      txtAnnotationKeys.setPropertyDataSource(state.getExportAnnotationKeys());

      txtParameters.setPropertyDataSource(state.getExportParameters());
      
      cbAlignmc.setPropertyDataSource(state.getAlignmc());

    }

  }

  @Override
  public void attach()
  {
    super.attach();
    this.ui = UI.getCurrent();
    
    if (this.ui instanceof AnnisBaseUI)
    {
      PluginManagerUtil util = new PluginManagerUtil(((AnnisBaseUI) getUI()).getPluginManager());
      for (ExporterPlugin e : util.getPlugins(ExporterPlugin.class))
      {
        exporterClassContainer.addItem(e.getClass());
      }
    }
    exporterClassContainer.sort(new Object[] {"simpleName"}, new boolean[] {true});
    cbExporter.setItemCaptionMode(ItemCaptionMode.PROPERTY);
    cbExporter.setItemCaptionPropertyId("simpleName");
    
    if(exporterClassContainer.size() > 0)
    {
      cbExporter.setValue(exporterClassContainer.getIdByIndex(0));
    }
    
  }

  public class ExporterSelectionHelpListener implements Property.ValueChangeListener
  {

    @Override
    public void valueChange(ValueChangeEvent event)
    {
      @SuppressWarnings("unchecked")
      ExporterPlugin exporter = ps.getExporter((Class<? extends ExporterPlugin>) event.getProperty().getValue());
      if (exporter != null)
      {
        btCancel.setVisible(exporter.isCancelable());
        
        cbAlignmc.setVisible(exporter.isAlignable());

        String helpMessage = exporter.getHelpMessage();
        if (helpMessage != null)
        {
          lblHelp.setValue(helpMessage);
        }
        else
        {
          lblHelp.setValue("No help available for this exporter");
        }
      }
      else
      {
        btCancel.setVisible(false);
        cbAlignmc.setVisible(false);
        lblHelp.setValue("No valid exporter selected");
      }
    }
  }

  @Subscribe
  public void handleExportProgress(final Integer exports)
  {
    if (ui != null)
    {
      // if we ui access() here it seems to confuse the isInterrupted() flag
      // of the parent thread and cancelling won't work any longer
      ui.accessSynchronously(new Runnable()
      {
        @Override
        public void run()
        {
          if (exportTime != null && exportTime.isRunning())
          {
            progressLabel.setValue("exported " + exports + " items in " + exportTime.toString());
          }
          else
          {
            progressLabel.setValue("exported " + exports + " items");
          }
        }

      });
    }
  }

  @Override
  public void detach()
  {
    super.detach();
    if (tmpOutputFile != null && tmpOutputFile.exists())
    {
      if (!tmpOutputFile.delete())
      {
        log.warn("Could not delete {}", tmpOutputFile.getAbsolutePath());
      }
    }
  }

  public void showResult(File currentTmpFile, Exception exportError)
  {
    btExport.setEnabled(true);
    btCancel.setEnabled(false);
    progressBar.setVisible(false);
    progressLabel.setValue("");

    // copy the result to the class member in order to delete if
    // when not longer needed
    tmpOutputFile = currentTmpFile;
    //
    if (exportError instanceof CacheException | exportError instanceof IllegalStateException 
    		| exportError instanceof  ClassCastException)
    {
    	 Notification.show(exportError.getMessage(), Notification.Type.ERROR_MESSAGE);
    }
    else  if (tmpOutputFile == null)
    {
      Notification.show("Could not create the Exporter",
          "The server logs might contain more information about this "
              + "so you should contact the provider of this ANNIS installation " + "for help.",
          Notification.Type.ERROR_MESSAGE);
    }
    else if (exportError instanceof InterruptedException)
    {
      // we were aborted, don't do anything
      Notification.show("Export cancelled", Notification.Type.WARNING_MESSAGE);
    }
    
    else
    {
      if (downloader != null && btDownload.getExtensions().contains(downloader))
      {
        btDownload.removeExtension(downloader);
      }
      downloader = new FileDownloader(new FileResource(tmpOutputFile));

      downloader.extend(btDownload);
      btDownload.setEnabled(true);

      Notification.show("Export finished",
          "Click on the button right to the export button to actually download the file.",
          Notification.Type.HUMANIZED_MESSAGE);
    }
  }

  private class ExportButtonListener implements Button.ClickListener
  {

    @Override
    public void buttonClick(ClickEvent event)
    {
      // clean up old export
      if (tmpOutputFile != null && tmpOutputFile.exists())
      {
        if (!tmpOutputFile.delete())
        {
          log.warn("Could not delete {}", tmpOutputFile.getAbsolutePath());
        }
      }
      tmpOutputFile = null;

      @SuppressWarnings("unchecked")
      final ExporterPlugin exporter = ps.getExporter((Class<? extends ExporterPlugin>) cbExporter.getValue());
      if (exporter != null)
      {
        if ("".equals(queryPanel.getQuery()))
        {
          Notification.show("Empty query", Notification.Type.WARNING_MESSAGE);
          btExport.setEnabled(true);
          return;
        }
        else if (state.getSelectedCorpora().getValue().isEmpty())
        {
          Notification.show("Please select a corpus", Notification.Type.WARNING_MESSAGE);
          btExport.setEnabled(true);
          return;
        }

        btDownload.setEnabled(false);
        progressBar.setVisible(true);
        progressLabel.setValue("");

        if (exporter.isCancelable())
        {
          btCancel.setEnabled(true);
          btCancel.setDisableOnClick(true);
        }

        controller.executeExport(ExportPanel.this, eventBus);

        if (exportTime == null)
        {
          exportTime = Stopwatch.createUnstarted();
        }
        exportTime.reset();
        exportTime.start();

      }
    }
  }

  private class CancelButtonListener implements Button.ClickListener
  {

    @Override
    public void buttonClick(ClickEvent event)
    {
      controller.cancelExport();
    }

  }

}
