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

import java.util.Collection;
import java.util.Set;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;

/**
 *
 * @author tom
 */
public class AddMenu extends Panel
{
  
  private MenuBar addMenu = new MenuBar();
  private VerticalNode vn;
  private SimpleQuery sq;
  
  public AddMenu(Collection<String> annonames, final SimpleQuery sq, final VerticalNode vn)
  {
    this.vn = vn;
    this.sq = sq;
    final MenuBar.MenuItem add = addMenu.addItem("Add condition", null);
    for (final String annoname : annonames)
    {
      add.addItem(sq.killNamespace(annoname), new Command() {
        @Override
        public void menuSelected(MenuItem selectedItem) {
          vn.createSearchBox(sq.killNamespace(annoname));
        }
      });
    }
    addComponent(addMenu);
  }
  
}
