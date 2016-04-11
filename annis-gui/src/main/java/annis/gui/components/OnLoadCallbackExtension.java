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

import org.json.JSONException;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.ui.JavaScriptFunction;

import elemental.json.JsonArray;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@JavaScript(
{
  "vaadin://jquery.js", "onloadcallback.js"
})
public class OnLoadCallbackExtension extends AbstractJavaScriptExtension
{

  private AbstractClientConnector target;

  public OnLoadCallbackExtension(Callback c)
  {
    this(c, 250);
  }

  public OnLoadCallbackExtension(final Callback c, final int recallDelay)
  {
    addFunction("loaded", new JavaScriptFunction()
    {
      @Override
      public void call(JsonArray arguments) throws JSONException
      {
        if (c != null)
        {
          boolean handled = c.onCompononentLoaded(target);
          if (!handled)
          {
            callFunction("requestRecall", recallDelay);
          }
        }
      }
    });
  }

  @Override
  public void extend(AbstractClientConnector target)
  {
    super.extend(target);
    this.target = target;
  }

  /**
   * A callback for {@link OnLoadCallbackExtension}.
   */
  public static interface Callback
  {

    /**
     * Called whenever the extended component was rendered. If you want to get a
     * repeated callback (e.g. because you are waiting for a longer process to
     * complete it's calculation) you can return "false".
     *
     * @param source
     * @return True if handled, if false the callback will be called again after
     * a certain time span.
     */
    public boolean onCompononentLoaded(AbstractClientConnector source);
  }
}
