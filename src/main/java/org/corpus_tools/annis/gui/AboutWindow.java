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
package org.corpus_tools.annis.gui;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.Version;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.corpus_tools.annis.VersionInfo;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.libgui.IDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class AboutWindow extends Window {

    private static class OkClickListener implements Button.ClickListener {

        private static final long serialVersionUID = -4082849367760765188L;
        private final AboutWindow finalThis;

        public OkClickListener(AboutWindow finalThis) {
            this.finalThis = finalThis;
        }

        @Override
        public void buttonClick(ClickEvent event) {
            UI.getCurrent().removeWindow(finalThis);
        }
    }

    private static final long serialVersionUID = -7355736941508460795L;

    private static final Logger log = LoggerFactory.getLogger(AboutWindow.class);

    private VerticalLayout layout;

    private Button btClose;

    private TextArea txtThirdParty;

    public AboutWindow() {
        setSizeFull();

        layout = new VerticalLayout();
        setContent(layout);
        layout.setSizeFull();
        layout.setMargin(true);

        HorizontalLayout hLayout = new HorizontalLayout();

        Image logoAnnis = new Image();
        logoAnnis.setSource(new ThemeResource("images/org.corpus_tools.annis-logo-128.png"));
        hLayout.addComponent(logoAnnis);

        Image logoSfb = new Image();
        logoSfb.setSource(new ThemeResource("images/sfb-logo.jpg"));
        hLayout.addComponent(logoSfb);

        Link lnkFork = new Link();
        lnkFork.setResource(new ExternalResource("https://github.com/korpling/ANNIS"));
        lnkFork.setIcon(new ExternalResource("https://s3.amazonaws.com/github/ribbons/forkme_right_red_aa0000.png"));
        lnkFork.setTargetName("_blank");
        hLayout.addComponent(lnkFork);

        hLayout.setComponentAlignment(logoAnnis, Alignment.MIDDLE_LEFT);
        hLayout.setComponentAlignment(logoSfb, Alignment.MIDDLE_RIGHT);
        hLayout.setComponentAlignment(lnkFork, Alignment.TOP_RIGHT);

        layout.addComponent(hLayout);

        layout.addComponent(
                new Label("ANNIS is a project of the " + "<a href=\"http://www.sfb632.uni-potsdam.de/\">SFB632</a>.",
                        ContentMode.HTML));
        layout.addComponent(new Label(
                "Homepage: " + "<a href=\"http://corpus-tools.org/org.corpus_tools.annis/\">" + "http://corpus-tools.org/org.corpus_tools.annis/</a>.",
                ContentMode.HTML));
        layout.addComponent(new Label("Version: " + VersionInfo.getVersion()));
        layout.addComponent(new Label("Vaadin-Version: " + Version.getFullVersion()));

        txtThirdParty = new TextArea();
        txtThirdParty.setSizeFull();
        txtThirdParty.setReadOnly(true);
        txtThirdParty.addStyleName("shared-text");
        txtThirdParty.setWordWrap(false);

        layout.addComponent(txtThirdParty);

        btClose = new Button("Close");
        final AboutWindow finalThis = this;
        btClose.addClickListener(new OkClickListener(finalThis));
        layout.addComponent(btClose);

        layout.setComponentAlignment(hLayout, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(btClose, Alignment.MIDDLE_CENTER);
        layout.setExpandRatio(txtThirdParty, 1.0f);

      }

      @Override
      public void attach() {
        super.attach();
        IDGenerator.assignIDForFields(AboutWindow.this, btClose);

        try {
          File citationFile = new ClassPathResource("CITATION.cff").getFile();
          txtThirdParty.setValue(FileUtils.readFileToString(citationFile));
        } catch (IOException ex) {
          ExceptionDialog.show(ex, getUI());
        }
    }
}
