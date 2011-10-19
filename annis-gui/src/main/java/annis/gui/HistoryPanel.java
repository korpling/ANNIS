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

import annis.gui.beans.HistoryEntry;
import annis.gui.controlpanel.ControlPanel;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.List;

/**
 *
 * @author thomas
 */
public class HistoryPanel extends Panel
  implements ValueChangeListener
{

  private Table tblHistory;
  private BeanItemContainer<HistoryEntry> containerHistory;
  private ControlPanel parent;

  public HistoryPanel(List<HistoryEntry> history, ControlPanel parent)
  {
    this.parent = parent;
    
    setSizeFull();
    ((VerticalLayout) getContent()).setSizeFull();

    containerHistory = new BeanItemContainer(HistoryEntry.class);
    containerHistory.addAll(history);

    tblHistory = new Table();

    addComponent(tblHistory);
    tblHistory.setSizeFull();
    tblHistory.setSelectable(true);
    tblHistory.setMultiSelect(false);
    tblHistory.setContainerDataSource(containerHistory);

    tblHistory.addGeneratedColumn("number", new Table.ColumnGenerator()
    {

      @Override
      public Object generateCell(Table source, Object itemId, Object columnId)
      {
        return new Label("" + (containerHistory.indexOfId(itemId) + 1));
      }
    });

    tblHistory.setVisibleColumns(new String[]
      {
        "number", "query"
      });
    tblHistory.setColumnHeader("number", "#");
    tblHistory.setColumnHeader("query", "Query");
    tblHistory.setColumnExpandRatio("query", 1.0f);
    tblHistory.setImmediate(true);
    tblHistory.addListener((ValueChangeListener) this);

  }

  @Override
  public void valueChange(ValueChangeEvent event)
  {
    HistoryEntry e = (HistoryEntry) event.getProperty().getValue();
    
    if(parent != null)
    {
      parent.setQuery(e.getQuery(), e.getCorpora());
    }
  }
}
