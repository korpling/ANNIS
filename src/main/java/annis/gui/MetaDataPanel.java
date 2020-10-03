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
package annis.gui;

import annis.gui.components.ExceptionDialog;
import annis.libgui.Background;
import annis.libgui.Helper;
import com.google.common.util.concurrent.FutureCallback;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.corpus_tools.annis.api.SearchApi;
import org.corpus_tools.annis.api.model.AnnoKey;
import org.corpus_tools.annis.api.model.Annotation;
import org.corpus_tools.salt.core.SMetaAnnotation;

/**
 * Provides all corpus annotations for a corpus or for a specific search result.
 *
 * // TODO cleanup the toplevelCorpus side effects.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class MetaDataPanel extends Panel {
  /**
   * 
   */
  private static final long serialVersionUID = -3607697674053863447L;


  private VerticalLayout layout;

  private String toplevelCorpusName;

  // this is only set if the metadata panel is called from a specific result.
  private Optional<String> documentName;

  // holds the current corpus annotation table, when called from corpus browser
  private Grid<Annotation> corpusAnnotationTable = null;

  private final ProgressBar progress = new ProgressBar();

  /**
   * this empty label is currently use for empty metadata list on the left side of the corpusbrowser
   */
  private Label emptyLabel = new Label("(no metadata)");

  public MetaDataPanel(String toplevelCorpusName) {
    this(toplevelCorpusName, Optional.empty());
  }

  public MetaDataPanel(String toplevelCorpusName, Optional<String> documentName) {
    super("Metadata");

    this.toplevelCorpusName = toplevelCorpusName;
    this.documentName = documentName;

    setSizeFull();
    layout = new VerticalLayout();
    layout.setSizeFull();
    setContent(layout);

    progress.setIndeterminate(true);
    progress.setSizeFull();

    layout.addComponent(progress);
    layout.setComponentAlignment(progress, Alignment.MIDDLE_CENTER);

  }

  /**
   * Places a label in the middle center of the corpus browser panel.
   */
  private void addEmptyLabel() {
    if (emptyLabel == null) {
      emptyLabel = new Label("none");
    }

    if (corpusAnnotationTable != null) {
      layout.removeComponent(corpusAnnotationTable);
    }

    layout.addComponent(emptyLabel);

    // this has only an effect after adding the component to a parent. Bug by
    // vaadin?
    emptyLabel.setSizeUndefined();

    layout.setComponentAlignment(emptyLabel, Alignment.MIDDLE_CENTER);
    layout.setExpandRatio(emptyLabel, 1.0f);
  }

  @Override
  public void attach() {
    super.attach();

    final UI ui = getUI();

    Background.runWithCallback(() -> {
      List<Annotation> result = new LinkedList<>();
      SearchApi api = new SearchApi(Helper.getClient(ui));

      // Get the corpus graph and with it the meta data on the corpus/document nodes
      Collection<SMetaAnnotation> annos;
      if (documentName.isPresent()) {
        annos = Helper.getMetaDataDoc(toplevelCorpusName, documentName.get(), ui);
      } else {
        annos = Helper.getMetaData(toplevelCorpusName, Optional.empty(), ui);
      }
      for (SMetaAnnotation metaAnno : annos) {
        Annotation anno = new Annotation();
        AnnoKey key = new AnnoKey();
        key.setNs(metaAnno.getNamespace());
        key.setName(metaAnno.getName());
        anno.setKey(key);
        anno.setVal(metaAnno.getValue_STEXT());
        result.add(anno);
      }
      return result;
    }, new FutureCallback<List<Annotation>>() {
      @Override
      public void onFailure(Throwable t) {
        layout.removeComponent(progress);
        ExceptionDialog.show(t, "Could not get meta data", getUI());
      }

      @Override
      public void onSuccess(List<Annotation> result) {
        layout.removeComponent(progress);
        Accordion accordion = new Accordion();
        accordion.setSizeFull();

        // set output to none if no metadata are available
        if (result.isEmpty()) {
          addEmptyLabel();
        } else {
          String path = documentName.isPresent() ? "document: " + documentName.get()
              : "corpus: " + toplevelCorpusName;

          accordion.addTab(setupTable(new ListDataProvider<>(result)), path);

          layout.addComponent(accordion);
        }

      }
    });

  }

  private Grid<Annotation> setupTable(ListDataProvider<Annotation> metaData) {
    ValueProvider<Annotation, String> nameProvider = anno -> Helper.getQName(anno.getKey());
    metaData.setSortOrder(nameProvider, SortDirection.ASCENDING);

    Grid<Annotation> tblMeta = new Grid<>(Annotation.class);
    tblMeta.setDataProvider(metaData);
    Column<Annotation, String> nameColumn = tblMeta.addColumn(nameProvider);
    nameColumn.setWidthUndefined();
    nameColumn.setCaption("Name");
    nameColumn.setId("genname");
    Column<Annotation, ?> valueColumn =
        tblMeta.addComponentColumn(anno -> new Label(anno.getVal(), ContentMode.HTML));
    valueColumn.setId("genval");
    valueColumn.setCaption("Value");

    tblMeta.setColumns(nameColumn.getId(), valueColumn.getId());

    tblMeta.setSizeFull();
    valueColumn.setExpandRatio(1);
    return tblMeta;
  }
}
