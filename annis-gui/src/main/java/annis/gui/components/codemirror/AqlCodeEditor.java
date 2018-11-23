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

import annis.model.AqlParseError;
import annis.model.QueryNode;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
import elemental.json.JsonArray;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A code editor component for the ANNIQ Query Language.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@JavaScript(
  {
    "vaadin://jquery.js",
    "lib/codemirror.js",
    "mode/aql/aql.js",
    "lib/edit/matchbrackets.js",
    "lib/lint/lint.js",
    "lib/display/placeholder.js",
    "AqlCodeEditor.js"
  })
@StyleSheet(
  {
    "lib/codemirror.css",
    "lib/lint/lint.css",
    "AqlCodeEditor.css"
  })
//basic server-side component
public class AqlCodeEditor extends AbstractJavaScriptComponent
  implements FieldEvents.TextChangeNotifier, Property.Viewer,
  Property.ValueChangeListener
{

  private static final Logger log = LoggerFactory.getLogger(AqlCodeEditor.class);

  private int timeout;

  private Property<String> dataSource;

  public AqlCodeEditor()
  {
    addFunction("textChanged", new TextChangedFunction());
    addStyleName("aql-code-editor");

    AqlCodeEditor.this.setPropertyDataSource(
      new ObjectProperty<String>("", String.class));

    // init to one so the client (which starts with 0) at initialization always uses
    // the the values provided by the server state
    AqlCodeEditor.this.getState().serverRequestCounter = 1;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setPropertyDataSource(Property newDataSource)
  {
    if (newDataSource == null)
    {
      throw new IllegalArgumentException("Data source must not be null");
    }
    if(newDataSource.getType() != String.class)
    {
      throw new IllegalArgumentException("Data source must be of type String");
    }

    if (this.dataSource instanceof Property.ValueChangeNotifier)
    {
      ((Property.ValueChangeNotifier) this.dataSource).
        removeValueChangeListener(this);
    }

    this.dataSource = newDataSource;

    if (newDataSource instanceof Property.ValueChangeNotifier)
    {
      ((Property.ValueChangeNotifier) this.dataSource).
        addValueChangeListener(this);
    }

  }

  @Override
  public Property getPropertyDataSource()
  {
    return this.dataSource;
  }

  @Override
  public void valueChange(Property.ValueChangeEvent event)
  {
    log.debug("valueChange \"{}\"/\"{}", event.getProperty().getValue(),
      this.dataSource.getValue());
    String oldText = getState().text;
    String newText = this.dataSource.getValue();

    if (oldText == null || !oldText.equals(newText))
    {
      getState().text = newText;
      // this is a server side state change and we have to explicitly tell the client we want to change the text
      getState().serverRequestCounter++;

      log.debug("invalidating \"{}\"/\"{}\"", oldText, getState().text);
      markAsDirty();
    }
  }

  private class TextChangedFunction implements JavaScriptFunction
  {

    @Override
    public void call(JsonArray args) throws JSONException
    {
      log.debug("TextChangedFunction \"{}\"", args.getString(0));
      getState().text = args.getString(0);
      getPropertyDataSource().setValue(args.getString(0));

      final String textCopy = dataSource.getValue();
      final int cursorPos = (int) args.getNumber(1);
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

  public void setNodes(List<QueryNode> nodes)
  {
    getState().nodeMappings.clear();
    if(nodes != null)
    {
      getState().nodeMappings.putAll(mapQueryNodes(nodes));
    }
  }

  private TreeMap<String, Integer> mapQueryNodes(List<QueryNode> nodes)
  {
    TreeMap<String, Integer> result = new TreeMap<>();
    Map<Integer, TreeSet<Long>> alternative2Nodes = new HashMap<>();

    for (QueryNode n : nodes)
    {
      TreeSet<Long> orderedNodeSet = alternative2Nodes.get(n.
        getAlternativeNumber());
      if (orderedNodeSet == null)
      {
        orderedNodeSet = new TreeSet<>();
        alternative2Nodes.put(n.getAlternativeNumber(), orderedNodeSet);
      }
      orderedNodeSet.add(n.getId());
    }

    for (TreeSet<Long> orderedNodeSet : alternative2Nodes.values())
    {
      int newID = 1;
      for (long var : orderedNodeSet)
      {
        result.put("" + var, newID);
        newID++;
      }
    }

    return result;
  }

  public void setInputPrompt(String prompt)
  {
    getState().inputPrompt = prompt;
    markAsDirty();
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

  public String getTextareaStyle()
  {
    return getState().textareaClass == null ? "" : getState().textareaClass;
  }

  public void setTextareaStyle(String style)
  {
    getState().textareaClass = "".equals(style) ? null : style;
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
    return dataSource.getValue();
  }

  public void setValue(String value)
  {
    dataSource.setValue(value);
  }

  @Override
  protected AqlCodeEditorState getState()
  {
    return (AqlCodeEditorState) super.getState();
  }

  public void setErrors(List<AqlParseError> errors)
  {
    getState().errors.clear();
    if (errors != null)
    {
      for (AqlParseError e : errors)
      {
        getState().errors.add(new AqlCodeEditorState.ParseError(e));
      }
    }
    markAsDirty();
  }

}
