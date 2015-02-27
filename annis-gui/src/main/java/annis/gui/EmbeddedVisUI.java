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
package annis.gui;

import annis.gui.docbrowser.DocBrowserController;
import annis.libgui.Helper;
import annis.libgui.visualizers.VisualizerInput;
import annis.service.objects.Visualizer;
import annis.visualizers.htmlvis.HTMLVis;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class EmbeddedVisUI extends CommonUI
{

  @Override
  protected void init(VaadinRequest request)
  {
    super.init(request);
    evalParams(request.getParameterMap());
  }
  
  
  private void evalParams(Map<String, String[]> args)
  {
    ////example local testing query url
    ////corpus=shenoute.a22, vis=htmldoc, doc=YA421-428, sty=“config:dipl"
    //http://localhost:8084/annis-gui/embeddedvis#_c=c2hlbm91dGUuYTIy&vis=htmldoc&_doc=WUE0MjEtNDI4&_sty=ZGlwbA==

    // do nothing for empty fragments
    if (args == null || args.isEmpty() )
    {
      return;
    }

    //get other parameters: -visualizer(htmldoc),-document name(doc),-style(sty) 
    if (args.get("vis") != null && args.get("vis").length > 0
      && args.get("doc") != null && args.get("doc").length > 0
      && args.get("sty") != null && args.get("sty").length > 0
      && args.get("c") != null && args.get("c").length > 0)
    {
      String vis = args.get("vis")[0];
      String doc = args.get("doc")[0];
      String sty = args.get("sty")[0];
      String corpus = args.get("c")[0];

      // get input parameters
      HTMLVis visualizer;
      visualizer = new HTMLVis();

      VisualizerInput input;
      Visualizer config;
      config = new Visualizer();
      config.setDisplayName(" ");
      config.setMappings("config:" + sty);
      config.setNamespace(null);
      config.setType(vis);

      //create input    
      input = DocBrowserController.createInput(corpus, doc, config, false, null);
      //create components, put in a panel
      Panel viszr = visualizer.createComponent(input, null);

      // Set the panel as the content of the UI
      setContent(viszr);

    }
    else
    {
      setContent(new Label("<h1>Missing required argument</h1>"
        + "<p>"
        + "The following arguments are required:"
        + "<ul>"
        + "<li><code>vis</code>: visualizer name (currently only \"htmldoc\" is supported)</li>"
        + "<li><code>c</code>: corpus name</li>"
        + "<li><code>doc</code>: name of the document to visualize</li>"
        + "</ul>"
        + "</p>", 
        ContentMode.HTML ));
    }
  }
  
}
