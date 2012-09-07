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
package annis.gui.visualizers.component;

import annis.CommonHelper;
import annis.gui.MatchedNodeColors;
import annis.gui.resultview.SingleResultPanel;
import annis.gui.resultview.VisualizerPanel;
import annis.gui.visualizers.AbstractVisualizer;
import annis.gui.visualizers.VisualizerInput;
import annis.model.AnnisConstants;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.*;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.util.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.slf4j.LoggerFactory;

/**
 * Key words in context visualizer (KWIC).
 *
 * @author Thomas Krause <krause@informatik.hu-berlin.>
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@PluginImplementation
public class KWICPanel extends AbstractVisualizer<KWICPanel.KWICPanelImpl>
{

  @Override
  public String getShortName()
  {
    return "kwic";
  }

  @Override
  public KWICPanelImpl createComponent(VisualizerInput visInput)
  {
    return new KWICPanelImpl(visInput);
  }

  @Override
  public void setVisibleTokenAnnosVisible(KWICPanelImpl visualizerImplementation, Set<String> annos)
  {
    visualizerImplementation.setVisibleTokenAnnosVisible(annos);
  }

  public class KWICPanelImpl extends Table implements ItemClickEvent.ItemClickListener
  {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(KWICPanelImpl.class);
    private SDocument result;
    private static final String DUMMY_COLUMN = "dummyColumn";
    private BeanItemContainer<String> containerAnnos;
    private Map<SNode, Long> markedAndCovered;
    private List<String> mediaIDs;
    private List<VisualizerPanel> mediaVisualizer;
    // only used for media files
    private String startTime;
    private String endTime;
    private String[] media_annotations =
    {
      "time"
    };
    private VisualizerInput visInput;

    /**
     * Used by the Pluginsystem.
     */
    public KWICPanelImpl()
    {
    }

    public KWICPanelImpl(VisualizerInput visInput)
    {
      this.visInput = visInput;
    }

    @Override
    public void attach()
    {

      if (visInput != null)
      {
        initKWICPanel(visInput.getSResult(),
          visInput.getToken(),
          visInput.getVisibleTokenAnnos(),
          visInput.getMarkedAndCovered(),
          visInput.getText(),
          visInput.getMediaIDs(),
          visInput.getMediaVisualizer(),
          visInput.getSingleResultPanel(),
          visInput.getSegmentationName());
      }
    }

    private void initKWICPanel(SDocument result, List<SNode> token,
      Set<String> tokenAnnos, Map<SNode, Long> markedAndCovered, STextualDS text,
      List<String> mediaIDs, List<VisualizerPanel> mediaVisualizer,
      SingleResultPanel parent, String segmentationName)
    {
      this.result = result;
      this.markedAndCovered = markedAndCovered;
      this.mediaIDs = mediaIDs;
      this.mediaVisualizer = mediaVisualizer;
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

      addStyleName("single-result");

      if (CommonHelper.containsRTLText(text.getSText()))
      {
        addStyleName("rtl");
      }

      SDocumentGraph graph = result.getSDocumentGraph();

      ArrayList<Object> visible = new ArrayList<Object>(10);
      Long lastTokenIndex = null;

      for (SNode t : token)
      {
        STextualDS tokenText = null;
        EList<STYPE_NAME> types = new BasicEList<STYPE_NAME>();
        types.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);


        EList<SDataSourceSequence> dataSources = graph.getOverlappedDSSequences(t,
          types);
        if (dataSources != null)
        {
          for (SDataSourceSequence seq : dataSources)
          {
            if (seq.getSSequentialDS() instanceof STextualDS)
            {
              tokenText = (STextualDS) seq.getSSequentialDS();
              break;
            }
          }
        }

        SFeature featTokenIndex = t.getSFeature(AnnisConstants.ANNIS_NS,
          segmentationName == null ? AnnisConstants.FEAT_TOKENINDEX
          : AnnisConstants.FEAT_SEGLEFT);

        if (tokenText == text)
        {
          // TODO: howto nativly detect gaps in Salt?
          if (lastTokenIndex != null && featTokenIndex != null
            && featTokenIndex.getSValueSNUMERIC().longValue() > (lastTokenIndex.
            longValue() + 1))
          {
            // add "(...)"
            Long gapColumnID = featTokenIndex.getSValueSNUMERIC();
            addGeneratedColumn(gapColumnID, new KWICPanelImpl.GapColumnGenerator());
            setColumnExpandRatio(gapColumnID, 0.0f);
            visible.add(gapColumnID);
          }

          //add a column for each token
          try
          {
            addGeneratedColumn(t, new KWICPanelImpl.TokenColumnGenerator(t, segmentationName));
            setColumnExpandRatio(t, 0.0f);
          }
          catch (IllegalArgumentException ex)
          {
            log.error("unknown", ex);
          }
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
        public Object generateCell(Table source, Object itemId, Object columnId)
        {
          return "";
        }
      });
      setColumnWidth(DUMMY_COLUMN, 0);
      setColumnExpandRatio(DUMMY_COLUMN, 1.0f);
      visible.add(DUMMY_COLUMN);
      containerAnnos.addAll(tokenAnnos);

      setContainerDataSource(containerAnnos);
      setVisibleColumns(visible.toArray());


      setCellStyleGenerator(new KWICPanelImpl.KWICStyleGenerator());
      setItemDescriptionGenerator(new KWICPanelImpl.TooltipGenerator());

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

    public class TooltipGenerator implements AbstractSelect.ItemDescriptionGenerator
    {

      public String generateDescription(String layer, SToken token)
      {
        SAnnotation a = token.getSAnnotation(layer);
        if (a != null)
        {
          return a.getQName();
        }

        return null;
      }

      @Override
      public String generateDescription(Component source, Object itemId, Object propertyId)
      {
        if (propertyId != null && propertyId instanceof SToken)
        {
          return generateDescription((String) itemId, (SToken) propertyId);
        }
        else
        {
          return null;
        }
      }
    }

    public class KWICStyleGenerator implements Table.CellStyleGenerator
    {

      public String getStyle(String layer, SNode token)
      {
        BasicEList<STYPE_NAME> textualRelation = new BasicEList<STYPE_NAME>();
        textualRelation.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);


        if ("tok".equals(layer))
        {

          if (markedAndCovered.containsKey(token))
          {
            // add color
            return MatchedNodeColors.colorClassByMatch(markedAndCovered.get(token));
          }
          else
          {
            return null;
          }
        }
        else
        {
          SAnnotation a = token.getSAnnotation(layer);
          if (a != null)
          {
            for (String media_anno : media_annotations)
            {
              if (media_anno.equals(a.getName()))
              {
                if (!a.getValueString().matches("\\-[0-9]*(\\.[0-9]*)?"))
                {
                  return "kwic-clickable";
                }
              }
            }
          }
        }
        return "kwic-anno";
      }

      @Override
      public String getStyle(Object itemId, Object propertyId)
      {
        if (propertyId != null && propertyId instanceof SToken)
        {
          return getStyle((String) itemId, (SNode) propertyId);
        }
        else
        {
          return null;
        }
      }
    }

    public class GapColumnGenerator implements Table.ColumnGenerator
    {

      public Object generateCell(String layer)
      {
        if ("tok".equals(layer))
        {
          return "(...)";
        }

        return "";
      }

      @Override
      public Object generateCell(Table source, Object itemId, Object columnId)
      {
        return generateCell((String) itemId);
      }
    }

    public class TokenColumnGenerator implements Table.ColumnGenerator
    {

      private Map<String, SAnnotation> annotationsByQName;
      private SNode token;
      private String segmentationName;

      public TokenColumnGenerator(SNode token, String segmentationName)
      {
        this.token = token;
        this.segmentationName = segmentationName;
        annotationsByQName = new HashMap<String, SAnnotation>();
        for (SAnnotation a : token.getSAnnotations())
        {
          annotationsByQName.put(a.getQName(), a);
          // also add non-qualified name if we are working on a segmentation path
          if (a.getSName().equals(segmentationName))
          {
            annotationsByQName.put(a.getSName(), a);
          }
        }
      }

      public Object generateCell(String layer)
      {

        BasicEList<STYPE_NAME> textualRelation = new BasicEList<STYPE_NAME>();
        textualRelation.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
        SDocumentGraph docGraph = result.getSDocumentGraph();


        if ("tok".equals(layer))
        {
          if (segmentationName == null)
          {
            SDataSourceSequence seq = docGraph.getOverlappedDSSequences(token,
              textualRelation).get(0);

            return ((String) seq.getSSequentialDS().getSData()).substring(seq.
              getSStart(), seq.getSEnd());

          }
          else
          {
            SAnnotation a = annotationsByQName.get(segmentationName);
            if (a != null)
            {
              return a.getValueString();
            }

          }

        }
        else
        {
          SAnnotation a = annotationsByQName.get(layer);
          if (a != null)
          {
            for (String media_anno : media_annotations)
            {
              if (media_anno.equals(a.getName()))
              {
                String startTime = getStartTime((String) a.getValue());
                String endTime = getEndTime((String) a.getValue());
                startTime = trimTimeAnno(startTime);
                endTime = trimTimeAnno(endTime);
                return (startTime + "-" + endTime);
              }
            }

            return a.getValueString();
          }
        }
        return "";
      }

      @Override
      public Object generateCell(Table source, Object itemId, Object columnId)
      {
        return generateCell((String) itemId);
      }
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
        vis.toggleVisualizer(false);
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
}
