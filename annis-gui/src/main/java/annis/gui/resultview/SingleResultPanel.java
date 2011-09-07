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

import annis.service.ifaces.AnnisResult;
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
  public SingleResultPanel(AnnisResult result)
  {
    this.result = result;

    setWidth("100%");
    setHeight("-1px");
    
    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setMargin(false);
    
    addComponent(new KWICPanel(result));
    addComponent(new Label("Hello World"));
  }
}
