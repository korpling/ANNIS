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
package annis.gui.components.codemirror;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.event.FieldEvents;
import com.vaadin.shared.ui.JavaScriptComponentState;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.TextArea;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * A code editor component for the ANNIQ Query Language.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@JavaScript(
{
  "lib/codemirror.js", "mode/properties/properties.js", "mode/aql/aql.js", "AqlCodeEditor.js"
})
@StyleSheet(
{
  "lib/codemirror.css", "AqlCodeEditor.css"
})
public class AqlCodeEditor extends AbstractJavaScriptComponent
  implements FieldEvents.TextChangeNotifier
{
  private int timeout;
  
  public AqlCodeEditor()
  {
    addFunction("textChanged", new TextChangedFunction());
  }
  
  private class TextChangedFunction implements JavaScriptFunction
  {
    @Override
    public void call(JSONArray args) throws JSONException
    {
      getState().text = args.getString(0);
      final String textCopy = getState().text;
      final int cursorPos = args.getInt(1);
      fireEvent(new FieldEvents.TextChangeEvent(AqlCodeEditor.this)
      {
        
        @Override
        public String getText()
        {
          return textCopy;
        }
        
        @Override
        public int getCursorPosition()
        {
          return cursorPos;
        }
      });
    }
  }
  
  
  public void setInputPrompt(String prompt)
  {
    //TODO
  }
  
  public void setTextChangeTimeout(int timeout)
  {
    callFunction("setChangeDelayTime", timeout);
    this.timeout = timeout;
  }
  
  public int getTextChangeTimeout()
  {
    return this.timeout;
  }

  @Override
  public void addTextChangeListener(FieldEvents.TextChangeListener listener)
  {
    addListener(FieldEvents.TextChangeListener.EVENT_ID,
      FieldEvents.TextChangeEvent.class,
      listener, FieldEvents.TextChangeListener.EVENT_METHOD);
  }

  @Override
  public void addListener(FieldEvents.TextChangeListener listener)
  {
    addTextChangeListener(listener);
  }

  @Override
  public void removeTextChangeListener(FieldEvents.TextChangeListener listener)
  {
    removeListener(FieldEvents.TextChangeListener.EVENT_ID,
      FieldEvents.TextChangeEvent.class,
      listener);
  }

  @Override
  public void removeListener(FieldEvents.TextChangeListener listener)
  {
    removeTextChangeListener(listener);
  }

  public String getValue()
  {
    return getState().text;
  }

  public void setValue(String value)
  {
    getState().text = value;
    markAsDirty();
  }

  @Override
  protected AqlCodeEditorState getState()
  {
    return (AqlCodeEditorState) super.getState();
  }
  
  

  
}
