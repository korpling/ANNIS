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
package annis.gui;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.UI;

import annis.libgui.InstanceConfig;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class HelpPanel extends Accordion {

	private static final Logger log = LoggerFactory.getLogger(HelpPanel.class);

	private BrowserFrame help;
	private final ExampleQueriesPanel examples;

	public HelpPanel(AnnisUI ui) {
		setSizeFull();
		
		
		if (ui instanceof AnnisUI) {
			InstanceConfig cfg = ((AnnisUI) ui).getInstanceConfig();
			
			URL url = null;
			if(cfg.getHelpUrl() != null && !cfg.getHelpUrl().isEmpty()) {
				try {
					url = new URL(cfg.getHelpUrl());
				} catch (MalformedURLException ex) {
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
							oldPath + relativeFile, null, null).toURL();
		
				} catch (URISyntaxException | MalformedURLException ex) {
					log.error("Invalid help URI", ex);
				}
			}
			
			if(url != null) {
				help = new BrowserFrame(null, new ExternalResource(url));
				help.setSizeFull();
				addComponent(help);
				help.setHeight("99%");
				addTab(help, "Help", FontAwesome.BOOK);
			}
		}
		
		examples = new ExampleQueriesPanel(ui, this);
		examples.setHeight("99%");
		
		addTab(examples, "Example Queries", FontAwesome.LIST_ALT);
		


		addStyleName("help-tab");
	}

	@Override
	public void attach() {
		super.attach();
		if(help != null) {
			setSelectedTab(help);
		} else if(examples != null) {
			setSelectedTab(examples);
		}
	}

	public ExampleQueriesPanel getExamples() {
		return examples;
	}

}
