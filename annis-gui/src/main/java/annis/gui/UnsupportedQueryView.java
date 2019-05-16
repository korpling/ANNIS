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

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.declarative.Design;

public class UnsupportedQueryView extends Panel implements View {

    private final AnnisUI ui;

    public static final String NAME = "unsupported-query";

    private Button btExecute;


    public UnsupportedQueryView(AnnisUI ui) {
        this.ui = ui;

        Design.read("UnsupportedQueryView.html", this);

    }

    @Override
    public void enter(ViewChangeEvent event) {
      //  lblTest.setValue(event.getParameters());
    }

}