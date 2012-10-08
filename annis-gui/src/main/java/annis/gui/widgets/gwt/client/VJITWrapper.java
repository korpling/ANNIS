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
  // javascript overlay object for the visualization
  private JITVisualization visualization;
  // the json data for the visualization
  private JSONObject jsonData;
  private JITConf config;
  // some css properties
  protected final String background = "#ECF0F6";
  protected final String width = "900px";
  protected final String height = "600px";

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

    // save this reference for reading inner fields in functions
    var saveThis = this;

    var st = {};

    //This method is called on DOM label creation.
    //Use this method to add event handlers and styles to
    //your node.
    config['onCreateLabel'] = function(label, node){

      label.id = node.id;

      // put the sentences into the label
      console.log(node.data);
      if(node.data.sentence){
        label.innerHTML = node.data.sentence;
      }
      else{
        label.innerHTML = node.name;
      }

      label.onclick = function(){
        st.onClick(node.id);
      };

      //set label styles
      var style = label.style;
      style.width = 60 + 'px';
      style.height = 17 + 'px';
      style.cursor = 'pointer';
      style.color = '#333';
      style.fontSize = '0.8em';
      style.textAlign= 'center';
      style.paddingTop = '3px';
    };

    //This method is called right before plotting
    //a node. It's useful for changing an individual node
    //style properties before plotting it.
    //The data properties prefixed with a dollar
    //sign will override the global node style properties.
    config['onBeforePlotNode'] = function(node){
      //add some color to the nodes in the path between the
      //root node and the selected node.
      if (node.selected) {
        node.data.$color = "#ff7";
      }
      else {
        delete node.data.$color;
        //if the node belongs to the last plotted level
        if(!node.anySubnode("exist")) {
          //count children number
          var count = 0;
          node.eachSubnode(function(n) { count++; });
          //assign a node color based on
          //how many children it has
          node.data.$color = ['#aaa', '#baa', '#caa', '#daa', '#eaa', '#faa'][count];
        }
      }
      // if node contains a sentence, set it too the background color
      if (node.data.sentence)
      {
        node.data.$color = saveThis.@annis.gui.widgets.gwt.client.VJITWrapper::background;
      }
    };

    //This method is called right before plotting
    //an edge. It's useful for changing an individual edge
    //style properties before plotting it.
    //Edge data proprties prefixed with a dollar sign will
    //override the Edge global style properties.
    config['onBeforePlotLine'] = function(adj){
      if (adj.nodeFrom.selected && adj.nodeTo.selected) {
        adj.data.$color = "#eed";
        adj.data.$lineWidth = 3;
      }
      else {
        delete adj.data.$color;
        delete adj.data.$lineWidth;
      }
    };

    //set animation transition type
    config['transition'] = $wnd.$jit.Trans.Quart.easeInOut;

    st = new $wnd.$jit.ST(config);
    return st;
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
    config.setProperty("orientation", "top");
    config.setProperty("levelsToShow", 3);



    // node config
    JITConf node = new JITConf();
    node.setProperty("overridable", true);
    node.setProperty("width", 60);
    node.setProperty("height", 20);
    node.setProperty("color", "#ccc");
    config.setProperty("Node", node);

    JITConf edge = new JITConf();
    edge.setProperty("type", "bezier");
    edge.setProperty("overridable", true);
    config.setProperty("Edge", edge);

  }
}
