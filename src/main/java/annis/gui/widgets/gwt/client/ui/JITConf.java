/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package annis.gui.widgets.gwt.client.ui;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class JITConf extends JSONObject { // NO_UCD (use default)

  protected JITConf() {}

  public JavaScriptObject getNativeJavascriptObject() {
    return this.getJavaScriptObject();
  }

  public void setProperty(String key, Boolean value) { // NO_UCD (unused code)
    this.put(key, JSONBoolean.getInstance(value));
  }

  public void setProperty(String key, Integer value) { // NO_UCD (unused code)
    this.put(key, new JSONNumber(value));
  }

  public void setProperty(String key, JSONObject value) { // NO_UCD (use default)
    this.put(key, value);
  }

  public void setProperty(String key, String value) { // NO_UCD (use default)
    this.put(key, new JSONString(value));
  }
}
