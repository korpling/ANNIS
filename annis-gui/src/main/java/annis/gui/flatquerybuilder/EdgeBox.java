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
package annis.gui.flatquerybuilder;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Panel;

/**
 *
 * @author tom und martin
 */
public class EdgeBox extends Panel
{
  private ComboBox edge;
  private int id;
  private static final String[][] EDGE_OPERATORS = new String[][]
  {
    {".",".2",".1,2",".*"},
    { ".       [is directly preceding]",
      ".2      [is preceding with one token in between]",
      ".1,2    [is directly preceding or with one token in between]",
      ".*      [is indirectly preceding]"}
  };
  private static final String WIDTH = "45px";
  
  
  
  public EdgeBox (FlatQueryBuilder sq)
  {
    edge = new ComboBox();
    for(String o : EDGE_OPERATORS[1])
    {
      edge.addItem(o);
    }
    edge.setNewItemsAllowed(true);
    edge.setValue(EDGE_OPERATORS[1][0]);
    edge.setWidth(WIDTH);
    edge.setNullSelectionAllowed(false);
    setContent(edge);
  }
  
  public String getValue()
  {
    int i=0;
    while((i<EDGE_OPERATORS[1].length)&&(!EDGE_OPERATORS[1][i].equals(edge.getValue().toString())))
    {
      i++;
    }
    return EDGE_OPERATORS[0][i];
  }
  
}
