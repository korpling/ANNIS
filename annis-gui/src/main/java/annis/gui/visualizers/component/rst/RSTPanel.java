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
package annis.gui.visualizers.component.rst;

import annis.gui.visualizers.VisualizerInput;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import java.util.Properties;
import org.vaadin.csstools.RenderInfo;
import org.vaadin.csstools.client.VRenderInfoFetcher.CssProperty;

/**
 *
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class RSTPanel extends Panel
{

  RSTPanel(VisualizerInput visInput)
  {
    String btWidth = "30px";
    HorizontalLayout grid = new HorizontalLayout();
    final int scrollStep = 200;
    final Panel rstView = new RSTImpl(visInput);

    this.setHeight("-1px");
    this.setWidth("100%");
    grid.setHeight("-1px");
    grid.setWidth("100%");


    Button buttonLeft = new Button();
    buttonLeft.setWidth(btWidth);
    buttonLeft.setHeight("100%");
    buttonLeft.addStyleName("left-button");

    buttonLeft.addListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        getScrollLeft();

        if (rstView.getScrollLeft() < scrollStep)
        {
          rstView.setScrollLeft(0);
        }
        else
        {
          rstView.setScrollLeft(rstView.getScrollLeft() - scrollStep);
        }
      }
    });

    Button buttonRight = new Button();
    buttonRight.setWidth(btWidth);
    buttonRight.setHeight("100%");
    buttonRight.addStyleName("right-button");

    buttonRight.addListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        final Properties props = new Properties();
        RenderInfo.get(rstView, new RenderInfo.Callback()
        {
          @Override
          public void infoReceived(RenderInfo info)
          {
            props.put("width", info.getProperty(CssProperty.width));
            String width = ((String) props.get("width")).replaceAll("px", "");
            int maxWidth = Integer.parseInt(width);
            if (maxWidth > rstView.getScrollLeft())
            {
              rstView.setScrollLeft(rstView.getScrollLeft() + scrollStep);
            }
          }
        });
      }
    });

    grid.addComponent(buttonLeft);
    grid.addComponent(rstView);
    grid.addComponent(buttonRight);
    setContent(grid);
    grid.setExpandRatio(rstView, 1.0f);
  }
}
