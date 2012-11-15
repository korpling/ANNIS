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
import annis.gui.media.MediaController;
import annis.gui.media.MediaControllerFactory;
import annis.gui.media.MediaControllerHolder;
import annis.gui.visualizers.AbstractVisualizer;
import annis.gui.visualizers.VisualizerInput;
import annis.model.AnnisConstants;
import com.vaadin.Application;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.*;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import org.apache.commons.lang3.StringUtils;
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
public class KWICPanel extends AbstractVisualizer<KWICPanel.KWICInterface>
{
  
  @InjectPlugin
  public MediaControllerFactory mcFactory;

  @Override
  public String getShortName()
  {
    return "kwic";
  }

  @Override
  public KWICInterface createComponent(VisualizerInput visInput, Application application)
  {
    MediaController mediaController = null;
    if(mcFactory != null && application instanceof MediaControllerHolder)
    {
      mediaController = mcFactory.getOrCreate((MediaControllerHolder) application);
    }
    
    EList<STextualDS> texts = visInput.getDocument().getSDocumentGraph().getSTextualDSs();
    
    // having the KWIC nested in a panel can slow down rendering
    if(texts.size() == 1)
    {
      // directly return the single non-nested KWIC panel
      return new KWICPanelImpl(visInput, mediaController, texts.get(0));
    }
    else
    {
      // return a more complicated implementation which can handle several texts
      return new KWICMultipleTextImpl(visInput,mediaController);
    }
  }

  @Override
  public void setVisibleTokenAnnosVisible(KWICInterface visualizerImplementation, Set<String> annos)
  {
    visualizerImplementation.setVisibleTokenAnnosVisible(annos);
  }
  
  
  @Override
  public void setSegmentationLayer(KWICInterface visualizerImplementation, 
    String segmentationName, Map<SNode, Long> markedAndCovered)
  {
    visualizerImplementation.setSegmentationLayer(segmentationName, markedAndCovered);
  }
  
  /**
   * A KWIC (Keyword in context) visualization shows the token of the match and their context
   * in a table like view.
   * This is the basic interface for different variants of the KWIC panel implementation.
   */
  public interface KWICInterface extends Component
  {
    public void setVisibleTokenAnnosVisible(Set<String> annos);
    public void setSegmentationLayer(String segmentationName, Map<SNode, Long> markedAndCovered);
  }
  
  /**
   * Implementation that can display several texts but has slower rendering due
   * to an extra div.
   */
  public static class KWICMultipleTextImpl extends CssLayout
    implements KWICInterface
  {
    private List<KWICPanelImpl> kwicPanels;
    public KWICMultipleTextImpl(VisualizerInput visInput, MediaController mediaController)
    {
      this.kwicPanels = new LinkedList<KWICPanelImpl>();
      if(visInput != null)
      {
        EList<STextualDS> texts = visInput.getDocument().getSDocumentGraph().getSTextualDSs();
        for(STextualDS t : texts)
        {
          KWICPanelImpl kwic = new KWICPanelImpl(visInput, mediaController, t);
          kwicPanels.add(kwic);
          
          addComponent(kwic);
        }
      }
    }
    
    @Override
    public void setVisibleTokenAnnosVisible(Set<String> annos)
    {
      for(KWICPanelImpl kwic : kwicPanels)
      {
        kwic.setVisibleTokenAnnosVisible(annos);
      }
    }
    
    @Override
    public void setSegmentationLayer(String segmentationName, Map<SNode, Long> markedAndCovered)
    {
      for(KWICPanelImpl kwic : kwicPanels)
      {
        kwic.setSegmentationLayer(segmentationName, markedAndCovered);
      } 
    }
  } // end class KWICMultipleTextImpl
  

  /**
   * Implementation for one single text.
   */
  public static class KWICPanelImpl extends Table 
    implements ItemClickEvent.ItemClickListener, KWICInterface
  {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(KWICPanelImpl.class);
    private transient SDocument result;
    private static final String DUMMY_COLUMN = "dummyColumn";
    private BeanItemContainer<String> containerAnnos;
    private transient Map<SNode, Long> markedAndCovered;
    private transient MediaController mediaController;
    
    // only used for media files
    private String[] media_annotations =
    {
      "time"
    };
    private List<Object> generatedColumns;
    
    private transient VisualizerInput visInput;
    private transient STextualDS text;

    public KWICPanelImpl(VisualizerInput visInput, MediaController mediaController, STextualDS text)
    {
      this.generatedColumns = new LinkedList<Object>();
      this.visInput = visInput;
      this.mediaController = mediaController;
      this.text = text;
    }

    @Override
    public void attach()
    {

      if (visInput != null)
      {
        initKWICPanel(visInput.getSResult(),
            visInput.getVisibleTokenAnnos(),
            visInput.getMarkedAndCovered(),
            visInput.getSegmentationName());
      }
    }
    

    private void initKWICPanel(SDocument result,
      Set<String> tokenAnnos, Map<SNode, Long> markedAndCovered, 
      String segmentationName)
    {      
      this.result = result;
      this.markedAndCovered = markedAndCovered;
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

      List<SNode> token = CommonHelper.getSortedSegmentationNodes(segmentationName, graph);
      
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
          : AnnisConstants.FEAT_SEGINDEX);

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
            generatedColumns.add(gapColumnID);
            setColumnExpandRatio(gapColumnID, 0.0f);
            visible.add(gapColumnID);
            
          }

          //add a column for each token
          try
          {
            addGeneratedColumn(t.getSId(), new KWICPanelImpl.TokenColumnGenerator(t, segmentationName));
            generatedColumns.add(t.getSId());
            setColumnExpandRatio(t.getSId(), 0.0f);
          }
          catch (IllegalArgumentException ex)
          {
            log.error("unknown", ex);
          }
          visible.add(t.getSId());


