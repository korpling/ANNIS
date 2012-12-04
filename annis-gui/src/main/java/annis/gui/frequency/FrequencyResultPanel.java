/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.frequency;

import annis.service.objects.FrequencyTableEntry;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class FrequencyResultPanel extends VerticalLayout
{
  
  private Table tbResult;
  private String aql;
  private Set<String> corpora;
  private List<FrequencyTableEntry> freqDefinition;

  public FrequencyResultPanel(String aql,
    Set<String> corpora,
    List<FrequencyTableEntry> freqDefinition)
  {
    this.aql = aql;
    this.corpora = corpora;
    this.freqDefinition = freqDefinition;
    
    setSizeFull();
  }

  @Override
  public void attach()
  {
    super.attach();
    
    addComponent(new Label("query was: " + aql));
  }
  
}
