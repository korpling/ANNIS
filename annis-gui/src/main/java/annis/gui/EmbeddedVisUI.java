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

import annis.CommonHelper;
import annis.gui.docbrowser.DocBrowserController;
import annis.gui.util.ANNISFontIcon;
import annis.libgui.AnnisUser;
import annis.libgui.Helper;
import annis.libgui.LoginDataLostException;
import annis.libgui.visualizers.VisualizerInput;
import annis.libgui.visualizers.VisualizerPlugin;
import annis.service.objects.Visualizer;
import annis.visualizers.htmlvis.HTMLVis;
import com.google.common.base.Splitter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.annotations.Theme;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Theme("annis_embeddedvis")
public class EmbeddedVisUI extends CommonUI
{
  private static final Logger log = LoggerFactory.getLogger(EmbeddedVisUI.class);
  
  public static final String URL_PREFIX = "/embeddedvis";
  
  public static final String KEY_PREFIX = "embedded_";
  public static final String KEY_SALT =  KEY_PREFIX +  "salt";
  public static final String KEY_NAMESPACE =  KEY_PREFIX +  "ns";
  public static final String KEY_SEARCH_INTERFACE =  KEY_PREFIX +  "interface";
  public static final String KEY_BASE_TEXT =  KEY_PREFIX +  "base";

  @Override
  protected void init(VaadinRequest request)
  {
    super.init(request);
    
    String rawPath = request.getPathInfo();
    List<String> splittedPath = new LinkedList<>();
    if(rawPath != null)
    {
      rawPath = rawPath.substring(URL_PREFIX.length());
      splittedPath = Splitter.on("/").omitEmptyStrings().trimResults().limit(
        3).splitToList(rawPath);
    }
    
    if(splittedPath.size() == 1)
    {
      // a visualizer definition which get the results from a remote salt file
      String saltUrl = request.getParameter(KEY_SALT);
      if(saltUrl == null)
      {
        displayGeneralHelp();
      }
      else
      {
        generateVisFromRemoteURL(splittedPath.get(0), saltUrl, request.getParameterMap());
      }
    }
    else if(splittedPath.size() >= 3)
    {
      // a visualizer definition visname/corpusname/documentname
      if("htmldoc".equals(splittedPath.get(0)))
      {
        showHtmlDoc(splittedPath.get(1), splittedPath.get(2), request.getParameterMap());
      }
      else
      {
        displayMessage("Unknown visualizer \"" + splittedPath.get(0) + "\"", "Only \"htmldoc\" is supported yet.");
      }
    }
    else
    {
      displayGeneralHelp();
    }
    addStyleName("loaded-embedded-vis");
  }
  
  private void displayGeneralHelp()
  {
    displayMessage("Path not complete",  
      "You have to specify what visualizer to use and which document of which corpus you want to visualizer by giving the correct path:<br />"
      + "<code>http://example.com/annis/embeddedvis/&lt;vis&gt;/&lt;corpus&gt;/&lt;doc&gt;</code>"
      + "<ul>"
      + "<li><code>vis</code>: visualizer name (currently only \"htmldoc\" is supported)</li>"
      + "<li><code>corpus</code>: corpus name</li>"
      + "<li><code>doc</code>: name of the document to visualize</li>"
      + "</ul>");
  }
  
