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
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.server.ClientConnector;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScriptFunction;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@JavaScript({"jquery-1.9.1.min.js","css_render_info.js"})
public class CssRenderInfo extends AbstractJavaScriptExtension
{
  public CssRenderInfo(final Callback callback)
  {
    addFunction("publishResults", new JavaScriptFunction()
    {
      @Override
      public void call(JSONArray arguments) throws JSONException
      {
        if (callback != null)
        {
          callback.renderInfoReceived(arguments.getInt(0), arguments.getInt(
            1));
        }
      }
    });
  }
  public void calculate(String selector)
  {
    callFunction("calculate", selector);
  }

  @Override
  protected Class<? extends ClientConnector> getSupportedParentType()
  {
    return Component.class;
  }
  
  public static interface Callback
  {
    /**
     * Called whenever new data is available.
     * 
     * @param width The rendered width of the component
     * @param height The rendered height of the component
     */
    public void renderInfoReceived(int width, int height);
  }
  
}
