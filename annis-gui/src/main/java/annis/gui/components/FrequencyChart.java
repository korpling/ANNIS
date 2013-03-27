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

import annis.service.objects.FrequencyTable;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.UI;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@JavaScript({"Chart.js", "jquery-1.9.1.min.js", "frequencychart.js"})
public class FrequencyChart extends AbstractJavaScriptComponent
{
  public static final org.slf4j.Logger log = LoggerFactory.getLogger(FrequencyChart.class);
  
  public static final int MAX_ITEMS = 25;
  
  public FrequencyChart()
  {
    setWidth("100%");
    setHeight("400px");
    
    addStyleName("frequency-chart");
  }
  
  public void setData(FrequencyTable table)
  {
    List<String> labels = new LinkedList<String>();
    List<Long> values = new LinkedList<Long>();

    for(FrequencyTable.Entry e : table.getEntries())
    {
      labels.add(StringUtils.join(e.getTupel(), " | "));
      values.add(e.getCount());
      
      if(values.size() >= MAX_ITEMS)
      {
        break;
      }
      
    }

    callFunction("showData", labels, values);
    
  }
}
