/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licsense is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui;

import annis.VersionInfo;
import annis.gui.requesthandler.ResourceRequestHandler;
import annis.gui.requesthandler.LoginServletRequestHandler;
import annis.gui.components.ExceptionDialog;
import annis.libgui.AnnisBaseUI;
import annis.libgui.InstanceConfig;
import annis.libgui.Helper;
import annis.gui.controlpanel.ControlPanel;
import annis.gui.docbrowser.DocBrowserController;
import annis.libgui.media.MediaController;
import annis.libgui.media.MimeTypeErrorListener;
import annis.libgui.media.MediaControllerImpl;
import annis.gui.objects.PagedResultQuery;
import annis.gui.objects.Query;
import annis.gui.querybuilder.TigerQueryBuilderPlugin;
import annis.gui.flatquerybuilder.FlatQueryBuilderPlugin;
import annis.gui.frequency.FrequencyQueryPanel;
import annis.gui.requesthandler.BinaryRequestHandler;
import annis.gui.resultview.ResultViewPanel;
import annis.gui.servlets.ResourceServlet;
import static annis.libgui.Helper.*;
import annis.libgui.media.PDFController;
import annis.libgui.media.PDFControllerImpl;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.CorpusConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.annotations.Theme;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;

import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.Tab;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.util.uri.ClassURI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * GUI for searching in corpora.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Theme("annis")
public class SearchUI extends AnnisBaseUI
  implements   MimeTypeErrorListener,
  Page.UriFragmentChangedListener,
  ErrorHandler, TabSheet.CloseHandler,
  LoginListener, Sidebar
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    SearchUI.class);
  
  
  private transient Cache<String, CorpusConfig> corpusConfigCache;

  // regular expression matching, CLEFT and CRIGHT are optional
  // indexes: AQL=1, CIDS=2, CLEFT=4, CRIGHT=6
  private final Pattern citationPattern =
    Pattern.
    compile(
    "AQL\\((.*)\\),CIDS\\(([^)]*)\\)(,CLEFT\\(([^)]*)\\),)?(CRIGHT\\(([^)]*)\\))?",
    Pattern.MULTILINE | Pattern.DOTALL);

  private MainToolbar toolbar;


  private ControlPanel controlPanel;

  private TabSheet mainTab;

  private QueryController queryController;

  private String lastQueriedFragment;

  private InstanceConfig instanceConfig;

  private DocBrowserController docBrowserController;

  public final static int CONTROL_PANEL_WIDTH = 360;

  private void initTransients()
  {
    corpusConfigCache = CacheBuilder.newBuilder().maximumSize(250).build();
  }
  
  public SearchUI()
  {
    initTransients();
  }
  
  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    initTransients();
  }
    
  @Override
  protected void init(VaadinRequest request)
  {
    super.init(request);
    setErrorHandler(this);

    this.instanceConfig = getInstanceConfig(request);
    
    getPage().setTitle(
      instanceConfig.getInstanceDisplayName() + " (ANNIS Corpus Search)");

    // init a doc browser controller
    docBrowserController = new DocBrowserController(this);

    queryController = new QueryController(this);

    // always get the resize events directly
    setImmediate(true);

    GridLayout mainLayout = new GridLayout(2,2);
    setContent(mainLayout);
    
    mainLayout.setSizeFull();
    mainLayout.setMargin(false);
    mainLayout.setRowExpandRatio(1, 1.0f);
    mainLayout.setColumnExpandRatio(1, 1.0f);
    
    toolbar = new MainToolbar(SearchUI.this);
    toolbar.addLoginListener(SearchUI.this);
    addExtension(toolbar.getScreenshotExtension());

    mainLayout.addComponent(toolbar, 0,0, 1, 0);
   
    final HelpPanel help = new HelpPanel(this);

    mainTab = new TabSheet();
    mainTab.setSizeFull();
    mainTab.setCloseHandler(this);
    mainTab.addSelectedTabChangeListener(queryController);
    mainTab.addStyleName("blue-tab");

    Tab helpTab = mainTab.addTab(help, "Help/Examples");
    helpTab.setIcon(FontAwesome.QUESTION_CIRCLE);
    helpTab.setClosable(false);
    controlPanel = new ControlPanel(queryController, instanceConfig,
      help.getExamples(), this);

    controlPanel.setWidth(CONTROL_PANEL_WIDTH, Layout.Unit.PIXELS);
    controlPanel.setHeight(100f, Layout.Unit.PERCENTAGE);
    
    mainLayout.addComponent(controlPanel, 0, 1);
    mainLayout.addComponent(mainTab, 1,1);
    
    addAction(new ShortcutListener("Tutor^eial")
    {
      @Override
      public void handleAction(Object sender, Object target)
      {
        mainTab.setSelectedTab(help);
      }
    });

    getPage().addUriFragmentChangedListener(this);

    getSession().addRequestHandler(new CitationRequestHandler());
    getSession().addRequestHandler(new ResourceRequestHandler());
    getSession().addRequestHandler(new LoginServletRequestHandler());
    getSession().addRequestHandler(new BinaryRequestHandler());

    getSession().setAttribute(MediaController.class, new MediaControllerImpl());

    getSession().setAttribute(PDFController.class, new PDFControllerImpl());

    loadInstanceFonts();

    checkCitation();
    lastQueriedFragment = "";
    evaluateFragment(getPage().getUriFragment());
    
    checkServiceVersion();
  }
  
  private void checkServiceVersion()
  {
    try
    {
      WebResource resRelease 
        = Helper.getAnnisWebResource().path("version").path("release");
      String releaseService = resRelease.get(String.class);
      String releaseGUI = VersionInfo.getReleaseName();

      // check if the release version differs and show a big warning
      if(!releaseGUI.equals(releaseService))
      {
        Notification.show("Different service version",
          "The service uses version " + releaseService
          + " but the user interface is using version  " + releaseGUI
          + ". This can produce unwanted errors.",
          Notification.Type.WARNING_MESSAGE);
      }
      else
      {
        // show a smaller warning if the revisions are not the same
        WebResource resRevision 
          = Helper.getAnnisWebResource().path("version").path("revision");
        String revisionService = resRevision.get(String.class);
        String revisionGUI = VersionInfo.getBuildRevision();
        if(!revisionService.equals(revisionGUI))
        {
          Notification.show("Different service revision",
            "The service uses revision " + revisionService
            + " but the user interface is using revision  " + revisionGUI
            + ".",
            Notification.Type.TRAY_NOTIFICATION);
        }
      }
    }
    catch(UniformInterfaceException ex)
    {
      log.warn("Could not get the version of the service", ex);
    }
    catch(ClientHandlerException ex)
    {
      log.warn("Could not get the version of the service because service is not running", ex);
    }
  }
  
  
  @Override
  public void error(com.vaadin.server.ErrorEvent event)
  {
    log.error("Unknown error in some component: " + event.getThrowable().
      getLocalizedMessage(),
      event.getThrowable());
    // get the source throwable (thus the one that triggered the error)
    Throwable source = event.getThrowable();
    if(source != null)
    {
      while (source.getCause() != null)
      {
        source = source.getCause();
      }
      ExceptionDialog.show(source);
    }
  }

  public boolean canReportBugs()
  {
    if(toolbar != null)
    {
      return toolbar.canReportBugs();
    }
    return false;
  }

  public void reportBug()
  {
    toolbar.reportBug();
  }

  public void reportBug(Throwable cause)
  {
    toolbar.reportBug(cause);
  }

  private void loadInstanceFonts()
  {
    if (instanceConfig != null && instanceConfig.getFont() != null)
    {
      FontConfig cfg = instanceConfig.getFont();

      if (cfg.getSize() == null || cfg.getSize().isEmpty())
      {
        injectUniqueCSS(
          "@import url(" + cfg.getUrl() + ");\n"
          + "." + CORPUS_FONT_FORCE + " {font-family: '" + cfg.getName() + "', monospace !important; }\n"
          + "." + CORPUS_FONT + " {font-family: '" + cfg.getName() + "', monospace; }\n"
          // this one is for the virtual keyboard
          + "#keyboardInputMaster tbody tr td table tbody tr td {\n"
          + "  font-family: '" + cfg.getName() + "', 'Lucida Console','Arial Unicode MS',monospace; "
          + "}");
      }
      else
      {
        injectUniqueCSS(
          "@import url(" + cfg.getUrl() + ");\n"
          + "." + CORPUS_FONT_FORCE + " {\n"
          + "  font-family: '" + cfg.getName() + "', monospace !important;\n"
          + "  font-size: " + cfg.getSize() + " !important;\n"
          + "}\n"
          + "." + CORPUS_FONT + " {\n"
          + "  font-family: '" + cfg.getName() + "', monospace;\n"
          + "  font-size: " + cfg.getSize() + ";\n"
          + "}\n"
          + "." + CORPUS_FONT + " .v-table-table {\n"
          + "    font-size: " + cfg.getSize() + ";\n"
          + "}"
          // this one is for the virtual keyboard
          + "#keyboardInputMaster tbody tr td table tbody tr td {\n"
          + "  font-family: '" + cfg.getName() + "', 'Lucida Console','Arial Unicode MS',monospace; "
          + "}");
      }
    }
    else
    {
      injectUniqueCSS(
        // use original font definition from keyboard.css if no font given
        "#keyboardInputMaster tbody tr td table tbody tr td {\n"
        + "  font-family: 'Lucida Console','Arial Unicode MS',monospace;"
        + "}");
    }
  }

  private InstanceConfig getInstanceConfig(VaadinRequest request)
  {
    String instance = null;
    String pathInfo = request.getPathInfo();

    if (pathInfo != null && pathInfo.startsWith("/"))
    {
      pathInfo = pathInfo.substring(1);
    }
    if (pathInfo != null && pathInfo.endsWith("/"))
    {
      pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
    }

    Map<String, InstanceConfig> allConfigs = loadInstanceConfig();

    if (pathInfo != null && !pathInfo.isEmpty())
    {
      instance = pathInfo;
    }

    if (instance != null && allConfigs.containsKey(instance))
    {
      // return the config that matches the parsed name
      return allConfigs.get(instance);
    }
    else if (allConfigs.containsKey("default"))
    {
      // return the default config
      return allConfigs.get("default");
    }
    else if (allConfigs.size() > 0)
    {
      // just return any existing config as a fallback
      log.
        warn(
          "Instance config {} not found or null and default config is not available.",
          instance);
      return allConfigs.values().iterator().next();
    }

    // default to an empty instance config
    return new InstanceConfig();
  }

  /**
   * Get a cached version of the {@link CorpusConfig} for a corpus.
   *
   * @param corpus
   * @return
   */
  public CorpusConfig getCorpusConfigWithCache(String corpus)
  {
    CorpusConfig config = new CorpusConfig();
    if (corpusConfigCache != null)
    {
      config = corpusConfigCache.getIfPresent(corpus);
      if (config == null)
      {
        if (corpus.equals(DEFAULT_CONFIG))
        {
          config = Helper.getDefaultCorpusConfig();
        }
        else
        {
          config = Helper.getCorpusConfig(corpus);
        }

        corpusConfigCache.put(corpus, config);
      }
    }

    return config;
  }

  public void clearCorpusConfigCache()
  {
    if (corpusConfigCache != null)
    {
      corpusConfigCache.invalidateAll();
    }
  }

  @Override
  protected void addCustomUIPlugins(PluginManager pluginManager)
  {
    pluginManager.addPluginsFrom(new ClassURI(TigerQueryBuilderPlugin.class).
      toURI());
    pluginManager.addPluginsFrom(new ClassURI(FlatQueryBuilderPlugin.class).
      toURI());
    pluginManager.addPluginsFrom(new ClassURI(ResourceServlet.class).toURI());
  }

  public void checkCitation()
  {
    if (VaadinSession.getCurrent() == null || VaadinSession.getCurrent().
      getSession() == null)
    {
      return;
    }
    Object origURLRaw = VaadinSession.getCurrent().getSession().getAttribute(
      "citation");
    if (origURLRaw == null || !(origURLRaw instanceof String))
    {
      return;
    }
    String origURL = (String) origURLRaw;
    String parameters = origURL.replaceAll(".*?/Cite(/)?", "");
    if (!"".equals(parameters) && !origURL.equals(parameters))
    {
      try
      {
        String decoded = URLDecoder.decode(parameters, "UTF-8");
        evaluateCitation(decoded);
      }
      catch (UnsupportedEncodingException ex)
      {
        log.error(null, ex);
      }
    }

  }

  public void evaluateCitation(String relativeUri)
  {
    Matcher m = citationPattern.matcher(relativeUri);
    if (m.matches())
    {
      // AQL
      String aql = "";
      if (m.group(1) != null)
      {
        aql = m.group(1);
      }

      // CIDS
      Set<String> selectedCorpora = new HashSet<>();
      if (m.group(2) != null)
      {
        String[] cids = m.group(2).split(",");
        selectedCorpora.addAll(Arrays.asList(cids));
      }

      // filter by actually avaible user corpora in order not to get any exception later
      WebResource res = Helper.getAnnisWebResource();
      List<AnnisCorpus> userCorpora =
        res.path("query").path("corpora").
        get(new AnnisCorpusListType());

      LinkedList<String> userCorporaStrings = new LinkedList<>();
      for (AnnisCorpus c : userCorpora)
      {
        userCorporaStrings.add(c.getName());
      }

      selectedCorpora.retainAll(userCorporaStrings);

      // CLEFT and CRIGHT
      if (m.group(4) != null && m.group(6) != null)
      {
        int cleft = 0;
        int cright = 0;
        try
        {
          cleft = Integer.parseInt(m.group(4));
          cright = Integer.parseInt(m.group(6));
        }
        catch (NumberFormatException ex)
        {
          log.error(
            "could not parse context value", ex);
        }
        queryController.
          setQuery(
          new PagedResultQuery(cleft, cright, 0, 10, null, aql, selectedCorpora));
      }
      else
      {
        queryController.setQuery(new Query(aql, selectedCorpora));
      }

      // remove all currently openend sub-windows
      Set<Window> all = new HashSet<>(getWindows());
      for (Window w : all)
      {
        removeWindow(w);
      }
    }
    else
    {
      Notification.show("Invalid citation", Notification.Type.WARNING_MESSAGE);
    }

  }
  
  @Override
  public void updateSidebarState(SidebarState state)
  {
    if(controlPanel != null && state != null && toolbar != null)
    {
      controlPanel.setVisible(state.isSidebarVisible());
      
      // set cookie
      Cookie c = new Cookie("annis-sidebar-state", state.name());
      c.setMaxAge(30*24*60*60); // 30 days
      c.setPath(VaadinService.getCurrentRequest().getContextPath());
      VaadinService.getCurrentResponse().addCookie(c);
    }
  }
  

  @Override
  public void onTabClose(TabSheet tabsheet, Component tabContent)
  {
    tabsheet.removeComponent(tabContent);
    if (tabContent instanceof ResultViewPanel)
    {
      getQueryController().notifyTabClose((ResultViewPanel) tabContent);
    }
    else if (tabContent instanceof FrequencyQueryPanel)
    {
      controlPanel.getQueryPanel().notifyFrequencyTabClose();
    }
  }

  public ControlPanel getControlPanel()
  {
    return controlPanel;
  }

  public InstanceConfig getInstanceConfig()
  {
    return instanceConfig;
  }

  public QueryController getQueryController()
  {
    return queryController;
  }
  
  public TabSheet getMainTab()
  {
    return mainTab;
  }

  @Override
  public void notifyCannotPlayMimeType(String mimeType)
  {
    if (mimeType == null)
    {
      return;
    }

    if (mimeType.startsWith("audio/ogg") || mimeType.startsWith("video/web"))
    {
      String browserList =
        "<ul>"
        + "<li>Mozilla Firefox: <a href=\"http://www.mozilla.org/firefox\" target=\"_blank\">http://www.mozilla.org/firefox</a></li>"
        + "<li>Google Chrome: <a href=\"http://www.google.com/chrome\" target=\"_blank\">http://www.google.com/chrome</a></li>"
        + "</ul>";

      WebBrowser browser = getPage().getWebBrowser();

      // IE9 users can install a plugin
      Set<String> supportedByIE9Plugin = new HashSet<>();
      supportedByIE9Plugin.add("video/webm");
      supportedByIE9Plugin.add("audio/ogg");
      supportedByIE9Plugin.add("video/ogg");

      if (browser.isIE()
        && browser.getBrowserMajorVersion() >= 9 && supportedByIE9Plugin.
        contains(mimeType))
      {
        Notification n =
        new Notification("Media file type unsupported by your browser",
          "Please install the WebM plugin for Internet Explorer 9 from "
          + "<a target=\"_blank\" href=\"https://tools.google.com/dlpage/webmmf\">https://tools.google.com/dlpage/webmmf</a> "
          + " or use a browser from the following list "
          + "(these are known to work with WebM or OGG files)<br/>"
          + browserList
          + "<br/><br /><strong>Click on this message to hide it</strong>",
          Notification.Type.WARNING_MESSAGE, true);
        n.setDelayMsec(15000);
        
        n.show(Page.getCurrent());
      }
      else
      {
        Notification n = new Notification("Media file type unsupported by your browser",
          "Please use a browser from the following list "
          + "(these are known to work with WebM or OGG files)<br/>"
          + browserList
          + "<br/><br /><strong>Click on this message to hide it</strong>",
          Notification.Type.WARNING_MESSAGE, true);
        n.setDelayMsec(15000);
        n.show(Page.getCurrent());
      }
    }
    else
    {
      Notification.show(
        "Media file type \"" + mimeType + "\" unsupported by your browser!",
        "Try to check your browsers documentation how to enable "
        + "support for the media type or inform the corpus creator about this problem.",
        Notification.Type.WARNING_MESSAGE);
    }

  }
  
  public void notifiyQueryStarted()
  {
    toolbar.notifiyQueryStarted();
  }

  @Override
  public void onLogin()
  {
    queryController.updateCorpusSetList();
  }
  
  @Override
  public void onLogout()
  {
    queryController.updateCorpusSetList();
  }

  @Override
  public void notifyMightNotPlayMimeType(String mimeType)
  {
    /*
     if(!warnedAboutPossibleMediaFormatProblem)
     {
     Notification notify = new Notification("Media file type \"" + mimeType  + "\" might be unsupported by your browser!",
     "This means you might get errors playing this file.<br/><br /> "
     + "<em>If you have problems with this media file:</em><br /> Try to check your browsers "
     + "documentation how to enable "
     + "support for the media type or inform the corpus creator about this problem.",
     Notification.Type.TRAY_NOTIFICATION, true);
     notify.setDelayMsec(15000);
     showNotification(notify);
     warnedAboutPossibleMediaFormatProblem = true;
     }
     */
  }

  @Override
  public void uriFragmentChanged(UriFragmentChangedEvent event)
  {
    evaluateFragment(event.getUriFragment());
  }
  
  /**
   * Takes a list of raw corpus names as given by the #c parameter and returns
   * a list of corpus names that are known to exist. 
   * It also replaces alias names
   * with the real corpus names.
   * @param originalNames
   * @return 
   */
  private Set<String> getMappedCorpora(List<String> originalNames)
  {
    WebResource rootRes = Helper.getAnnisWebResource();
    Set<String> mappedNames = new HashSet<>();
    // iterate over given corpora and map names if necessary
    for (String selectedCorpusName : originalNames)
    {
      // get the real corpus descriptions by the name (which could be an alias)
      try
      {
        List<AnnisCorpus> corporaByName
          = rootRes.path("query").path("corpora").path(URLEncoder.encode(selectedCorpusName, "UTF-8"))
          .get(new GenericType<List<AnnisCorpus>>()
            {
          });

        for (AnnisCorpus c : corporaByName)
        {
          mappedNames.add(c.getName());
        }
      }
      catch(UnsupportedEncodingException ex)
      {
        log.
          error("UTF-8 encoding is not supported on server, this is weird", ex);
      }
      catch (ClientHandlerException ex)
      {
        String msg = "alias mapping does not work for alias: "
          + selectedCorpusName;
        log.error(msg, ex);
        Notification.show(msg, Notification.Type.TRAY_NOTIFICATION);
      }
    }
    return mappedNames;
  }

  private void evaluateFragment(String fragment)
  {
    // do nothing if not changed
    if (fragment == null || fragment.isEmpty() || fragment.equals(
      lastQueriedFragment))
    {
      return;
    }

    Map<String, String> args = Helper.parseFragment(fragment);

    Set<String> corpora = new TreeSet<>();

    if (args.containsKey("c"))
    {
      String[] originalCorpusNames = args.get("c").split("\\s*,\\s*");
      corpora = getMappedCorpora(Arrays.asList(originalCorpusNames));
    }

    if (args.containsKey("c") && args.size() == 1)
    {
      // special case: we were called from outside and should only select,
      // but not query, the selected corpora
      if (corpora.isEmpty())
      {
        // show a warning message that the corpus was not imported yet
        new Notification("Linked corpus does not exist",
          "<div><p>The corpus you wanted to access unfortunally does not (yet) exist"
          + " in ANNIS.</p>"
          + "<h2>possible reasons are:</h2>"
          + "<ul><li>that it has not been imported yet.</li>"
          + "<li>The ANNIS service is not running</li></ul>"
          + "<p>Please ask the responsible person of the site that contained "
          + "the link to import the corpus.</p></div>",
          Notification.Type.WARNING_MESSAGE, true).show(Page.getCurrent());
      }
      else
      {
        getControlPanel().getCorpusList().selectCorpora(corpora);
        
      }

    }
    else if (args.get("cl") != null && args.get("cr") != null)
    {
      // do not change the manually selected search options
      controlPanel.getSearchOptions().setOptionsManuallyChanged(true);
      
      // full query with given context
      queryController.setQuery(new PagedResultQuery(
        Integer.parseInt(args.get("cl")),
        Integer.parseInt(args.get("cr")),
        Integer.parseInt(args.get("s")), Integer.parseInt(args.get("l")),
        args.get("seg"),
        args.get("q"), corpora));
      queryController.executeQuery();
    }
    else
    {
      // do not change the manually selected search options
      controlPanel.getSearchOptions().setOptionsManuallyChanged(true);
      
      // use default context
      queryController.setQuery(new Query(args.get("q"), corpora));
      queryController.executeQuery();
    }
  }

  /**
   * Updates the browser address bar with the current query paramaters and the
   * query itself.
   *
   * This is for convenient reloading the vaadin app and easy copying citation
   * links.
   *
   * @param q The query where the parameters are extracted from.
   */
  public void updateFragment(PagedResultQuery q)
  {
    List<String> args = Helper.citationFragment(q.getQuery(), q.getCorpora(),
      q.getContextLeft(), q.getContextRight(),
      q.getSegmentation(), q.getOffset(), q.getLimit());

    // set our fragment
    lastQueriedFragment = StringUtils.join(args, "&");
    UI.getCurrent().getPage().setUriFragment(lastQueriedFragment);

    // reset title
    getPage().setTitle(
      instanceConfig.getInstanceDisplayName() + " (ANNIS Corpus Search)");
  }

  /**
   * Adds the _c fragement to the URL in the browser adress bar when a corpus is
   * selected.
   *
   * @param corpora A list of corpora, which are add to the fragment.
   */
  public void updateFragementWithSelectedCorpus(Set<String> corpora)
  {
    if (corpora != null && !corpora.isEmpty())
    {
      String fragment = "_c=" + Helper.encodeBase64URL(StringUtils.
        join(corpora, ","));
      UI.getCurrent().getPage().setUriFragment(fragment);
    }
    else
    {
      UI.getCurrent().getPage().setUriFragment("");
    }
  }

  

  private class CitationRequestHandler implements RequestHandler
  {

    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request,
      VaadinResponse response) throws IOException
    {
      checkCitation();
      return false;
    }
  }

  private static class AnnisCorpusListType extends GenericType<List<AnnisCorpus>>
  {

    public AnnisCorpusListType()
    {
    }
  }

  public TabSheet getTabSheet()
  {
    return mainTab;
  }

  public DocBrowserController getDocBrowserController()
  {
    return docBrowserController;
  }
}
