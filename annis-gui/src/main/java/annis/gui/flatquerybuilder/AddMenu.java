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

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import java.util.List;

/**
 * @author martin
 * @author tom
 */
public class AddMenu extends Panel
{
  private MenuBar addMenu = new MenuBar();
  
  private static final String BUTTON_ADDLEVEL_LABEL = "+"; 
  
  public AddMenu(final FlatQueryBuilder sq, final VerticalNode vn, String firstLevel)
  {
    final MenuItem add = addMenu.addItem(BUTTON_ADDLEVEL_LABEL, null);
    for (final String annoname : vn.getAnnonames())
    {      
              
      add.addItem(annoname, new Command() {
        @Override
        public void menuSelected(MenuItem selectedItem) {         
          vn.createSearchBox(annoname);
          selectedItem.setVisible(false);
        }
      });
      /*FIRST ITEM HAS TO BE IN THE LIST*/
      if(annoname.equals(firstLevel))
      {
        add.getChildren().get(add.getChildren().size()-1).setVisible(false);
      }
    }
    addMenu.setSizeUndefined();
    setContent(addMenu);
  }
  public void reActivateItem(final String level)
  {
    List<MenuItem> items = addMenu.getItems().get(0).getChildren();
    boolean found = false;
    for(int i=0; (i<items.size())&!found; i++)
    {
      MenuItem itm = items.get(i);
      if(itm.getText().equals(level))
      {
        itm.setVisible(true);
        found = true;
      }
    }
  }
}