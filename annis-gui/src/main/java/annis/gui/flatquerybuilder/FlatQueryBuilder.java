
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
import annis.gui.objects.Query;
import annis.libgui.Helper;
import annis.service.objects.AnnisAttribute;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author martin klotz (martin.klotz@hu-berlin.de)
 * @author tom ruette (tom.ruette@hu-berlin.de)
 */
public class FlatQueryBuilder extends Panel implements Button.ClickListener, Property.ValueChangeListener
{
  private static final Logger log = LoggerFactory.getLogger(FlatQueryBuilder.class);
  
  private Button btGo;
  private Button btClear;
  private Button btInverse;
  private Button btInitSpan;
  private Button btInitMeta;
  private Button btInitLanguage;
  
  private MenuBar addMenu;
  private MenuBar addMenuSpan;
  private MenuBar addMenuMeta;
  
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
  private SpanBox spbox;
  private String query;
  private MenuBar.MenuItem spanMenu;
  private ReducingStringComparator rsc;
  
  private static final String[] REGEX_CHARACTERS = {"\\", "+", ".", "[", "*", 
    "^","$", "|", "?", "(", ")"};

  private static final String BUTTON_GO_LABEL = "Create AQL Query";
  private static final String BUTTON_CLEAR_LABEL = "Clear the Query Builder";
  private static final String BUTTON_INV_LABEL = "Refresh Query Builder";
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
  
  private static final String TOOLBAR_CAPTION = "Toolbar";
  private static final String META_CAPTION = "Meta information";
  private static final String SPAN_CAPTION = "Scope";
  private static final String LANG_CAPTION = "Linguistic sequence";
  private static final String ADVANCED_CAPTION = "Advanced settings"; 
  
  public FlatQueryBuilder(QueryController cp)
  {
    setSizeFull();
    launch(cp); 
    
  }

  private void launch(QueryController cp)
  {
    this.cp = cp;
    rsc = new ReducingStringComparator();
    this.query = "";
    mainLayout = new VerticalLayout();
    // tracking lists for vertical nodes, edgeboxes and metaboxes
    vnodes = new ArrayList<>();
    eboxes = new ArrayList<>();
    mboxes = new ArrayList<>();
    spbox = null;
    // buttons and checks    
    btGo = new Button(BUTTON_GO_LABEL, (Button.ClickListener) this);
    btGo.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btClear = new Button(BUTTON_CLEAR_LABEL, (Button.ClickListener) this);
    btClear.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btInverse = new Button(BUTTON_INV_LABEL, (Button.ClickListener) this);
    btInverse.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btInitLanguage = new Button("Initialize", (Button.ClickListener) this);
    btInitLanguage.setDescription(INFO_INIT_LANG);
    btInitSpan = new Button("Initialize", (Button.ClickListener) this);
    btInitSpan.setDescription(INFO_INIT_SPAN);
    btInitMeta = new Button("Initialize", (Button.ClickListener) this);
    btInitMeta.setDescription(INFO_INIT_META);
    filtering = new NativeSelect("Filtering mechanisms");
    filtering.setDescription(INFO_FILTER);
    ReducingStringComparator rdc = new ReducingStringComparator();
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
    language.addComponent(languagenodes);
    language.addComponent(btInitLanguage);
    language.setMargin(true);
    language.setCaption(LANG_CAPTION);
    language.addStyleName("linguistics-panel");
    // span layout
    span = new HorizontalLayout();
    span.setSpacing(true);
    span.addComponent(btInitSpan);
    span.setMargin(true);
    span.setCaption(SPAN_CAPTION);
    span.addStyleName("span-panel");
    // meta layout
    meta = new HorizontalLayout();
    meta.setSpacing(true);
    meta.addComponent(btInitMeta);
    meta.setMargin(true);
    meta.setCaption(META_CAPTION);
    meta.addStyleName("meta-panel");
    // toolbar layout
    toolbar = new HorizontalLayout();
    toolbar.setSpacing(true);
    toolbar.addComponent(btGo);
    toolbar.addComponent(btClear);
    toolbar.addComponent(btInverse);
    toolbar.setMargin(true);
    toolbar.setCaption(TOOLBAR_CAPTION);
    toolbar.addStyleName("toolbar-panel");
    // advanced
    advanced = new HorizontalLayout();
    advanced.setSpacing(true);
    advanced.addComponent(filtering);
    advanced.setMargin(true);
    advanced.setCaption(ADVANCED_CAPTION);
    advanced.addStyleName("advanced-panel");
    // put everything on the layout
    mainLayout.setSpacing(true);
    mainLayout.addComponent(language);
    mainLayout.addComponent(span);
    mainLayout.addComponent(meta);
    mainLayout.addComponent(toolbar);
    mainLayout.addComponent(advanced);
    setContent(mainLayout);
    getContent().setWidth("100%");
    getContent().setHeight("-1px");
  }

