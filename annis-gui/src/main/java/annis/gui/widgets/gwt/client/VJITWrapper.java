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
package annis.gui.widgets.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.*;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class VJITWrapper extends Widget implements Paintable
{

  private Document doc = Document.get();
  /**
   * The infovis has to be injected to an html element, so we create an an
   * unique element identifier from this count value.
   */
  private static int count = 0;
  private final String elementID;
  private JITVisualization visualization;
  // the json data for the visualization
  private JSONObject jsonData;
  private JITConf config;
  // some css properties
  protected String background = "#ECF0F6";
  protected String width = "900px";
  protected String height = "600px";

  public VJITWrapper()
  {
    super();

    // build the html id
    elementID = "jit_wrapper_" + count;

    // count the JITWrapper objects
    count++;

    // init container
    DivElement container = doc.createDivElement();
    DivElement wrapper = container.appendChild(doc.createDivElement());
    setElement(container);

    container.setAttribute("style", "background:" + background + "; width:" + width + "; height:" + height);
    container.setAttribute("id", "container_" + elementID);
    wrapper.setAttribute("style", "background:" + background + "; width:" + width + "; height:" + height);
    wrapper.setAttribute("id", elementID);

    // initialize some browser compatibilies of jit
    setupJIT();

    // setup config for visualization
    setupConfig();
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
     
      if (visualization == null)
      {
        visualization = visualizationInit(config.getJavaScriptObject());
      }

      if (jsonData != null)
      {          
        visualization.loadJSON(jsonData.getJavaScriptObject());
        visualization.compute();
        visualization.onClick(visualization);
      }
      else
      {
        GWT.log("jsonData are null");
      }
    }
  }

  /**
   * Some internal jit setup stuff, copied from examples script
   */
  private native void setupJIT() /*-{
   var labelType, useGradients, nativeTextSupport, animate;
   (function() {
   var ua = navigator.userAgent,
   iStuff = ua.match(/iPhone/i) || ua.match(/iPad/i),
   typeOfCanvas = typeof HTMLCanvasElement,
   nativeCanvasSupport = (typeOfCanvas == 'object' || typeOfCanvas == 'function'),
   textSupport = nativeCanvasSupport 
   && (typeof document.createElement('canvas').getContext('2d').fillText == 'function');
   //I'm setting this based on the fact that ExCanvas provides text support for IE
   //and that as of today iPhone/iPad current text support is lame
   labelType = (!nativeCanvasSupport || (textSupport && !iStuff))? 'Native' : 'HTML';
   nativeTextSupport = labelType == 'Native';
   useGradients = nativeCanvasSupport;
   animate = !(iStuff || !nativeCanvasSupport);
   })();
   }-*/;

  public native JITVisualization visualizationInit(JavaScriptObject config)/*-{   
    
    // add some methods too config    
    config['onCreateLabel'] = function(label, node)
    {
      //add some styles to the node label
      var style = label.style;
      label.id = node.id;
      label.innerHTML = node.name;
    }

    //init Spacetree
    //Create a new ST instance
    return new $wnd.$jit.ST(config);    
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

  private void setupConfig()
  {
    config = new JITConf();
    config.setProperty("injectInto", elementID);

    JITConf node = new JITConf();
    node.setProperty("overridable", true);
    node.setProperty("width", "auto");
    node.setProperty("height", "auto");
    node.setProperty("color", "#ccc");
    config.setProperty("node", node);
  }
}
