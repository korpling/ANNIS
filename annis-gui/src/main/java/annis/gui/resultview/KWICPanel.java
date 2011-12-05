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

import annis.CommonHelper;
import annis.gui.Helper;
import annis.gui.MatchedNodeColors;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.service.ifaces.AnnisResult;
import com.vaadin.ui.themes.ChameleonTheme;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author thomas
 */
public class KWICPanel extends Table
{

  private AnnisResult result;
  private static final String DUMMY_COLUMN = "dummyColumn";
  private BeanItemContainer<String> containerAnnos;
  private Map<AnnisNode, Long> markedAndCovered;
  private long textID;

  public KWICPanel(AnnisResult result, Set<String> tokenAnnos,
    Map<AnnisNode, Long> markedAndCovered, long textID)
  {
    this.result = result;
    this.markedAndCovered = markedAndCovered;
    this.textID = textID;

    this.addStyleName("kwic");
    setSizeFull();
    setHeight("-1px");

    addStyleName(ChameleonTheme.PANEL_BORDERLESS);

    containerAnnos = new BeanItemContainer<String>(String.class);

    containerAnnos.addItem("tok");

    setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    addStyleName(ChameleonTheme.TABLE_BORDERLESS);
    setWidth("100%");
    setHeight("-1px");
    setPageLength(0);
    if (checkRTL(result.getGraph().getTokens()))
    {
      addStyleName("rtl");
    }

    List<AnnisNode> token = result.getGraph().getTokens();
    ArrayList<Object> visible = new ArrayList<Object>(10);
    Long lastTokenIndex = null;

    for (AnnisNode t : token)
    {
      if (t.getTextId() == textID)
      {
        // add a column for each token
        addGeneratedColumn(t, new TokenColumnGenerator(t));
        setColumnWidth(t, -1);
        setColumnExpandRatio(t, 0.0f);
        visible.add(t);

        if (lastTokenIndex != null && t.getTokenIndex() != null
          && t.getTokenIndex().longValue() > (lastTokenIndex.longValue() + 1))
        {
          // add "(...)"
          Long gapColumnID = t.getTokenIndex();
          addGeneratedColumn(gapColumnID, new GapColumnGenerator());
          setColumnWidth(gapColumnID, -1);
          setColumnExpandRatio(gapColumnID, 0.0f);
          visible.add(gapColumnID);

        }
        lastTokenIndex = t.getTokenIndex();
      }
    }

    addGeneratedColumn(DUMMY_COLUMN, new Table.ColumnGenerator()
    {

      @Override
      public Component generateCell(Table source, Object itemId, Object columnId)
      {
        Label lbl = new Label("");
        return lbl;
      }
    });
    setColumnWidth(DUMMY_COLUMN, 0);
    setColumnExpandRatio(DUMMY_COLUMN, 1.0f);
    visible.add(DUMMY_COLUMN);
    containerAnnos.addAll(tokenAnnos);

    setContainerDataSource(containerAnnos);
    setVisibleColumns(visible.toArray());
    
  }
  

  public void setVisibleTokenAnnosVisible(Set<String> annos)
  {
    if (containerAnnos != null)
    {
      containerAnnos.removeAllItems();
      containerAnnos.addItem("tok");
      containerAnnos.addAll(annos);
    }
  }

  public interface KWICComponentGenerator extends Table.ColumnGenerator
  {

    public Object generateCell(String layer);
  }

  public static class GapColumnGenerator implements KWICComponentGenerator
  {

    @Override
    public Object generateCell(String layer)
    {
      Label l = new Label();

      if ("tok".equals(layer))
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

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      return generateCell((String) itemId);
    }
  }

  public class TokenColumnGenerator implements KWICComponentGenerator
  {

    private Map<String, Annotation> annotationsByQName;
    private AnnisNode token;

    public TokenColumnGenerator(AnnisNode token)
    {
      this.token = token;
      annotationsByQName = new HashMap<String, Annotation>();
      for (Annotation a : token.getNodeAnnotations())
      {
        annotationsByQName.put(a.getQualifiedName(), a);
      }
    }

    @Override
    public Object generateCell(String layer)
    {
      Label l = new Label("");
      l.setSizeUndefined();

      if ("tok".equals(layer))
      {
        l.setValue(token.getSpannedText());
        if (markedAndCovered.containsKey(token))
        {
          // add color
          l.addStyleName(
            MatchedNodeColors.colorClassByMatch(markedAndCovered.get(token)));
        }
      }
      else
      {
        Annotation a = annotationsByQName.get(layer);
        if (a != null)
        {
          l.setValue(a.getValue());
          l.setDescription(a.getQualifiedName());
          l.addStyleName("kwic-anno");
        }
      }
      return l;
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      return generateCell((String) itemId);
    }
  }

  private boolean checkRTL(List<AnnisNode> tokenList)
  {
    for (AnnisNode tok : tokenList)
    {
      String tokText = tok.getSpannedText();
      if (CommonHelper.containsRTLText(tokText))
      {
        return true;
      }
    }

    return false;
  }
}
