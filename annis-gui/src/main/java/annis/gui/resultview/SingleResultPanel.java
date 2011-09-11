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

import annis.gui.TestPanel;
import annis.service.ifaces.AnnisResult;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author thomas
 */
public class SingleResultPanel extends Panel
{
  private AnnisResult result;
  public SingleResultPanel(AnnisResult result, int resultNumber)
  {
    this.result = result;

    setWidth("100%");
    setHeight("-1px");
       
    setScrollable(true);
    
    HorizontalLayout hLayout = new HorizontalLayout();
    setContent(hLayout);
    
    hLayout.setWidth("100%");
    hLayout.setHeight("-1px");
    hLayout.setMargin(true);
    
    Label lblNumber = new Label("" + resultNumber);
    hLayout.addComponent(lblNumber);
    lblNumber.setSizeUndefined();
    
    VerticalLayout vLayout = new VerticalLayout();
    hLayout.addComponent(vLayout);
    
    
    KWICPanel kwic = new KWICPanel(result, resultNumber);
    vLayout.addComponent(kwic);
    vLayout.addComponent(new VisualizerPanel(result, resultNumber));
    vLayout.setWidth("100%");
    vLayout.setHeight("-1px");
    
    hLayout.setExpandRatio(vLayout, 1.0f);
  }
}
