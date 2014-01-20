/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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

package annis.gui;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

/**
 * The current behavior state of the side bar.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public enum SidebarState
{
  VISIBLE(new ThemeResource("menu_left.png"), true), 
  HIDDEN(new ThemeResource("menu_right.png"), false), 
  AUTO_VISIBLE(new ThemeResource("menu_auto.png"), true),
  AUTO_HIDDEN(new ThemeResource("menu_auto.png"), false);
  
  private final Resource icon;
  private final boolean sidebarVisible;
  
  SidebarState(Resource icon, boolean sidebarVisible)
  {
    this.icon = icon;
    this.sidebarVisible = sidebarVisible;
  }

  public Resource getIcon()
  {
    return icon;
  }

  public boolean isSidebarVisible()
  {
    return sidebarVisible;
  }

  
}
