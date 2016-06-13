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

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDANNOS;
import static annis.model.AnnisConstants.FEAT_MATCHEDIDS;
import static annis.model.AnnisConstants.FEAT_MATCHEDNODE;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.ws.rs.core.Response;

import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.util.concurrent.FutureCallback;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import annis.CommonHelper;
import annis.gui.docbrowser.DocBrowserController;
import annis.gui.util.ANNISFontIcon;
import annis.libgui.AnnisUser;
import annis.libgui.Background;
import annis.libgui.Helper;
import annis.libgui.IDGenerator;
import annis.libgui.InstanceConfig;
import annis.libgui.LoginDataLostException;
import annis.libgui.visualizers.VisualizerInput;
import annis.libgui.visualizers.VisualizerPlugin;
import annis.service.objects.Match;
import annis.service.objects.Visualizer;
import annis.visualizers.htmlvis.HTMLVis;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Theme("annis_embeddedvis")
@Push(value = PushMode.AUTOMATIC, transport = Transport.LONG_POLLING)
public class EmbeddedVisUI extends CommonUI
{

  private static final Logger log = LoggerFactory.getLogger(EmbeddedVisUI.class);

  public static final String URL_PREFIX = "/embeddedvis";

  public static final String KEY_PREFIX = "embedded_";

  public static final String KEY_SALT = KEY_PREFIX + "salt";

  public static final String KEY_NAMESPACE = KEY_PREFIX + "ns";

  public static final String KEY_SEARCH_INTERFACE = KEY_PREFIX + "interface";

  public static final String KEY_BASE_TEXT = KEY_PREFIX + "base";

  public static final String KEY_MATCH = KEY_PREFIX + "match";

  public static final String KEY_INSTANCE = KEY_PREFIX + "instance";

  public EmbeddedVisUI()
  {
    super(URL_PREFIX);
  }

