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
package annis.gui.resultview;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.service.ifaces.AnnisResult;
import com.vaadin.addon.chameleon.ChameleonTheme;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author thomas
 */
public class KWICPanel extends Panel
{
  private AnnisResult result;
  
  public KWICPanel(AnnisResult result, int resultNumber)
  {
    this.result = result;
    
    //addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    //addStyleName("kwic");
   
    setWidth("100%");
    setHeight("-1px");
    
    TreeSet<String> annos = new TreeSet<String>();
    List<AnnisNode> token = result.getGraph().getTokens();
    for(AnnisNode t : token)
    {
      // add to annotation overview      
      for(Annotation a : t.getNodeAnnotations())
      {
        annos.add(a.getQualifiedName());
      }      
    }

    
    GridLayout tokenGrid = new GridLayout(token.size(), 1+ annos.size());    
    setContent(tokenGrid);
    tokenGrid.setSizeUndefined();

    int column = 0;
    for(AnnisNode t : token)
    {
      Label lblTok = new Label(t.getSpannedText());
      lblTok.setSizeUndefined();
      tokenGrid.addComponent(lblTok, column, 0);    
      
      int row=1;
      for(String a : annos)
      {
        Label l = new Label();
        l.setSizeUndefined();
        for(Annotation anno : t.getNodeAnnotations())
        {
          if(anno.getQualifiedName().equals(a))
          {
            // this is the right annotation
            l.setValue(anno.getValue());
            l.setDescription(anno.getQualifiedName());
            l.addStyleName("kwic-anno");
            break;
          }
        }
        tokenGrid.addComponent(l, column, row);
        row++;
      }
      
      column++;
    }
    
    
  }
  
}
