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

import annis.gui.frequency.FrequencyResultPanel;
import annis.service.objects.FrequencyTable;
import com.vaadin.ui.Panel;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class FrequencyChart extends Panel
{

  public static final org.slf4j.Logger log = LoggerFactory.getLogger(
    FrequencyChart.class);

  public static final int MAX_ITEMS = 25;

  private FrequencyWhiteboard whiteboard;
  
  public FrequencyChart(FrequencyResultPanel freqPanel)
  {
    setSizeFull();
    whiteboard = new FrequencyWhiteboard(freqPanel);
    setContent(whiteboard);

  }

  public void setData(FrequencyTable table)
  {
    whiteboard.setData(table);
  }
}
