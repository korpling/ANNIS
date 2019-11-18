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

import java.util.List;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.ItemClick;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.ItemClickListener;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.themes.ValoTheme;

import annis.model.DisplayedResultQuery;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@DesignRoot
public class HistoryPanel extends Panel
    implements SelectionListener<DisplayedResultQuery>, ItemClickListener<DisplayedResultQuery> {

  private Grid<DisplayedResultQuery> tblHistory;
  private QueryController controller;

  public HistoryPanel(final List<DisplayedResultQuery> containerHistory, QueryController controller) {
    this.controller = controller;

    Design.read("HistoryPanel.html", this);

    tblHistory.setItems(containerHistory);

    tblHistory.addColumn(query -> {
      int idx = containerHistory.indexOf(query);
      return "" + (idx + 1);
    }).setId("gennumber");

    tblHistory.addColumn(query -> {
      Button btLink = new Button();
      btLink.addStyleName(ValoTheme.BUTTON_BORDERLESS);
      btLink.setIcon(VaadinIcons.SHARE);
      btLink.setDescription("Share query reference link");
      btLink.addClickListener(new LinkClickListener(query));
      return btLink;
    }).setId("genlink");

    tblHistory.addSelectionListener(this);
    tblHistory.addItemClickListener(this);

  }

  @Override
  public void selectionChange(SelectionEvent<DisplayedResultQuery> event) {
    if (event.getFirstSelectedItem().isPresent() && controller != null) {
      controller.setQuery(event.getFirstSelectedItem().get());
    }
  }

  @Override
  public void itemClick(ItemClick<DisplayedResultQuery> event) {
    if (controller != null && event.getMouseEventDetails().isDoubleClick()) {
      controller.executeSearch(true, true);
      if (getParent() instanceof Window) {
        UI.getCurrent().removeWindow((Window) getParent());
      }
    }
  }

}
