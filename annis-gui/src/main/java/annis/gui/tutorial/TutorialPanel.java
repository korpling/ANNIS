/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.tutorial;

import annis.gui.components.NavigateableSinglePage;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author thomas
 */
public class TutorialPanel extends VerticalLayout
{
  private NavigateableSinglePage embedded;
  public TutorialPanel()
  {
    setSizeFull();   
    
    embedded = new NavigateableSinglePage();
    embedded.setSizeFull();
    addComponent(embedded);

    String contextPath = VaadinService.getCurrentRequest().getContextPath();
    embedded.setSource(contextPath + "/VAADIN/tutorial/index_singlepage.html");    
  }
}
