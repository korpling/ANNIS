/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.util.concurrent.FutureCallback;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletContext;
import javax.xml.stream.XMLStreamException;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.api.model.SubgraphWithContext;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.corpus_tools.annis.gui.docbrowser.DocBrowserController;
import org.corpus_tools.annis.gui.graphml.DocumentGraphMapper;
import org.corpus_tools.annis.gui.objects.Match;
import org.corpus_tools.annis.gui.objects.RawTextWrapper;
import org.corpus_tools.annis.gui.security.AuthenticationSuccessListener;
import org.corpus_tools.annis.gui.util.ANNISFontIcon;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.annis.gui.visualizers.VisualizerPlugin;
import org.corpus_tools.annis.gui.visualizers.htmlvis.HTMLVis;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@Theme("annis_embeddedvis")
@Push(value = PushMode.AUTOMATIC, transport = Transport.LONG_POLLING)
@SpringUI(path = "/embeddedvis/*")
@Widgetset("org.corpus_tools.annis.gui.widgets.gwt.AnnisWidgetSet")
public class EmbeddedVisUI extends CommonUI {

  private class GraphMLLoaderCallback implements FutureCallback<File> {

    private final String corpusNodeId;
    private final VisualizerPlugin visPlugin;
    private final Map<String, String[]> args;

    GraphMLLoaderCallback(String corpusNodeId, VisualizerPlugin visPlugin,
        Map<String, String[]> args) {
      this.visPlugin = visPlugin;
      this.args = args;
      this.corpusNodeId = corpusNodeId;
    }

    @Override
    public void onFailure(Throwable t) {
      log.error("Could not query graph for embedded visualization.", t);
      displayMessage("Could not query the result.", t.getMessage());
    }

    @Override
    public void onSuccess(File result) {
      try {
        final SaltProject p = SaltFactory.createSaltProject();
        SCorpusGraph cg = p.createCorpusGraph();
        org.eclipse.emf.common.util.URI docURI =
            org.eclipse.emf.common.util.URI.createURI("salt:/" + corpusNodeId);
        SDocument doc = cg.createDocument(docURI);
        SDocumentGraph docGraph = DocumentGraphMapper.map(result);
        doc.setDocumentGraph(docGraph);

        generateVisualizerFromDocument(doc, args, visPlugin);
      } catch (IOException | XMLStreamException ex) {
        log.error("Could not parse GraphML", ex);
        displayMessage("Could not parse GraphML", ex.toString());
      }
    }
  }

  /**
   * 
   */
  private static final long serialVersionUID = 3171707930515328817L;

  private static final Logger log = LoggerFactory.getLogger(EmbeddedVisUI.class);

  public static final String URL_PREFIX = "/embeddedvis";

  public static final String KEY_PREFIX = "embedded_";

  private static final String KEY_SALT = KEY_PREFIX + "salt";

  public static final String KEY_NAMESPACE = KEY_PREFIX + "ns";

  public static final String KEY_SEARCH_INTERFACE = KEY_PREFIX + "interface";

  public static final String KEY_BASE_TEXT = KEY_PREFIX + "base";

  public static final String KEY_SEGMENTATION = KEY_PREFIX + "segmentation";

  public static final String KEY_MATCH = KEY_PREFIX + "match";

  public static final String KEY_FULLTEXT = KEY_PREFIX + "fulltext";

  public static final String KEY_LEFT = KEY_PREFIX + "left";
  public static final String KEY_RIGHT = KEY_PREFIX + "right";

  public static final String KEY_INSTANCE = KEY_PREFIX + "instance";

  @Autowired
  private List<VisualizerPlugin> visualizers;


  @Autowired
  private transient ServletContext servletContext;


  @Autowired(required = false)
  private transient OAuth2ClientProperties oauth2Clients;

  @Autowired
  private UIConfig config;

  private final AuthenticationSuccessListener authListener;

  @Autowired
  public EmbeddedVisUI(ServiceStarter serviceStarter, AuthenticationSuccessListener authListener) {
    super(URL_PREFIX, serviceStarter, authListener);
    this.authListener = authListener;
  }

