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

import annis.libgui.Helper;
import annis.model.AqlParseError;
import annis.model.QueryNode;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONArray;
import org.json.JSONException;

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
public class AqlCodeEditor extends AbstractJavaScriptComponent
  implements FieldEvents.TextChangeNotifier, Property.Viewer, Property.ValueChangeListener
{

  private int timeout;
  private Property<String> dataSource;

  public AqlCodeEditor()
  {
    addFunction("textChanged", new TextChangedFunction());
    addStyleName("aql-code-editor");
    
    AqlCodeEditor.this.setPropertyDataSource(new ObjectProperty("", String.class));
  }
  

  @Override
  public void setPropertyDataSource(Property newDataSource)
  {
    if(newDataSource == null)
    {
      throw new IllegalArgumentException("Data source must not be null");
    }
    
    if(this.dataSource instanceof Property.ValueChangeNotifier)
    {
      ((Property.ValueChangeNotifier) this.dataSource).removeValueChangeListener(this);
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
    String oldText = getState().text;
    getState().text = this.dataSource.getValue();
    
    if(oldText == null || !oldText.equals(getState().text))
    {
      markAsDirty();
    }
  }

  private class TextChangedFunction implements JavaScriptFunction
  {

    @Override
    public void call(JSONArray args) throws JSONException
    {
      getState().text = args.getString(0);
      getPropertyDataSource().setValue(args.getString(0));
      
      validate(dataSource.getValue());
      final String textCopy = dataSource.getValue();
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

  private void validate(String query)
  {
    setErrors(null);
    getState().nodeMappings.clear();
    if(query == null || query.isEmpty())
    {
      // don't validate the empty query
      return;
    }
    try
    {
      AsyncWebResource annisResource = Helper.getAnnisAsyncWebResource();
      Future<List<QueryNode>> future = annisResource.path("query").path("parse/nodes").
        queryParam("q", Helper.encodeJersey(query))
        .get(new GenericType<List<QueryNode>>(){});

      // wait for maximal one seconds
      try
      {
        List<QueryNode> result = future.get(1, TimeUnit.SECONDS);
        
        getState().nodeMappings.putAll(mapQueryNodes(result));
      }
      catch (InterruptedException ex)
      {
      }
      catch (ExecutionException ex)
      {
        if (ex.getCause() instanceof UniformInterfaceException)
        {
          UniformInterfaceException cause = (UniformInterfaceException) ex.
            getCause();
          if (cause.getResponse().getStatus() == 400)
          {
            List<AqlParseError> errorsFromServer = 
                cause.getResponse().getEntity(new GenericType<List<AqlParseError>>() {});
            
            setErrors(errorsFromServer);
          }
        }
      }
      catch (TimeoutException ex)
      {
      }
    }
    catch (ClientHandlerException ex)
    {
    }
  }
  
  private TreeMap<String, Integer> mapQueryNodes(List<QueryNode> nodes)
  {
    Map<Integer, TreeSet<Long>> alternative2Nodes = new HashMap<>();
   
    for (QueryNode n : nodes)
    {
      TreeSet<Long> orderedNodeSet = alternative2Nodes.get(n.getAlternativeNumber());
      if(orderedNodeSet == null)
      {
        orderedNodeSet = new TreeSet<>();
        alternative2Nodes.put(n.getAlternativeNumber(), orderedNodeSet);
      }
      orderedNodeSet.add(n.getId());
    }
    
    TreeMap<String, Integer> result = new TreeMap<>();
    for(TreeSet<Long> orderedNodeSet : alternative2Nodes.values())
    {
      int newID=1;
      for(long var : orderedNodeSet)
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
  
  public String getTextareaStyle() {
    return getState().textareaClass == null ? "" : getState().textareaClass;
  }
  
  public void setTextareaStyle(String style) {
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
      getState().errors.addAll(errors);
    }
    markAsDirty();
  }

}
