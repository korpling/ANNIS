/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.admin;

import annis.gui.admin.view.CorpusListView;
import annis.service.objects.AnnisCorpus;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CorpusAdminPanel extends Panel
  implements CorpusListView
{

  private final List<CorpusListView.Listener> listeners = new LinkedList<>();

  private final BeanContainer<String, AnnisCorpus> corpusContainer = new BeanContainer<String, AnnisCorpus>(
    AnnisCorpus.class);

  public CorpusAdminPanel()
  {
    corpusContainer.setBeanIdProperty("name");

    final Table tblCorpora = new Table();
    tblCorpora.setContainerDataSource(corpusContainer);
    tblCorpora.setSizeFull();
    tblCorpora.setSelectable(true);
    tblCorpora.setMultiSelect(true);
    tblCorpora.addStyleName(ChameleonTheme.TABLE_STRIPED);
    tblCorpora.addStyleName("grey-selection");
    
    tblCorpora.
      setVisibleColumns("name", "textCount", "tokenCount", "sourcePath");
    tblCorpora.setColumnHeaders("Name", "Texts", "Tokens", "Source path");

    Button btDelete = new Button("Delete selected");
    btDelete.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        Set<String> selection = (Set<String>) tblCorpora.getValue();
        if (selection != null)
        {
          for (CorpusListView.Listener l : listeners)
          {
            l.deleteCorpora(selection);
          }
        }
      }
    });

    VerticalLayout layout = new VerticalLayout(btDelete, tblCorpora);
    layout.setSizeFull();
    layout.setExpandRatio(tblCorpora, 1.0f);
    layout.setSpacing(true);
    layout.setMargin(new MarginInfo(true, false, false, false));
    
    layout.setComponentAlignment(btDelete, Alignment.MIDDLE_CENTER);
    
    setContent(layout);
    setSizeFull();
  }

  @Override
  public void addListener(CorpusListView.Listener listener)
  {
    listeners.add(listener);
  }

  @Override
  public void setAvailableCorpora(Collection<AnnisCorpus> corpora)
  {
    corpusContainer.removeAllItems();
    corpusContainer.addAll(corpora);
  }

}
