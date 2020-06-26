/*
 * Copyright 2019 Corpuslinguistic working group Humboldt University Berlin.
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

import annis.libgui.Helper;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.declarative.Design;

@SpringUI(path = "/unsupported-query")
@Widgetset("annis.gui.widgets.gwt.AnnisWidgetSet")
public class UnsupportedQueryUI extends CommonUI {

    public static class UnsupportedQueryPanel extends Panel {

        /**
         * 
         */
        private static final long serialVersionUID = 5891948595136418081L;
        private Button btExecute;
        private final String url;

        public UnsupportedQueryPanel(String url) {
            this.url = url;

            Page.getCurrent().setTitle("ANNIS: Unsupported query for citation link");

            Design.read("UnsupportedQueryPanel.html", this);

            btExecute.addClickListener(event -> {
                if (url != null) {
                    getUI().getPage().setLocation(Helper.getContext(UI.getCurrent()) + url);
                }
            });
        }

    }

    private static final long serialVersionUID = 3022711576267350004L;

    public static final String URL_PREFIX = "/unsupported-query";

    public UnsupportedQueryUI() {
        super(URL_PREFIX);

    }

    @Override
    protected void init(VaadinRequest request) {
        UnsupportedQueryPanel panel = new UnsupportedQueryPanel(request.getParameter("url"));
        setContent(panel);
    }
}