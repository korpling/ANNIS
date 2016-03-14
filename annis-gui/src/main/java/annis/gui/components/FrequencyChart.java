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
package annis.gui.components;

import annis.gui.AnnisUI;
import annis.gui.frequency.FrequencyResultPanel;
import annis.libgui.Helper;
import annis.libgui.InstanceConfig;
import annis.service.objects.FrequencyTable;
import com.vaadin.data.Property;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FrequencyChart extends VerticalLayout
{

  public static final org.slf4j.Logger log = LoggerFactory.getLogger(
    FrequencyChart.class);

  public static final int MAX_ITEMS = 25;

  private FrequencyWhiteboard whiteboard;
  private final OptionGroup options;
  private FrequencyTable lastTable;

  public FrequencyChart(FrequencyResultPanel freqPanel)
  {
    setSizeFull();

    options = new OptionGroup();
    options.setSizeUndefined();
    options.addItem(FrequencyWhiteboard.Scale.LINEAR);
    options.addItem(FrequencyWhiteboard.Scale.LOG10);
    options.setItemCaption(FrequencyWhiteboard.Scale.LINEAR, "linear scale");
    options.setItemCaption(FrequencyWhiteboard.Scale.LOG10, "log<sub>10</sub> scale");
    options.setHtmlContentAllowed(true);
    options.setImmediate(true);
    options.setValue(FrequencyWhiteboard.Scale.LINEAR);
    //options.addStyleName("horizontal");
    
    options.addValueChangeListener(new Property.ValueChangeListener()
    {
      @Override
      public void valueChange(Property.ValueChangeEvent event)
      {
        // redraw graph with right scale
        if (lastTable != null)
        {
          setFrequencyData(lastTable);
        }
      }
    });

    addComponent(options);
    InnerPanel panel = new InnerPanel(freqPanel);
    addComponent(panel);

    setExpandRatio(panel, 1.0f);

  }

  public void setFrequencyData(FrequencyTable table)
  {
    String font = "sans-serif";
    float fontSize = 7.0f; // in pixel
    UI ui = UI.getCurrent();
    if(ui instanceof AnnisUI)
    {
      InstanceConfig cfg = ((AnnisUI) ui).getInstanceConfig();
      if(cfg != null && cfg.getFont() != null)
      {
        if(cfg.getFrequencyFont() != null)
        {
          font = cfg.getFrequencyFont().getName();
          // only use the font size if given in pixel (since flotr2 can only use this kind of information)
          String size = cfg.getFrequencyFont().getSize();
          if(size != null && size.trim().endsWith("px"))
          {
            fontSize = Float.parseFloat(size.replace("px", "").trim());
            // the label sizes will be multiplied by 1.3 in the Flotr2 library, thus
            // divide here to get the correct size
            fontSize = fontSize/1.3f;
          }
          else
          {
            log.warn("No valid font size (must in in \"px\" unit) given for frequency font configuration. "
              + "The value is {}", fontSize);
          }
        }
        else if(cfg.getFont() != null)
        {
          font = cfg.getFont().getName();
          // only use the font size if given in pixel (since flotr2 can only use this kind of information)
          String size = cfg.getFont().getSize();
          if(size != null && size.trim().endsWith("px"))
          {
            fontSize = Float.parseFloat(size.replace("px", "").trim());
            // the label sizes will be multiplied by 1.3 in the Flotr2 library, thus
            // divide here to get the correct size
            fontSize = fontSize/1.3f;
          }
        }
      }
    }
    
    lastTable = table;
    whiteboard.setFrequencyData(table, (FrequencyWhiteboard.Scale) options.
      getValue(), font, fontSize);
  }

  /**
   * This panel allows us to scroll the chart.
   */
  private class InnerPanel extends Panel
  {

    public InnerPanel(FrequencyResultPanel freqPanel)
    {
      setSizeFull();
      
      whiteboard = new FrequencyWhiteboard(freqPanel);
      whiteboard.addStyleName(Helper.CORPUS_FONT_FORCE);
      
      setContent(whiteboard);
    }
  }
}
