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
import annis.gui.MatchedNodeColors;
import com.vaadin.ui.themes.ChameleonTheme;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

/**
 *
 * @author thomas
 */
public class KWICPanel extends Table
{

  private SDocument result;
  private static final String DUMMY_COLUMN = "dummyColumn";
  private BeanItemContainer<String> containerAnnos;
  private Map<SNode, Long> markedAndCovered;
  private STextualDS text;

  public KWICPanel(SDocument result, Set<String> tokenAnnos,
    Map<SNode, Long> markedAndCovered, STextualDS text)
  {
    this.result = result;
    this.markedAndCovered = markedAndCovered;
    this.text = text;

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

    if (CommonHelper.containsRTLText(text.getSText()))
    {
      addStyleName("rtl");
    }

    List<SToken> token = result.getSDocumentGraph().getSortedSTokenByText();
    ArrayList<Object> visible = new ArrayList<Object>(10);
//    Long lastTokenIndex = null;

    for (SToken t : token)
    {
      STextualDS tokenText = null;
      EList<Edge> edges = t.getSDocumentGraph().getOutEdges(t.getSId());
      for (Edge e : edges)
      {
        if (e instanceof STextualRelation)
        {
          STextualRelation textRel = (STextualRelation) e;
          tokenText = textRel.getSTextualDS();
          break;
        }
      }
      if (tokenText == text)
      {
        // add a column for each token
        addGeneratedColumn(t, new TokenColumnGenerator(t));
        setColumnWidth(t, -1);
        setColumnExpandRatio(t, 0.0f);
        visible.add(t);

        // TODO: howto detect gaps in Salt?
//        if (lastTokenIndex != null && t.getTokenIndex() != null
//          && t.getTokenIndex().longValue() > (lastTokenIndex.longValue() + 1))
//        {
//          // add "(...)"
//          Long gapColumnID = t.getTokenIndex();
//          addGeneratedColumn(gapColumnID, new GapColumnGenerator());
//          setColumnWidth(gapColumnID, -1);
//          setColumnExpandRatio(gapColumnID, 0.0f);
//          visible.add(gapColumnID);
//
//        }
//        lastTokenIndex = t.getTokenIndex();
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

    private Map<String, SAnnotation> annotationsByQName;
    private SToken token;

    public TokenColumnGenerator(SToken token)
    {
      this.token = token;
      annotationsByQName = new HashMap<String, SAnnotation>();
      for (SAnnotation a : token.getSAnnotations())
      {
        annotationsByQName.put(a.getQName(), a);
      }
    }

    @Override
    public Object generateCell(String layer)
    {

      BasicEList<STYPE_NAME> textualRelation = new BasicEList<STYPE_NAME>();
      textualRelation.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
      SDocumentGraph docGraph = result.getSDocumentGraph();

      Label l = new Label("");
      l.setSizeUndefined();

      if ("tok".equals(layer))
      {

        SDataSourceSequence seq = docGraph.getOverlappedDSSequences(token,
          textualRelation).get(0);
        
        l.setValue(((String) seq.getSSequentialDS().getSData()).
            substring(seq.getSStart(), seq.getSEnd()));
        if (markedAndCovered.containsKey(token))
        {
          // add color
          String styleName = MatchedNodeColors.colorClassByMatch(markedAndCovered.get(token));
          l.addStyleName(styleName);
        }
      }
      else
      {
        SAnnotation a = annotationsByQName.get(layer);
        if (a != null)
        {
          l.setValue(a.getValue());
          l.setDescription(a.getQName());
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
}
