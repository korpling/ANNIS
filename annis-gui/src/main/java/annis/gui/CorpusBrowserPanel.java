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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;

import org.corpus_tools.ApiException;
import org.corpus_tools.annis.AnnoKey;
import org.corpus_tools.annis.Annotation;
import org.corpus_tools.annis.AnnotationComponentType;
import org.corpus_tools.annis.Component;
import org.corpus_tools.annis.CorporaApi;

import annis.gui.beans.CorpusBrowserEntry;
import annis.gui.components.ExceptionDialog;
import annis.libgui.Background;
import annis.model.Query;
import annis.service.objects.QueryLanguage;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class CorpusBrowserPanel extends Panel {

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

        lblNoEdgeAnno = new Label("(No Edge Annotations)");
        VerticalLayout tabEdgeAnno = new VerticalLayout(tblEdgeAnno, lblNoEdgeAnno);
        tabEdgeAnno.setCaption("Edge Annotations");

        lblNoEdgeTypes = new Label("(No Edge Types)");
        VerticalLayout tabEdgeTypes = new VerticalLayout(tblEdgeTypes, lblNoEdgeTypes);
        tabEdgeTypes.setCaption("Edge Types");

        lblNoMetaAnno = new Label("(No Meta Annotations)");
        VerticalLayout tabMetaAnno = new VerticalLayout(tblMetaAnno, lblNoMetaAnno);
        tabMetaAnno.setCaption("Meta Annotations");

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

        Background.run(() -> fetchAnnotationsInBackground());
    }

    private void fetchAnnotationsInBackground() {
        CorporaApi api = new CorporaApi();

        try {
            final List<Annotation> nodeAnnos = api.corpusNodeAnnotations(corpus, true, true);
            final List<Component> components = api.corpusComponents(corpus, "Dominance", null);
            final Map<Component, List<Annotation>> edgeAnnos = new LinkedHashMap<>();
            components.addAll(api.corpusComponents(corpus, "Pointing", null));
            for (Component c : components) {
                try {
                    edgeAnnos.put(c, api.corpusEdgeAnnotations(corpus, c.getType().getValue(), c.getLayer(),
                            c.getName(), true, true));
                } catch (ApiException ex) {
                    // Ignore any not found errors
                }
            }

            getUI().access(() -> {

                TreeSet<CorpusBrowserEntry> nodeAnnoItems = new TreeSet<>();
                TreeSet<CorpusBrowserEntry> edgeAnnoItems = new TreeSet<>();
                TreeSet<CorpusBrowserEntry> edgeTypeItems = new TreeSet<>();
                TreeSet<CorpusBrowserEntry> metaAnnoItems = new TreeSet<>();

                progress.setVisible(false);
                accordion.setVisible(true);

                boolean stripNodeAnno = true;
                boolean stripEdgeName = true;
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
                    if (!edgeNames.add(getQName(c))) {
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

                for (List<Annotation> annos : edgeAnnos.values()) {
                    for (Annotation a : annos) {
                        // check for ambiguous names
                        if (!edgeAnnoNames.add(a.getKey().getName())) {
                            stripEdgeAnno = false;
                        }
                    }
                }

                // fill the actual containers
                for (Annotation a : nodeAnnos) {
                    String name = stripNodeAnno ? a.getKey().getName() : getQName(a.getKey());
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
                    String name = stripEdgeName ? c.getName() : getQName(c);
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
                for (Map.Entry<Component, List<Annotation>> entry : edgeAnnos.entrySet()) {
                    Component c = entry.getKey();
                    for (Annotation a : entry.getValue()) {
                        CorpusBrowserEntry cbeEdgeAnno = new CorpusBrowserEntry();
                        String edgeAnno = stripEdgeAnno ? a.getKey().getName() : getQName(a.getKey());
                        cbeEdgeAnno.setName(edgeAnno);
                        cbeEdgeAnno.setCorpus(corpus);
                        if (c.getType() == AnnotationComponentType.POINTING) {
                            cbeEdgeAnno.setExample("node & node & #1 ->" + c.getName() + "[" + a.getKey().getName()
                                    + "=\"" + a.getVal() + "\"] #2");
                        } else if (c.getType() == AnnotationComponentType.POINTING) {
                            cbeEdgeAnno.setExample(
                                    "node & node & #1 >[" + a.getKey().getName() + "=\"" + a.getVal() + "\"] #2");
                        }
                        edgeAnnoItems.add(cbeEdgeAnno);

                    }
                }

                // TODO: meta???
                Set<String> metaAnnosKey = new HashSet<>();
                // if the annotation name is already in the example skip this.
                // if (a.getType() == AnnisAttribute.Type.meta &&
                // !metaAnnosKey.contains(killNamespace(a.getName()))) {
                // String name = killNamespace(a.getName());
                // metaAnnosKey.add(name);
                // CorpusBrowserEntry cbe = new CorpusBrowserEntry();
                // cbe.setName(name);
                // cbe.setExample("node @* " + name + "=\"" + getFirst(a.getValueSet()) + "\"");
                // cbe.setCorpus(corpus);
                // containerMetaAnno.addBean(cbe);
                // }

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

            });

        } catch (ApiException e) {
            getUI().access(() -> {
                ExceptionDialog.show(e, "Error fetching corpus annotations", getUI());
            });
        }

    }

    private static String getQName(AnnoKey key) {
        if (key.getNs() == null || key.getNs().isEmpty()) {
            return key.getName();
        } else {
            return key.getNs() + ":" + key.getName();
        }
    }

    private static String getQName(Component c) {
        if (c.getLayer() == null || c.getLayer().isEmpty()) {
            return c.getName();
        } else {
            return c.getLayer() + ":" + c.getName();
        }
    }

    public class ExampleListener implements SelectionListener<CorpusBrowserEntry> {

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
}