  @Override
  public void valueChange(Property.ValueChangeEvent event)
  {
    
    initialize();
  }
  
  @Override
  public void attach()
  {
    super.attach();
    cp.getState().getSelectedCorpora().addValueChangeListener(this);
  }
  
  

  @Override
  public void detach()
  {
    super.detach();
    cp.getState().getSelectedCorpora().removeValueChangeListener(this);
  }
  
  
  
  private void initialize()
  {
    // try to remove all existing menus
    try {
      language.removeComponent(addMenu);
      span.removeComponent(addMenuSpan);
      meta.removeComponent(addMenuMeta);
    } catch (Exception e) 
    {
      log.error(null, e);
    }
    
    //init variables:
    final FlatQueryBuilder sq = this;
    Collection<String> annonames = getAvailableAnnotationNames();
    Collection<String> metanames = getAvailableMetaNames();
    
    //Code from btInitLanguage:    
    addMenu = new MenuBar();
    //addMenu.setDescription(INFO_INIT_LANG);
    addMenu.setAutoOpen(false);
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
    language.removeComponent(btInitLanguage);
    language.addComponent(addMenu);
    
    //Code from btInitSpan:    
    addMenuSpan = new MenuBar();
    //addMenuSpan.setDescription(INFO_INIT_SPAN);
    addMenuSpan.setAutoOpen(false);
    final MenuBar.MenuItem addSpan = addMenuSpan.addItem(ADD_SPAN_PARAM, null);
    for (final String annoname : annonames)
    {
      addSpan.addItem(annoname, new MenuBar.Command() {
        @Override
        public void menuSelected(MenuBar.MenuItem selectedItem) {          
          sq.removeSpanBox();
          sq.addSpanBox(annoname);
          addMenuSpan.setAutoOpen(false);
        }
      });
    }
    spanMenu = addSpan;
    span.removeComponent(btInitSpan);
    span.addComponent(addMenuSpan);
    
    //Code from btInitMeta:    
    addMenuMeta = new MenuBar();
    //addMenuMeta.setDescription(INFO_INIT_META);  
    addMenuMeta.setAutoOpen(false);
    final MenuBar.MenuItem addMeta = addMenuMeta.addItem(ADD_META_PARAM, null);
    for (final String annoname : metanames)
    {
      addMeta.addItem(annoname, new MenuBar.Command() {
        @Override
        public void menuSelected(MenuBar.MenuItem selectedItem) {
          MetaBox mb = new MetaBox(annoname, sq);
          meta.addComponent(mb);
          mboxes.add(mb);
          addMenuMeta.setAutoOpen(false);
          //addMeta.removeChild(selectedItem);
          selectedItem.setVisible(false);
        }
      });
    }
    meta.removeComponent(btInitMeta);
    meta.addComponent(addMenuMeta);
  }
  
