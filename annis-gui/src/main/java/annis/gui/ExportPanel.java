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

import annis.gui.beans.HistoryEntry;
import annis.gui.components.HelpButton;
import annis.gui.controlpanel.CorpusListPanel;
import annis.gui.controlpanel.QueryPanel;
import annis.gui.controlpanel.SearchOptionsPanel;
import annis.gui.exporter.CSVExporter;
import annis.gui.exporter.Exporter;
import annis.gui.exporter.GridExporter;
import annis.gui.exporter.SimpleTextExporter;
import annis.gui.exporter.TextExporter;
import annis.gui.exporter.WekaExporter;
import annis.libgui.Helper;
import annis.libgui.PollControl;
import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class ExportPanel extends FormLayout
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    ExportPanel.class);

  private final ComboBox cbLeftContext;

  private final ComboBox cbRightContext;

  private final TextField txtAnnotationKeys;

  private final TextField txtParameters;

  private static final Exporter[] EXPORTER = new Exporter[]
  {
    new WekaExporter(),
    new CSVExporter(),
    new TextExporter(),
    new GridExporter(),
    new SimpleTextExporter()
  };

  private final Map<String, String> help4Exporter = new HashMap<>();

  private final ComboBox cbExporter;

  private final Button btDownload;

  private final Button btExport;

  private final Button btCancel;

  private final Map<String, Exporter> exporterMap;

  private final QueryPanel queryPanel;

  private final CorpusListPanel corpusListPanel;

  private File tmpOutputFile;

  private final ProgressBar progressBar;

  private final Label progressLabel;

  private FileDownloader downloader;

  private final transient EventBus eventBus;

  private transient Stopwatch exportTime = new Stopwatch();

  private final QueryController controller;

  private transient Future<File> exportFuture = null;

  private UI ui;
  
  public ExportPanel(QueryPanel queryPanel, CorpusListPanel corpusListPanel,
    QueryController controller)
  {
    this.queryPanel = queryPanel;
    this.corpusListPanel = corpusListPanel;
    this.controller = controller;

    this.eventBus = new EventBus();
    this.eventBus.register(this);
    
    setWidth("99%");
    setHeight("-1px");

    initHelpMessages();

    cbExporter = new ComboBox("Exporter");
    cbExporter.setNewItemsAllowed(false);
    cbExporter.setNullSelectionAllowed(false);
    cbExporter.setImmediate(true);
    exporterMap = new HashMap<>();
    for (Exporter e : EXPORTER)
    {
      String name = e.getClass().getSimpleName();
      exporterMap.put(name, e);
      cbExporter.addItem(name);
    }
    cbExporter.setValue(EXPORTER[0].getClass().getSimpleName());
    cbExporter.addValueChangeListener(new ExporterSelectionHelpListener());
    cbExporter.setDescription(help4Exporter.get((String) cbExporter.getValue()));

    addComponent(new HelpButton(cbExporter));

    cbLeftContext = new ComboBox("Left Context");
    cbRightContext = new ComboBox("Right Context");

    cbLeftContext.setNullSelectionAllowed(false);
    cbRightContext.setNullSelectionAllowed(false);

    cbLeftContext.setNewItemsAllowed(true);
    cbRightContext.setNewItemsAllowed(true);

    cbLeftContext.addValidator(new IntegerRangeValidator("must be a number",
      Integer.MIN_VALUE, Integer.MAX_VALUE));
    cbRightContext.addValidator(new IntegerRangeValidator("must be a number",
      Integer.MIN_VALUE, Integer.MAX_VALUE));

    for (Integer i : SearchOptionsPanel.PREDEFINED_CONTEXTS)
    {
      cbLeftContext.addItem(i);
      cbRightContext.addItem(i);
    }

    cbLeftContext.setValue(5);
    cbRightContext.setValue(5);

    addComponent(cbLeftContext);
    addComponent(cbRightContext);

    txtAnnotationKeys = new TextField("Annotation Keys");
    txtAnnotationKeys.setDescription("Some exporters will use this comma "
      + "seperated list of annotation keys to limit the exported data to these "
      + "annotations.");
    addComponent(new HelpButton(txtAnnotationKeys));

    txtParameters = new TextField("Parameters");
    txtParameters.setDescription("You can input special parameters "
      + "for certain exporters. See the description of each exporter "
      + "(‘?’ button above) for specific parameter settings.");
    addComponent(new HelpButton(txtParameters));

    btExport = new Button("Perform Export");
    btExport.setIcon(new ThemeResource(
      "images/tango-icons/16x16/media-playback-start.png"));
    btExport.setDisableOnClick(true);
    btExport.addClickListener(new ExportButtonListener());

    btCancel = new Button("Cancel Export");
    btCancel.setIcon(new ThemeResource("images/tango-icons/16x16/process-stop.png"));
    btCancel.setEnabled(false);
    btCancel.addClickListener(new CancelButtonListener());
    Exporter exporter = exporterMap.get((String) cbExporter.getValue());
    if(exporter != null)
    {
      btCancel.setVisible(exporter.isCancelable());
    }

    btDownload = new Button("Download");
    btDownload.setDescription("Click here to start the actual download.");
    btDownload.setIcon(new ThemeResource("images/tango-icons/16x16/document-save.png"));
    btDownload.setDisableOnClick(true);
    btDownload.setEnabled(false);

    HorizontalLayout layoutExportButtons = new HorizontalLayout(btExport,
      btCancel,
      btDownload);
    addComponent(layoutExportButtons);

    VerticalLayout vLayout = new VerticalLayout();
    addComponent(vLayout);

    progressBar = new ProgressBar();
    progressBar.setEnabled(false);
    progressBar.setIndeterminate(true);
    vLayout.addComponent(progressBar);

    progressLabel = new Label();
    vLayout.addComponent(progressLabel);
  }

  @Override
  public void attach()
  {
    super.attach();
    this.ui = UI.getCurrent();
  }
  
  

  private void initHelpMessages()
  {
    help4Exporter.put(EXPORTER[0].getClass().getSimpleName(),
      "The WEKA Exporter exports only the "
      + "values of the elements searched for by the user, ignoring the context "
      + "around search results. The values for all annotations of each of the "
      + "found nodes is given in a comma-separated table (CSV). At the top of "
      + "the export, the names of the columns are given in order according to "
      + "the WEKA format.<br/><br/>"
      + "Parameters: <br/>"
      + "<em>metakeys</em> - comma seperated list of all meta data to include in the result (e.g. "
      + "<code>metakeys=title,documentname</code>)");

    help4Exporter.put(EXPORTER[1].getClass().getSimpleName(),
      "The CSV Exporter exports only the "
      + "values of the elements searched for by the user, ignoring the context "
      + "around search results. The values for all annotations of each of the "
      + "found nodes is given in a comma-separated table (CSV). <br/><br/>"
      + "Parameters: <br/>"
      + "<em>metakeys</em> - comma seperated list of all meta data to include in the result (e.g. "
      + "<code>metakeys=title,documentname</code>)");

    help4Exporter.put(EXPORTER[2].getClass().getSimpleName(),
      "The Text Exporter exports just the plain text of every search result and "
      + "its context, one line per result.");

    help4Exporter.put(EXPORTER[3].getClass().getSimpleName(),
      "The Grid Exporter can export all annotations of a search result and its "
      + "context. Each annotation layer is represented in a separate line, and the "
      + "tokens covered by each annotation are given as number ranges after each "
      + "annotation in brackets. To suppress token numbers, input numbers=false "
      + "into the parameters box below. To display only a subset of annotations "
      + "in any order use the \"Annotation keys\" text field, input e.g. \"tok,pos,cat\" "
      + "to show tokens and the "
      + "annotations pos and cat.<br /><br />"
      + "Parameters: <br/>"
      + "<em>metakeys</em> - comma seperated list of all meta data to include in the result (e.g. "
      + "<code>metakeys=title,documentname</code>) <br />"
      + "<em>numbers</em> - set to \"false\" if the grid event numbers should not be included in the output (e.g. "
      + "<code>numbers=false</code>)");
  }

  public class ExporterSelectionHelpListener implements
    Property.ValueChangeListener
  {

    @Override
    public void valueChange(ValueChangeEvent event)
    {
      String helpMessage = help4Exporter.get((String) event.getProperty().
        getValue());
      if (helpMessage != null)
      {
        cbExporter.setDescription(helpMessage);
      }
      else
      {
        cbExporter.setDescription("No help available for this exporter");
      }
      
      Exporter exporter = exporterMap.get((String) event.getProperty().getValue());
      if(exporter != null)
      {
        btCancel.setVisible(exporter.isCancelable());
      }
      
    }
  }

  @Subscribe
  public void handleExportProgress(final Integer exports)
  {
    if(ui != null)
    {
      ui.access(new Runnable()
      {

        @Override
        public void run()
        {
          if (exportTime != null && exportTime.isRunning())
          {
            progressLabel.setValue(
              "exported " + exports + " items in " + exportTime.toString());
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
      if (exportFuture != null && !exportFuture.isDone())
      {
        exportFuture.cancel(true);
      }
      exportFuture = null;

      String exporterName = (String) cbExporter.getValue();
      final Exporter exporter = exporterMap.get(exporterName);
      if (exporter != null)
      {
        if (corpusListPanel.getSelectedCorpora().isEmpty())
        {
          Notification.show("Please select a corpus",
            Notification.Type.WARNING_MESSAGE);
          btExport.setEnabled(true);
          return;
        }

        HistoryEntry e = new HistoryEntry();
        e.setCorpora(corpusListPanel.getSelectedCorpora());
        e.setQuery(queryPanel.getQuery());
        controller.addHistoryEntry(e);
        progressBar.setEnabled(true);
        progressLabel.setValue("");

        if (exporter.isCancelable())
        {
          btCancel.setEnabled(true);
          btCancel.setDisableOnClick(true);
        }

        exportFuture = PollControl.callInBackground(1000, null,
          new BackgroundJob(exporter, ui));
        if (exportFuture != null)
        {
          if (exportTime == null)
          {
            exportTime = new Stopwatch();
          }
          exportTime.reset();
          exportTime.start();
        }
      }
    }
  }

  private class CancelButtonListener implements Button.ClickListener
  {

    @Override
    public void buttonClick(ClickEvent event)
    {
      if (exportFuture != null)
      {
        if (!exportFuture.cancel(true))
        {
          log.warn("Could not cancel export");
        }
      }
    }

  }

  private class BackgroundJob implements Callable<File>
  {

    private final Exporter exporter;
    private final UI ui;

    public BackgroundJob(Exporter exporter, UI ui)
    {
      this.exporter = exporter;
      this.ui = ui;
    }

    @Override
    public File call() throws Exception
    {
      final File currentTmpFile = File.createTempFile("annis-export", ".txt");
      currentTmpFile.deleteOnExit();

      

      final AtomicBoolean success = new AtomicBoolean(false);
      try(OutputStreamWriter outWriter
        = new OutputStreamWriter(new FileOutputStream(currentTmpFile), "UTF-8");)
      {
        exporter.convertText(queryPanel.getQuery(),
          (Integer) cbLeftContext.getValue(),
          (Integer) cbRightContext.getValue(),
          corpusListPanel.getSelectedCorpora(),
          txtAnnotationKeys.getValue(),
          txtParameters.getValue(),
          Helper.getAnnisWebResource().path("query"),
          outWriter, eventBus);
        success.set(true);
      }
      finally
      {
        ui.access(new Runnable()
        {
          @Override
          public void run()
          {
            btExport.setEnabled(true);
            btCancel.setEnabled(false);
            progressBar.setEnabled(false);
            progressLabel.setValue("");

            // copy the result to the class member in order to delete if
            // when not longer needed
            tmpOutputFile = currentTmpFile;

            if (tmpOutputFile == null)
            {
              Notification.show("Could not create the Exporter",
                "The server logs might contain more information about this "
                + "so you should contact the provider of this ANNIS installation "
                + "for help.", Notification.Type.ERROR_MESSAGE);
            }
            else if (!success.get())
            {
              // we were aborted, don't do anything
              Notification.show("Export cancelled",
                Notification.Type.WARNING_MESSAGE);
            }
            else
            {
              if (downloader != null && btDownload.getExtensions().contains(
                downloader))
              {
                btDownload.removeExtension(downloader);
              }
              downloader = new FileDownloader(new FileResource(
                tmpOutputFile));

              downloader.extend(btDownload);
              btDownload.setEnabled(true);

              Notification.show("Export finished",
                "Click on the button right to the export button to actually download the file.",
                Notification.Type.HUMANIZED_MESSAGE);
            }
          }
        });
      }
      return currentTmpFile;
    }

  }

}
