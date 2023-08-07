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
package org.corpus_tools.annis.gui.corpusbrowser;

import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.corpus_tools.annis.api.model.AnnoKey;
import org.corpus_tools.annis.api.model.Annotation;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.Component;
import org.corpus_tools.annis.gui.CommonUI;
import org.corpus_tools.annis.gui.components.ExampleTable;
import org.corpus_tools.annis.gui.controller.QueryController;
import org.corpus_tools.annis.gui.objects.Query;
import org.corpus_tools.annis.gui.objects.QueryLanguage;
import org.corpus_tools.annis.gui.util.Helper;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class CorpusBrowserPanel extends Panel {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(Helper.class);

  private class ExampleListener implements SelectionListener<CorpusBrowserEntry> {

    private static final long serialVersionUID = 5456621606184042619L;



    @Override
    public void selectionChange(SelectionEvent<CorpusBrowserEntry> event) {
      Optional<CorpusBrowserEntry> selected = event.getFirstSelectedItem();
      Set<String> corpusNameSet = new HashSet<>();
      if (corpus != null) {
        corpusNameSet.add(corpus);
      }
      if (controller != null && selected.isPresent()) {
        controller.setQuery(new Query(selected.get().getQuery(), QueryLanguage.AQL, corpusNameSet));
      }
    }
  }

  private static final long serialVersionUID = -1029743017413951838L;

  private String corpus;

  private ExampleTable tblNodeAnno;

  private Label lblNoNodeAnno;

  private ExampleTable tblEdgeTypes;

  private Label lblNoEdgeTypes;

  private ExampleTable tblEdgeAnno;

  private Label lblNoEdgeAnno;

  private ExampleTable tblMetaAnno;

  private Label lblNoMetaAnno;

  private QueryController controller;

  private ProgressBar progress;

  private Accordion accordion;

  private VerticalLayout layout;

  public CorpusBrowserPanel() {
    this(null, null);
  }

  public CorpusBrowserPanel(String corpus, QueryController controller) {
    super("Available annotations");
    this.corpus = corpus;
    this.controller = controller;

    setSizeFull();
    progress = new ProgressBar();
    progress.setIndeterminate(true);

    tblNodeAnno = new ExampleTable();
    tblEdgeTypes = new ExampleTable();
    tblEdgeAnno = new ExampleTable();
    tblMetaAnno = new ExampleTable();

    tblNodeAnno.addSelectionListener(new ExampleListener());
    tblEdgeTypes.addSelectionListener(new ExampleListener());
    tblEdgeAnno.addSelectionListener(new ExampleListener());
    tblMetaAnno.addSelectionListener(new ExampleListener());

    tblNodeAnno.sort("name");
    tblEdgeTypes.sort("name");
    tblEdgeAnno.sort("name");

    lblNoNodeAnno = new Label("(No Node Annotations)");
    VerticalLayout tabNodeAnno = new VerticalLayout(tblNodeAnno, lblNoNodeAnno);
    tabNodeAnno.setCaption("Node Annotations");
    tabNodeAnno.setMargin(false);

    lblNoEdgeAnno = new Label("(No Edge Annotations)");
    VerticalLayout tabEdgeAnno = new VerticalLayout(tblEdgeAnno, lblNoEdgeAnno);
    tabEdgeAnno.setCaption("Edge Annotations");
    tabEdgeAnno.setMargin(false);

    lblNoEdgeTypes = new Label("(No Edge Types)");
    VerticalLayout tabEdgeTypes = new VerticalLayout(tblEdgeTypes, lblNoEdgeTypes);
    tabEdgeTypes.setCaption("Edge Types");
    tabEdgeTypes.setMargin(false);

    lblNoMetaAnno = new Label("(No Meta Annotations)");
    VerticalLayout tabMetaAnno = new VerticalLayout(tblMetaAnno, lblNoMetaAnno);
    tabMetaAnno.setCaption("Meta Annotations");
    tabMetaAnno.setMargin(false);

    accordion = new Accordion(tabNodeAnno, tabEdgeAnno, tabEdgeTypes, tabMetaAnno);
    accordion.setSizeFull();
    accordion.setVisible(true);
    progress.setVisible(true);
    progress.setSizeFull();

    layout = new VerticalLayout();
    layout.addComponents(progress, accordion);
    layout.setSizeFull();
    layout.setMargin(false);
    layout.setComponentAlignment(progress, Alignment.MIDDLE_CENTER);
    setContent(layout);

  }

  @Override
  public void attach() {
    super.attach();

    final UI ui = getUI();

    if (ui instanceof CommonUI) {
      fetchAnnotations((CommonUI) ui);
    }
  }

  private boolean canExcludeNamespace(Collection<Annotation> annos) {
    Set<String> names = new HashSet<>();
    for (Annotation a : annos) {
      if (!names.add(a.getKey().getName())) {
        return false;
      }
    }
    return true;
  }

  private Flux<Component> createComponentQuery(WebClient client) {
    Flux<Component> dominanceComponents = client.get().uri(
        ub -> ub.path("/corpora/{corpus}/components").queryParam("type", "Dominance").build(corpus))
        .retrieve().bodyToFlux(Component.class);

    Flux<Component> pointingComponents = client.get().uri(
        ub -> ub.path("/corpora/{corpus}/components").queryParam("type", "Pointing").build(corpus))
        .retrieve().bodyToFlux(Component.class);


    return Flux.merge(dominanceComponents, pointingComponents);
  }

  private void fetchAnnotations(CommonUI ui) {

    WebClient client = ui.getWebClient();

    Flux<Tuple2<Annotation, Component>> edgeAnnosByComponent =
        createComponentQuery(client).filter(c -> !c.getName().isEmpty()).flatMap(c -> {
          Flux<Annotation> componentEdgeAnnos = client.get()
              .uri(ub -> ub.path("/corpora/{corpus}/edge-annotations/{type}/{layer}/{name}/")
                  .queryParam("list_values", true).queryParam("only_most_frequent_values", true)
                  .build(corpus, c.getType().getValue(), c.getLayer(), c.getName()))

              .retrieve().bodyToFlux(Annotation.class);
          return componentEdgeAnnos.zipWith(Mono.just(c).repeat());
        });

    Flux<Component> components = createComponentQuery(client);

    Flux<Annotation> nodeAnnos = client.get()
        .uri(ub -> ub.path("/corpora/{corpus}/node-annotations").queryParam("list_values", true)
            .queryParam("only_most_frequent_values", true).build(corpus))
        .retrieve().bodyToFlux(Annotation.class)
        .filter(a -> !Objects.equals(a.getKey().getNs(), "annis")
            && !Objects.equals(a.getKey().getName(), "tok"));
    Flux<AnnoKey> metaAnnoKeys = Helper.getMetaAnnotationNames(corpus, client);


    showEntries(nodeAnnos.collectList().block(), components.collectList().block(),
        edgeAnnosByComponent.collectMultimap(Tuple2::getT2, Tuple2::getT1).block(),
        metaAnnoKeys.collectList().block());
  }

  private void showEntries(List<Annotation> nodeAnnos, List<Component> components,
      Map<Component, Collection<Annotation>> edgeAnnosByComponent, List<AnnoKey> metaAnnoKeysList) {

    Set<AnnoKey> metaAnnoKeys = new LinkedHashSet<>(metaAnnoKeysList);
    
    TreeSet<CorpusBrowserEntry> nodeAnnoItems = new TreeSet<>();
    TreeSet<CorpusBrowserEntry> edgeAnnoItems = new TreeSet<>();
    TreeSet<CorpusBrowserEntry> edgeTypeItems = new TreeSet<>();
    TreeSet<CorpusBrowserEntry> metaAnnoItems = new TreeSet<>();

    progress.setVisible(false);
    accordion.setVisible(true);


    List<Annotation> metaAnnos = new LinkedList<>(nodeAnnos);
    nodeAnnos.removeIf(anno -> metaAnnoKeys.contains(anno.getKey()));
    metaAnnos.removeIf(anno -> !metaAnnoKeys.contains(anno.getKey()));

    Set<Annotation> allEdgeAnnos = edgeAnnosByComponent.entrySet().stream()
        .flatMap(annos -> annos.getValue().stream()).collect(Collectors.toSet());


    boolean stripNodeAnno = canExcludeNamespace(nodeAnnos);
    boolean stripEdgeName = canExcludeNamespace(allEdgeAnnos);
    boolean stripEdgeAnno = true;
    HashSet<String> nodeAnnoNames = new HashSet<>();
    HashSet<String> edgeAnnoNames = new HashSet<>();
    HashSet<String> edgeNames = new HashSet<>();
    boolean hasDominance = false;
    boolean hasEmptyDominance = false;

    // do some preparations first
    for (Annotation a : nodeAnnos) {
      // check for ambiguous names
      if (!nodeAnnoNames.add(a.getKey().getName())) {
        stripNodeAnno = false;
      }
    }
    for (Component c : components) {
      // check if collected edge names are unique
      if (!edgeNames.add(Helper.getQName(c))) {
        stripEdgeName = false;
      }
      // check if we need to add the general dominance example edge
      if (c.getType() == AnnotationComponentType.DOMINANCE) {
        hasDominance = true;
        if (c.getName() == null || c.getName().isEmpty()) {
          hasEmptyDominance = true;
        }
      }
    }

    for (Collection<Annotation> annos : edgeAnnosByComponent.values()) {
      for (Annotation a : annos) {
        // check for ambiguous names
        if (!edgeAnnoNames.add(a.getKey().getName())) {
          stripEdgeAnno = false;
        }
      }
    }

    // fill the actual containers
    for (Annotation a : nodeAnnos) {
      String name = stripNodeAnno ? a.getKey().getName() : Helper.getQName(a.getKey());
      CorpusBrowserEntry cbe = new CorpusBrowserEntry();
      cbe.setName(name);
      cbe.setExample(name + "=\"" + a.getVal() + "\"");
      cbe.setCorpus(corpus);
      nodeAnnoItems.add(cbe);
    }

    // edge type entry
    if (hasDominance && !hasEmptyDominance) {
      CorpusBrowserEntry cbe = new CorpusBrowserEntry();
      cbe.setName("(dominance)");
      cbe.setCorpus(corpus);
      cbe.setExample("node & node & #1 > #2");
      edgeTypeItems.add(cbe);
    }
    for (Component c : components) {
      CorpusBrowserEntry cbeEdgeType = new CorpusBrowserEntry();
      String name = stripEdgeName ? c.getName() : Helper.getQName(c);
      if ((name == null || name.isEmpty()) && c.getType() == AnnotationComponentType.DOMINANCE) {
        cbeEdgeType.setName("(dominance)");
      } else {
        cbeEdgeType.setName(name);
      }
      cbeEdgeType.setCorpus(corpus);
      if (c.getType() == AnnotationComponentType.POINTING) {
        cbeEdgeType.setExample("node & node & #1 ->" + c.getName() + " #2");
      } else if (c.getType() == AnnotationComponentType.DOMINANCE) {
        cbeEdgeType.setExample("node & node & #1 >" + c.getName() + " #2");
      }
      edgeTypeItems.add(cbeEdgeType);
    }

    // edge annotation entries
    for (Map.Entry<Component, Collection<Annotation>> entry : edgeAnnosByComponent.entrySet()) {
      Component c = entry.getKey();
      for (Annotation a : entry.getValue()) {
        CorpusBrowserEntry cbeEdgeAnno = new CorpusBrowserEntry();
        String edgeAnno = stripEdgeAnno ? a.getKey().getName() : Helper.getQName(a.getKey());
        cbeEdgeAnno.setName(edgeAnno);
        cbeEdgeAnno.setCorpus(corpus);
        if (c.getType() == AnnotationComponentType.POINTING) {
          cbeEdgeAnno.setExample("node & node & #1 ->" + c.getName() + "[" + a.getKey().getName()
              + "=\"" + a.getVal() + "\"] #2");
        } else if (c.getType() == AnnotationComponentType.DOMINANCE) {
          cbeEdgeAnno.setExample(
              "node & node & #1 >[" + a.getKey().getName() + "=\"" + a.getVal() + "\"] #2");
        }
        edgeAnnoItems.add(cbeEdgeAnno);
      }
    }

    boolean stripMetaName = canExcludeNamespace(metaAnnos);
    for (Annotation a : nodeAnnos) {
      String name = stripMetaName ? a.getKey().getName() : Helper.getQName(a.getKey());
      CorpusBrowserEntry cbe = new CorpusBrowserEntry();
      cbe.setName(name);
      cbe.setExample(name + "=\"" + a.getVal() + "\"");
      cbe.setCorpus(corpus);
      nodeAnnoItems.add(cbe);
    }
    for (Annotation a : metaAnnos) {
      String name = stripNodeAnno ? a.getKey().getName() : Helper.getQName(a.getKey());
      CorpusBrowserEntry cbe = new CorpusBrowserEntry();
      cbe.setName(name);
      cbe.setExample(name + "=\"" + a.getVal() + "\"");
      cbe.setCorpus(corpus);
      metaAnnoItems.add(cbe);
    }

    lblNoNodeAnno.setVisible(nodeAnnoItems.isEmpty());
    tblNodeAnno.setVisible(!nodeAnnoItems.isEmpty());
    tblNodeAnno.setItems(new ArrayList<>(nodeAnnoItems));

    lblNoEdgeAnno.setVisible(edgeAnnoItems.isEmpty());
    tblEdgeAnno.setVisible(!edgeAnnoItems.isEmpty());
    tblEdgeAnno.setItems(edgeAnnoItems);

    lblNoEdgeTypes.setVisible(edgeTypeItems.isEmpty());
    tblEdgeTypes.setVisible(!edgeTypeItems.isEmpty());
    tblEdgeTypes.setItems(edgeTypeItems);

    lblNoMetaAnno.setVisible(metaAnnoItems.isEmpty());
    tblMetaAnno.setVisible(!metaAnnoItems.isEmpty());
    tblMetaAnno.setItems(metaAnnoItems);

  }
}
