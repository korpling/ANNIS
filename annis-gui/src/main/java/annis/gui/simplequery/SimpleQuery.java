/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.simplequery;

import annis.gui.Helper;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.HorizontalLayout;
import annis.gui.controlpanel.ControlPanel;
import annis.gui.simplequery.VerticalNode;
import annis.gui.simplequery.AddMenu;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisCorpus;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import org.slf4j.LoggerFactory;


/**
 *
 * @author tom
 */
public class SimpleQuery extends Panel implements Button.ClickListener
{
  private Button btInitLanguage;
  private int id = 0;
  private Button btInitMeta;
  private ControlPanel cp;
  private HorizontalLayout language;
  private HorizontalLayout meta;
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SimpleQuery.class);
  
  public SimpleQuery(ControlPanel cp)
  {
    this.cp = cp;
    
    HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    btInitLanguage = new Button("Start with linguistic search", (Button.ClickListener) this);
    btInitLanguage.setStyleName(ChameleonTheme.BUTTON_SMALL);
    toolbar.addComponent(btInitLanguage);

    btInitMeta = new Button("Start with meta search", (Button.ClickListener) this);
    btInitMeta.setStyleName(ChameleonTheme.BUTTON_SMALL);
    toolbar.addComponent(btInitMeta);
    
    language = new HorizontalLayout();
    meta = new HorizontalLayout();
    
    HorizontalLayout option = new HorizontalLayout();
    CheckBox cb = new CheckBox("Search within sentence");
    cb.setDescription("Add some AQL code to the query to make it limited to a sentence.");
    cb.setImmediate(true);
    option.addComponent(cb);
    
    addComponent(toolbar);
    addComponent(language);
    addComponent(meta);
    addComponent(option);

  }
  
  @Override
  public void buttonClick(Button.ClickEvent event)
  {

    final SimpleQuery sq = this;
    
    if(event.getButton() == btInitLanguage)
    {

      MenuBar addMenu = new MenuBar();
      Collection<String> annonames = getAvailableAnnotationNames();
      final MenuBar.MenuItem add = addMenu.addItem("Add position", null);
      for (final String annoname : annonames)
      {
        add.addItem(killNamespace(annoname), new Command() {
          @Override
          public void menuSelected(MenuBar.MenuItem selectedItem) {
            id = id + 1;
            if (id > 1)
            {
              EdgeBox eb = new EdgeBox(id, sq);
              language.addComponent(eb);
            }
            VerticalNode vn = new VerticalNode(id, killNamespace(annoname), sq);
            language.addComponent(vn);
          }
        });
      }
      language.addComponent(addMenu);
    }
    
    if(event.getButton() == btInitMeta)
    {
      
      TextField tf = new TextField("meta");
      meta.addComponent(tf);
    }
  }
  
public Set<String> getAvailableAnnotationNames()
  {
    Set<String> result = new TreeSet<String>();

    WebResource service = Helper.getAnnisWebResource(getApplication());

    // get current corpus selection
    Set<String> corpusSelection = cp.getSelectedCorpora();

    if (service != null)
    {
      try
      {
        List<AnnisAttribute> atts = new LinkedList<AnnisAttribute>();
        
        for(String corpus : corpusSelection)
        {
          atts.addAll(
            service.path("query").path("corpora").path(corpus).path("annotations")
              .queryParam("fetchvalues", "false")
              .queryParam("onlymostfrequentvalues", "true")
              .get(new GenericType<List<AnnisAttribute>>() {})
            );
        }

        for (AnnisAttribute a : atts)
        {
          if (a.getType() == AnnisAttribute.Type.node)
          {
            result.add(a.getName());
          }
        }

      }
      catch (Exception ex)
      {
        log.error(null, ex);
      }
    }
    return result;
  }
  
  public Collection<String> getAvailableAnnotationLevels(String meta)
  {
    Collection<String> result = new TreeSet<String>();

    WebResource service = Helper.getAnnisWebResource(getApplication());

    // get current corpus selection
    Set<String> corpusSelection = cp.getSelectedCorpora();

    if (service != null)
    {
      try
      {
        List<AnnisAttribute> atts = new LinkedList<AnnisAttribute>();
        
        for(String corpus : corpusSelection)
        {
          atts.addAll(
            service.path("query").path("corpora").path(corpus).path("annotations")
              .queryParam("fetchvalues", "true")
              .queryParam("onlymostfrequentvalues", "false")
              .get(new GenericType<List<AnnisAttribute>>() {})
            );
        }
        
        for (AnnisAttribute a : atts)
        {
          if (a.getType() == AnnisAttribute.Type.node)
          {
            String aa = killNamespace(a.getName());
            if (aa.equals(meta))
            {
              result = a.getValueSet();
              break;
            }
          }
        }

      }
      catch (Exception ex)
      {
        log.error(null, ex);
      }
    }
    return result;
  }
    
  public String killNamespace(String qName)
  {
    String[] splitted = qName.split(":");
    return splitted[splitted.length - 1];
  }
}
