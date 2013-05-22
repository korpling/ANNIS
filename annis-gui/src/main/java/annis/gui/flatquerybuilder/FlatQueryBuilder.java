
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
import annis.gui.model.Query;
import annis.libgui.Helper;
import annis.service.objects.AnnisAttribute;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author martin
 * @author tom
 */
public class FlatQueryBuilder extends Panel implements Button.ClickListener
  {
  private Button btInitLanguage;
  private Label infoInitLanguage;
  private Button btInitSpan;
  private Label infoInitSpan;
  private Button btInitMeta;
  private Label infoInitMeta;
  private Button btGo;
  private Button btClear;
  private QueryController cp;
  private HorizontalLayout language;
  private HorizontalLayout span;
  private HorizontalLayout meta;
  private HorizontalLayout toolbar;
  private VerticalLayout mainLayout;
  private OptionGroup filtering;
  private Collection<VerticalNode> vnodes;
  private Collection<EdgeBox> eboxes;
  private Collection<MetaBox> mboxes;
  
  private static final String[] REGEX_CHARACTERS = {"\\", "+", ".", "[", "*", 
    "^","$", "|", "?", "(", ")"};
  private static final String BUTTON_LANGUAGE_LABEL = "Initialize linguistic search";
  private static final String BUTTON_SPAN_LABEL = "Initialize span constraint";
  private static final String BUTTON_META_LABEL = "Initialize meta search";
  private static final String BUTTON_GO_LABEL = "Create AQL Query";
  private static final String BUTTON_CLEAR_LABEL = "Clear the Query Builder";
  private static final String NO_CORPORA_WARNING = "No corpora selected, please select "
    + "at least one corpus.";
  private static final String INCOMPLETE_QUERY_WARNING = "Query seems to be incomplete.";
  private static final String NO_MULTIPLE_SPANS = "Only one span can be added as a "
    + "constraint.";
  private static final String ADD_LING_PARAM = "Add linguistic constraint";
  private static final String ADD_SPAN_PARAM = "Add span constraint";
  private static final String ADD_META_PARAM = "Add meta constraint";
  private static final String LING_MENU_DESC = "Choose an annotation level to "
    + "expand the query to the right";
  private static final String INFO_INIT_LANG = "In this part of the Query Builder, "
    + "blocks of the linguistic query can be constructed from left to right.";
  private static final String INFO_INIT_SPAN = "This part of the Query Builder "
    + "allows you to define a span annotation within which the above query blocks "
    + "are confined.";
  private static final String INFO_INIT_META = "Here, you can constrain the linguistic "
    + "query by selecting meta levels.";
  private String TOOLBAR_CAPTION = "Toolbar";
  private String META_CAPTION = "Meta constraints";
  private String SPAN_CAPTION = "Span constraints";
  private String LANG_CAPTION = "Precedence constraints";

  public FlatQueryBuilder(QueryController cp)
  {
    launch(cp);
  }

  private void launch(QueryController cp)
  {
    this.cp = cp;
    mainLayout = new VerticalLayout();
    // tracking lists for vertical nodes, edgeboxes and metaboxes
    vnodes = new ArrayList<VerticalNode>();
    eboxes = new ArrayList<EdgeBox>();
    mboxes = new ArrayList<MetaBox>();
    // buttons and checks
    btInitLanguage = new Button(BUTTON_LANGUAGE_LABEL, (Button.ClickListener) this);
    btInitLanguage.setStyleName(ChameleonTheme.BUTTON_SMALL);
    infoInitLanguage = new Label(INFO_INIT_LANG);
    btInitSpan = new Button(BUTTON_SPAN_LABEL, (Button.ClickListener) this);
    infoInitSpan = new Label(INFO_INIT_SPAN);
    btInitSpan.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btInitMeta = new Button(BUTTON_META_LABEL, (Button.ClickListener) this);
    btInitMeta.setStyleName(ChameleonTheme.BUTTON_SMALL);
    infoInitMeta = new Label(INFO_INIT_META);
    btGo = new Button(BUTTON_GO_LABEL, (Button.ClickListener) this);
    btGo.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btClear = new Button(BUTTON_CLEAR_LABEL, (Button.ClickListener) this);
    btClear.setStyleName(ChameleonTheme.BUTTON_SMALL);
    filtering = new OptionGroup("Filtering mechanism");
    filtering.addItem(1);
    filtering.setItemCaption(1, "Generic (Levenshtein)");
    filtering.addItem(2);
    filtering.setItemCaption(2, "Specified (pre-defined mapping)");
    filtering.select(2);
    filtering.setNullSelectionAllowed(false);
    filtering.setImmediate(true);
    // language layout
    language = new HorizontalLayout();
    language.setSpacing(true);
    language.addComponent(btInitLanguage);
    language.addComponent(infoInitLanguage);
    language.setMargin(true);
    language.setCaption(LANG_CAPTION);
    // span layout
    span = new HorizontalLayout();
    span.setSpacing(true);
    span.addComponent(btInitSpan);
    span.addComponent(infoInitSpan);
    span.setMargin(true);
    span.setCaption(SPAN_CAPTION);
    // meta layout
    meta = new HorizontalLayout();
    meta.setSpacing(true);
    meta.addComponent(btInitMeta);
    meta.addComponent(infoInitMeta);
    meta.setMargin(true);
    meta.setCaption(META_CAPTION);
    // toolbar layout
    toolbar = new HorizontalLayout();
    toolbar.setSpacing(true);
    toolbar.addComponent(filtering);
    toolbar.addComponent(btGo);
    toolbar.addComponent(btClear);
    toolbar.setMargin(true);
    toolbar.setCaption(TOOLBAR_CAPTION);
    // put everything on the layout
    mainLayout.setSpacing(true);
    mainLayout.addComponent(language);
    mainLayout.addComponent(span);
    mainLayout.addComponent(meta);
    mainLayout.addComponent(toolbar);
    setContent(mainLayout);
    getContent().setSizeFull();
  }
  
  private String getAQLFragment(SearchBox sb)
  {
    String result, value=sb.getValue(), level=sb.getAttribute();
    if (sb.isRegEx())
    {
      result = (value==null) ? level+"=/.*/" : level+"=/"+value.replace(
        "/", "\\x2F") +"/";
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
    
    return result.replace("/", "\\x2F");
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
      String edgeQueryAdds = (itEboxes.hasNext()) ? "\n& #"+(count-1)+" "
        +itEboxes.next().getValue()+" #"+count : "";
      edgeQuery += edgeQueryAdds;
    }
    String addQuery = "";
    try
    {
      SpanBox spb = (SpanBox) span.getComponent(1);
      if ((!spb.isRegEx())&&(!spb.getValue().isEmpty()))
      {
        addQuery = "\n& "+ spb.getAttribute() + " = \"" + spb.getValue() + "\"";        
      }
      if (spb.isRegEx())
      {
        addQuery = "\n& "+ spb.getAttribute() + " = /" + spb.getValue().replace("/", "\\x2F") + "/";
      }
      query += addQuery;
      for(Integer i : sentenceVars)
      {
        sentenceQuery += "\n& #" + count + "_i_#"+i.toString();
      }
    } catch (Exception ex){
      ex = null;
    }
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
    try{
      cp.setQuery(new Query(getAQLQuery(), null));
    } catch (java.lang.NullPointerException ex) {
      getUI().showNotification(INCOMPLETE_QUERY_WARNING);
    }
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
        language.removeComponent(infoInitLanguage);
        MenuBar addMenu = new MenuBar();
        addMenu.setDescription(LING_MENU_DESC);
        addMenu.setWidth("150px");
        Collection<String> annonames = getAvailableAnnotationNames();
        final MenuBar.MenuItem add = addMenu.addItem(ADD_LING_PARAM, null);
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
      if(event.getButton() == btInitSpan)
      {
        span.removeComponent(btInitSpan);
        span.removeComponent(infoInitSpan);
        MenuBar addMenu = new MenuBar();
        Collection<String> annonames = getAvailableAnnotationNames();
        final MenuBar.MenuItem add = addMenu.addItem(ADD_SPAN_PARAM, null);
        for (final String annoname : annonames)
        {
          add.addItem(annoname, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
              SpanBox spb = new SpanBox(annoname, sq);
              if (span.getComponentCount() < 2){
                span.addComponent(spb);
                span.setComponentAlignment(spb, Alignment.MIDDLE_LEFT);
              }
              if (span.getComponentCount() > 1){
                getUI().showNotification(NO_MULTIPLE_SPANS);
              }
            }
          });
        }
        span.addComponent(addMenu);
      }
      if(event.getButton() == btInitMeta)
      {
        meta.removeComponent(btInitMeta);
        meta.removeComponent(infoInitMeta);
        MenuBar addMenu = new MenuBar();
        Collection<String> annonames = getAvailableMetaNames();
        final MenuBar.MenuItem add = addMenu.addItem(ADD_META_PARAM, null);
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
        language.removeAllComponents();
        span.removeAllComponents();
        meta.removeAllComponents();
        toolbar.removeAllComponents();
        mainLayout.removeComponent(language);
        mainLayout.removeComponent(span);
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

public void removeSpanBox(SpanBox v)
  {
    span.removeComponent(v);
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
          atts.addAll(service.path("query").path("corpora").path(corpus).path("annotations")
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
          atts.addAll(service.path("query").path("corpora").path(corpus)
              .path("annotations")
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
          atts.addAll(service.path("query").path("corpora").path(corpus)
              .path("annotations")
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
          atts.addAll(service.path("query").path("corpora").path(corpus)
              .path("annotations")
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
  
  public String getFilterMechanism()
  {
    String out = "";
    if (filtering.getValue().equals(1)){
      out = "levenshtein";
    }
    if (filtering.getValue().equals(2)){
      out = "specific";
    }
    return out;
  }
  
  public void adjustBuilderToQuery()
    /*
     * this method is called, when the
     * query is changed in the textfield,
     * so that the query represented by 
     * the query builder is not equal to
     * the one delivered by the text field
     */
  {
    
  }
}