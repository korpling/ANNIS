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
package annis.gui.frequency;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.LayoutEvents;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import annis.gui.QueryController;
import annis.gui.admin.PopupTwinColumnSelect;
import annis.gui.objects.FrequencyQuery;
import annis.gui.objects.QueryUIState;
import annis.libgui.Helper;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.FrequencyTable;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FrequencyQueryPanel extends VerticalLayout implements Serializable, FieldEvents.TextChangeListener
{
  
  private static final Logger log = LoggerFactory.getLogger(FrequencyQueryPanel.class);
  
  private Table tblFrequencyDefinition;
  private final IndexedContainer metaNamesContainer;
  private final Button btAdd;
  private final Button btReset;
  private final CheckBox cbAutomaticMode;
  private Button btDeleteRow;
  private Button btShowFrequencies;
  private int counter;
  private FrequencyResultPanel resultPanel;
  private Button btShowQuery;
  private VerticalLayout queryLayout;
  private final Label lblCorpusList;
  private final Label lblAQL;
  private final Label lblErrorOrMsg;
  
  private transient WeakHashMap<Field<?>,Object> field2ItemID;
  
  private final ProgressBar pbQuery = new ProgressBar();
  
  private final QueryUIState state;

  
  public FrequencyQueryPanel(final QueryController controller, QueryUIState state)
  {    
    this.state = state;
    
    setWidth("99%");
    setHeight("99%");
    setMargin(true);
    
    queryLayout = new VerticalLayout();
    queryLayout.setWidth("100%");
    queryLayout.setHeight("100%");
    
    HorizontalLayout queryDescriptionLayout = new HorizontalLayout();
    queryDescriptionLayout.setSpacing(true);
    queryDescriptionLayout.setWidth("100%");
    queryDescriptionLayout.setHeight("-1px");
    queryLayout.addComponent(queryDescriptionLayout);
    
    lblCorpusList = new Label("");
    lblCorpusList.setCaption("selected corpora:");
    lblCorpusList.setWidth("100%");
    
    lblAQL = new Label("");
    lblAQL.setCaption("query to analyze:");
    lblAQL.setWidth("100%");
    lblAQL.addStyleName(Helper.CORPUS_FONT_FORCE);
    
    queryDescriptionLayout.addComponent(lblCorpusList);
    queryDescriptionLayout.addComponent(lblAQL);
    
    queryDescriptionLayout.setComponentAlignment(lblCorpusList,
      Alignment.MIDDLE_LEFT);
    queryDescriptionLayout.setComponentAlignment(lblAQL,
      Alignment.MIDDLE_RIGHT);
    
    tblFrequencyDefinition = new Table();
    tblFrequencyDefinition.setImmediate(true);
    tblFrequencyDefinition.setSortEnabled(false);
    tblFrequencyDefinition.setSelectable(true);
    tblFrequencyDefinition.setMultiSelect(true);
    tblFrequencyDefinition.setTableFieldFactory(new FieldFactory());
    tblFrequencyDefinition.setEditable(true);
    tblFrequencyDefinition.addValueChangeListener(new Property.ValueChangeListener() 
    {
      @Override
      public void valueChange(ValueChangeEvent event)
      {
        if(tblFrequencyDefinition.getValue() == null 
          || ((Set<Object>) tblFrequencyDefinition.getValue()).isEmpty())
        {
          btDeleteRow.setEnabled(false);
        }
        else
        {
          btDeleteRow.setEnabled(true);
        }
      }
    });
    
    lblErrorOrMsg = new Label(
      "No node with explicit name in OR expression found! "
      + "When using OR expression you need to explicitly name the nodes "
      + "you want to include in the frequency analysis with \"#\", "
      + "like e.g. in <br />"
      + "<pre>"
      + "(n1#tok=\"fun\" | n1#tok=\"severity\")"
      + "</pre>");
    lblErrorOrMsg.setContentMode(ContentMode.HTML);
    lblErrorOrMsg.addStyleName("embedded-warning");
    lblErrorOrMsg.setWidth("100%");
    lblErrorOrMsg.setVisible(false);
    queryLayout.addComponent(lblErrorOrMsg);
    
    tblFrequencyDefinition.setWidth("100%");
    tblFrequencyDefinition.setHeight("100%");
  
   
    tblFrequencyDefinition.setContainerDataSource(state.getFrequencyTableDefinition());
    
    tblFrequencyDefinition.setColumnHeader("nr", "Node number/name");
    tblFrequencyDefinition.setColumnHeader("annotation", "Selected annotation of node");
    tblFrequencyDefinition.setColumnHeader("comment", "Comment");
    
    tblFrequencyDefinition.addStyleName(Helper.CORPUS_FONT_FORCE);
    
    tblFrequencyDefinition.setRowHeaderMode(Table.RowHeaderMode.INDEX);
    
    
    tblFrequencyDefinition.setColumnExpandRatio("nr", 0.15f);
    tblFrequencyDefinition.setColumnExpandRatio("annotation", 0.35f);
    tblFrequencyDefinition.setColumnExpandRatio("comment", 0.5f);
    tblFrequencyDefinition.setVisibleColumns("nr", "annotation", "comment");
    
    queryLayout.addComponent(tblFrequencyDefinition);
    
    metaNamesContainer = new IndexedContainer();
    PopupTwinColumnSelect metaSelect = new PopupTwinColumnSelect();
    metaSelect.setSelectableContainer(metaNamesContainer);
    metaSelect.setPropertyDataSource(state.getFrequencyMetaData());
    metaSelect.setCaption("Metadata");
    
    queryLayout.addComponent(metaSelect);
    
    
    if(controller != null)
    {
      createAutomaticEntriesForQuery(state.getAql().getValue());
      updateQueryInfo(state.getAql().getValue());
    }
    
    HorizontalLayout layoutButtons = new HorizontalLayout();
    
    btAdd = new Button("Add");
    btAdd.addClickListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        cbAutomaticMode.setValue(Boolean.FALSE);
        
        int nr = 1;
        // get the highest number of values from the existing defitions
        for(Object id : tblFrequencyDefinition.getItemIds())
        {
          String textNr = (String) tblFrequencyDefinition.getItem(id)
            .getItemProperty("nr").getValue();
          try
          {
            nr = Math.max(nr, Integer.parseInt(textNr));
          }
          catch(NumberFormatException ex)
          {
            // was not a number but a named node
          }
        }
        if(controller != null)
        {
          List<QueryNode> nodes = parseQuery(FrequencyQueryPanel.this.state.getAql().getValue());
          nr = Math.min(nr, nodes.size()-1);
          int id = counter++;
          UserGeneratedFrequencyEntry entry = new UserGeneratedFrequencyEntry();
          entry.setAnnotation("tok");
          entry.setComment("");
          entry.setNr("" + (nr+1));
          FrequencyQueryPanel.this.state
            .getFrequencyTableDefinition().addItem(id, entry);
        }
      }
    });
    layoutButtons.addComponent(btAdd);
    
    btDeleteRow = new Button("Delete selected row(s)");
    btDeleteRow.setEnabled(false);
    btDeleteRow.addClickListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        Object rawValue = tblFrequencyDefinition.getValue();
        if(rawValue instanceof Collection<?>)
        {
          Set<Object> selected = new HashSet<Object>((Collection<?>) rawValue);
          for(Object o : selected)
          {
            cbAutomaticMode.setValue(Boolean.FALSE);
            tblFrequencyDefinition.removeItem(o);
            
          }
        }
      }
    });
    layoutButtons.addComponent(btDeleteRow);
    
    cbAutomaticMode = new CheckBox("Automatic mode", true);
    cbAutomaticMode.setImmediate(true);
    cbAutomaticMode.addValueChangeListener(new Property.ValueChangeListener()
    {
      @Override
      public void valueChange(ValueChangeEvent event)
      {
        btShowFrequencies.setEnabled(true);
        if(cbAutomaticMode.getValue())
        {
          tblFrequencyDefinition.removeAllItems();
          if(controller != null)
          {
            createAutomaticEntriesForQuery(
              FrequencyQueryPanel.this.state.getAql().getValue());
          }
        }
      }
    });
    layoutButtons.addComponent(cbAutomaticMode);
    
    btReset = new Button("Reset to default");
    btReset.addClickListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        cbAutomaticMode.setValue(Boolean.TRUE);
        btShowFrequencies.setEnabled(true);
        tblFrequencyDefinition.removeAllItems();
        if(controller != null)
        {
          createAutomaticEntriesForQuery(
            FrequencyQueryPanel.this.state.getAql().getValue());
        }
      }
    });
    //layoutButtons.addComponent(btReset);
    
    layoutButtons.setComponentAlignment(btAdd, Alignment.MIDDLE_LEFT);
    layoutButtons.setComponentAlignment(btDeleteRow, Alignment.MIDDLE_LEFT);
    layoutButtons.setComponentAlignment(cbAutomaticMode, Alignment.MIDDLE_RIGHT);
    layoutButtons.setExpandRatio(btAdd, 0.0f);
    layoutButtons.setExpandRatio(btDeleteRow, 0.0f);
    layoutButtons.setExpandRatio(cbAutomaticMode, 1.0f);
    
    layoutButtons.setMargin(true);
    layoutButtons.setSpacing(true);
    layoutButtons.setHeight("-1px");
    layoutButtons.setWidth("100%");
    
    queryLayout.addComponent(layoutButtons);
    
    btShowFrequencies = new Button("Perform frequency analysis");
    btShowFrequencies.setDisableOnClick(true);
    btShowFrequencies.addClickListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        
        
        if(controller != null)
        {
          try
          {
            if (resultPanel != null)
            {
              removeComponent(resultPanel);
            }
            queryLayout.setVisible(false);
            
            pbQuery.setCaption(
              "Please wait, the frequencies analysis can take some time");
            pbQuery.setIndeterminate(true);
            pbQuery.setEnabled(true);
            pbQuery.setVisible(true);
            
            controller.executeFrequency(FrequencyQueryPanel.this);
          }
          catch(Exception ex)
          {
            btShowFrequencies.setEnabled(true);
          }
        }
          
      }
    });
    queryLayout.addComponent(btShowFrequencies);
    
    queryLayout.setComponentAlignment(tblFrequencyDefinition, Alignment.TOP_CENTER);
    queryLayout.setComponentAlignment(layoutButtons, Alignment.TOP_CENTER);
    queryLayout.setComponentAlignment(btShowFrequencies, Alignment.TOP_CENTER);
    
    queryLayout.setExpandRatio(tblFrequencyDefinition, 1.0f);
    queryLayout.setExpandRatio(layoutButtons, 0.0f);
    queryLayout.setExpandRatio(btShowFrequencies, 0.0f);
    
    queryLayout.addLayoutClickListener(new LayoutEvents.LayoutClickListener()
    {
      @Override
      public void layoutClick(LayoutEvents.LayoutClickEvent event)
      {
        Component c = event.getClickedComponent();
        if(c instanceof Field)
        {
          Object itemID = getField2ItemID().get((Field<?>) c);
          if(itemID != null)
          {
            if(!event.isCtrlKey() && !event.isShiftKey())
            {
              // deselect everything else if no modifier key was clicked
              tblFrequencyDefinition.setValue(null);
            }
            // select the item
            tblFrequencyDefinition.select(itemID);
          }
        }
      }
    });
    
    btShowQuery = new Button("New Analysis", new Button.ClickListener() 
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        showQueryDefinitionPanel();
      }
    });
    btShowQuery.setVisible(false);
    
    pbQuery.setVisible(false);
    addComponent(pbQuery);
    
    addComponent(queryLayout);
    addComponent(btShowQuery);
    
    setComponentAlignment(btShowQuery, Alignment.TOP_CENTER);
    setComponentAlignment(pbQuery, Alignment.TOP_CENTER);

    if(controller != null)
    {
      state.getSelectedCorpora().addValueChangeListener(new Property.ValueChangeListener()
      {

        @Override
        public void valueChange(ValueChangeEvent event)
        {
          if (cbAutomaticMode.getValue())
          {
            createAutomaticEntriesForQuery(FrequencyQueryPanel.this.state.getAql().getValue());
          }
          updateQueryInfo(FrequencyQueryPanel.this.state.getAql().getValue());
        }
      });
    }
  }
  
  public Set<String> getAvailableMetaNames()
  {
    Set<String> result = new TreeSet<>();
    WebResource service = Helper.getAnnisWebResource();
    // get current corpus selection
    Set<String> corpusSelection = state.getSelectedCorpora().getValue();
    if (service != null)
    {
      try
      {
        List<AnnisAttribute> atts = new LinkedList<>();

        for (String corpus : corpusSelection)
        {
          atts.addAll(service.path("query").path("corpora").path(corpus)
            .path("annotations")
            .get(new GenericType<List<AnnisAttribute>>()
              {
            })
          );
        }
        for (AnnisAttribute a : atts)
        {
          if (a.getType() == AnnisAttribute.Type.meta)
          {
            result.add(a.getName());
          }
        }
      }
      catch (ClientHandlerException | UniformInterfaceException ex)
      {
        log.error(null, ex);
      }
    }
    return result;
  }

  @Override
  public void textChange(FieldEvents.TextChangeEvent event)
  {
    if(cbAutomaticMode.getValue())
    {
      createAutomaticEntriesForQuery(event.getText());
    }
    updateQueryInfo(event.getText());
  }
  
  
  public void showResult(FrequencyTable result, FrequencyQuery query)
  {
    pbQuery.setVisible(false);
    resultPanel = new FrequencyResultPanel(result, query, this);
    addComponent(resultPanel);
    setExpandRatio(resultPanel, 1.0f);

    queryLayout.setVisible(false);
  }
  
  public void showQueryDefinitionPanel()
  {
    btShowFrequencies.setEnabled(true);
    pbQuery.setVisible(false);
    btShowQuery.setVisible(false);
    queryLayout.setVisible(true);
    resultPanel.setVisible(false);
  }
  
  private List<QueryNode> parseQuery(String query)
  {
    if(query == null || query.isEmpty())
    {
      return new LinkedList<>();
    }
    // let the service parse the query
    WebResource res = Helper.getAnnisWebResource();
    List<QueryNode> nodes = res.path("query/parse/nodes").queryParam("q", Helper.encodeJersey(query))
      .get(new GenericType<List<QueryNode>>() {});
    
    return nodes;
  }
  
  private void createAutomaticEntriesForQuery(String query)
  {
    if(query == null || query.isEmpty())
    {
      return;
    }
    
    try
    { 

      state.getFrequencyTableDefinition().removeAllItems();
      lblErrorOrMsg.setVisible(false);
      
      counter = 0;
      List<QueryNode> nodes = parseQuery(query);
      Collections.sort(nodes, new Comparator<QueryNode>()
      {

        @Override
        public int compare(QueryNode o1, QueryNode o2)
        {
          if(o1.getVariable() == null)
          {
            return o2 == null ? 0 : -1;
          }
          return o1.getVariable().compareTo(o2.getVariable());
        }
      });
      
      // calculate the nodes that are part of every alternative
      Multimap<String, Integer> alternativesOfVariable = LinkedHashMultimap.create();
      int maxAlternative = 0;
      for(QueryNode n : nodes)
      {
        if(n.getAlternativeNumber() != null)
        {
          maxAlternative = Math.max(n.getAlternativeNumber(), maxAlternative);
          alternativesOfVariable.put(n.getVariable(), n.getAlternativeNumber());
        }
      }
      Set<String> allowedVariables = new LinkedHashSet<>();
      for(QueryNode n : nodes)
      {
        // we assume that the alternative numbering is continuous and without gaps
        if(alternativesOfVariable.get(n.getVariable()).size() == (maxAlternative+1))
        {
          allowedVariables.add(n.getVariable());
        }
      }
      
      if(maxAlternative > 0 && allowedVariables.isEmpty())
      {
        lblErrorOrMsg.setVisible(true);
      }
      
      Set<UserGeneratedFrequencyEntry> generatedEntries = new HashSet<>();
      
      for(QueryNode n : nodes)
      {
        if(!n.isArtificial() && allowedVariables.contains(n.getVariable()))
        {
          if(n.getNodeAnnotations().isEmpty())
          {
            UserGeneratedFrequencyEntry entry = new UserGeneratedFrequencyEntry();
            entry.setAnnotation("tok");
            entry.setComment("automatically created from " + n.toAQLNodeFragment());
            entry.setNr(n.getVariable());
            
            if(!generatedEntries.contains(entry))
            {
              int id = counter++;
              state.getFrequencyTableDefinition().addItem(id, entry);
              generatedEntries.add(entry);
            }
          }
          else
          {
            QueryAnnotation firstAnno = n.getNodeAnnotations().iterator().next();
            
            UserGeneratedFrequencyEntry entry = new UserGeneratedFrequencyEntry();
            entry.setAnnotation(firstAnno.getName());
            entry.setComment("automatically created from " + n.toAQLNodeFragment());
            entry.setNr(n.getVariable());
            
            if(!generatedEntries.contains(entry))
            {
              int id = counter++;
              state.getFrequencyTableDefinition().addItem(id, entry);
              generatedEntries.add(entry);
            }
          }
        }
      }
    }
    catch(UniformInterfaceException ex)
    {
      // non-valid query, ignore
    }
    
  }
  
  private void updateQueryInfo(String query)
  {
    metaNamesContainer.removeAllItems();
    Set<String> allMetaNames = getAvailableMetaNames();
    Set<String> oldSelection = new TreeSet<>(state.getFrequencyMetaData().getValue());
    for(String m : allMetaNames)
    {
      metaNamesContainer.addItem(m);
    }
    // remove all selections that are no longer present
    oldSelection.retainAll(allMetaNames);
    state.getFrequencyMetaData().setValue(oldSelection);
    
    Set<String> selectedCorpora = state.getSelectedCorpora().getValue();
    if(selectedCorpora.isEmpty())
    {
      lblCorpusList.setValue("none");
    }
    else
    {
      lblCorpusList.
        setValue(Joiner.on(", ").join(selectedCorpora));
    }
    if(query == null || query.isEmpty())
    {
      lblAQL.setValue("<empty query>");
    }
    else
    {
      lblAQL.setValue(query.replaceAll("[\n\r]+", " "));
    }
  }
  
  public void notifiyQueryFinished()
  {
    btShowFrequencies.setEnabled(true);
    btShowQuery.setVisible(true);
  }
  
  public class FieldFactory extends DefaultFieldFactory
  {

    public FieldFactory()
    {
    }
    
    @Override
    public Field<?> createField(Container container, final Object itemId,
      Object propertyId, Component uiContext)
    {
      if ("nr".equals(propertyId) || "annotation".equals(propertyId))
      {
        TextField txt = new TextField(container.getContainerProperty(itemId,
          propertyId));
        txt.setWidth("100%");
        if(itemId != null)
        {
          getField2ItemID().put(txt, itemId);
        }
        return txt;
      }
      else if("comment".equals(propertyId))
      {
        // explicitly request a read-only label
        return null;
      }
      
      return super.createField(container, itemId, propertyId, uiContext);
    }
    
  }

  private WeakHashMap<Field<?>, Object> getField2ItemID()
  {
    if(field2ItemID == null)
    {
      field2ItemID = new WeakHashMap<>();
    }
    return field2ItemID;
  }
  
  
  
}
