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
import annis.gui.widgets.JITWrapper;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import java.util.Properties;
import org.vaadin.csstools.RenderInfo;
import org.vaadin.csstools.client.VRenderInfoFetcher.CssProperty;

/**
 * RSTPanel manages the scrollbuttons and calles then {@link RSTImpl} the actual
 * implementation of RST. {@link RSTImpl} compute the json out of the Salt
 * document. The whole rendering stuff is done in {@link JITWrapper}.
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

    // the calculation of the output json is done here.
    final Panel rstView = new RSTImpl(visInput);

    this.setHeight("-1px");
    this.setWidth("100%");
    grid.setHeight("-1px");
    grid.setWidth("100%");


    final Button buttonLeft = new Button();
    buttonLeft.setWidth(btWidth);
    buttonLeft.setHeight("100%");
    buttonLeft.addStyleName("left-button");
    buttonLeft.setEnabled(false);

    final Button buttonRight = new Button();
    buttonRight.setWidth(btWidth);
    buttonRight.setHeight("100%");
    buttonRight.addStyleName("right-button");

    buttonLeft.addListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        if (rstView.getScrollLeft() < scrollStep)
        {
          buttonLeft.setEnabled(false);
          rstView.setScrollLeft(0);
        }
        else
        {
          //if the right button was deactivated set it back
          rstView.setScrollLeft(rstView.getScrollLeft() - scrollStep);
        }

        buttonRight.setEnabled(true);
      }
    });

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

            if (maxWidth - rstView.getScrollLeft() > scrollStep)
            {
              buttonLeft.setEnabled(true);
              rstView.setScrollLeft(rstView.getScrollLeft() + scrollStep);
            }
            else
            {
              rstView.
                setScrollLeft(
                rstView.getScrollLeft() - (maxWidth - rstView.getScrollLeft()));

              buttonLeft.setEnabled(true);
              buttonRight.setEnabled(false);
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
