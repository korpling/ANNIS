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
package annis.gui;

import annis.gui.admin.ImportPanel;
import annis.libgui.AnnisBaseUI;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Theme("reindeer")
public class AdminUI extends AnnisBaseUI
{
  private VerticalLayout layout;
  
  @Override
  protected void init(VaadinRequest request)
  {
    super.init(request);
    
    layout = new VerticalLayout();
    layout.setSizeFull();
    setContent(layout);
    
    TabSheet tabSheet = new TabSheet();
    tabSheet.addTab(new ImportPanel(), "Import Corpus");
    tabSheet.setSizeFull();
    
    layout.addComponent(tabSheet);
  }
  
}
