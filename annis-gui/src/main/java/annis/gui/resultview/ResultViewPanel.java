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

import annis.libgui.PluginSystem;
import annis.gui.QueryController;
import annis.gui.model.PagedResultQuery;
import annis.gui.paging.PagingComponent;
import annis.libgui.Helper;
import annis.libgui.InstanceConfig;
import static annis.gui.controlpanel.SearchOptionsPanel.KEY_DEFAULT_BASE_TEXT_SEGMENTATION;
import annis.service.objects.CorpusConfig;
import annis.service.objects.Match;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class ResultViewPanel extends VerticalLayout
{
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ResultViewPanel.class);

  public static final String NULL_SEGMENTATION_VALUE = "tokens (default)";

  private PagingComponent paging;
  private ResultSetPanel resultPanel;
  private ProgressBar progressResult;
  private PluginSystem ps;
  private MenuItem miTokAnnos;
  private MenuItem miSegmentation;
  private TreeMap<String, Boolean> tokenAnnoVisible;
  private QueryController controller;
  private String selectedSegmentationLayer;
  private InstanceConfig instanceConfig;

  public ResultViewPanel(QueryController controller,
    PluginSystem ps, InstanceConfig instanceConfig)
  {
    this.tokenAnnoVisible = new TreeMap<String, Boolean>();
    this.ps = ps;
    this.controller = controller;
    this.selectedSegmentationLayer = controller.getQuery().getSegmentation();
    Set<String> corpora = controller.getQuery().getCorpora();

    if (corpora.size() == 1)
    {
      CorpusConfig corpusConfig = Helper.getCorpusConfig(corpora.iterator().next());
      if (corpusConfig != null && corpusConfig.getConfig() != null
        && corpusConfig.getConfig().containsKey(KEY_DEFAULT_BASE_TEXT_SEGMENTATION))
      {
        this.selectedSegmentationLayer = corpusConfig.getConfig(
          KEY_DEFAULT_BASE_TEXT_SEGMENTATION);
      }
    }

    this.instanceConfig = instanceConfig;

    setSizeFull();
    setMargin(false);


    MenuBar mbResult = new MenuBar();
    mbResult.setWidth("100%");

    miSegmentation = mbResult.addItem("Base text", null);

    miTokAnnos = mbResult.addItem("Token Annotations", null);

    PagedResultQuery q = controller.getQuery();

    paging = new PagingComponent(q.getOffset(), q.getLimit());
    paging.setInfo(q.getQuery());
    paging.addCallback(controller);

    addComponent(mbResult);
    addComponent(paging);

    progressResult = new ProgressBar();
    progressResult.setIndeterminate(true);
    progressResult.setCaption("Searching for \"" + q.getQuery().replaceAll("\n", " ") + "\"");
    progressResult.setEnabled(true);
    progressResult.setVisible(true);

    addComponent(progressResult);

    setComponentAlignment(paging, Alignment.TOP_CENTER);
    setComponentAlignment(progressResult, Alignment.MIDDLE_CENTER);

    setExpandRatio(mbResult, 0.0f);
    setExpandRatio(paging, 0.0f);
    setExpandRatio(progressResult, 1.0f);

  }

  public void setResult(List<Match> result, int contextLeft, int contextRight,
    String segmentationLayer, int offset)
  {
    progressResult.setVisible(false);
    progressResult.setEnabled(false);

    if (resultPanel != null)
    {
      removeComponent(resultPanel);
    }
    resultPanel = null;

    if (result != null && result.size() > 0)
    {
      resultPanel = new ResultSetPanel(result, ps, instanceConfig,
        contextLeft, contextRight,
        segmentationLayer, this, offset);

      addComponent(resultPanel);
      setExpandRatio(resultPanel, 1.0f);
      setComponentAlignment(resultPanel, Alignment.TOP_CENTER);

      resultPanel.setVisible(true);
    }
    else
    {
      // nothing to show since we have an empty result
      Label lblNoResult = new Label("No matches found.");
      lblNoResult.setSizeUndefined();
      addComponent(lblNoResult);
      setComponentAlignment(lblNoResult, Alignment.MIDDLE_CENTER);
      setExpandRatio(lblNoResult, 1.0f);
    }

  }

  public void setCount(int count)
  {
    paging.setCount(count, false);
    paging.setStartNumber(controller.getQuery().getOffset());
  }


  public Set<String> getVisibleTokenAnnos()
  {
    TreeSet<String> result = new TreeSet<String>();

    for (Entry<String, Boolean> e : tokenAnnoVisible.entrySet())
    {
      if (e.getValue().booleanValue() == true)
      {
        result.add(e.getKey());
      }
    }

    return result;
  }

  public void updateSegmentationLayer(Set<String> segLayers)
  {
    miSegmentation.removeChildren();

    segLayers.add("");

    for(String s : segLayers)
    {
      MenuItem miSingleSegLayer =
        miSegmentation.addItem((s == null || "".equals(s)) ?  NULL_SEGMENTATION_VALUE : s,
        new MenuBar.Command()
      {

        @Override
        public void menuSelected(MenuItem selectedItem)
        {
          selectedSegmentationLayer = selectedItem.getText();
          if(NULL_SEGMENTATION_VALUE.equals(selectedSegmentationLayer))
          {
            selectedSegmentationLayer = null;
          }
          for(MenuItem mi : miSegmentation.getChildren())
          {
            mi.setChecked(mi == selectedItem);
          }

          resultPanel.setSegmentationLayer(selectedSegmentationLayer);
        }
      });

      miSingleSegLayer.setCheckable(true);
      miSingleSegLayer.setChecked(
        (selectedSegmentationLayer == null && "".equals(s))
        || s.equals(selectedSegmentationLayer));
    }
  }

  public void updateTokenAnnos(Set<String> tokenAnnotationLevelSet)
  {
    // add new annotations
    for (String s : tokenAnnotationLevelSet)
    {
      if (!tokenAnnoVisible.containsKey(s))
      {
        tokenAnnoVisible.put(s, Boolean.TRUE);
      }
    }

    miTokAnnos.removeChildren();

    for (String a : tokenAnnotationLevelSet)
    {
      MenuItem miSingleTokAnno = miTokAnnos.addItem(a, new MenuBar.Command()
      {

        @Override
        public void menuSelected(MenuItem selectedItem)
        {

          if (selectedItem.isChecked())
          {
            tokenAnnoVisible.put(selectedItem.getText(), Boolean.TRUE);
          }
          else
          {
            tokenAnnoVisible.put(selectedItem.getText(), Boolean.FALSE);
          }

          resultPanel.setVisibleTokenAnnosVisible(getVisibleTokenAnnos());
        }
      });

      miSingleTokAnno.setCheckable(true);
      miSingleTokAnno.setChecked(tokenAnnoVisible.get(a).booleanValue());

    }

  }

  public PagingComponent getPaging()
  {
    return paging;
  }


}
