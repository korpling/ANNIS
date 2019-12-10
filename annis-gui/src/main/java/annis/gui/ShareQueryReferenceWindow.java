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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.shared.ui.label.ContentMode;

import annis.gui.components.ExceptionDialog;
import annis.libgui.Helper;
import annis.model.DisplayedResultQuery;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class ShareQueryReferenceWindow extends Window implements Button.ClickListener {

    private static final Logger log = LoggerFactory.getLogger(ShareQueryReferenceWindow.class);

    private final DisplayedResultQuery query;
    private final boolean shorten;

    private TextArea txtCitation;

    public ShareQueryReferenceWindow(DisplayedResultQuery query, boolean shorten) {
        super("Query reference link");

        this.query = query;
        this.shorten = shorten;

        VerticalLayout wLayout = new VerticalLayout();
        setContent(wLayout);
        wLayout.setSizeFull();
        wLayout.setMargin(true);

        Label lblInfo = new Label(
                "<p style=\"font-size: 18px\" >" + "<strong>Share your query:</strong>&nbsp;"
                        + "1.&nbsp;Copy the generated link 2.&nbsp;Share this link with your peers. " + "</p>",
                ContentMode.HTML);
        wLayout.addComponent(lblInfo);
        wLayout.setExpandRatio(lblInfo, 0.0f);

        txtCitation = new TextArea();

        txtCitation.setWidth("100%");
        txtCitation.setHeight("100%");
        txtCitation.addStyleName(ValoTheme.TEXTFIELD_LARGE);
        txtCitation.addStyleName("shared-text");
        txtCitation.setWordwrap(true);
        txtCitation.setReadOnly(true);

        wLayout.addComponent(txtCitation);

        Button btClose = new Button("Close");
        btClose.addClickListener((Button.ClickListener) this);
        btClose.setSizeUndefined();

        wLayout.addComponent(btClose);

        wLayout.setExpandRatio(txtCitation, 1.0f);
        wLayout.setComponentAlignment(btClose, Alignment.BOTTOM_CENTER);

        setWidth("400px");
        setHeight("300px");

    }

    @Override
    public void attach() {
        super.attach();

        String shortURL = "ERROR";
        if (query != null) {
            URI appURI = UI.getCurrent().getPage().getLocation();
            String fragment;
            try {
                fragment = StringUtils.join(query.toCitationFragment(), "&");
                URI url = new URI(appURI.getScheme(), null, appURI.getHost(), appURI.getPort(), appURI.getPath(), null,
                        fragment);

                if (shorten) {
                    shortURL = Helper.shortenURL(url, UI.getCurrent());
                } else {
                    shortURL = url.toASCIIString();
                }
            } catch (URISyntaxException e) {
                log.error("Could not generate query share link", e);
                ExceptionDialog.show(e, "Could not generate query share link", UI.getCurrent());
            }

        }
        
        txtCitation.setReadOnly(false);
        txtCitation.setValue(shortURL);
        txtCitation.setReadOnly(true);
    }

    @Override
    public void buttonClick(ClickEvent event) {
        UI.getCurrent().removeWindow(this);
    }
}
