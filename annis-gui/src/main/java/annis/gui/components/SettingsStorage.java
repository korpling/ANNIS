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
package annis.gui.components;

import annis.libgui.Helper;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.UI;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@JavaScript({"js.cookie-2.0.3.min.js", "settingsstorage.js"})
public class SettingsStorage extends AbstractJavaScriptExtension
{
  private final ConcurrentMap<String, String> storage = new ConcurrentHashMap<>();
  private final List<LoadedListener> loadedListeners = new LinkedList<>();
  
  public SettingsStorage(UI ui)
  { 
    if(ui != null)
    {
      extend(ui);
    }
    
    addFunction("loadFromClient", new JavaScriptFunction()
    {
      @Override
      public void call(JsonArray args)
      {
        JsonObject values = args.get(0);
        for(String key : values.keys())
        {
          JsonValue v = values.get(key);
          if(v.getType() == JsonType.STRING)
          {
            storage.put(key, v.asString());
          }
          else if(v.getType() == JsonType.OBJECT)
          {
            storage.put(key, v.toJson());
          }
        }
        
        for(LoadedListener l : loadedListeners)
        {
          l.onSettingsLoaded(SettingsStorage.this);
        }
        
      }
    });
    
  }
  
  public void addedLoadedListener(LoadedListener listener)
  {
    loadedListeners.add(listener);
  }
  
  public void set(String name, String value, int lifetimeInDays)
  {
    storage.put(name, value);
    callFunction("set", name, value, Helper.getContext(), lifetimeInDays);
  }
  
  public String get(String name)
  {
    return storage.get(name);
  }
  
  /**
   * Callback for the event that the settings have been loaded
   * from the client.
   */
  public static interface LoadedListener
  {
    /**
     * Called when the settings have been loaded from the client.
     * Will be only called once.
     * @param settings 
     */
    public void onSettingsLoaded(SettingsStorage settings);
  }
}
