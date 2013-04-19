
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
package annis.gui.flatquerybuilder;

import annis.gui.QueryController;
import annis.gui.controlpanel.ControlPanel;
import annis.gui.model.Query;
import annis.libgui.Helper;
import annis.model.Annotation;
import annis.service.objects.AnnisAttribute;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import com.vaadin.ui.themes.ChameleonTheme;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

/**
 *
 * @author tom
 */
public class FlatQueryBuilder extends Panel implements Button.ClickListener
  {
  private Button btInitLanguage;
  private Button btInitMeta;
  private Button btGo;
  private Button btClear;
  private QueryController cp;
  private HorizontalLayout language;
  private HorizontalLayout meta;
  private HorizontalLayout option;
  private HorizontalLayout toolbar;
  private VerticalLayout mainLayout;
  private SpanBox spb;
  private Collection<VerticalNode> vnodes;
  private Collection<EdgeBox> eboxes;
  private Collection<MetaBox> mboxes;
  
  private static final String[] REGEX_CHARACTERS = {"\\", "+", ".", "[", "*", "^","$", "|", "?", "(", ")"};
  private static final String BUTTON_LANGUAGE_LABEL = "Start with linguistic search";
  private static final String BUTTON_META_LABEL = "Start with meta search";
  private static final String BUTTON_GO_LABEL = "Create AQL Query";
  private static final String BUTTON_CLEAR_LABEL = "Clear the Query Builder";
  private static final String NO_CORPORA_WARNING = "No corpora selected";
  private static final String BUTTON_ADD_LABEL = "Add position";

  public FlatQueryBuilder(QueryController cp)
  {
    launch(cp);
  }

  private void launch(QueryController cp)
  {
    this.cp = cp;
    mainLayout = new VerticalLayout();
    
    vnodes = new ArrayList<VerticalNode>();
    eboxes = new ArrayList<EdgeBox>();
    mboxes = new ArrayList<MetaBox>();

    btInitLanguage = new Button(BUTTON_LANGUAGE_LABEL, (Button.ClickListener) this);
    btInitLanguage.setStyleName(ChameleonTheme.BUTTON_SMALL);

    btInitMeta = new Button(BUTTON_META_LABEL, (Button.ClickListener) this);
    btInitMeta.setStyleName(ChameleonTheme.BUTTON_SMALL);

    btGo = new Button(BUTTON_GO_LABEL, (Button.ClickListener) this);
    btGo.setStyleName(ChameleonTheme.BUTTON_SMALL);

    btClear = new Button(BUTTON_CLEAR_LABEL, (Button.ClickListener) this);
    btClear.setStyleName(ChameleonTheme.BUTTON_SMALL);

    spb = new SpanBox(this);
    
    language = new HorizontalLayout();
    language.addComponent(btInitLanguage);
    meta = new HorizontalLayout();
    meta.addComponent(btInitMeta);
    option = new HorizontalLayout();
    option.addComponent(spb);
    toolbar = new HorizontalLayout();
    toolbar.addComponent(btGo);
    toolbar.addComponent(btClear);
    
    mainLayout.addComponent(language);
    mainLayout.addComponent(meta);
    mainLayout.addComponent(option);
    mainLayout.addComponent(toolbar);
    
    setContent(mainLayout);
//    setScrollable(true);
    getContent().setSizeUndefined();
    setHeight("100%");
    
  }
  
  private String getAQLFragment(SearchBox sb)
  {
    String result, value=sb.getValue(), level=sb.getAttribute();
    if (sb.isRegEx())
    {
      result = (value==null) ? level+"=/.*/" : level+"=/"+value+"/";
    }
    else
    {
      result = (value==null) ? level+"=/.*/" : level+"=\""+value+"\"";      
    }
    return result;
  }
  
  private String getMetaQueryFragment(MetaBox mb)
  {    
    Collection<String> values = mb.getValues();
    String result = "\n& meta::"+mb.getMetaDatum()+" = ";
    if(values.size()==1)
    {
      result += "\""+values.iterator().next()+"\"";
    }
    else
    {      
      Iterator<String> itValues = values.iterator();
      result += "/(" + escapeRegexCharacters(itValues.next())+")";
      while(itValues.hasNext())
      {
        result+= "|("+escapeRegexCharacters(itValues.next())+")";
      }
      result += "/";
    }   
    
    return result;
  }
  
  public String escapeRegexCharacters(String tok)
  {
    if(tok==null | tok.equals("")){return "";}
    String result=tok;
    for (int i = 0; i<REGEX_CHARACTERS.length; i++)
    {
      result = result.replace(REGEX_CHARACTERS[i], "\\"+REGEX_CHARACTERS[i]);
    }
    return result;
  }

  private String getAQLQuery()
  {
    int count = 1;    
    String query = "", edgeQuery = "", sentenceQuery = "";
    Collection<Integer> sentenceVars = new ArrayList<Integer>();
    Iterator<EdgeBox> itEboxes = eboxes.iterator();
    
    for (VerticalNode v : vnodes)
    {
      Collection<SearchBox> sboxes = v.getSearchBoxes();
      for (SearchBox s : sboxes)
      {
        query += " & " + getAQLFragment(s);
      }
      if (sboxes.isEmpty())
      {
        //not sure we want to do it this way:
        query += "\n& /.*/";
      }
      
      sentenceVars.add(new Integer(count));

      for(int i=1; i < sboxes.size(); i++)
      {
        String addQuery = "\n& #" + count +"_=_"+ "#" + ++count;
        edgeQuery += addQuery;
      }
      
      count++;
      String edgeQueryAdds = (itEboxes.hasNext()) ? "\n& #" + (count-1) +" "+ itEboxes.next().getValue() +" #" + count : "";
      edgeQuery += edgeQueryAdds;
    }    
    
    //search within span?
    if(spb.searchWithinSpan())
    {
      String addQuery;
      
      if ((!spb.isRegEx())&&(!spb.getSpanValue().equals("")))
      {
        addQuery = "\n& "+ spb.getSpanName() + " = \"" + spb.getSpanValue() + "\"";        
      }
      else if (spb.getSpanValue().equals(""))
      {        
        addQuery = "\n& "+ spb.getSpanName() + " = /.*/";
      }
      else
      {
        addQuery = "\n& "+ spb.getSpanName() + " = /" + spb.getSpanValue() + "/";
      }
      query += addQuery;
      for(Integer i : sentenceVars)
      {
        sentenceQuery += "\n& #" + count + "_i_#"+i.toString();
      }
    }
    
    //metaquery:
    String metaQuery = "";
    Iterator<MetaBox> itMetaBoxes = mboxes.iterator();
    while(itMetaBoxes.hasNext())
    {
      metaQuery += getMetaQueryFragment(itMetaBoxes.next());
    }

    String fullQuery = (query+edgeQuery+sentenceQuery+metaQuery);
    if (fullQuery.length() < 3) {return "";}
    fullQuery = fullQuery.substring(3);//deletes leading " & "    
    return fullQuery;
  }

  public void updateQuery()
  {
    cp.setQuery(new Query(getAQLQuery(), null));
  }

  @Override
  public void buttonClick(Button.ClickEvent event)
  {

    final FlatQueryBuilder sq = this;
    
    if (cp.getSelectedCorpora().isEmpty()){
      getUI().showNotification(NO_CORPORA_WARNING);
    }
    
    else
    {
      if(event.getButton() == btInitLanguage)
      {
        language.removeComponent(btInitLanguage);
        MenuBar addMenu = new MenuBar();
        Collection<String> annonames = getAvailableAnnotationNames();
        final MenuBar.MenuItem add = addMenu.addItem(BUTTON_ADD_LABEL, null);
        for (final String annoname : annonames)
        {
          add.addItem(annoname, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
              if (!vnodes.isEmpty())
              {
                EdgeBox eb = new EdgeBox(sq);
                language.addComponent(eb);
                eboxes.add(eb);
              }

              VerticalNode vn = new VerticalNode(annoname, sq);
              language.addComponent(vn);
              vnodes.add(vn);

            }
          });
        }
        language.addComponent(addMenu);
      }
      if(event.getButton() == btInitMeta)
      {
        meta.removeComponent(btInitMeta);
        MenuBar addMenu = new MenuBar();
        Collection<String> annonames = getAvailableMetaNames();
        final MenuBar.MenuItem add = addMenu.addItem("Add position", null);
        for (final String annoname : annonames)
        {
          add.addItem(annoname, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
              MetaBox mb = new MetaBox(annoname, sq);
              meta.addComponent(mb);
              mboxes.add(mb);
            }
          });
        }
        meta.addComponent(addMenu);
      }
      if (event.getButton() == btGo)
      {
        updateQuery();
      }

      if (event.getButton() == btClear)
      {
        option.removeAllComponents();
        language.removeAllComponents();
        meta.removeAllComponents();
        toolbar.removeAllComponents();
        mainLayout.removeComponent(option);
        mainLayout.removeComponent(language);
        mainLayout.removeComponent(meta);
        mainLayout.removeComponent(toolbar);
        vnodes.clear();
        eboxes.clear();
        mboxes.clear();
        updateQuery();
        launch(cp);
      }
    }
  }