  private void displayGeneralHelp() {
    displayMessage("Path not complete",
        "You have to specify what visualizer to use and which document of which corpus you want to visualizer by giving the correct path:<br />"
            + "<code>http://example.com/annis/embeddedvis/&lt;vis&gt;/&lt;corpus&gt;/&lt;doc&gt;</code>"
            + "<ul>"
            + "<li><code>vis</code>: visualizer name (currently only \"htmldoc\" is supported)</li>"
            + "<li><code>corpus</code>: corpus name</li>"
            + "<li><code>doc</code>: name of the document to visualize</li>" + "</ul>");
  }

  private void displayLoadingIndicator() {
    VerticalLayout layout = new VerticalLayout();

    layout.addStyleName("v-app-loading");
    layout.setSizeFull();

    setContent(layout);
  }

  private void displayMessage(String header, String content) {
    Label label =
        new Label("<h1>" + header + "</h1>" + "<div>" + content + "</div>", ContentMode.HTML);
    label.setSizeFull();
    setContent(label);
  }

  private void generateVisFromParameters(final String visName, Map<String, String[]> args) {

    // find the matching visualizer
    final Optional<VisualizerPlugin> visPlugin =
        visualizers.stream().filter(vis -> Objects.equal(vis.getShortName(), visName)).findAny();
    if (!visPlugin.isPresent()) {
      displayMessage("Unknown visualizer \"" + visName + "\"",
          "This ANNIS instance does not know the given visualizer.");
      return;
    }

    ApiClient client = getClient();

    displayLoadingIndicator();

    // Create a subgraph query
    CorporaApi api = new CorporaApi(client);
    Match match = Match.parseFromString(args.get(KEY_MATCH)[0]);
    String corpusNodeId = match.getSaltIDs().get(0);
    List<String> corpusPathDecoded = Helper.getCorpusPath(corpusNodeId, true);
    String toplevelCorpus = corpusPathDecoded.get(0);

    if (args.containsKey(KEY_FULLTEXT)) {
      
      boolean isUsingRawText = visPlugin.get().isUsingRawText();
      String aql = Helper.buildDocumentQuery(corpusNodeId, null, isUsingRawText);

      Background.runWithCallback(
          () -> api.subgraphForQuery(toplevelCorpus, aql, QueryLanguage.AQL,
              isUsingRawText ? AnnotationComponentType.ORDERING : null),
          new GraphMLLoaderCallback(corpusNodeId, visPlugin.get(), args));


    } else {
      SubgraphWithContext subgraphQuery = new SubgraphWithContext();
      subgraphQuery.setLeft(Integer.parseInt(args.get(KEY_LEFT)[0]));
      subgraphQuery.setRight(Integer.parseInt(args.get(KEY_RIGHT)[0]));
      subgraphQuery.setNodeIds(match.getSaltIDs());

      if (args.containsKey(KEY_SEGMENTATION)) {
        subgraphQuery.setSegmentation(args.get(KEY_SEGMENTATION)[0]);
      } else {
        subgraphQuery.setSegmentation(null);
      }
      Background.runWithCallback(
          () -> api.subgraphForNodes(toplevelCorpus, subgraphQuery),
          new GraphMLLoaderCallback(corpusNodeId, visPlugin.get(), args));
    }
  }

