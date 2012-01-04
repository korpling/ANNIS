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

import annis.service.ifaces.AnnisCorpus;
import com.vaadin.Application;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 *
 * @author thomas
 */
public class CitationWindow extends Window
  implements Button.ClickListener
{
  
  public CitationWindow(Application app, String query, Map<String, AnnisCorpus> corpora, 
    int contextLeft, int contextRight)
  {
    super("Citation");
    
    VerticalLayout wLayout = (VerticalLayout) getContent();
    wLayout.setSizeFull();
    
    List<String> corpusNames = new LinkedList<String>(corpora.keySet());
    String url = Helper.generateCitation(app, 
      query, corpusNames, contextLeft, contextRight);
    TextArea txtCitation = new TextArea();

    txtCitation.setWidth("100%");
    txtCitation.setHeight("100%");
    txtCitation.addStyleName(ChameleonTheme.TEXTFIELD_BIG);
    txtCitation.addStyleName("citation");
    txtCitation.setValue(url);
    txtCitation.setWordwrap(true);
    txtCitation.setReadOnly(true);
    
    addComponent(txtCitation);
    
    Button btOk = new Button("OK");
    btOk.addListener((Button.ClickListener) this);
    btOk.setSizeUndefined();
    
    addComponent(btOk);
    
    wLayout.setExpandRatio(txtCitation, 1.0f);
    wLayout.setComponentAlignment(btOk, Alignment.BOTTOM_CENTER);
    
    setWidth("400px");
    setHeight("200px");
    
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    this.getParent().removeWindow(this);
  }
}
