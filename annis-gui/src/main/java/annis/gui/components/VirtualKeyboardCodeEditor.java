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

import annis.gui.components.codemirror.AqlCodeEditor;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.server.ClientConnector;
import com.vaadin.shared.JavaScriptExtensionState;
import com.vaadin.ui.JavaScriptFunction;
import elemental.json.JsonArray;
import elemental.json.impl.JreJsonNull;
import org.json.JSONException;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@StyleSheet(
{
  "keyboard.css"
})
@JavaScript(
{
  "vaadin://jquery.js", "keyboard.js", "virtualkeyboard_codeeditor.js"
})
public class VirtualKeyboardCodeEditor extends AbstractJavaScriptExtension
{

  public VirtualKeyboardCodeEditor()
  {
    addFunction("updateLang", new UpdateLangJSFunction());
  }

  @Override
  protected Class<? extends ClientConnector> getSupportedParentType()
  {
    return AqlCodeEditor.class;
  }

  public void extend(AqlCodeEditor target)
  {
    super.extend(target);

  }

  @Override
  protected VKState getState()
  {
    return (VKState) super.getState();
  }

  public void show()
  {
    callFunction("show");
  }

  public void setKeyboardLayout(String layout)
  {
    getState().setKeyboardLayout(layout);
  }

  public static class VKState extends JavaScriptExtensionState
  {

    private String keyboardLayout = "";

    public String getKeyboardLayout()
    {
      return keyboardLayout;
    }

    public void setKeyboardLayout(String keyboardLayout)
    {
      this.keyboardLayout = keyboardLayout;
    }

  }

  private class UpdateLangJSFunction implements JavaScriptFunction
  {

    public UpdateLangJSFunction()
    {
    }

    @Override
    public void call(JsonArray arguments) throws JSONException
    {
      if (arguments.length() > 0 && !(arguments.get(0) instanceof JreJsonNull))
      {
        ((VKState) getState()).setKeyboardLayout(arguments.getString(0));
      }
      else
      {
        ((VKState) getState()).setKeyboardLayout(null);
      }
    }
  }

}
