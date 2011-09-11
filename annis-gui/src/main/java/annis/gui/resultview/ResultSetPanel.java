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
import annis.service.ifaces.AnnisResultSet;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author thomas
 */
public class ResultSetPanel extends Panel
{
  public ResultSetPanel(AnnisResultSet resultSet, int start)
  {
    setWidth("100%");
    setHeight("-1px");
    
    ((VerticalLayout) getContent()).setWidth("100%");
    ((VerticalLayout) getContent()).setHeight("-1px");
    
    int i=start; 
    for(AnnisResult r : resultSet)
    {
      SingleResultPanel panel = new SingleResultPanel(r, i);
      addComponent(panel);
      i++;
    }
  }
}
