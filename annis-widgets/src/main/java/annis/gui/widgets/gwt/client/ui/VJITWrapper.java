/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.widgets.gwt.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;
import com.vaadin.client.ValueMap;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class VJITWrapper extends Widget implements Paintable
{

  private Document doc = Document.get();


  private final String elementID;

  // javascript overlay object for the visualization
  private JITVisualization visualization;

  // the json data for the visualization
  private JSONObject jsonData;

  private JITConf config;

  // some css properties
  protected static final String background = "#ECF0F6";

  protected static final String width = "900px";

  protected static final String height = "600px";

  public VJITWrapper()
  {
    super();

    // build the html id
    elementID =  Document.get().createUniqueId();

    // init container
    DivElement wrapper = doc.createDivElement();
    DivElement container = wrapper.appendChild(doc.createDivElement());
    setElement(wrapper);

    container.setAttribute("id", "container_" + elementID);
    wrapper.setAttribute("id", elementID);
  }

  @Override
  public void updateFromUIDL(UIDL uidl, ApplicationConnection client)
  {

    // This call should be made first.
    // It handles sizes, captions, tooltips, etc. automatically.
    if (client.updateComponent(this, uidl, true))
    {
      // If client.updateComponent returns true there has been no changes and we
      // do not need to update anything.
      return;
    }


    if (uidl.hasAttribute("visData"))
    {

      jsonData = parseStringToJSON(uidl.getStringAttribute("visData"));

      // setup config for visualization
      if (uidl.hasAttribute("mappings"))
      {
        setupConfig(uidl.getMapAttribute("mappings"));
      }
      else
      {
        setupConfig();
      }

      if (visualization == null)
      {
        visualization = visualizationInit(config.getJavaScriptObject());
      }

      if (jsonData != null)
      {
        visualization.render();
      }
      else
      {
        GWT.log("jsonData are null");
      }
    }
  }

  public native JITVisualization visualizationInit(JavaScriptObject config)/*-{
   return $wnd.$viz(config);
   }-*/;

  public JSONObject parseStringToJSON(String jsonString)
  {
    JSONObject json = null;

    try
    {
      json = JSONParser.parseStrict(jsonString).isObject();
    }
    catch (Exception ex)
    {
      GWT.log("this json " + jsonString + " is not parsed", ex);
    }

    return json;
  }

  private void setupConfig(ValueMap mappings)
  {
    if (config == null)
    {
      config = new JITConf();
    }

    for (String key : mappings.getKeySet())
    {
      config.setProperty(key, mappings.getString(key));
    }

    setupConfig();
  }

  private void setupConfig()
  {
    if (config == null)
    {
      config = new JITConf();
    }

    config.setProperty("json", jsonData);
    config.setProperty("wrapper", elementID);
    config.setProperty("container", "container_" + elementID);
  }
}
