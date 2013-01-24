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

import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author tom
 */
class MetaBox extends Panel
{

  public MetaBox(String ebene, PrecedenceQueryBuilder sq)
  {
    VerticalLayout sb = new VerticalLayout();
    sb.setImmediate(true);
        
    // searchbox values for ebene
    Collection<String> annonames = new TreeSet<String>();
    for(String a :sq.getAvailableMetaLevels(ebene))
    {
      annonames.add(a.replaceFirst("^[^:]*:", ""));
    }
    
    TwinColSelect l = new TwinColSelect(ebene);
    for (String annoname : annonames) {
      l.addItem(annoname);
    }
    l.setRows(10);
    l.setNullSelectionAllowed(true);
    l.setMultiSelect(true);
    l.setImmediate(true);
    l.setLeftColumnCaption("Available levels");
    l.setRightColumnCaption("Selected levels");
    l.setWidth("350px");

    sb.addComponent(l);
    addComponent(sb);

  }
  
}
