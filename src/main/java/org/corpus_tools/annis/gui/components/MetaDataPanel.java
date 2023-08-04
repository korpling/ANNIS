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
package org.corpus_tools.annis.gui.components;

import com.google.common.collect.ComparisonChain;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializableComparator;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.corpus_tools.annis.api.model.AnnoKey;
import org.corpus_tools.annis.api.model.Annotation;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.gui.util.Helper;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.eclipse.emf.common.util.URI;
import reactor.core.publisher.Mono;

/**
 * Provides all corpus annotations for a corpus or for a specific search result.
 *
 * // TODO cleanup the toplevelCorpus side effects.
 *
 * @author Thomas Krause {@literal <thomas.krause@hu-berlin.de>}
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class MetaDataPanel extends Panel {

  private static class CorpusMetadataCallResult {
    SCorpusGraph metadata;
    CorpusConfiguration config;
  }

  private static class ConfiguredSortOrderComparator implements SerializableComparator<Annotation> {

    private static final long serialVersionUID = 1L;
    private ArrayList<String> corpusAnnotationOrder;

    public ConfiguredSortOrderComparator(Collection<String> corpusAnnotationOrder) {
      if (corpusAnnotationOrder == null) {
        this.corpusAnnotationOrder = new ArrayList<>();
      } else {
        this.corpusAnnotationOrder = new ArrayList<>(corpusAnnotationOrder);
      }

    }

    @Override
    public int compare(Annotation o1, Annotation o2) {

      String q1 = Helper.getQName(o1.getKey());
      q1 = q1 == null ? "" : q1;
      String q2 = Helper.getQName(o2.getKey());
      q2 = q2 == null ? "" : q2;


      int pos1 = this.corpusAnnotationOrder.indexOf(q1);
      int pos2 = this.corpusAnnotationOrder.indexOf(q2);

      if (pos1 < 0) {
        pos1 = this.corpusAnnotationOrder.size();
      }
      if (pos2 < 0) {
        pos2 = this.corpusAnnotationOrder.size();
      }
      return ComparisonChain.start().compare(pos1, pos2).compare(q1, q2).result();
    }
  }


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


  @Override
  public void attach() {
    super.attach();

    final UI ui = getUI();

    Mono<CorpusConfiguration> config =
        Helper.getCorpusConfig(toplevelCorpusName, ui).doOnError(ex -> {
          ui.access(() -> {
            layout.removeComponent(progress);
            ExceptionDialog.show(ex, "Could not get meta data", getUI());
          });
        });

    Mono<SCorpusGraph> metaData =
        Helper.getMetaData(toplevelCorpusName, documentName, ui).doOnError(ex -> {
          ui.access(() -> {
            layout.removeComponent(progress);
            ExceptionDialog.show(ex, "Could not get corpus configuration", getUI());
          });
        });

    Mono.zip(config, metaData).subscribe(t -> {

      CorpusMetadataCallResult result = new CorpusMetadataCallResult();
      result.config = t.getT1();
      result.metadata = t.getT2();
      ui.access(() -> {
        layout.removeComponent(progress);
        Accordion accordion = new Accordion();
        accordion.setSizeFull();

        boolean hasDocument = addDocumentMetadata(result, accordion);
        boolean hasCorpus = addCorpusMetadata(result, accordion);

        // set output to none if no metadata are available
        if (hasDocument || hasCorpus) {
          layout.addComponent(accordion);
        } else {
          addEmptyLabel();
        }
      });
    });

  }

  private boolean addCorpusMetadata(CorpusMetadataCallResult result, Accordion accordion) {
    boolean hasResult = false;

    // Sort the (sub-) corpora so sub-corpora come first
    List<SCorpus> corpora = new ArrayList<>(result.metadata.getCorpora());
    corpora.sort((c1, c2) -> {
      URI u1 = c1.getPath();
      URI u2 = c2.getPath();
      return ComparisonChain.start().compare(u1.segmentCount(), u2.segmentCount())
          .compare(u1.toString(), u2.toString()).result();
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
        accordion.addTab(setupTable(new ListDataProvider<>(corpusAnnos), result.config), path);
        hasResult = true;
      }
    }
    return hasResult;
  }

  private boolean addDocumentMetadata(CorpusMetadataCallResult result, Accordion accordion) {
    boolean hasResult = false;

    // Add all document metadata first, then the corpus metadata
    List<SDocument> documents = result.metadata.getDocuments();
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

          accordion.addTab(setupTable(new ListDataProvider<>(docAnnos), result.config), path);
          hasResult = true;
        }
      }
    }
    return hasResult;
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


  private Grid<Annotation> setupTable(ListDataProvider<Annotation> metaData,
      CorpusConfiguration config) {
    ValueProvider<Annotation, String> nameProvider = anno -> Helper.getQName(anno.getKey());


    if (config == null) {
      metaData.setSortOrder(nameProvider, SortDirection.ASCENDING);
    } else {
      metaData.setSortComparator(
          new ConfiguredSortOrderComparator(config.getView().getCorpusAnnotationOrder()));
    }
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
