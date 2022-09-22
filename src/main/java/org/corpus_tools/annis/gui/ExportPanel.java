/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.data.Binder;
import com.vaadin.data.HasValue;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox.NewItemProvider;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextField;
import java.io.File;
import java.util.Optional;
import org.corpus_tools.annis.gui.components.HelpButton;
import org.corpus_tools.annis.gui.controlpanel.SearchOptionsPanel;
import org.corpus_tools.annis.gui.converter.CommaSeperatedStringConverterList;
import org.corpus_tools.annis.gui.exporter.ExporterPlugin;
import org.corpus_tools.annis.gui.objects.QueryUIState;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class ExportPanel extends GridLayout {


  private class CancelButtonListener implements Button.ClickListener {

    /**
     * 
     */
    private static final long serialVersionUID = -4013613247987995764L;

    @Override
    public void buttonClick(ClickEvent event) {
      ui.getQueryController().cancelExport();
    }

  }

  private class ExportButtonListener implements Button.ClickListener {

    /**
     * 
     */
    private static final long serialVersionUID = -6350964897738024356L;

    @Override
    public void buttonClick(ClickEvent event) {
      // clean up old export
      if (tmpOutputFile != null && tmpOutputFile.exists()) {
        if (!tmpOutputFile.delete()) {
          log.warn("Could not delete {}", tmpOutputFile.getAbsolutePath());
        }
      }
      tmpOutputFile = null;

      Optional<ExporterPlugin> exporter = ui.getExporterPlugins().stream()
          .filter((e) -> e.getClass().equals(ui.getQueryState().getExporter())).findAny();

      if (exporter.isPresent()) {
        if ("".equals(ui.getQueryState().getAql().getValue())) {
          Notification.show("Empty query", Notification.Type.WARNING_MESSAGE);
          btExport.setEnabled(true);
          return;
        } else if (state.getSelectedCorpora().isEmpty()) {
          Notification.show("Please select a corpus", Notification.Type.WARNING_MESSAGE);
          btExport.setEnabled(true);
          return;
        }

        btDownload.setEnabled(false);
        progressBar.setVisible(true);
        progressLabel.setValue("");

        if (exporter.get().isCancelable()) {
          btCancel.setEnabled(true);
          btCancel.setDisableOnClick(true);
        }

        ui.getQueryController().executeExport(ExportPanel.this, eventBus);

        if (exportTime == null) {
          exportTime = Stopwatch.createUnstarted();
        }
        exportTime.reset();
        exportTime.start();

      }
    }
  }

  public class ExporterSelectionHelpListener implements HasValue.ValueChangeListener<String> {

    /**
     * 
     */
    private static final long serialVersionUID = 732470870668073722L;


    @Override
    public void valueChange(com.vaadin.data.HasValue.ValueChangeEvent<String> event) {
      Optional<ExporterPlugin> exporter = ui.getExporterPlugins().stream()
          .filter((e) -> e.getClass().getSimpleName().equals(event.getValue())).findAny();
      if (exporter.isPresent()) {
        btCancel.setVisible(exporter.get().isCancelable());

        cbAlignmc.setVisible(exporter.get().isAlignable());

        String helpMessage = exporter.get().getHelpMessage();
        if (helpMessage != null) {
          lblHelp.setValue(helpMessage);
        } else {
          lblHelp.setValue("No help available for this exporter");
        }

        cbLeftContext.setVisible(exporter.get().needsContext());
        cbRightContext.setVisible(exporter.get().needsContext());
      } else {
        btCancel.setVisible(false);
        cbAlignmc.setVisible(false);
        cbLeftContext.setVisible(false);
        cbRightContext.setVisible(false);
        lblHelp.setValue("No valid exporter selected");
      }
    }

  }

  /**
   * 
   */
  private static final long serialVersionUID = -4669376862731675681L;

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExportPanel.class);

  private final com.vaadin.ui.ComboBox<Integer> cbLeftContext;

  private final com.vaadin.ui.ComboBox<Integer> cbRightContext;

  private final TextField txtAnnotationKeys;

  private final TextField txtParameters;

  private final com.vaadin.ui.ComboBox<String> cbExporter;

  private final Button btDownload;

  private final Button btExport;

  private final Button btCancel;

  private final AnnisUI ui;

  private File tmpOutputFile;

  private final ProgressBar progressBar;

  private final Label progressLabel;

  private FileDownloader downloader;

  private final transient EventBus eventBus;
  private transient Stopwatch exportTime = Stopwatch.createUnstarted();


  private final QueryUIState state;

  private final FormLayout formLayout;

  private final Label lblHelp;

  private final CheckBox cbAlignmc;

  public ExportPanel(AnnisUI ui) {
    super(2, 3);
    this.ui = ui;
    this.state = ui.getQueryState();

    this.eventBus = new EventBus();
    this.eventBus.register(ExportPanel.this);

    this.formLayout = new FormLayout();
    formLayout.setWidth("-1px");

    setWidth("99%");
    setHeight("-1px");

    setColumnExpandRatio(0, 0.0f);
    setColumnExpandRatio(1, 1.0f);

    cbExporter = new com.vaadin.ui.ComboBox<>("Exporter");
    cbExporter.setEmptySelectionAllowed(false);

    cbExporter.addValueChangeListener(new ExporterSelectionHelpListener());

    formLayout.addComponent(cbExporter);
    addComponent(formLayout, 0, 0);

    lblHelp = new Label();
    lblHelp.setContentMode(ContentMode.HTML);
    addComponent(lblHelp, 1, 0);

    cbLeftContext = new com.vaadin.ui.ComboBox<>("Left Context");
    cbRightContext = new com.vaadin.ui.ComboBox<>("Right Context");

    cbLeftContext.setEmptySelectionAllowed(false);
    cbRightContext.setEmptySelectionAllowed(false);

    NewItemProvider<Integer> contextNewItemProvider = v -> {

      try {
        int numericValue = Integer.parseInt(v);
        if (numericValue >= 0) {
          return Optional.of(numericValue);
        }
      } catch (NumberFormatException ex) {

      }

      return Optional.empty();
    };
    cbLeftContext.setNewItemProvider(contextNewItemProvider);
    cbRightContext.setNewItemProvider(contextNewItemProvider);

    cbLeftContext.setItems(SearchOptionsPanel.PREDEFINED_CONTEXTS);
    cbRightContext.setItems(SearchOptionsPanel.PREDEFINED_CONTEXTS);


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

    // check box for match-with-context exporter
    cbAlignmc = new CheckBox("align matches" + "<br/>" + "by node number");
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

    if (state != null) {
      Binder<QueryUIState> binder = ui.getQueryController().getBinder();
      binder.forField(cbLeftContext).bind("leftContext");
      binder.forField(cbRightContext).bind("rightContext");
      binder.forField(cbExporter).bind(source -> source.getExporter().getSimpleName(),
          (field, value) -> {
            // Get the matching plugin from the class name
            Optional<ExporterPlugin> matchingExporter = ui.getExporterPlugins().stream()
                .filter((e) -> e.getClass().getSimpleName().equals(value)).findAny();
            if (matchingExporter.isPresent()) {
              field.setExporter(matchingExporter.get().getClass());
            }
          });

      // Make sure all other binded components are also updated
      cbLeftContext.addSelectionListener(event -> binder.setBean(state));
      cbRightContext.addSelectionListener(event -> binder.setBean(state));

      txtAnnotationKeys.setConverter(new CommaSeperatedStringConverterList());
      txtAnnotationKeys.setPropertyDataSource(state.getExportAnnotationKeys());

      txtParameters.setPropertyDataSource(state.getExportParameters());

      cbAlignmc.setPropertyDataSource(state.getAlignmc());

    }

  }

  @Override
  public void attach() {
    super.attach();

    cbExporter
        .setItems(ui.getExporterPlugins().stream().map(p -> p.getClass().getSimpleName()).sorted());

    IDGenerator.assignIDForFields(this, cbExporter, btDownload, btExport, txtAnnotationKeys,
        txtParameters);
  }

  @Override
  public void detach() {
    super.detach();
    if (tmpOutputFile != null && tmpOutputFile.exists()) {
      if (!tmpOutputFile.delete()) {
        log.warn("Could not delete {}", tmpOutputFile.getAbsolutePath());
      }
    }
  }

  @Subscribe
  public void handleExportProgress(final Integer exports) { // NO_UCD (unused code)
    UI ui = getUI();
    if (ui != null) {
      // if we ui access() here it seems to confuse the isInterrupted() flag
      // of the parent thread and cancelling won't work any longer
      ui.accessSynchronously(() -> {
        if (exportTime != null && exportTime.isRunning()) {
          progressLabel.setValue("exported " + exports + " items in " + exportTime.toString());
        } else {
          progressLabel.setValue("exported " + exports + " items");
        }
      });
    }
  }

  public void showResult(File currentTmpFile, Exception exportError) {
    btExport.setEnabled(true);
    btCancel.setEnabled(false);
    progressBar.setVisible(false);
    progressLabel.setValue("");

    // copy the result to the class member in order to delete if
    // when not longer needed
    tmpOutputFile = currentTmpFile;
    //
    if (exportError instanceof IllegalStateException || exportError instanceof ClassCastException) {
      Notification.show(exportError.getMessage(), Notification.Type.ERROR_MESSAGE);
    } else if (tmpOutputFile == null) {
      Notification.show("Could not create the Exporter",
          "The server logs might contain more information about this "
              + "so you should contact the provider of this ANNIS installation " + "for help.",
          Notification.Type.ERROR_MESSAGE);
    } else if (exportError instanceof InterruptedException) {
      // we were aborted, don't do anything
      Notification.show("Export cancelled", Notification.Type.WARNING_MESSAGE);
    }

    else {
      if (downloader != null && btDownload.getExtensions().contains(downloader)) {
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

}
