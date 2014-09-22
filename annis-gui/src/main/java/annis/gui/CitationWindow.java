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
package annis.gui;

import annis.libgui.Helper;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Set;


/**
 *
 * @author thomas
 */
public class CitationWindow extends Window
  implements Button.ClickListener
{
  
  public CitationWindow( String query, Set<String> corpora, 
    int contextLeft, int contextRight)
  {
    super("Citation");
    
    VerticalLayout wLayout = new VerticalLayout();
    setContent(wLayout);
    wLayout.setSizeFull();
    
    String url = Helper.generateCitation(query, corpora, contextLeft,
      contextRight, null, 0, 10);
    
    TextArea txtCitation = new TextArea();

    txtCitation.setWidth("100%");
    txtCitation.setHeight("100%");
    txtCitation.addStyleName(ValoTheme.TEXTFIELD_LARGE);
    txtCitation.addStyleName("citation");
    txtCitation.setValue(url);
    txtCitation.setWordwrap(true);
    txtCitation.setReadOnly(true);
    
    wLayout.addComponent(txtCitation);
    
    Button btOk = new Button("OK");
    btOk.addListener((Button.ClickListener) this);
    btOk.setSizeUndefined();
    
    wLayout.addComponent(btOk);
    
    wLayout.setExpandRatio(txtCitation, 1.0f);
    wLayout.setComponentAlignment(btOk, Alignment.BOTTOM_CENTER);
    
    setWidth("400px");
    setHeight("200px");
    
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    UI.getCurrent().removeWindow(this);
  }
}
