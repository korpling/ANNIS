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
package annis.gui.components;

import java.net.URI;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.VerticalLayout;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elemental.json.JsonArray;
import elemental.json.JsonType;
import elemental.json.JsonValue;

/**
 * Embedds a single HTML page and adds navigation to it's headers (if they have
 * an id).
 *
 * This is e.g. useful for documentation.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class StatefulBrowserComponent extends VerticalLayout
{

  private final static Logger log = LoggerFactory.getLogger(
    StatefulBrowserComponent.class);

  private final IFrameComponent iframe = new IFrameComponent();

  public StatefulBrowserComponent(URI externalURI)
  {
    iframe.setSizeFull();

    addComponent(iframe);

    setExpandRatio(iframe, 1.0f);

    setSource(externalURI);
  }


  private void setSource(URI externalURI)
  {
    iframe.getState().setSource(externalURI.toASCIIString());
  }



  @JavaScript(
    {
      "vaadin://jquery.js", "statefulbrowsercomponent.js"
    })
  private class IFrameComponent extends AbstractJavaScriptComponent
  {

    public IFrameComponent()
    {
      addFunction("urlChanged", new JavaScriptFunction()
      {

        @Override
        public void call(JsonArray arguments) throws JSONException
        {
          getState().setSource(arguments.get(0).asString());
        }
      });
      addFunction("scrolled", new JavaScriptFunction()
      {

        @Override
        public void call(JsonArray arguments) throws JSONException
        {
          getState().setLastScrollPos((int) arguments.getNumber(0));
        }
      });
    }

    
    @Override
    public final IframeState getState()
    {
      return (IframeState) super.getState();
    }
  }
}