  private String getAQLFragment(SearchBox sb)
  {
    String result = "";
    String value = null;
    try {
      value = sb.getValue();
    } catch (java.lang.NullPointerException ex) {
      value = null;
    }
    String level=sb.getAttribute();
    if (value == null){
      result = level;
    }
    if (value != null){
      if (sb.isRegEx() && !sb.isNegativeSearch())
      {
        result = level+"=/"+value.replace("/", "\\x2F") +"/";
      }
      if (sb.isRegEx() && sb.isNegativeSearch())
      {
        result = level+"!=/"+value.replace("/", "\\x2F") +"/";
      }
      if (!sb.isRegEx() && sb.isNegativeSearch())
      {
        result = level+"!=\""+value.replace("\"", "\\x22") +"\"";            
      }
      if (!sb.isRegEx() && !sb.isNegativeSearch())
      {
        result = level+"=\""+value.replace("\"", "\\x22") +"\"";      
      }
    }
    return result;
  }
  
  private String getMetaQueryFragment(MetaBox mb)
  {    
    Collection<String> values = mb.getValues();
    if(!values.isEmpty())
    {
      StringBuilder result = new StringBuilder("\n& meta::"+mb.getMetaDatum()+" = ");
      if(values.size()==1)
      {
        result.append("\""+values.iterator().next().replace("\"", "\\x22")+"\"");
      }
      else
      {      
        Iterator<String> itValues = values.iterator();
        result.append("/(" + escapeRegexCharacters(itValues.next())+")");
        while(itValues.hasNext())
        {
          result.append("|("+escapeRegexCharacters(itValues.next())+")");
        }
        result.append("/");
      }   
      return result.toString();
    }
    return "";
  }
  
  public String escapeRegexCharacters(String tok)
  {
    if(tok==null){return "";}
    if(tok.equals("")){return "";}
    String result=tok;
    for (int i = 0; i<REGEX_CHARACTERS.length; i++)
    {
      result = result.replace(REGEX_CHARACTERS[i], "\\"+REGEX_CHARACTERS[i]);
    }
    
    return result.replace("/", "\\x2F");
  }
  