  @Override
  protected void init(VaadinRequest request)
  {
    super.init(request);

    String rawPath = request.getPathInfo();
    List<String> splittedPath = new LinkedList<>();
    if (rawPath != null)
    {
      rawPath = rawPath.substring(URL_PREFIX.length());
      splittedPath = Splitter.on("/").omitEmptyStrings().trimResults().limit(
        3).splitToList(rawPath);
    }

    if (splittedPath.size() == 1)
    {
      // a visualizer definition which get the results from a remote salt file
      String saltUrl = request.getParameter(KEY_SALT);
      if (saltUrl == null)
      {
        displayGeneralHelp();
      }
      else
      {
        generateVisFromRemoteURL(splittedPath.get(0), saltUrl, request.
          getParameterMap());
      }
    }
    else if (splittedPath.size() >= 3)
    {
      // a visualizer definition visname/corpusname/documentname
      if ("htmldoc".equals(splittedPath.get(0)))
      {
        showHtmlDoc(splittedPath.get(1), splittedPath.get(2), request.
          getParameterMap());
      }
      else
      {
        displayMessage("Unknown visualizer \"" + splittedPath.get(0) + "\"",
          "Only \"htmldoc\" is supported yet.");
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

  private void generateVisFromRemoteURL(final String visName, final String rawUri,
    Map<String, String[]> args)
  {
    try
    {
      // find the matching visualizer
      final VisualizerPlugin visPlugin = this.getVisualizer(visName);
      if (visPlugin == null)
      {
        displayMessage("Unknown visualizer \"" + visName + "\"",
          "This ANNIS instance does not know the given visualizer.");
        return;
      }

      URI uri = new URI(rawUri);
      // fetch content of the URI
      Client client = null;
      AnnisUser user = Helper.getUser();
      if (user != null)
      {
        client = user.getClient();
      }
      if (client == null)
      {
        client = Helper.createRESTClient();
      }
      final WebResource saltRes = client.resource(uri);

      displayLoadingIndicator();
      
      // copy the arguments for using them later in the callback
      final Map<String, String[]> argsCopy = new LinkedHashMap<>(args);
      
      Background.runWithCallback(new Callable<SaltProject>()
      {
        @Override
        public SaltProject call() throws Exception
        {
          return saltRes.get(SaltProject.class);
        }
      }, new FutureCallback<SaltProject>()
      {
        @Override
        public void onFailure(Throwable t)
        {
          displayMessage("Could not query the result.", t.getMessage());
        }
        
        @Override
        public void onSuccess(SaltProject p)
        {
          // TODO: allow to display several visualizers when there is more than one document
          SCorpusGraph firstCorpusGraph = null;
          SDocument doc = null;
          if (p.getCorpusGraphs() != null && !p.getCorpusGraphs().isEmpty())
          {
            firstCorpusGraph = p.getCorpusGraphs().get(0);
            if (firstCorpusGraph.getDocuments() != null
              && !firstCorpusGraph.getDocuments().isEmpty())
            {
              doc = firstCorpusGraph.getDocuments().get(0);
            }
          }
          if (doc == null)
          {
            displayMessage("No documents found in provided URL.", "");
            return;
          }

          if (argsCopy.containsKey(KEY_INSTANCE))
          {
            Map<String, InstanceConfig> allConfigs = loadInstanceConfig();
            InstanceConfig newConfig = allConfigs.get(argsCopy.get(KEY_INSTANCE)[0]);
            if (newConfig != null)
            {
              setInstanceConfig(newConfig);
            }
          }
          // now it is time to load the actual defined instance fonts
          loadInstanceFonts();

          // generate the visualizer
          VisualizerInput visInput = new VisualizerInput();
          visInput.setDocument(doc);
          if (getInstanceConfig() != null && getInstanceConfig().getFont() != null)
          {
            visInput.setFont(getInstanceFont());
          }
          Properties mappings = new Properties();
          for (Map.Entry<String, String[]> e : argsCopy.entrySet())
          {
            if (!KEY_SALT.equals(e.getKey()) && e.getValue().length > 0)
            {
              mappings.put(e.getKey(), e.getValue()[0]);
            }
          }
          visInput.setMappings(mappings);
          String[] namespace = argsCopy.get(KEY_NAMESPACE);
          if (namespace != null && namespace.length > 0)
          {
            visInput.setNamespace(namespace[0]);
          }
          else
          {
            visInput.setNamespace(null);
          }

          String baseText = null;
          if (argsCopy.containsKey(KEY_BASE_TEXT))
          {
            String[] value = argsCopy.get(KEY_BASE_TEXT);
            if (value.length > 0)
            {
              baseText = value[0];
            }
          }

          List<SNode> segNodes = CommonHelper.getSortedSegmentationNodes(
            baseText,
            doc.getDocumentGraph());

          if (argsCopy.containsKey(KEY_MATCH))
          {
            String[] rawMatch = argsCopy.get(KEY_MATCH);
            if (rawMatch.length > 0)
            {
              // enhance the graph with match information from the arguments
              Match match = Match.parseFromString(rawMatch[0]);
              Helper.addMatchToDocumentGraph(match, doc);
            }
          }

          Map<String, String> markedColorMap = new HashMap<>();
          Map<String, String> exactMarkedMap
            = Helper.calculateColorsForMarkedExact(doc);
          Map<String, Long> markedAndCovered = Helper.
            calculateMarkedAndCoveredIDs(doc, segNodes, baseText);
          Helper.calulcateColorsForMarkedAndCovered(doc, markedAndCovered,
            markedColorMap);
          visInput.setMarkedAndCovered(markedAndCovered);
          visInput.setMarkableMap(markedColorMap);
          visInput.setMarkableExactMap(exactMarkedMap);
          visInput.setContextPath(Helper.getContext());
          String template = Helper.getContext()
            + "/Resource/" + visName + "/%s";
          visInput.setResourcePathTemplate(template);
          visInput.setSegmentationName(baseText);
          // TODO: which other thing do we have to provide?

          Component c = visPlugin.createComponent(visInput, null);
          // add the styles
          c.addStyleName("corpus-font");
          c.addStyleName("vis-content");

          Link link = new Link();
          link.setCaption("Show in ANNIS search interface");
          link.setIcon(ANNISFontIcon.LOGO);
          link.setVisible(false);
          link.addStyleName("dontprint");
          link.setTargetName("_blank");
          if (argsCopy.containsKey(KEY_SEARCH_INTERFACE))
          {
            String[] interfaceLink = argsCopy.get(KEY_SEARCH_INTERFACE);
            if (interfaceLink.length > 0)
            {
              link.setResource(new ExternalResource(interfaceLink[0]));
              link.setVisible(true);
            }
          }
          VerticalLayout layout = new VerticalLayout(link, c);
          layout.setComponentAlignment(link, Alignment.TOP_LEFT);
          layout.setSpacing(true);
          layout.setMargin(true);

          setContent(layout);
          
          IDGenerator.assignID(link);
        }

      });

    }
    catch (URISyntaxException ex)
    {
      displayMessage("Invalid URL", "The provided URL is malformed:<br />"
        + ex.getMessage());
    }
    catch (LoginDataLostException ex)
    {
      displayMessage("LoginData Lost",
        "No login data available any longer in the session:<br /> "
        + ex.getMessage());
    }
    catch (UniformInterfaceException ex)
    {
      if (ex.getResponse().getStatus() == Response.Status.FORBIDDEN.
        getStatusCode())
      {
        displayMessage("Corpus access forbidden",
          "You are not allowed to access this corpus. "
          + "Please login at the <a target=\"_blank\" href=\"" + Helper.
          getContext() + "\">main application</a> first and then reload this page.");
      }
      else
      {
        displayMessage("Service error", ex.getMessage());
      }
    }
    catch (ClientHandlerException ex)
    {
      displayMessage(
        "Could not generate the visualization because the ANNIS service reported an error.",
        ex.getMessage());
    }
    catch (Throwable ex)
    {
      displayMessage("Could not generate the visualization.",
        ex.getMessage() == null ? ("An unknown error of type "
          + ex.getClass().getSimpleName()) + " occured." : ex.getMessage());
    }
  }

  private void showHtmlDoc(String corpus, String doc, Map<String, String[]> args)
  {
    // do nothing for empty fragments
    if (args == null || args.isEmpty())
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
        input = DocBrowserController.createInput(corpus, doc, visConfig, false,
          null);
        //create components, put in a panel
        Panel viszr = visualizer.createComponent(input, null);

        // Set the panel as the content of the UI
        setContent(viszr);
      }
      catch (UniformInterfaceException ex)
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
  
  private void displayLoadingIndicator()
  {
    VerticalLayout layout = new VerticalLayout();
    
    layout.addStyleName("v-app-loading");   
    layout.setSizeFull();
      
    setContent(layout);
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
