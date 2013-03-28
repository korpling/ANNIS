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
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@JavaScript(value =
{
  "flotr2.js", "jquery-1.9.1.min.js", "frequencychart.js"
})
public class FrequencyWhiteboard extends AbstractJavaScriptComponent
{

  public final int PIXEL_PER_VALUE = 45;
  
  public FrequencyWhiteboard()
  {
    setWidth("99%");
    setHeight("99%");
    addStyleName("frequency-chart");
  }

  public void setData(FrequencyTable table)
  {
    List<String> labels = new LinkedList<String>();
    List<Long> values = new LinkedList<Long>();
    int i=1;
    for (FrequencyTable.Entry e : table.getEntries())
    {
      labels.add(StringUtils.join(e.getTupel(), "/"));
      values.add(e.getCount());
      i++;
    }
    setWidth(PIXEL_PER_VALUE * values.size(), Unit.PIXELS);
    callFunction("showData", labels, values);
  }
  
}