  private void generateVisFromRemoteURL(String visName, String rawUri, Map<String, String[]> args)
  {
    try
    {
      // find the matching visualizer
      VisualizerPlugin visPlugin = this.getVisualizer(visName);
      if(visPlugin == null)
      {
        displayMessage("Unknown visualizer \"" + visName + "\"", 
          "This ANNIS instance does not know the given visualizer.");
        return;
      }
      
      URI uri = new URI(rawUri);
      // fetch content of the URI
      Client client = null;
      AnnisUser user = Helper.getUser();
      if(user != null)
      {
        client = user.getClient();
      }
      if(client == null)
      {
        client = Helper.createRESTClient();
      }
      WebResource saltRes = client.resource(uri);
      SaltProject p = saltRes.get(SaltProject.class);
      // TODO: allow to display several visualizers when there is more than one document

      SCorpusGraph firstCorpusGraph = null;
      SDocument doc = null;
      if(p.getSCorpusGraphs() != null && !p.getSCorpusGraphs().isEmpty())
      {
        firstCorpusGraph = p.getSCorpusGraphs().get(0);
        if(firstCorpusGraph.getSDocuments() != null && 
          !firstCorpusGraph.getSDocuments().isEmpty())
        {
          doc = firstCorpusGraph.getSDocuments().get(0);
        }
      }
      if(doc == null)
      {
        displayMessage("No documents found in provided URL.", "");
        return;
      }
      // generate the visualizer
      VisualizerInput visInput = new VisualizerInput();
      visInput.setDocument(doc);
      Properties mappings = new Properties();
      for(Map.Entry<String, String[]> e : args.entrySet())
      {
        if(!KEY_SALT.equals(e.getKey()) && e.getValue().length > 0)
        {
          mappings.put(e.getKey(), e.getValue()[0]);
        }
      }
      visInput.setMappings(mappings);
      String[] namespace = args.get(KEY_NAMESPACE);
      if(namespace != null && namespace.length > 0)
      {
        visInput.setNamespace(namespace[0]);
      }
      
      String segmentation = null;
      if(args.containsKey(KEY_BASE_TEXT))
      {
        String[] value = args.get(KEY_BASE_TEXT);
        if(value.length > 0)
        {
          segmentation = value[0];
        }
      }
      
      List<SNode> segNodes = CommonHelper.getSortedSegmentationNodes(
        segmentation,
        doc.getSDocumentGraph());

      Map<String, String> markedColorMap = new HashMap<>();
      Map<String, Long> markedAndCovered = Helper.calculateMarkedAndCoveredIDs(doc, segNodes, segmentation);
      Helper.calulcateColorsForMarkedAndCovered(doc, markedAndCovered, markedColorMap);
      visInput.setMarkedAndCovered(markedAndCovered);
      visInput.setMarkableMap(markedColorMap);
      
      // TODO: which other thing do we have to provide?
      
      Component c = visPlugin.createComponent(visInput, null);
      
      Link link = new Link();
      link.setCaption("Show in ANNIS search interface");
      link.setIcon(ANNISFontIcon.LOGO);
      link.setVisible(false);
      if(args.containsKey(KEY_SEARCH_INTERFACE))
      {
        String[] interfaceLink = args.get(KEY_SEARCH_INTERFACE);
        if(interfaceLink.length > 0)
        {
          link.setResource(new ExternalResource(interfaceLink[0]));
          link.setVisible(true);
        }
      }
      VerticalLayout layout = new VerticalLayout(link, c);
      layout.setComponentAlignment(link, Alignment.TOP_LEFT);
      setContent(layout);
      
    }
    catch (URISyntaxException ex)
    {
      displayMessage("Invalid URL", "The provided URL is malformed:<br />"
        + ex.getMessage());
    }
    catch (LoginDataLostException ex)
    {
      displayMessage("LoginData Lost", "No login data available any longer in the session:<br /> "
        + ex.getMessage());
    }
  }
  
  
  private void showHtmlDoc(String corpus, String doc, Map<String, String[]> args)
  {
    // do nothing for empty fragments
    if (args == null || args.isEmpty() )
    {
      return;
    }

    if (args.get("config") != null && args.get("config").length > 0)
    {
      String config = args.get("config")[0];

      // get input parameters
      HTMLVis visualizer;
      visualizer = new HTMLVis();

      VisualizerInput input;
      Visualizer visConfig;
      visConfig = new Visualizer();
      visConfig.setDisplayName(" ");
      visConfig.setMappings("config:" + config);
      visConfig.setNamespace(null);
      visConfig.setType("htmldoc");

      //create input
      try
      {
        input = DocBrowserController.createInput(corpus, doc, visConfig, false, null);
        //create components, put in a panel
        Panel viszr = visualizer.createComponent(input, null);

        // Set the panel as the content of the UI
        setContent(viszr);
      }
      catch(UniformInterfaceException ex)
      {
        displayMessage("Could not query document", "error was \"" 
          + ex.getMessage() + "\" (detailed error is available in the server log-files)");
        log.error("Could not get document for embedded visualizer", ex);
      }

    }
    else
    {
      displayMessage("Missing required argument for visualizer \"htmldoc\"",
        "The following arguments are required:"
        + "<ul>"
        + "<li><code>config</code>: the internal config file to use (same as <a href=\"http://korpling.github.io/ANNIS/doc/classannis_1_1visualizers_1_1htmlvis_1_1HTMLVis.html\">\"config\" mapping parameter)</a></li>"
        + "</ul>");
    }
  }
  
  private void displayMessage(String header, String content)
  {
    Label label = new Label(
      "<h1>" + header + "</h1>" + "<div>" + content + "</div>",
      ContentMode.HTML);
    label.setSizeFull();
    setContent(label);
  }
  
}
