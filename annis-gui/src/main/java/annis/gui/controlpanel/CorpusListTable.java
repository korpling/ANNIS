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
package annis.gui.controlpanel;

import com.vaadin.ui.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A customized extension of the default Vaadin {@link Table}.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CorpusListTable extends Table
{
  
  public static final String PAGELENGTH_CHANGED_IN_CLIENT_ID = "pageLengthChangedInClient";

  private final List<PageLengthChangedInClientListener> pageLengthListeners = new ArrayList<>();
  
  public void addPageLengthChangedInClientListener(PageLengthChangedInClientListener listener)
  {
    this.pageLengthListeners.add(listener);
  }
  
  public void removePageLengthChangedInClientListener(PageLengthChangedInClientListener listener)
  {
    this.pageLengthListeners.remove(listener);
  }
  

  @Override
  public void changeVariables(Object source, Map<String, Object> variables)
  {
    int originalPageLength = getPageLength();
    
    super.changeVariables(source, variables);
    
    int newPageLength = getPageLength();
    if(newPageLength != originalPageLength)
    {
      // pagelength changed
      for(PageLengthChangedInClientListener listener : pageLengthListeners)
      {
        listener.pageLengthChangedInClient(newPageLength);
      }
    }
  }
  
  public interface PageLengthChangedInClientListener
  {
    void pageLengthChangedInClient(int newPageLength);
  }

}
