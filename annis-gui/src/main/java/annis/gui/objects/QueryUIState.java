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
package annis.gui.objects;

import com.vaadin.data.util.ObjectProperty;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Helper class to bundle all query relevant state information of the UI.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class QueryUIState
{
  
  private final ObjectProperty<String> aql = new ObjectProperty<>("");
  private final ObjectProperty<? extends Set<String>> selectedCorpora 
    = new ObjectProperty<>(new LinkedHashSet<String>());

  public ObjectProperty<String> getAql()
  {
    return aql;
  }

  public ObjectProperty<? extends Set<String>> getSelectedCorpora()
  {
    return selectedCorpora;
  }
  
  
  
}