          if (featTokenIndex != null)
          {
            lastTokenIndex = featTokenIndex.getSValueSNUMERIC();
          }
        } // end if token belongs to text
      }

      addGeneratedColumn(DUMMY_COLUMN, new Table.ColumnGenerator()
      {
        @Override
        public Object generateCell(Table source, Object itemId, Object columnId)
        {
          return "";
        }
      });
      generatedColumns.add(DUMMY_COLUMN);
            
      setColumnWidth(DUMMY_COLUMN, 0);
      setColumnExpandRatio(DUMMY_COLUMN, 1.0f);
      visible.add(DUMMY_COLUMN);
      containerAnnos.addAll(tokenAnnos);

      setContainerDataSource(containerAnnos);
      setVisibleColumns(visible.toArray());


      setCellStyleGenerator(new KWICPanelImpl.KWICStyleGenerator());
      setItemDescriptionGenerator(new KWICPanelImpl.TooltipGenerator());

    }

    @Override
    public void setVisibleTokenAnnosVisible(Set<String> annos)
    {
      if (containerAnnos != null)
      {
        containerAnnos.removeAllItems();
        containerAnnos.addItem("tok");
        containerAnnos.addAll(annos);
      }
    }
    
    @Override
    public void setSegmentationLayer(String segmentationName, Map<SNode, Long> markedAndCovered)
    {
      // delete old columns
      if(generatedColumns != null)
      {
        for(Object o : generatedColumns)
        {
          removeGeneratedColumn(o);
        }
      }
      
      // re-init the complete KWIC
      
      generatedColumns = new LinkedList<Object>();
      if (visInput != null)
      {
        initKWICPanel(visInput.getSResult(),
          visInput.getVisibleTokenAnnos(),
          markedAndCovered,
          segmentationName);

      }
    }

    
    public class TooltipGenerator implements AbstractSelect.ItemDescriptionGenerator
    {

      public String generateDescription(String layer, SNode node)
      {
        SAnnotation a = node.getSAnnotation(layer);
        if (a != null)
        {
          if("annis::time".equals(a.getQName()))
          {
            
            return "play excerpt " + getShortenedTime(a.getValueString());
          }
          return a.getQName();
        }

        return null;
      }

      @Override
      public String generateDescription(Component source, Object itemId, Object propertyId)
      {
        if(result == null)
        {
          log.error("TooltipGenerator was restored from serialization and "
            + "can not generate new cells");
          return null;
        }
        
        if (propertyId != null && propertyId instanceof String)
        {
          SNode node  = result.getSDocumentGraph().getSNode((String) propertyId);
          if(node != null)
          {
            return generateDescription((String) itemId, node);
          }
        }
        return null;
      }
    }

    public class KWICStyleGenerator implements Table.CellStyleGenerator
    {

      public String getStyle(String layer, SNode node)
      {
        if ("tok".equals(layer))
        {

          if (markedAndCovered != null && markedAndCovered.containsKey(node))
          {
            // add color
            return MatchedNodeColors.colorClassByMatch(markedAndCovered.get(node));
          }
          else
          {
            return null;
          }
        }
        else
        {
          SAnnotation a = node.getSAnnotation(layer);
          if (a != null)
          {
            for (String media_anno : media_annotations)
            {
              if (media_anno.equals(a.getName()))
              {
                return "kwic-media";
              }
            }
          }
        }
        return "kwic-anno";
      }

      @Override
      public String getStyle(Object itemId, Object propertyId)
      {
         if (result == null)
        {
          log.error("KWICStyleGenerator was restored from serialization and "
            + "can not generate new cells");
          return null;
          
        }
        if (propertyId != null && propertyId instanceof String)
        {
          SNode node = result.getSDocumentGraph().getSNode((String) propertyId);
          if(node != null)
          {
            return getStyle((String) itemId, node);
          }
        }
        
        return null;
      }
    }

    public static class GapColumnGenerator implements Table.ColumnGenerator
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

      private transient Map<String, SAnnotation> annotationsByQName;
      private transient SNode token;
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
        
        if(result == null || token == null || annotationsByQName == null)
        {
          log.error("TokenColumnGenerator was restored from serialization and "
            + "can not generate new cells");
          return new Label("ERROR");
        }

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
                return "";
//                return getShortenedTime(a.getValueString());
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
    
    private String getShortenedTime(String time)
    {
      String[] splitTime = StringUtils.splitByWholeSeparator(time,
        "-", 2);
      double startTime = Double.parseDouble(splitTime[0]);
      double endTime = splitTime.length > 1 ? Double.parseDouble(splitTime[1]) : -1;

      NumberFormat format = DecimalFormat.getInstance(Locale.US);
      format.setMaximumFractionDigits(4);
      format.setMinimumFractionDigits(0);
      if(endTime >= 0)
      {
        return format.format(startTime) + "-" + format.format(endTime);
      }
      else
      {
        return format.format(startTime);
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
      String tokenID = (String) event.getPropertyId();
      
      SNode token = result.getSDocumentGraph().getSNode(tokenID);
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

      startMediaVisualizers(time);

    }

    public void startMediaVisualizers(String time)
    {
      // do not start the media player, when there is only an
      // end time defined
      if (time == null || time.matches("\\-[0-9]*(\\.[0-9]*)?"))
      {
        return;
      }
      
      if(mediaController != null && visInput != null)
      {
          
        String[] split = time.split("-");
        if(split.length == 1)
        {
          mediaController.play(visInput.getId(), Double.parseDouble(split[0]));
        }
        else if(split.length == 2)
        {
          mediaController.play(visInput.getId(), 
            Double.parseDouble(split[0]), Double.parseDouble(split[1]));
        }
      }
    }
  }
}
