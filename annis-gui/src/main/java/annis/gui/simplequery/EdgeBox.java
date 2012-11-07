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
package annis.gui.simplequery;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Panel;

/**
 *
 * @author tom
 */
public class EdgeBox extends Panel
{
  private ComboBox edge;
  private int id;
  private static final String[] EDGE_OPERATORS = new String[]
  {
    ".",".*", ".*",">",">*", ">@l", ">@r", "$", "$*", "->", "_=_", "_i_",
    "_l_", "'_r_", "_o_", "_ol_", "_or_"
  };
  
  public EdgeBox (int id, SimpleQuery sq)
  {
    edge = new ComboBox();
    for(String o : EDGE_OPERATORS)
    {
      edge.addItem(o);
    }
    edge.setValue(EDGE_OPERATORS[0]);
    edge.setWidth("50px");
    addComponent(edge);
  }
  
}
