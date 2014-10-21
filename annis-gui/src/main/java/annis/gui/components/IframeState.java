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
package annis.gui.components;

import com.vaadin.shared.ui.JavaScriptComponentState;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class IframeState extends JavaScriptComponentState
{
  private String source;
  private Integer lastScrollPos;

  public String getSource()
  {
    return source;
  }

  public void setSource(String source)
  {
    this.source = source;
  }

  public Integer getLastScrollPos()
  {
    return lastScrollPos;
  }

  public void setLastScrollPos(Integer lastScrollPos)
  {
    this.lastScrollPos = lastScrollPos;
  }
  
  
  
}
