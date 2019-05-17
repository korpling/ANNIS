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

import com.vaadin.annotations.DesignRoot;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.declarative.Design;

import annis.model.Query;
import annis.libgui.Helper;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@DesignRoot
public class HistoryPanel extends Panel
  implements ValueChangeListener, ItemClickListener
{

  private Table tblHistory;
  private QueryController controller;
  private final CitationLinkGenerator citationGenerator;

  public HistoryPanel(final BeanItemContainer<Query> containerHistory, 
    QueryController controller)
  {
    this.controller = controller;
    
    Design.read("HistoryPanel.html", this);
    
    tblHistory.setContainerDataSource(containerHistory);

    tblHistory.addGeneratedColumn("gennumber", new Table.ColumnGenerator()
    {

      @Override
      public Object generateCell(Table source, Object itemId, Object columnId)
      {
        int idx = containerHistory.indexOfId(itemId);
        return new Label("" + (idx+1));
      }
    });
    citationGenerator = new CitationLinkGenerator();
    tblHistory.addGeneratedColumn("genlink", citationGenerator);
    tblHistory.setVisibleColumns("gennumber", "query", "genlink");

    tblHistory.addStyleName(Helper.CORPUS_FONT);
    tblHistory.addValueChangeListener((ValueChangeListener) this);
    tblHistory.addItemClickListener((ItemClickListener) this);

  }

  @Override
  public void valueChange(ValueChangeEvent event)
  {
    Query q = (Query) event.getProperty().getValue();
    
    if(q != null && controller != null)
    {
      controller.setQuery(q);
    }
  }

  @Override
  public void itemClick(ItemClickEvent event)
  {
    if(controller != null && event.isDoubleClick())
    {
      controller.executeSearch(true, true);
      if(getParent() instanceof Window)
      {
        UI.getCurrent().removeWindow((Window) getParent());
      }
    }
  }
  
  
}
