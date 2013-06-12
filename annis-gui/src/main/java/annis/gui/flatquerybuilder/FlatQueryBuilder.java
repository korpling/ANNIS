
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
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/*
 * @author martin
 * @author tom
 */
public class FlatQueryBuilder extends Panel implements Button.ClickListener
  {
  private Button btInitLanguage;
  private Button btInitSpan;
  private Button btInitMeta;
  private Button btGo;
  private Button btClear;
  private Button btInverse;
  private QueryController cp;
  private HorizontalLayout language;
  private HorizontalLayout languagenodes;
  private HorizontalLayout span;
  private HorizontalLayout meta;
  private HorizontalLayout toolbar;
  private HorizontalLayout advanced;
  private VerticalLayout mainLayout;
  private NativeSelect filtering;
  private Collection<VerticalNode> vnodes;
  private Collection<EdgeBox> eboxes;
  private Collection<MetaBox> mboxes;
  private String query;
  private MenuBar.MenuItem spanMenu;
  
  private static final String[] REGEX_CHARACTERS = {"\\", "+", ".", "[", "*", 
    "^","$", "|", "?", "(", ")"};

  private static final String BUTTON_GO_LABEL = "Create AQL Query";
  private static final String BUTTON_CLEAR_LABEL = "Clear the Query Builder";
  private static final String BUTTON_INV_LABEL = "Load Query";
  private static final String NO_CORPORA_WARNING = "No corpora selected, please select "
    + "at least one corpus.";
  private static final String INCOMPLETE_QUERY_WARNING = "Query seems to be incomplete.";

  private static final String ADD_LING_PARAM = "Add";
  private static final String ADD_SPAN_PARAM = "Add";
  private static final String CHANGE_SPAN_PARAM = "Change";
  private static final String ADD_META_PARAM = "Add";
  
  private static final String INFO_INIT_LANG = "In this part of the Query Builder, "
    + "blocks of the linguistic query can be constructed from left to right.";
  private static final String INFO_INIT_SPAN = "This part of the Query Builder "
    + "allows you to define a span annotation within which the above query blocks "
    + "are confined.";
  private static final String INFO_INIT_META = "Here, you can constrain the linguistic "
    + "query by selecting meta levels.";
  private static final String INFO_FILTER = "When searching in the fields, the "
    + "hits are sorted and filtered according to different mechanisms. Please "
    + "choose a filtering mechanism here.";
  
  private String TOOLBAR_CAPTION = "Toolbar";
  private String META_CAPTION = "Restrict the search by means of meta information";
  private String SPAN_CAPTION = "Restrict the scope of the linguistic sequence";
  private String LANG_CAPTION = "Create a linguistic sequence";
  private String ADVANCED_CAPTION = "Advanced settings";

  public FlatQueryBuilder(QueryController cp)
  {
    launch(cp);
  }

  private void launch(QueryController cp)
  {
    this.cp = cp;
    this.query = "";
    mainLayout = new VerticalLayout();
    // tracking lists for vertical nodes, edgeboxes and metaboxes
    vnodes = new ArrayList<VerticalNode>();
    eboxes = new ArrayList<EdgeBox>();
    mboxes = new ArrayList<MetaBox>();
    // buttons and checks
    btInitLanguage = new Button(ADD_LING_PARAM, (Button.ClickListener) this);
    btInitLanguage.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btInitLanguage.setDescription(INFO_INIT_LANG);
    btInitSpan = new Button(ADD_SPAN_PARAM, (Button.ClickListener) this);
    btInitSpan.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btInitSpan.setDescription(INFO_INIT_SPAN);
    btInitMeta = new Button(ADD_META_PARAM, (Button.ClickListener) this);
    btInitMeta.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btInitMeta.setDescription(INFO_INIT_META);
    btGo = new Button(BUTTON_GO_LABEL, (Button.ClickListener) this);
    btGo.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btClear = new Button(BUTTON_CLEAR_LABEL, (Button.ClickListener) this);
    btClear.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btInverse = new Button(BUTTON_INV_LABEL, (Button.ClickListener)this);
    btInverse.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btInverse.setEnabled(false);
    filtering = new NativeSelect("Filtering mechanisms");
    filtering.setDescription(INFO_FILTER);
    reducingStringComparator rdc = new reducingStringComparator();
    Set mappings = rdc.getMappings().keySet();
    int i;
    for (i=0; i<mappings.size(); i++){
      String mapname = (String) mappings.toArray()[i];
      filtering.addItem(i);
      filtering.setItemCaption(i, mapname);
    }
    filtering.addItem(i+1);
    filtering.setItemCaption(i+1, "generic");
    filtering.select(i+1);
    filtering.setNullSelectionAllowed(false);
    filtering.setImmediate(true);
    // language layout
    language = new HorizontalLayout();
    languagenodes = new HorizontalLayout();
    language.setSpacing(true);
    language.addComponent(languagenodes);
    language.addComponent(btInitLanguage);
    language.setMargin(true);
    language.setCaption(LANG_CAPTION);
    // span layout
    span = new HorizontalLayout();
    span.setSpacing(true);
    span.addComponent(btInitSpan);
    span.setMargin(true);
    span.setCaption(SPAN_CAPTION);
    // meta layout
    meta = new HorizontalLayout();
    meta.setSpacing(true);
    meta.addComponent(btInitMeta);
    meta.setMargin(true);
    meta.setCaption(META_CAPTION);
    // toolbar layout
    toolbar = new HorizontalLayout();
    toolbar.setSpacing(true);
    toolbar.addComponent(btGo);
    toolbar.addComponent(btClear);
    toolbar.addComponent(btInverse);
    toolbar.setMargin(true);
    toolbar.setCaption(TOOLBAR_CAPTION);
    // advanced
    advanced = new HorizontalLayout();
    advanced.setSpacing(true);
    advanced.addComponent(filtering);
    advanced.setMargin(true);
    advanced.setCaption(ADVANCED_CAPTION);
    // put everything on the layout
    mainLayout.setSpacing(true);
    mainLayout.addComponent(language);
    mainLayout.addComponent(span);
    mainLayout.addComponent(meta);
    mainLayout.addComponent(toolbar);
    mainLayout.addComponent(advanced);
    setContent(mainLayout);
    getContent().setSizeFull();
  }
  
  private String getAQLFragment(SearchBox sb)
  {
    String result = ""; 
    String value = sb.getValue();
    String level=sb.getAttribute();
    if (sb.isRegEx() && !sb.isNegativeSearch())
    {
      result = (value==null) ? level+"=/.*/" : level+"=/"+value.replace("/", "\\x2F") +"/";
    }
    if (sb.isRegEx() && sb.isNegativeSearch())
    {
      result = (value==null) ? level+"!=/.*/" : level+"!=/"+value.replace("/", "\\x2F") +"/";
    }
    if (!sb.isRegEx() && sb.isNegativeSearch())
    {
      result = (value==null) ? level+"!=/.*/" : level+"!=\""+value+"\"";            
    }
    if (!sb.isRegEx() && !sb.isNegativeSearch())
    {
      result = (value==null) ? level+"=/.*/" : level+"=\""+value+"\"";      
    }
    return result;
  }
  
  private String getMetaQueryFragment(MetaBox mb)
  {    
    Collection<String> values = mb.getValues();
    if(!values.isEmpty())
    {
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
    return "";
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
  
  private String unescape(String s)
	{
    //first unescape slashes:
		
		s = s.replace("\\x2F", "/");
    
    
    //unescape regex characters:
		int i=1;
		while(i<s.length())
		{
			char c0 = s.charAt(i-1);
			char c1 = s.charAt(i);
			for(int j=0; j<REGEX_CHARACTERS.length; j++)
			{
				if( (c1==REGEX_CHARACTERS[j].charAt(0)) & (c0=='\\') )
				{
					s = s.substring(0, i-1) + s.substring(i);
					break;
				}
				if(j==REGEX_CHARACTERS.length-1)
				{
					i++;
				}
			}			
		}		
    
    return s;
	}

  private String getAQLQuery()
  {
    int count = 1;    
    String ql = "", edgeQuery = "", sentenceQuery = "";
    Collection<Integer> sentenceVars = new ArrayList<Integer>();
    Iterator<EdgeBox> itEboxes = eboxes.iterator();
    for (VerticalNode v : vnodes)
    {
      Collection<SearchBox> sboxes = v.getSearchBoxes();
      for (SearchBox s : sboxes)
      {
        ql += " & " + getAQLFragment(s);
      }
      if (sboxes.isEmpty())
      {
        //not sure we want to do it this way:
        ql += "\n& /.*/";
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
      ql += addQuery;
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
    String fullQuery = (ql+edgeQuery+sentenceQuery+metaQuery);
    if (fullQuery.length() < 3) {return "";}
    fullQuery = fullQuery.substring(3);//deletes leading " & "
    this.query = fullQuery;
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
        btInverse.setEnabled(true);
        final MenuBar addMenu = new MenuBar();
        addMenu.setAutoOpen(true);
        addMenu.setDescription(INFO_INIT_LANG);
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
                languagenodes.addComponent(eb);
                eboxes.add(eb);
              }
              VerticalNode vn = new VerticalNode(annoname, sq);
              languagenodes.addComponent(vn);
              vnodes.add(vn);
              addMenu.setAutoOpen(false);
            }
          });
        }
        language.addComponent(addMenu);
      }
      if(event.getButton() == btInitSpan)
      {
        span.removeComponent(btInitSpan);
        final MenuBar addMenu = new MenuBar();
        addMenu.setAutoOpen(true);
        addMenu.setDescription(INFO_INIT_SPAN);
        Collection<String> annonames = getAvailableAnnotationNames();
        final MenuBar.MenuItem add = addMenu.addItem(ADD_SPAN_PARAM, null);
        for (final String annoname : annonames)
        {
          add.addItem(annoname, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
              SpanBox spb = new SpanBox(annoname, sq);
              if (span.getComponentCount() > 1){
                span.removeComponent(span.getComponent(1));
              }
              span.addComponent(spb);
              span.setComponentAlignment(spb, Alignment.MIDDLE_LEFT);
              addMenu.setAutoOpen(false);
              add.setText(CHANGE_SPAN_PARAM);
            }
          });
        }
        spanMenu = add;
        span.addComponent(addMenu);
      }
      if(event.getButton() == btInitMeta)
      {
        meta.removeComponent(btInitMeta);
        final MenuBar addMenu = new MenuBar();
        addMenu.setAutoOpen(true);
        addMenu.setDescription(INFO_INIT_META);
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
              addMenu.setAutoOpen(false);
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
        clear();
        updateQuery();
        launch(cp);
      }
      if (event.getComponent() == btInverse)
      {
        loadQuery();
      }
    }
  }

