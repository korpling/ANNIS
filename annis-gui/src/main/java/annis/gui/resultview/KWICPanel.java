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
import annis.model.AnnisConstants;
import annis.model.AnnisNode;
import com.vaadin.ui.themes.ChameleonTheme;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
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
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
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
public class KWICPanel extends Table implements ItemClickEvent.ItemClickListener
{

  private SDocument result;
  private static final String DUMMY_COLUMN = "dummyColumn";
  private BeanItemContainer<String> containerAnnos;
  private Map<SNode, Long> markedAndCovered;
  private STextualDS text;
  private List<String> mediaIDs;
  private List<VisualizerPanel> mediaVisualizer;
  private SingleResultPanel parent;
  // only used for media files  
  private String startTime;
  private String endTime;
  private String[] media_annotations =
  {
    "time"
  };

  public KWICPanel(SDocument result, Set<String> tokenAnnos,
    Map<SNode, Long> markedAndCovered, STextualDS text, List<String> mediaIDs,
    List<VisualizerPanel> mediaVisualizer, SingleResultPanel parent)
  {
    this.result = result;
    this.markedAndCovered = markedAndCovered;
    this.text = text;
    this.mediaIDs = mediaIDs;
    this.mediaVisualizer = mediaVisualizer;
    this.parent = parent;
    this.addListener((ItemClickEvent.ItemClickListener) this);
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
    Long lastTokenIndex = null;

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

      SFeature featTokenIndex = t.getSFeature(AnnisConstants.ANNIS_NS,
        AnnisConstants.FEAT_TOKENINDEX);
      if (tokenText == text)
      {
        // TODO: howto nativly detect gaps in Salt?
        if (lastTokenIndex != null && featTokenIndex != null
          && featTokenIndex.getSValueSNUMERIC().longValue() > (lastTokenIndex.
          longValue() + 1))
        {
          // add "(...)"
          Long gapColumnID = featTokenIndex.getSValueSNUMERIC();
          addGeneratedColumn(gapColumnID, new GapColumnGenerator());
          setColumnExpandRatio(gapColumnID, 0.0f);
          visible.add(gapColumnID);
        }

        // add a column for each token
        addGeneratedColumn(t, new TokenColumnGenerator(t));
        setColumnExpandRatio(t, 0.0f);
        visible.add(t);


        if (featTokenIndex != null)
        {
          lastTokenIndex = featTokenIndex.getSValueSNUMERIC();
        }
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
      l.setSizeUndefined();
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

        l.setValue(((String) seq.getSSequentialDS().getSData()).substring(seq.
          getSStart(), seq.getSEnd()));
        if (markedAndCovered.containsKey(token))
        {
          // add color
          String styleName =
            MatchedNodeColors.colorClassByMatch(markedAndCovered.get(token));
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

          for (String media_anno : media_annotations)
          {
            if (media_anno.equals(a.getName()))
            {
              if (!a.getValueString().matches("\\-[0-9]*(\\.[0-9]*)?"))
              {
                l.addStyleName("clickable");
              }
              String startTime = getStartTime((String) a.getValue());
              String endTime = getEndTime((String) a.getValue());
              startTime = trimTimeAnno(startTime);
              endTime = trimTimeAnno(endTime);
              l.setValue(startTime + "-" + endTime);
            }
          }
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

  @Override
  public void itemClick(ItemClickEvent event)
  {
    // check if it is a time annotation
    String buttonName = (String) event.getItemId();

    if (buttonName == null)
    {
      return;
    }

    if (!buttonName.matches("(annis::)?time"))
    {
      return;
    } // end check

    String time = null;
    SToken token = (SToken) event.getPropertyId();
    for (SAnnotation anno : token.getSAnnotations())
    {
      for (String media_anno : media_annotations)
      {
        if (media_anno.equals(anno.getName()))
        {
          time = anno.getValueString();
        }
      }
    }

    // do not start the media player, when there is only an 
    // end time defined
    if (time != null && time.matches("\\-[0-9]*(\\.[0-9]*)?"))
    {
      return;
    }

    for (VisualizerPanel vis : mediaVisualizer)
    {
      vis.openVisualizer(false);
    }

    time = (time == null) ? "no time given" : time;
    startTime = getStartTime(time);
    endTime = getEndTime(time);
    for (VisualizerPanel vp : mediaVisualizer)
    {
      vp.setKwicPanel(this);
    }
    startMediaVisualizers();
  }

  private String getStartTime(String time)
  {
    return time.split("-")[0];
  }

  private String getEndTime(String time)
  {
    String[] split = time.split("-");
    if (split.length < 2)
    {
      return "undefined";
    }
    return time.split("-")[1];
  }

  public void startMediaVisualizers()
  {
    for (String id : mediaIDs)
    {
      String playCommand = ""
        + "document.getElementById(\"" + id + "\")"
        + ".getElementsByTagName(\"iframe\")[0].contentWindow.seekAndPlay("
        + startTime + ", " + endTime + "); ";
      getWindow().executeJavaScript(playCommand);
    }
  }

  private String trimTimeAnno(String time)
  {

    if ("undefined".equals(time))
    {
      return "";
    }

    String[] timeArray = time.split("\\.");

    if (timeArray.length < 2)
    {
      return time;
    }

    return timeArray[0] + "."
      + timeArray[1].substring(0, (timeArray[1].length() < 3 ? timeArray[1].
      length() : 2));
  }
}
