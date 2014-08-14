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
import annis.gui.objects.Query;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author thomas
 */
public class HistoryPanel extends Panel
  implements ValueChangeListener, ItemClickListener
{

  private Table tblHistory;
  private BeanItemContainer<HistoryEntry> containerHistory;
  private QueryController controller;
  private CitationLinkGenerator citationGenerator;

  public HistoryPanel(List<HistoryEntry> history, QueryController controller)
  {
    this.controller = controller;
    
    VerticalLayout layout = new VerticalLayout();
    setContent(layout);
    
    setSizeFull();
    layout.setSizeFull();

    containerHistory = new BeanItemContainer(HistoryEntry.class);
    containerHistory.addAll(history);

    tblHistory = new Table();

    layout.addComponent(tblHistory);
    tblHistory.setSizeFull();
    tblHistory.setSelectable(true);
    tblHistory.setMultiSelect(false);
    tblHistory.setContainerDataSource(containerHistory);

    tblHistory.addGeneratedColumn("gennumber", new Table.ColumnGenerator()
    {

      @Override
      public Object generateCell(Table source, Object itemId, Object columnId)
      {
        return new Label("" + (containerHistory.indexOfId(itemId) + 1));
      }
    });
    citationGenerator = new CitationLinkGenerator();
    tblHistory.addGeneratedColumn("genlink", citationGenerator);

    tblHistory.setVisibleColumns(new String[]
      {
        "gennumber", "query", "genlink"
      });
    tblHistory.setColumnHeader("gennumber", "#");
    tblHistory.setColumnHeader("query", "Query");
    tblHistory.setColumnHeader("genlink", "URL");
    tblHistory.setColumnExpandRatio("query", 1.0f);
    tblHistory.setImmediate(true);
    tblHistory.addValueChangeListener((ValueChangeListener) this);
    tblHistory.addItemClickListener((ItemClickListener) this);

  }

  @Override
  public void valueChange(ValueChangeEvent event)
  {
    HistoryEntry e = (HistoryEntry) event.getProperty().getValue();
    
    if(e != null && controller != null)
    {
      controller.setQuery(new Query(e.getQuery(), new HashSet<>(e.getCorpora())));
    }
  }

  @Override
  public void itemClick(ItemClickEvent event)
  {
    if(controller != null && event.isDoubleClick())
    {
      controller.executeQuery();
      if(getParent() instanceof Window)
      {
        UI.getCurrent().removeWindow((Window) getParent());
      }
    }
  }
  
  
}
