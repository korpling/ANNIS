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
package org.corpus_tools.annis.gui.components;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.CommonUI;
import org.corpus_tools.annis.gui.Helper;

/**
 * A dialog that displays the message of an exception and allows to show the
 * stack trace if requested.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class ExceptionDialog extends Window implements Button.ClickListener {
    private static final long serialVersionUID = -9195384309481177718L;

    public static void show(Throwable ex, String caption, UI ui) {

      // Check if we can handle this error without showing an explicit dialog
      if (!(ui instanceof CommonUI) || !((CommonUI) ui).handleCommonError(ex, null)) {
        ExceptionDialog dlg = new ExceptionDialog(ex, caption);
        dlg.setClosable(true);
        dlg.setModal(true);
        dlg.setResizable(true);
        dlg.setWidth("500px");
        dlg.setHeight("-1");

        ui.addWindow(dlg);
        dlg.center();
      }
    }

    public static void show(Throwable ex, UI ui) {
      show(ex, null, ui);
    }

    private Panel detailsPanel;
    private Label lblStacktrace;
    private Button btDetails;
    private Button btClose;
    private Button btReportBug;
    private Throwable cause;

    private VerticalLayout layout;

    private HorizontalLayout actionsLayout;

    private ExceptionDialog(Throwable ex, String caption) {
        this.cause = ex;

        layout = new VerticalLayout();
        setContent(layout);
        layout.setWidth("100%");
        layout.setHeight("-1");

        if (caption == null) {
            setCaption("Unexpected error");
        } else {
            setCaption(caption);
        }

        Label lblInfo =
            new Label("An error occured.<br />The error message was:", ContentMode.HTML);
        lblInfo.setHeight("-1px");
        lblInfo.setWidth("100%");
        layout.addComponent(lblInfo);
        lblInfo.addStyleName("exception-message-caption");

        String message = ex != null ? ex.getMessage() : null;
        if (message == null || message.isEmpty()) {
            message = "<no message>";
        }
        if(ex instanceof ApiException) {
          message = ((ApiException) ex).getResponseBody() + " (" + message + ")";
        }
        Label lblMessage = new Label(message);
        lblMessage.addStyleName("exception-message-content");
        lblMessage.setHeight("-1px");
        lblMessage.setWidth("100%");
        layout.addComponent(lblMessage);

        actionsLayout = new HorizontalLayout();
        actionsLayout.addStyleName("exception-dlg-details");
        actionsLayout.setWidth("100%");
        actionsLayout.setHeight("-1px");
        layout.addComponent(actionsLayout);

        btDetails = new Button("Show Details", this);
        btDetails.setStyleName(ValoTheme.BUTTON_LINK);
        actionsLayout.addComponent(btDetails);

        btReportBug = new Button("Report Problem", this);
        btReportBug.setStyleName(ValoTheme.BUTTON_LINK);
        btReportBug.setVisible(false);
        btReportBug.setIcon(VaadinIcons.ENVELOPE_O);
        UI ui = UI.getCurrent();
        if (ui instanceof AnnisUI) {
            btReportBug.setVisible(((AnnisUI) ui).canReportBugs());
        }
        actionsLayout.addComponent(btReportBug);
        actionsLayout.setComponentAlignment(btDetails, Alignment.TOP_LEFT);
        actionsLayout.setComponentAlignment(btReportBug, Alignment.TOP_RIGHT);

        lblStacktrace = new Label(Helper.convertExceptionToMessage(ex), ContentMode.PREFORMATTED);
        detailsPanel = new Panel(lblStacktrace);
        detailsPanel.setWidth("100%");
        detailsPanel.setHeight("300px");
        detailsPanel.setVisible(false);
        lblStacktrace.setSizeUndefined();
        lblStacktrace.setVisible(true);
        layout.addComponent(detailsPanel);

        btClose = new Button("OK", this);
        layout.addComponent(btClose);

        layout.setComponentAlignment(btClose, Alignment.BOTTOM_CENTER);
        layout.setExpandRatio(detailsPanel, 0.0f);
        layout.setExpandRatio(actionsLayout, 1.0f);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        if (event.getButton() == btDetails) {
            if (detailsPanel.isVisible()) {
                detailsPanel.setVisible(false);
                btDetails.setCaption("Show Details");
                layout.setExpandRatio(detailsPanel, 0.0f);
                layout.setExpandRatio(actionsLayout, 1.0f);
            } else {
                detailsPanel.setVisible(true);
                btDetails.setCaption("Hide Details");
                layout.setExpandRatio(detailsPanel, 1.0f);
                layout.setExpandRatio(actionsLayout, 0.0f);
            }
        } else if (event.getButton() == btClose) {
            this.close();
        } else if (event.getButton() == btReportBug) {
            this.close();
            UI ui = UI.getCurrent();
            if (ui instanceof AnnisUI) {
                ((AnnisUI) ui).reportBug(cause);
            }
        }
    }
}