  private void generateVisualizerFromDocument(SDocument doc, Map<String, String[]> args,
      VisualizerPlugin visPlugin) {
    if (args.containsKey(KEY_INSTANCE)) {
      Map<String, InstanceConfig> allConfigs = loadInstanceConfig();
      InstanceConfig newConfig = allConfigs.get(args.get(KEY_INSTANCE)[0]);
      if (newConfig != null) {
        setInstanceConfig(newConfig);
      }
    }
    // now it is time to load the actual defined instance fonts
    loadInstanceFonts();

    // generate the visualizer
    VisualizerInput visInput = new VisualizerInput();
    visInput.setDocument(doc);
    if (visPlugin.isUsingRawText()) {
      visInput.setRawText(new RawTextWrapper(doc.getDocumentGraph()));
    }
    if (getInstanceConfig() != null && getInstanceConfig().getFont() != null) {
      visInput.setFont(getInstanceFont());
    }
    Map<String, String> mappings = new LinkedHashMap<>();
    for (Map.Entry<String, String[]> e : args.entrySet()) {
      if (!KEY_SALT.equals(e.getKey()) && e.getValue().length > 0) {
        mappings.put(e.getKey(), e.getValue()[0]);
      }
    }
    visInput.setMappings(mappings);
    String[] namespace = args.get(KEY_NAMESPACE);
    if (namespace != null && namespace.length > 0) {
      visInput.setNamespace(namespace[0]);
    } else {
      visInput.setNamespace(null);
    }

    String baseText = null;
    if (args.containsKey(KEY_BASE_TEXT)) {
      String[] value = args.get(KEY_BASE_TEXT);
      if (value.length > 0) {
        baseText = value[0];
      }
    }

    if (args.containsKey(KEY_MATCH)) {
      String[] rawMatch = args.get(KEY_MATCH);
      if (rawMatch.length > 0) {
        // enhance the graph with match information from the arguments
        Match match = Match.parseFromString(rawMatch[0]);
        Helper.addMatchToDocumentGraph(match, doc.getDocumentGraph());
      }
    }

    List<SNode> segNodes = Helper.getSortedSegmentationNodes(baseText, doc.getDocumentGraph());

    Map<SNode, Long> markedAndCovered = Helper.calculateMarkedAndCovered(doc, segNodes, baseText);
    visInput.setMarkedAndCovered(markedAndCovered);
    visInput.setContextPath(servletContext.getContextPath());
    String template =
        servletContext.getContextPath() + "/Resource/" + visPlugin.getShortName() + "/%s";
    visInput.setResourcePathTemplate(template);
    visInput.setSegmentationName(baseText);
    visInput.setUI(this);

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
    if (args.containsKey(KEY_SEARCH_INTERFACE)) {
      String[] interfaceLink = args.get(KEY_SEARCH_INTERFACE);
      if (interfaceLink.length > 0) {
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

  @Deprecated
  private void generateVisFromRemoteSaltURL(final String visName, final String rawUri,
      Map<String, String[]> args) {
    try {
      // find the matching visualizer
      final Optional<VisualizerPlugin> visPluginOpt =
          visualizers.stream().filter(vis -> Objects.equal(vis.getShortName(), visName)).findAny();

      if (visPluginOpt.isPresent()) {
        VisualizerPlugin visPlugin = visPluginOpt.get();
        URI uri = new URI(rawUri);
        // fetch content of the URI
        ApiClient client = Helper.getClient(this);

        displayLoadingIndicator();

        // copy the arguments for using them later in the callback
        final Map<String, String[]> argsCopy = new LinkedHashMap<>(args);
        Request request = new okhttp3.Request.Builder().url(uri.toASCIIString()).build();

        Background.runWithCallback(() -> client.getHttpClient().newCall(request).execute(),
            new FutureCallback<Response>() {
              @Override
              public void onFailure(Throwable t) {
                log.error("Could not query Salt graph for embedded visualization.", t);
                displayMessage("Could not query the result.", t.getMessage());
              }

              @Override
              public void onSuccess(Response response) {
                try {
                  File tmpFile = File.createTempFile("embeddded-vis-fetched-result-", ".salt");
                  try (FileOutputStream out = new FileOutputStream(tmpFile)) {
                    IOUtils.copy(response.body().byteStream(), out);
                  } catch (IOException ex) {
                    log.error("Could not copy fetched GraphML file:", ex);
                  }

                  SDocument doc = SaltFactory.createSDocument();
                  doc.loadDocumentGraph(
                      org.eclipse.emf.common.util.URI.createFileURI(tmpFile.getAbsolutePath()));
                  generateVisualizerFromDocument(doc, argsCopy, visPlugin);

                } catch (IOException ex) {
                  log.error("Could not parse Salt XML", ex);
                  displayMessage("Could not parse Salt XML", ex.toString());
                }
              }

            });
      } else {
        displayMessage("Unknown visualizer \"" + visName + "\"",
            "This ANNIS instance does not know the given visualizer.");
      }
    } catch (URISyntaxException ex) {
      displayMessage("Invalid URL", "The provided URL is malformed:<br />" + ex.getMessage());
    } catch (Throwable ex) {
      displayMessage("Could not generate the visualization.",
          ex.getMessage() == null
              ? ("An unknown error of type " + ex.getClass().getSimpleName()) + " occurred."
              : ex.getMessage());
    }
  }

  @Override
  protected void init(VaadinRequest request) {
    super.init(request);

    String rawPath = request.getPathInfo();
    attachToPath(rawPath, request);
  }

  protected void attachToPath(String rawPath, VaadinRequest request) {
    List<String> splittedPath = new LinkedList<>();

    if (rawPath != null) {
      rawPath = rawPath.substring(URL_PREFIX.length());
      splittedPath =
          Splitter.on("/").omitEmptyStrings().trimResults().limit(3).splitToList(rawPath);
    }

    if (splittedPath.size() == 1) {
      // a visualizer definition which get the results from a remote salt file
      String saltUrl = request.getParameter(KEY_SALT);
      if (saltUrl == null) {
        if (request.getParameter(KEY_MATCH) == null) {
          displayGeneralHelp();
        } else {
          generateVisFromParameters(splittedPath.get(0), request.getParameterMap());
        }
      } else {
        generateVisFromRemoteSaltURL(splittedPath.get(0), saltUrl, request.getParameterMap());
      }
    } else if (splittedPath.size() >= 3) {
      // a visualizer definition in the form
      // visname/corpusname/documentname
      if ("htmldoc".equals(splittedPath.get(0))) {
        // Reconstruct the document name from the rest of the list
        String documentNodeName = Joiner.on("/").join(splittedPath.subList(1, splittedPath.size()));
        List<String> docPathDecoded = Helper.getCorpusPath(documentNodeName, true);
        showHtmlDoc(docPathDecoded.get(0), documentNodeName, request.getParameterMap());
      } else {
        displayMessage("Unknown visualizer \"" + splittedPath.get(0) + "\"",
            "Only \"htmldoc\" is supported yet.");
      }
    } else {
      displayGeneralHelp();
    }
    addStyleName("loaded-embedded-vis");
  }

  private void showHtmlDoc(String corpus, String documentNodeName,
      Map<String, String[]> args) {
    // do nothing for empty fragments
    if (args == null || args.isEmpty()) {
      return;
    }

    if (args.get("config") != null && args.get("config").length > 0) {
      String configArg = args.get("config")[0];

      // get input parameters
      HTMLVis visualizer;
      visualizer = new HTMLVis();

      VisualizerInput input;
      VisualizerRule visConfig;
      visConfig = new VisualizerRule();
      visConfig.setDisplayName(" ");
      Map<String, String> mappings = new HashMap<>();
      mappings.put("config", configArg);
      visConfig.setMappings(mappings);
      visConfig.setLayer(null);
      visConfig.setVisType("htmldoc");

      // create input
      input = DocBrowserController.createInput(corpus, documentNodeName, visConfig, null,
          visualizer.isUsingRawText(), EmbeddedVisUI.this);
      // create components, put in a panel
      Panel viszr = visualizer.createComponent(input, null);

      // Set the panel as the content of the UI
      setContent(viszr);


    } else {
      displayMessage("Missing required argument for visualizer \"htmldoc\"",
          "The following arguments are required:" + "<ul>"
              + "<li><code>config</code>: the internal config file to use (same as <a href=\"http://korpling.github.io/ANNIS/doc/classannis_1_1visualizers_1_1htmlvis_1_1HTMLVis.html\">\"config\" mapping parameter)</a></li>"
              + "</ul>");
    }
  }
  
  @Override
  protected String getLastAccessToken() {
      return authListener.getToken();
  }

  @Override
  public ServletContext getServletContext() {
    return servletContext;
  }

  @Override
  public OAuth2ClientProperties getOauth2ClientProperties() {
    return this.oauth2Clients;
  }

  @Override
  public UIConfig getConfig() {
    return this.config;
  }

}
