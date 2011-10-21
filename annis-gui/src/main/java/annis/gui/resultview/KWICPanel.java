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
package annis.gui.resultview;

import annis.gui.Helper;
import annis.gui.MatchedNodeColors;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.service.ifaces.AnnisResult;
import com.vaadin.ui.themes.ChameleonTheme;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author thomas
 */
public class KWICPanel extends Panel
{
  private AnnisResult result;
  
  private final String DUMMY_COLUMN = "dummyColumn"; 
  
  private Table tblToken;
  private BeanItemContainer<String> containerAnnos;
  private Map<AnnisNode,Long> markedAndCovered;
  private long textID;
  
  public KWICPanel(AnnisResult result, Set<String> tokenAnnos, 
    Map<AnnisNode,Long> markedAndCovered, long textID)
  {
    this.result = result;
    this.markedAndCovered = markedAndCovered;
    this.textID = textID;
    
    this.addStyleName("kwic");
    setSizeFull();
    setHeight("-1px");
    
    addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    
    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setSizeFull();
    layout.setHeight("-1px");
    layout.setMargin(false);
    
    containerAnnos = new BeanItemContainer<String>(String.class);
    
    containerAnnos.addItem("tok");
    
    tblToken = new Table();
    tblToken.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    tblToken.addStyleName(ChameleonTheme.TABLE_BORDERLESS);
    tblToken.setWidth("100%");
    tblToken.setHeight("-1px");
    tblToken.setPageLength(0);
    if(checkRTL(result.getGraph().getTokens()))
    {
      tblToken.addStyleName("rtl");
    }
    
    List<AnnisNode> token = result.getGraph().getTokens();
    ArrayList<Object> visible = new ArrayList<Object>(10);
    Long lastTokenIndex = null;
    
    for(AnnisNode t : token)
    {
      if(t.getTextId() == textID)
      {
        // add a column for each token
        tblToken.addGeneratedColumn(t, new TokenColumnGenerator());
        tblToken.setColumnWidth(t, -1);
        tblToken.setColumnExpandRatio(t, 0.0f);
        visible.add(t);
        
        if(lastTokenIndex != null && t.getTokenIndex() != null 
          && t.getTokenIndex().longValue() > (lastTokenIndex.longValue()+1))
        {
          // add "(...)"
          Long gapColumnID = t.getTokenIndex();
          tblToken.addGeneratedColumn(gapColumnID, new GapColumnGenerator());
          tblToken.setColumnWidth(gapColumnID, -1);
          tblToken.setColumnExpandRatio(gapColumnID, 0.0f);
          visible.add(gapColumnID);
        
        }
        lastTokenIndex = t.getTokenIndex();
      }
    }
    
    tblToken.addGeneratedColumn(DUMMY_COLUMN, new Table.ColumnGenerator() {

      @Override
      public Component generateCell(Table source, Object itemId, Object columnId)
      {
        Label lbl = new Label("");
        return lbl;
      }
    });
    tblToken.setColumnWidth(DUMMY_COLUMN, 0);
    tblToken.setColumnExpandRatio(DUMMY_COLUMN, 1.0f);
    visible.add(DUMMY_COLUMN);
    containerAnnos.addAll(tokenAnnos);
    
    tblToken.setContainerDataSource(containerAnnos);
    tblToken.setVisibleColumns(visible.toArray());
    
    addComponent(tblToken);
  }
  
  public void setVisibleTokenAnnosVisible(Set<String> annos)
  {
    if(containerAnnos != null)
    {
      containerAnnos.removeAllItems();
      containerAnnos.addItem("tok");
      containerAnnos.addAll(annos);
    }
  }
  
  public class GapColumnGenerator implements Table.ColumnGenerator
  {
    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      Label l = new Label();
      
      if("tok".equals(itemId))
      {
        l.setValue("(...)");
      }
      else
      {
        l.setValue("");
        l.addStyleName("kwic-anno");
      }
      return l;
    }
    
  }
  
  public class TokenColumnGenerator implements Table.ColumnGenerator
  {

    @Override
    public Component generateCell(Table source, Object itemId, Object columnId)
    {
      Label l = new Label("");
      l.setSizeUndefined();
      AnnisNode t = (AnnisNode) columnId;
      
      if("tok".equals(itemId))
      {
        l.setValue(t.getSpannedText());
        if(markedAndCovered.containsKey(t))
        {
          // add color
          l.addStyleName(MatchedNodeColors.colorClassByMatch(markedAndCovered.get(t)));
        }
      }
      else
      {
        for(Annotation a : t.getNodeAnnotations())
        {
          if(a.getQualifiedName().equals(itemId))
          {
            // this is the right annotation
            l.setValue(a.getValue());
            l.setDescription(a.getQualifiedName());
            l.addStyleName("kwic-anno");
            break;
          }
        }
      }
      return l;
    }
  }
 
  private boolean checkRTL(List<AnnisNode> tokenList)
  {
    for(AnnisNode tok : tokenList)
    {
      String tokText = tok.getSpannedText();
      if(Helper.containsRTLText(tokText))
      {
        return true;
      }
    }

    return false;
  }
  
}
