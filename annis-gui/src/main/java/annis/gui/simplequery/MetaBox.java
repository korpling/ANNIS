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

import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
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

  public MetaBox(String ebene, SimpleQuery sq)
  {
    VerticalLayout sb = new VerticalLayout();
    sb.setImmediate(true);
        
    // searchbox values for ebene
    Collection<String> annonames = new TreeSet<String>();
    for(String a :sq.getAvailableMetaLevels(ebene))
    {
      annonames.add(a.replaceFirst("^[^:]*:", ""));
    }
    
    OptionGroup l = new OptionGroup(ebene);
    l.setMultiSelect(true);
    l.setWidth("130px");
    // configure & load content
    l.setImmediate(true);
    for (String annoname : annonames) 
    {
      l.addItem(annoname);
    }
    sb.addComponent(l);
    addComponent(sb);
    
  }
  
}
