/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.UI;
import java.net.URI;
import java.net.URISyntaxException;
import org.corpus_tools.annis.gui.components.StatefulBrowserComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class HelpPanel extends Accordion {

    /**
     * 
     */
    private static final long serialVersionUID = 3388826883552338877L;

    private static final Logger log = LoggerFactory.getLogger(HelpPanel.class);

    private StatefulBrowserComponent help;
    private final ExampleQueriesPanel examples;

    public HelpPanel(AnnisUI ui) {
        setSizeFull();

        if (ui != null) {
            InstanceConfig cfg = ui.getInstanceConfig();

            URI url = null;
            if (cfg.getHelpUrl() != null && !cfg.getHelpUrl().isEmpty()) {
                try {
                    url = new URI(cfg.getHelpUrl());
                } catch (URISyntaxException ex) {
                    log.error("Invalid help URL {} provided in instance configuration", cfg.getHelpUrl(), ex);
                }
            } else {
                URI appURI = UI.getCurrent().getPage().getLocation();
                String relativeFile = "/VAADIN/help/index.html";

                try {
                    String oldPath = VaadinService.getCurrentRequest().getContextPath();
                    if (oldPath == null) {
                        oldPath = "";
                    }
                    if (oldPath.endsWith("/")) {
                        oldPath = oldPath.substring(0, oldPath.length() - 1);
                    }
                    url = new URI(appURI.getScheme(), appURI.getUserInfo(), appURI.getHost(), appURI.getPort(),
                            oldPath + relativeFile, null, null);

                } catch (URISyntaxException ex) {
                    log.error("Invalid help URI", ex);
                }
            }

            if (url != null) {
                help = new StatefulBrowserComponent(url);
                help.setSizeFull();
                addComponent(help);
                help.setHeight("99%");
                addTab(help, "Help", FontAwesome.BOOK);
                setSelectedTab(help);
            }
        }

        examples = new ExampleQueriesPanel(ui, this);
        examples.setHeight("99%");

        addTab(examples, "Example Queries", FontAwesome.LIST_ALT);
        addStyleName("help-tab");
    }

    public ExampleQueriesPanel getExamples() {
        return examples;
    }

}
