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

import com.vaadin.ui.Button;
import java.util.Collection;
import java.util.Set;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import java.util.Iterator;

/**
 * @author martin
 * @author tom
 */
public class AddMenu extends Panel
{
  private MenuBar addMenu = new MenuBar();
  private VerticalNode vn;
  private FlatQueryBuilder sq;
  
  private static final String BUTTON_ADDLEVEL_LABEL = "Add level"; 
  
  public AddMenu(final FlatQueryBuilder sq, final VerticalNode vn, String firstLevel)
  {
    this.vn = vn;
    this.sq = sq;
    final MenuBar.MenuItem add = addMenu.addItem(BUTTON_ADDLEVEL_LABEL, null);
    for (final String annoname : vn.getAnnonames())
    {      
      if(!annoname.equals(firstLevel))
      {        
        add.addItem(annoname, new Command() {
          @Override
          public void menuSelected(MenuItem selectedItem) {         
            vn.createSearchBox(annoname);
            add.removeChild(selectedItem);
          }
        });
      }      
    }
    setContent(addMenu);
  }
  
  public void reActivateItem(final String ebene)
  {
    final VerticalNode vn = this.vn;
    final MenuBar.MenuItem root = addMenu.getItems().iterator().next();
    int p = 0;    
    Iterator<String> items = vn.getAnnonames().iterator();  
    Command com = new Command(){
      @Override
      public void menuSelected(MenuItem selectedItem)
      {
        vn.createSearchBox(ebene);
        root.removeChild(selectedItem);
      }
    };
    while(!items.next().equals(ebene))
    {
      p++;
    }
    if(items.hasNext())
    {
      root.addItemBefore(ebene, null, com, root.getChildren().get(p));
    }
    else 
    {
      root.addItem(ebene, com);
    }
  }
}