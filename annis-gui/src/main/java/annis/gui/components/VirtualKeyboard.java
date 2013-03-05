/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.TextArea;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@StyleSheet({"keyboard.css"})
@JavaScript({"keyboard.js", "virtualkeyboard.js"})
public class VirtualKeyboard extends AbstractJavaScriptExtension
{
  
  private final Resource keyboardImage = new ThemeResource("keyboard.png");

  public VirtualKeyboard()
  {
    setResource("keyboard", keyboardImage);
  }
  
  @Override
  protected Class<? extends ClientConnector> getSupportedParentType()
  {
    return TextArea.class;
  }

  public void extend(TextArea target)
  {
    super.extend(target);
    
  }
  
  public void show()
  {
    callFunction("show");
  }
  
  
  
}