public void removeVerticalNode(VerticalNode v)
  {
    Iterator<VerticalNode> itVnodes = vnodes.iterator();
    Iterator<EdgeBox> itEboxes = eboxes.iterator();
    VerticalNode vn = itVnodes.next();
    EdgeBox eb=null;
    
    while(!vn.equals(v))
    {
      vn = itVnodes.next();
      eb = itEboxes.next();
    }
    
    if((eb==null) & (itEboxes.hasNext()))
    {
      eb = itEboxes.next();
    }
    
    vnodes.remove(v);
    if(eb!=null)
    {
      eboxes.remove(eb);
      languagenodes.removeComponent(eb);
    }
    languagenodes.removeComponent(v);
    updateQuery();
  }

public void removeSpanBox(SpanBox v)
  {
    span.removeComponent(v);
    spanMenu.setText(ADD_SPAN_PARAM);
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
    return filtering.getItemCaption(filtering.getValue());
  }
  
  private void clear()
    //check whether it is necessary to do this in a method
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
  }
  
  public Collection<String> parseMetaExpression(String expression)
	/*
	 * only for complex regex expressions
	 */
	{		
		Collection<String> values = new TreeSet<String>();
		int i=0;
		
		while(expression.length()>0)
		{
			if((expression.charAt(i)=='|')|(i==expression.length()-1))
			{
				String value = (i==expression.length()-1) ? expression : expression.substring(0, i);
				if((value.startsWith("("))&(value.charAt(value.length()-1)==')'))
				{
					value = value.substring(1, value.length()-1);
				}
				value = unescape(value);
        values.add(value);
				expression = expression.substring(i+1);
				i=0;				
			}
			else
			{
				i++;
			}
		}
		
		return values;
	}
  
  public void loadQuery()
    /*
     * this method is called, when the
     * query is changed in the textfield,
     * so that the query represented by 
     * the query builder is not equal to
     * the one delivered by the text field
     */
  {
    
    String tq;//typed-in query
    
    try
    {
      //doesn't work (typed text not yet saved as query I guess)
      tq = cp.getQueryDraft();
    } catch (NullPointerException e)
    {
      tq = "";
    }
    tq = tq.replace("\n", " ");
    //2do: VALIDATE QUERY: (NOT SUFFICIENT YET)
    boolean valid = (tq!=null);
    if(!(query.equals(tq)) & valid)
    {
      //PROBLEM: LINE BREAKS
      //the dot marks the end of the string - it is not read, it's just a place holder
      tq += ".";
      HashMap<Integer, Constraint> constraints = new HashMap<Integer, Constraint>();
      ArrayList<Relation> pRelations = new ArrayList<Relation>();
      ArrayList<Relation> eRelations = new ArrayList<Relation>();
      Relation inclusion=null;
      Constraint conInclusion=null;
      ArrayList<Constraint> metaConstraints = new ArrayList<Constraint>();
      
      //parse typed-in Query
      //Step 1: get indices of tq-chars, where constraints are separated (&)
      String tempCon="";
      int count = 1;
      boolean inclusionCheck=false;
      
      for(int i=0; i<tq.length(); i++)
      {
        //improve this Algorithm (compare to constraint)
        char c = tq.charAt(i);
        if((c!='&') & (i!=tq.length()-1))
        {
          if(!((tempCon.length()==0) & (c==' '))) //avoids, that a constraint starts with a space
          {
            tempCon += c;
          }
        }
        else
        {
          while(tempCon.charAt(tempCon.length()-1)==' ')
          {
            tempCon = tempCon.substring(0, tempCon.length()-1);
          }
          
          if(tempCon.startsWith("meta::"))
          {
            metaConstraints.add(new Constraint(tempCon));
          }
          else if(tempCon.startsWith("#"))          
          {
            Relation r = new Relation(tempCon);
            if(r.getType()==RelationType.EQUALITY)
            {
              eRelations.add(r);
            }
            else if(r.getType()==RelationType.PRECEDENCE)
            {
              pRelations.add(r);
            }
            else if((r.getType()==RelationType.INCLUSION)&(!inclusionCheck))
            {
              inclusion = r;
              if(constraints.containsKey(r.getFirst()))
              {
                conInclusion = constraints.get(r.getFirst());
                constraints.remove(r.getFirst());
              }
              inclusionCheck=true;
            }
          }         
          else 
          {
            constraints.put(count++, new Constraint(tempCon));
          }
          
          tempCon = "";
        }
      }    
      
      //clean query builder surface
      for(VerticalNode vn : vnodes)
      {
        languagenodes.removeComponent(vn);        
      }
      vnodes.clear();
      
      for(EdgeBox eb : eboxes)
      {
        languagenodes.removeComponent(eb);
      }
      eboxes.clear();
      
      for(MetaBox mb : mboxes)
      {
        meta.removeComponent(mb);          
      }
      mboxes.clear();
      
      //too hacky?!
      span.removeAllComponents();
      span.addComponent(btInitSpan);
            
      HashMap<Integer, VerticalNode> indexedVnodes = new HashMap<Integer, VerticalNode>();
      VerticalNode vn=null;
      for(int i : constraints.keySet())
      {        
        Constraint con = constraints.get(i);        
        if(!indexedVnodes.containsKey(i))
        {
          String value = (con.isRegEx()) ? unescape(con.getValue()) : con.getValue();
          vn = new VerticalNode(con.getLevel(), value, this, con.isRegEx(), con.isNegative());
          indexedVnodes.put(i, vn);
        }
        
        for(Relation rel : eRelations)
        {
          if(rel.contains(i))
          {
            int b = rel.whosMyFriend(i);
            if(!indexedVnodes.containsKey(b))
            {
              indexedVnodes.put(b, null);
              Constraint bcon = constraints.get(b);
              SearchBox sb = new SearchBox(bcon.getLevel(), this, vn, bcon.isRegEx(), bcon.isNegative());
              String bvalue = (bcon.isRegEx()) ? unescape(bcon.getValue()) : bcon.getValue();
              sb.setValue(bvalue);
              vn.addSearchBox(sb);
            }
          }          
        }
      }
            
      VerticalNode first = null;
      int smP = (!pRelations.isEmpty()) ? pRelations.iterator().next().getFirst() : 0;
      int smE = (!eRelations.isEmpty()) ? eRelations.iterator().next().getFirst() : 0;
      if((smP+smE)==0)
      {
        if(!indexedVnodes.isEmpty())
        {
          first = indexedVnodes.values().iterator().next(); 
        }        
      }
      else if((smP!=0) & (smE!=0))
      {
        first = indexedVnodes.get(Math.min(smE, smP));
      }
      else
      {
        //one value is zero
        first = indexedVnodes.get(smE+smP);
      }
      
      if(first!=null)
      {
        vnodes.add(first);
        languagenodes.addComponent(first);

        for(Relation rel : pRelations)
        {
          EdgeBox eb = new EdgeBox(this);
          eb.setValue(rel.getOperator());
          eboxes.add(eb);
          VerticalNode v = indexedVnodes.get(rel.getSecond());
          vnodes.add(v);

          languagenodes.addComponent(eb);
          languagenodes.addComponent(v);  
        }
      }
      
      //build SpanBox
      if(inclusion!=null)
      {        
        String level = conInclusion.getLevel();
        String value = (conInclusion.isRegEx()) ? unescape(conInclusion.getValue()) : conInclusion.getValue();
        SpanBox spb = new SpanBox(level, this, conInclusion.isRegEx());
        spb.setValue(value);
        span.addComponent(spb);
      }
      
      //build MetaBoxes
      for(Constraint mc : metaConstraints)
      {
        if(mc.isRegEx())
        {
          Collection<String> values = parseMetaExpression(mc.getValue());
          MetaBox mb = new MetaBox(mc.getLevel(), this);
          mb.setValue(values);
          mboxes.add(mb);
          meta.addComponent(mb);
        }
        else
        {
          MetaBox mb = new MetaBox(mc.getLevel(), this);
          //for a particular reason (unknown) setValue with a String parameter
          //is not accepted by OptionGroup
          Collection<String> values = new TreeSet<String>();
          values.add(mc.getValue());
          mb.setValue(values);
          mboxes.add(mb);
          meta.addComponent(mb);
        }
      }
    }
    
    query = tq.substring(0, tq.length()-1);
  }
  
  private static class Constraint
  {
    private String level;
    private String value;
    private boolean regEx;
    private boolean negative;
    
    public Constraint(String s)
    {
      int e=0;
      while(s.charAt(e)!='=')
      {
        e++;
      }
      
      String l;
      
      if(s.charAt(e-1)=='!')
      {
        l = s.substring(0, e-1).replace(" ", "");
        negative = true;
      }
      else
      {
        l = s.substring(0, e).replace(" ", "").replace("meta::", "");
        negative = false;
      }
      
      String v = s.substring(e+1);
      while(v.startsWith(" "))
      {
        v = v.substring(1);
      }
      if(v.startsWith("\""))
      {
        regEx = false;
      }
      else
      {
        regEx = true;
      }
      //remove " or / :
      v = v.substring(1, v.length()-1);
      
      level = l;
      value = v;
    }
    
    public String getLevel()
    {
      return level;
    }
    
    public String getValue()
    {
      return value;
    }
    
    public boolean isRegEx()
    {
      return regEx;
    }
    
    public boolean isNegative()
    {
      return negative;
    }
  }
  
  private static class Relation
  /*
   * Problems:
   * if an operator is used, which is not in the EdgeBoxe's list
   * the programm will crash. Right now. I'm gonna fix this.
   * 
   */
  {
    //operands without '#'
    private int o1, o2;
    private String operator;
    private RelationType type;
    
    public Relation(String in)
    {
      
      String op = "";
      String o1str = "";
      String o2str = "";
      
      int i=1;
      char c = in.charAt(1);
      in = in.replace(" ", "");
      
      while((c!='.')&(c!='>')&(c!='_')&(c!='#')&(c!='-')&(c!='$'))
      {
        o1str+=c;
        i++;
        c = in.charAt(i);
      }     
      while(c!='#')
      {
        op+=c;
        i++;
        c = in.charAt(i);
      }
      i++;
      while((i<in.length()))
      {
        c = in.charAt(i);
        o2str+=c;
        i++;
      }
      
      operator = op;
      o1 = Integer.parseInt(o1str);
      o2 = Integer.parseInt(o2str);
      
      if(op.startsWith("."))
      {
       type = RelationType.PRECEDENCE; 
      }
      else if((op.equals("=")) | (op.equals("_=_")))
      {
        type = RelationType.EQUALITY;
      }
      else if(op.equals("_i_"))
      {
        type = RelationType.INCLUSION;
      }
    }
    
    public RelationType getType()
    {
      return type;
    }
    
    public int getFirst()
    {
      return o1;
    }
    
    public int getSecond()
    {
      return o2;
    }
    
    public String getOperator()
    {
      return operator;
    }
    
    public boolean contains(int a)
    {      
      return ((o1==a)|(o2==a));
    }
    
    public int whosMyFriend(int a)
    {
      if(a==o1)
      {
        return o2;
      }
      if(a==o2)
      {
        return o1;
      }
      return 0;
    }
  }
  
  private static enum RelationType
  {
    PRECEDENCE, DOMINANCE, INCLUSION, EQUALITY
  }
}