  public String unescape(String s)
	{
    //first unescape slashes and quotes:
		
		s = unescapeSlQ(s);
    
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
  
  public String unescapeSlQ(String s)
  {
    return s.replace("\\x2F", "/").replace("\\x22", "\"");
  }

  private String getAQLQuery()
  {
    int count = 1;    
    StringBuilder ql = new StringBuilder();
    StringBuilder edgeQuery = new StringBuilder();
    StringBuilder sentenceQuery = new StringBuilder();
    Collection<Integer> sentenceVars = new ArrayList<>();
    Iterator<EdgeBox> itEboxes = eboxes.iterator();
    for (VerticalNode v : vnodes)
    {
      Collection<SearchBox> sboxes = v.getSearchBoxes();
      for (SearchBox s : sboxes)
      {
        ql.append(" & " + getAQLFragment(s));
      }
      if (sboxes.isEmpty())
      {
        //not sure we want to do it this way:
        ql.append("\n& /.*/");
      }
      sentenceVars.add(Integer.valueOf(count));
      for(int i=1; i < sboxes.size(); i++)
      {
        String addQuery = "\n& #" + count +"_=_"+ "#" + ++count;
        edgeQuery.append(addQuery);
      }
      count++;
      String edgeQueryAdds = (itEboxes.hasNext()) ? "\n& #"+(count-1)+" "
        +itEboxes.next().getValue()+" #"+count : "";
      edgeQuery.append(edgeQueryAdds);
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
      if (spb.getValue().isEmpty()){
        addQuery = "\n&" + spb.getAttribute();
      }
      ql.append(addQuery);
      for(Integer i : sentenceVars)
      {
        sentenceQuery.append("\n& #").append(count).append("_i_#").append(i.toString());
      }
    } catch (Exception ex){
      ex = null;
    }
    StringBuilder metaQuery = new StringBuilder();
    Iterator<MetaBox> itMetaBoxes = mboxes.iterator();
    while(itMetaBoxes.hasNext())
    {
      metaQuery.append(getMetaQueryFragment(itMetaBoxes.next()));
    }
    String fullQuery = (ql.toString() + edgeQuery.toString()+sentenceQuery.toString()+metaQuery.toString());
    if (fullQuery.length() < 3) {return "";}
    fullQuery = fullQuery.substring(3);//deletes leading " & "
    this.query = fullQuery;
    return fullQuery;
  }

  public void updateQuery()
  {
    try{
      cp.setQuery(new Query(getAQLQuery(), cp.getState().getSelectedCorpora().getValue()));
    } catch (java.lang.NullPointerException ex) {
      Notification.show(INCOMPLETE_QUERY_WARNING);      
    }
  }

  @Override
  public void buttonClick(Button.ClickEvent event)
  {
    if (cp.getState().getSelectedCorpora().getValue().isEmpty())
    {
      Notification.show(NO_CORPORA_WARNING);
    }
    else
    {
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
        try
        {
          loadQuery();
        }
        catch(EmptyReferenceException e)
        {          
          log.error(null, e);
        }
        catch (EqualityConstraintException e)
        {
          log.error(null, e);
        }
        catch (InvalidCharacterSequenceException e)
        {
          log.error(null, e);
        }
        catch (MultipleAssignmentException e)
        {
          log.error(null, e);
        }
        catch (UnknownLevelException e)
        {
          log.error(null, e);
        }
      }
      if (event.getComponent() == btInitMeta || event.getComponent() == btInitSpan || event.getComponent() == btInitLanguage){
        initialize();
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
  }
  
  public void addSpanBox(String level)
  {
    spbox = new SpanBox(level, this);
    span.addComponent(spbox);
    span.setComponentAlignment(spbox, Alignment.MIDDLE_LEFT);
    spanMenu.setText(CHANGE_SPAN_PARAM);
  }
  
  public void addSpanBox(SpanBox spb)
  {
    if(spbox==null)
    {
      spanMenu.setText(CHANGE_SPAN_PARAM);
    }
    else
    {
      span.removeComponent(spbox);
    }
    spbox = spb;
    span.addComponent(spbox);
    span.setComponentAlignment(spbox, Alignment.MIDDLE_LEFT);
  }

  public void removeSpanBox()
  {
    if(spbox!=null)
    {
      span.removeComponent(spbox);
      spbox=null;
      spanMenu.setText(ADD_SPAN_PARAM);
    }
  }
  
  public void removeSpanBox(SpanBox spb)
  {
    if(spb.equals(spbox))
    {
      removeSpanBox();
    }
  }

  public void removeMetaBox(MetaBox v)
  {
    meta.removeComponent(v);
    mboxes.remove(v);       
    List<MenuBar.MenuItem> items = addMenuMeta.getItems().get(0).getChildren();
    boolean found = false;
    String metalevel = v.getMetaDatum();
    for(int i=0; (i<items.size())&!found; i++)
    {
      MenuBar.MenuItem itm = items.get(i);
      if(itm.getText().equals(metalevel))
      {
        itm.setVisible(true);
        found = true;
      }
    }
  }

  public Collection<String> getAnnotationValues(String level)
  {
    Collection<String> values = new TreeSet<>();
    for(String s : getAvailableAnnotationLevels(level))
    {      
      values.add(s);
    }
    return values;
  }

  public Set<String> getAvailableAnnotationNames()
  {
    Set<String> result = new TreeSet<>();
    WebResource service = Helper.getAnnisWebResource();
    // get current corpus selection
    Set<String> corpusSelection = cp.getState().getSelectedCorpora().getValue();
    if (service != null)
    {
      try
      {
        List<AnnisAttribute> atts = new LinkedList<>();
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
      catch (ClientHandlerException ex)
      {
        log.error(null, ex);
      }
      catch (UniformInterfaceException ex)
      {
        log.error(null, ex);
      }
    }
    result.add("tok");
    return result;
  }

  public Collection<String> getAvailableAnnotationLevels(String meta)
  {
    Collection<String> result = new TreeSet<>();
    WebResource service = Helper.getAnnisWebResource();
    // get current corpus selection
    Set<String> corpusSelection = cp.getState().getSelectedCorpora().getValue();
    if (service != null)
    {
      try
      {
        List<AnnisAttribute> atts = new LinkedList<>();
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
      catch (ClientHandlerException ex)
      {
        log.error(null, ex);
      }
      catch (UniformInterfaceException ex)
      {
        log.error(null, ex);
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
    Set<String> result = new TreeSet<>();
    WebResource service = Helper.getAnnisWebResource();
    // get current corpus selection
    Set<String> corpusSelection = cp.getState().getSelectedCorpora().getValue();
    if (service != null)
    {
      try
      {
        List<AnnisAttribute> atts = new LinkedList<>();

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
      catch (ClientHandlerException ex)
      {
        log.error(null, ex);
      }
      catch (UniformInterfaceException ex)
      {
        log.error(null, ex);
      }
    }
    return result;
  }

  public Set<String> getAvailableMetaLevels(String meta)
{
    Set<String> result = new TreeSet<>();
    WebResource service = Helper.getAnnisWebResource();
    // get current corpus selection
    Set<String> corpusSelection = cp.getState().getSelectedCorpora().getValue();
    if (service != null)
    {
      try
      {
        List<AnnisAttribute> atts = new LinkedList<>();
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
      catch (ClientHandlerException ex)
      {
        log.error(null, ex);
      }
      catch (UniformInterfaceException ex)
      {
        log.error(null, ex);
      }
    }
    return result;
  }
  
  public String getFilterMechanism()
  { 
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
  
  private Collection<String> splitMultipleValueExpression(String expression)
	/*
	 * only for complex regex expressions
	 */
	{	
    ArrayList<String> values = new ArrayList<>();
    String s = expression;
    
    while(s.length()>0)
    {
       if(s.charAt(0)=='|') {s = s.substring(1);}
       if(s.charAt(0)!='(')
       {
         values = new ArrayList<>();
         values.add(expression);
         return values;
       }
       else
       {
         int pc = 1;
         int i = 1;
         while(pc!=0)
         {
           char c = s.charAt(i);
           if(c==')') {pc--;}
           else if(c=='(') {pc++;}
           i++;
         }         
         values.add(unescapeSlQ(s.substring(1, i-1))); //in respect to removal of parentheses
         s = s.substring(i);
       }
    }
    
    return values;
	}
  
  public ReducingStringComparator getRSC()
  {
    return rsc;
  }
  
  public void loadQuery() throws UnknownLevelException, EqualityConstraintException, MultipleAssignmentException, InvalidCharacterSequenceException, EmptyReferenceException
    /*
     * this method is called by btInverse
     * When the query has changed in the
     * textfield, the query represented by 
     * the query builder is not equal to
     * the one delivered by the text field
     */
  {
    /*get clean query from control panel text field*/
    String tq = cp.getState().getAql().getValue().replace("\n", " ").replace("\r", "");
    //TODO VALIDATE QUERY: (NOT SUFFICIENT YET)
    boolean valid = (!tq.equals(""));
    if(!(query.equals(tq)) & valid)
    {
      //PROBLEM: LINE BREAKS (simple without anything else)
      try
      {
      
        //the dot marks the end of the string - it is not read, it's just a place holder
        tq += ".";
        HashMap<Integer, Constraint> constraints = new HashMap<>();
        ArrayList<Relation> pRelations = new ArrayList<>();
        ArrayList<Relation> eRelations = new ArrayList<>();
        Relation inclusion=null;
        Constraint conInclusion=null;
        ArrayList<Constraint> metaConstraints = new ArrayList<>();

        //parse typed-in Query
        //Step 1: get indices of tq-chars, where constraints are separated (&)
        String tempCon="";
        int count = 1;
        int maxId = 0;
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
              int newMax = (r.getFirst()>r.getSecond()) ? r.getFirst() : r.getSecond();
              maxId = (maxId<newMax) ? newMax : maxId;
            }         
            else 
            {
              constraints.put(count++, new Constraint(tempCon));
            }

            tempCon = "";
          }
        }
        /*CHECK FOR EMPTY REFERENCE*/
        /*IDEA: If the highest element-id is not empty, all lower ids can't be empty*/
        /*one additional increment of count has to be taken into account*/
        /*empty means, the element the id is refering to does not exist*/
        if(maxId>=count)
        {
          throw new EmptyReferenceException(Integer.toString(maxId));
        }
        
        /*CHECK FOR INVALID OR REDUNDANT MUTLIPLE VALUE ASSIGNMENT*/
        for(Relation rel : eRelations)
        {
          Constraint con1 = constraints.get(rel.getFirst());
          Constraint con2 = constraints.get(rel.getSecond());
          if(con1.getLevel().equals(con2.getLevel()))
          {
            throw new MultipleAssignmentException(con1.toString()+" <-> "+con2.toString());           
          }
        }
        
        //create Vertical Nodes
        HashMap<Integer, VerticalNode> indexedVnodes = new HashMap<>();
        VerticalNode vn=null;
        Collection<String> annonames = getAvailableAnnotationNames();        
        for(int i : constraints.keySet())
        {          
          Constraint con = constraints.get(i);                    
          if(!annonames.contains(con.getLevel()))
          {
            throw new UnknownLevelException(con.getLevel());
            //is that a good idea? YES
          }
          if(!indexedVnodes.containsKey(i))
          {            
            vn = new VerticalNode(con.getLevel(), con.getValue(), this, con.isRegEx(), con.isNegative());
            if(con.isRegEx())
            {
              SearchBox sb = vn.getSearchBoxes().iterator().next();
              /*CHECK FIRST IF WE REALLY HAVE A MULTIPLE VALUE EXPRESSION*/
              Collection<String> mvalue = splitMultipleValueExpression(con.getValue());
              if(mvalue.size()==1)
              {
                sb.setValue(mvalue.iterator().next());
              }
              else
              {
                sb.setValue(mvalue); 
              }              
            }
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
                Collection<String> values = splitMultipleValueExpression(bcon.getValue());
                if(values.size()>1)
                {
                  sb.setValue(values);
                }
                else
                {
                  sb.setValue(bcon.getValue());
                }
                vn.addSearchBox(sb);
              }
            }          
          }
          
        }
        
        //clean query builder surface
        for(VerticalNode v : vnodes)
        {
          languagenodes.removeComponent(v);        
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

        //remove SpanBox
        removeSpanBox();

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
          addSpanBox(new SpanBox(conInclusion.getLevel(), this, conInclusion.isRegEx()));
          spbox.setValue(conInclusion.getValue());
        }

        //build MetaBoxes
        for(Constraint mc : metaConstraints)
        {
          if(mc.isRegEx())
          {
            Collection<String> values = splitMultipleValueExpression(unescape(unescapeSlQ(mc.getValue())));
            MetaBox mb = new MetaBox(mc.getLevel(), this);
            mb.setValue(values);
            mboxes.add(mb);
            meta.addComponent(mb);
          }
          else
          {
            MetaBox mb = new MetaBox(mc.getLevel(), this);
            //for a particular reason (unknown) setValue() with a String parameter
            //is not accepted by OptionGroup
            Collection<String> values = new TreeSet<>();
            values.add(unescapeSlQ(mc.getValue()));
            mb.setValue(values);
            mboxes.add(mb);
            meta.addComponent(mb);
          }
        }
        query = tq.substring(0, tq.length()-1);
      }
      catch(EmptyReferenceException e)
      {
        Notification.show(e.getMessage());
      }
      catch (EqualityConstraintException e)
      {
        Notification.show(e.getMessage());
      }
      catch (InvalidCharacterSequenceException e)
      {
        Notification.show(e.getMessage());
      }
      catch (MultipleAssignmentException e)
      {
        Notification.show(e.getMessage());
      }
      catch (UnknownLevelException e)
      {
        Notification.show(e.getMessage());
      }
    }    
  }
  
  private class Constraint
  {
    private String level;
    private String value;
    private boolean regEx;
    private boolean negative;
    
    public Constraint(String s) throws InvalidCharacterSequenceException
    {
      int e=0;
      if(s.contains("="))
      {
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
      else if( ((s.charAt(0)=='\"') && 
        (s.charAt(s.length()-1)=='\"')) || ((s.charAt(0)=='/') && (s.charAt(s.length()-1)=='/')))
      {
        level = "tok";
        value = s.substring(1, s.length()-1);
      }
      else if((s.contains("\""))||(s.contains("/")))
      {
        throw new InvalidCharacterSequenceException(s);
      }
      else
      {
        level = s;
        value = "";
      }
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
    
    @Override
    public String toString()
    {
      String op = (negative) ? "!=" : "=";
      String val = (regEx) ? "/"+value+"/" : "\""+value+"\"";
      return level+op+val;
    }
  }
  
  private class Relation
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
    
    public Relation(String in) throws EqualityConstraintException
    {
      
      StringBuilder op = new StringBuilder();
      StringBuilder o1str = new StringBuilder();
      StringBuilder o2str = new StringBuilder();
      
      int i=1;
      char c = in.charAt(1);
      in = in.replace(" ", "");
      
      while((c!='.')&(c!='>')&(c!='_')&(c!='#')&(c!='-')&(c!='$')&(c!='='))
      {
        o1str.append(c);
        i++;
        c = in.charAt(i);
      }     
      while(c!='#')
      {
        op.append(c);
        i++;
        c = in.charAt(i);
      }
      i++;
      while((i<in.length()))
      {
        c = in.charAt(i);
        o2str.append(c);
        i++;
      }
      
      operator = op.toString();
      o1 = Integer.parseInt(o1str.toString());
      o2 = Integer.parseInt(o2str.toString());
      
      if(operator.startsWith("."))
      {
       type = RelationType.PRECEDENCE; 
      }
      else if((operator.equals("=")) || (operator.equals("_=_")))
      {
        type = RelationType.EQUALITY;
        if(o1>o2)
        {
          int tmp = o1;
          o1 = o2;
          o2 = tmp;
        }
        else if(o1==o2)
        {
          throw new EqualityConstraintException(in);
        }
      }
      else if(operator.equals("_i_"))
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
  
  private enum RelationType
  {
    PRECEDENCE, DOMINANCE, INCLUSION, EQUALITY
  }
  
  private abstract class LoadQueryException extends Exception
  {
    protected String ERROR_MESSAGE;
    protected String critical;
    
    public LoadQueryException(String s)
    {
      critical = s;
    }
    
    @Override
    public String getMessage()
    {
      return ERROR_MESSAGE+critical;
    }
  }
  
  private class UnknownLevelException extends LoadQueryException
  {
    public UnknownLevelException(String s)
    {
      super(s);
      ERROR_MESSAGE = "Unknown annotation level: ";
    }
  }
  
  private class MultipleAssignmentException extends LoadQueryException
  {
    public MultipleAssignmentException(String s)
    {
      super(s);
      ERROR_MESSAGE = "Invalid or redundant assignment of multiple values:\n\n";
    }
  }
  
  private class EqualityConstraintException extends LoadQueryException
  {
    public EqualityConstraintException(String s)
    {
      super(s);
      ERROR_MESSAGE = "Invalid use of equality operator: ";      
    }
  }
  
  private class InvalidCharacterSequenceException extends LoadQueryException
  {
    public InvalidCharacterSequenceException(String s)
    {
      super(s);
      ERROR_MESSAGE="Invalid character sequence: \n\n";
    }
  }
  
  private class EmptyReferenceException extends LoadQueryException
  {
    public EmptyReferenceException(String s)
    {
      super(s);
      ERROR_MESSAGE = "Element not found. Empty reference: #";
    }
  }
}
