/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.admin;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.TextArea;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class PopupTwinColumnSelectMultiline extends PopupTwinColumnSelect
{

  @Override
  protected AbstractTextField createTextField()
  {
    TextArea text = new TextArea();
    text.setRows(5);
    return text;
  }

  
  
}
