/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.admin;

import annis.gui.admin.view.CorpusListView;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class CorpusAdminPanel extends Panel implements CorpusListView {

  private static final long serialVersionUID = 2132712553250095606L;

  private final List<CorpusListView.Listener> listeners = new LinkedList<>();

  private final Grid<String> corporaGrid;

  public CorpusAdminPanel() {

    corporaGrid = new Grid<>();
    corporaGrid.setSizeFull();
    corporaGrid.setSelectionMode(Grid.SelectionMode.MULTI);

    Column<String, String> corpusColumn = corporaGrid.addColumn(corpus -> corpus);
    corpusColumn.setCaption("Corpus Name");

    Button btDelete = new Button("Delete selected");
    btDelete.addClickListener(event -> {
      Set<String> selection = corporaGrid.getSelectedItems();
      if (!selection.isEmpty()) {
        for (CorpusListView.Listener l : listeners) {
          l.deleteCorpora(selection);
        }
      }
      corporaGrid.asMultiSelect().clear();
    });

    VerticalLayout layout = new VerticalLayout(btDelete, corporaGrid);
    layout.setSizeFull();
    layout.setExpandRatio(corporaGrid, 1.0f);
    layout.setSpacing(true);
    layout.setMargin(new MarginInfo(true, false, false, false));

    layout.setComponentAlignment(btDelete, Alignment.MIDDLE_CENTER);

    setContent(layout);
    setSizeFull();
  }

  @Override
  public void addListener(CorpusListView.Listener listener) {
    listeners.add(listener);
  }

  @Override
  public void setAvailableCorpora(Collection<String> corpora) {
    corporaGrid.setItems(corpora);
  }

}
