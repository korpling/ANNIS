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

import com.vaadin.ui.MenuBar;
import java.util.Collection;
import java.util.Set;
import com.vaadin.ui.VerticalLayout;
import annis.gui.simplequery.SearchBox;
import annis.gui.simplequery.AddMenu;
import com.vaadin.ui.Panel;
import annis.gui.simplequery.SimpleQuery;

/**
 *
 * @author tom
 */
public class VerticalNode extends Panel
{
  
  private Set<String> annonames;
  private SimpleQuery sq;
  
  public VerticalNode(int id, String ebene, SimpleQuery sq)
  {
        
    this.sq = sq;
    VerticalLayout v = new VerticalLayout();
    SearchBox sb = new SearchBox(id, ebene, sq); //SearchBox has takes an argument to 
      // tell it for which annotation level it should search
    annonames = sq.getAvailableAnnotationNames();
    AddMenu am = new AddMenu(sb, annonames, sq, this); //AddMenu creates a menubar from 
      // which users can pick the annotation level they are interested in
    v.addComponent(am);
    v.addComponent(sb);
    setWidth("160px");
    addComponent(v);
  }
}