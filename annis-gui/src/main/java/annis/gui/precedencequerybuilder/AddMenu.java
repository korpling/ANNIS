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
package annis.gui.precedencequerybuilder;

import com.vaadin.ui.Button;
import java.util.Collection;
import java.util.Set;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import java.util.Iterator;

/**
 *
 * @author tom
 */
public class AddMenu extends Panel
{
  
  private MenuBar addMenu = new MenuBar();
  private VerticalNode vn;
  private PrecedenceQueryBuilder sq;
  private static final String BUTTON_ADDLEVEL_LABEL = "Add level";
  
  
  public AddMenu(final PrecedenceQueryBuilder sq, final VerticalNode vn)
  {
    this.vn = vn;
    this.sq = sq;
    final MenuBar.MenuItem add = addMenu.addItem(BUTTON_ADDLEVEL_LABEL, null);
    for (final String annoname : vn.getAnnonames())
    {      
      add.addItem(annoname, new Command() {
        @Override
        public void menuSelected(MenuItem selectedItem) {         
          vn.createSearchBox(annoname);
          add.removeChild(selectedItem);
        }
      });
    }
    addComponent(addMenu);
  }
  
  public void addItem(final String ebene)
  {
    final VerticalNode vn = this.vn;
    final MenuBar.MenuItem add = addMenu.getItems().iterator().next();
    int i = 0;
    Iterator<MenuBar.MenuItem> items = addMenu.getItems().iterator();
    /*find position for "new" Item here AND use insertItem instead of addItem*/
    add.addItem(ebene, new Command() {
      @Override
      public void menuSelected(MenuItem selectedItem)
      {
        vn.createSearchBox(ebene);
        add.removeChild(selectedItem);
      }
    });
  }
  
}