public void removeVerticalNode(VerticalNode v)
  {
    language.removeComponent(v);
    for (VerticalNode vnode : vnodes)
    {
      Iterator<EdgeBox> ebIterator = eboxes.iterator();
      if((ebIterator.hasNext()) && (v.equals(vnode)))
      {
        EdgeBox eb = eboxes.iterator().next();
        eboxes.remove(eb);
        language.removeComponent(eb);
        break;
      }
    }
    vnodes.remove(v);
    updateQuery();
  }

public void removeMetaBox(MetaBox v)
  {
    meta.removeComponent(v);
    mboxes.remove(v);
    updateQuery();
  }

public Collection<String> getAnnotationValues(String level)
  {
    Collection<String> values = new TreeSet<String>();
    
    for(String s : getAvailableAnnotationLevels(level))
    {      
      values.add(s);
    }
    
    return values;
  }

public Set<String> getAvailableAnnotationNames()
  {
    Set<String> result = new TreeSet<String>();

    WebResource service = Helper.getAnnisWebResource();

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
              .queryParam("onlymostfrequentvalues", "false")
              .get(new GenericType<List<AnnisAttribute>>() {})
            );
        }

        for (AnnisAttribute a : atts)
        {
          if (a.getType() == AnnisAttribute.Type.node)
          {
            result.add(killNamespace(a.getName()));
          }
        }

      }
      catch (Exception ex)
      {

      }
    }
    return result;
  }

  public Collection<String> getAvailableAnnotationLevels(String meta)
  {
    Collection<String> result = new TreeSet<String>();

    WebResource service = Helper.getAnnisWebResource();

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
              result.addAll(a.getValueSet());
            }
          }
        }

      }
      catch (Exception ex)
      {

      }
    }
    return result;
  }

  public String killNamespace(String qName)
  {
    String[] splitted = qName.split(":", 2);
    if (splitted.length > 1){
      return splitted[1];
    }
    else{
      return qName;
    }
  }

  public Set<String> getAvailableMetaNames()
  {
    Set<String> result = new TreeSet<String>();

    WebResource service = Helper.getAnnisWebResource();

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
              .get(new GenericType<List<AnnisAttribute>>() {})
            );
        }

        for (AnnisAttribute a : atts)
        {
          if (a.getType() == AnnisAttribute.Type.meta)
          {
            String aa = killNamespace(a.getName());
            result.add(aa);
          }
        }

      }
      catch (Exception ex)
      {

      }
    }
    return result;
  }

  public Set<String> getAvailableMetaLevels(String meta)
{
    Set<String> result = new TreeSet<String>();

    WebResource service = Helper.getAnnisWebResource();

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
          if (a.getType() == AnnisAttribute.Type.meta)
          {
            String aa = killNamespace(a.getName());
            if (aa.equals(meta))
            {
              result.addAll(a.getValueSet());
            }
          }
        }

      }
      catch (Exception ex)
      {

      }
    }
    return result;
  }
}
