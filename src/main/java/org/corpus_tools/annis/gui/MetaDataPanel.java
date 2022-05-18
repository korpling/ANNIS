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

import com.google.common.collect.ComparisonChain;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.corpus_tools.annis.api.model.AnnoKey;
import org.corpus_tools.annis.api.model.Annotation;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.eclipse.emf.common.util.URI;

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
      // Get the corpus graph and with it the meta data on the corpus/document nodes
      return Helper.getMetaData(toplevelCorpusName, documentName, ui);
    }, new FutureCallback<SCorpusGraph>() {
      @Override
      public void onFailure(Throwable t) {
        layout.removeComponent(progress);
        ExceptionDialog.show(t, "Could not get meta data", getUI());
      }

      @Override
      public void onSuccess(SCorpusGraph result) {
        layout.removeComponent(progress);
        Accordion accordion = new Accordion();
        accordion.setSizeFull();

        boolean hasResult = false;

        // Add all document metadata first, then the corpus metadata
        List<SDocument> documents = result.getDocuments();
        if (documents != null) {
          // There should only be one document in the corpus graph, but keeping the code generic
          // should not hurt
          for (SDocument d : documents) {
            List<Annotation> docAnnos = new ArrayList<>();
            for (SMetaAnnotation metaAnno : d.getMetaAnnotations()) {
              Annotation anno = new Annotation();
              AnnoKey key = new AnnoKey();
              key.setNs(metaAnno.getNamespace());
              key.setName(metaAnno.getName());
              anno.setKey(key);
              anno.setVal(metaAnno.getValue_STEXT());
              docAnnos.add(anno);
            }

            if (!docAnnos.isEmpty()) {
              String path = d.getName();

              // In case we are called to only output a corpus, this might have been mapped as
              // document. So only add the "document" suffix in case we are sure it is an actual
              // corpus.
              if (documentName.isPresent()) {
                path = path + " (document)";
              }

              accordion.addTab(setupTable(new ListDataProvider<>(docAnnos)), path);
              hasResult = true;
            }
          }


        }
        // Sort the (sub-) corpora so sub-corpora come first
        List<SCorpus> corpora = new ArrayList<>(result.getCorpora());
        corpora.sort(new Comparator<SCorpus>() {
          public int compare(SCorpus c1, SCorpus c2) {
            URI u1 = c1.getPath();
            URI u2 = c2.getPath();
            return ComparisonChain.start().compare(u1.segmentCount(), u2.segmentCount())
                .compare(u1.toString(), u2.toString()).result();
          };
        });

        for (SCorpus c : corpora) {
          List<Annotation> corpusAnnos = new ArrayList<>();
          for (SMetaAnnotation metaAnno : c.getMetaAnnotations()) {
            Annotation anno = new Annotation();
            AnnoKey key = new AnnoKey();
            key.setNs(metaAnno.getNamespace());
            key.setName(metaAnno.getName());
            anno.setKey(key);
            anno.setVal(metaAnno.getValue_STEXT());
            corpusAnnos.add(anno);
          }

          if (!corpusAnnos.isEmpty()) {
            String path = c.getPath().toString();
            if (path.startsWith("salt:/")) {
              path = path.substring("salt:/".length());
            }
            path = path + " (corpus)";
            accordion.addTab(setupTable(new ListDataProvider<>(corpusAnnos)), path);
            hasResult = true;
          }
        }

        // set output to none if no metadata are available
        if (hasResult) {
          layout.addComponent(accordion);
        } else {
          addEmptyLabel();
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
