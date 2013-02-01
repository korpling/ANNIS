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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class RSTPanel extends Panel
{

  RSTPanel(VisualizerInput visInput)
  {
    HorizontalLayout grid = new HorizontalLayout();
    this.setHeight("-1px");
    this.setWidth("100%");
    grid.setHeight("-1px");
    grid.setWidth("100%");
    final Panel rstView = new RSTImpl(visInput);

    Button buttonLeft = new Button("left");
    buttonLeft.addListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        rstView.setScrollLeft(rstView.getScrollLeft() - 100);
      }
    });

    buttonLeft.setWidth("10px");
    buttonLeft.setHeight("100%");

    Button buttonRight = new Button("right");
    buttonRight.addListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        rstView.setScrollLeft(rstView.getScrollLeft() + 100);
      }
    });
    buttonRight.setWidth("10px");
    buttonRight.setHeight("100%");

    grid.addComponent(buttonLeft);
    grid.addComponent(rstView);
    grid.addComponent(buttonRight);
    setContent(grid);
    grid.setExpandRatio(rstView, 1.0f);
  }
}
