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

import annis.libgui.AnnisBaseUI;
import annis.libgui.InstanceConfig;
import annis.libgui.Helper;
import annis.gui.components.ScreenshotMaker;
import annis.gui.controlpanel.ControlPanel;
import annis.libgui.media.MediaController;
import annis.libgui.media.MimeTypeErrorListener;
import annis.libgui.media.MediaControllerImpl;
import annis.gui.model.PagedResultQuery;
import annis.gui.model.Query;
import annis.gui.precedencequerybuilder.PrecedenceQueryBuilderPlugin;
import annis.gui.querybuilder.QueryBuilderChooser;
import annis.gui.querybuilder.TigerQueryBuilderPlugin;
import annis.gui.servlets.ResourceServlet;
import annis.gui.tutorial.TutorialPanel;
import annis.libgui.visualizers.IFrameResource;
import annis.libgui.visualizers.IFrameResourceMap;
import annis.libgui.AnnisUser;
import annis.service.objects.AnnisCorpus;
import com.github.wolfie.refresher.Refresher;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Page;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ChameleonTheme;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.util.uri.ClassURI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.vaadin.cssinject.CSSInject;

/**
 * GUI for searching in corpora.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class SearchUI extends AnnisBaseUI
  implements ScreenshotMaker.ScreenshotCallback,
  LoginWindow.LoginListener,
  MimeTypeErrorListener,
  Page.UriFragmentChangedListener
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SearchUI.class);
    
  // regular expression matching, CLEFT and CRIGHT are optional
  // indexes: AQL=1, CIDS=2, CLEFT=4, CRIGHT=6
  private Pattern citationPattern =
    Pattern.compile(
    "AQL\\((.*)\\),CIDS\\(([^)]*)\\)(,CLEFT\\(([^)]*)\\),)?(CRIGHT\\(([^)]*)\\))?",
    Pattern.MULTILINE | Pattern.DOTALL);
  private Label lblUserName;
  private Button btLoginLogout;
  private Button btBugReport;
  private ControlPanel controlPanel;
  private TutorialPanel tutorial;
  private TabSheet mainTab;
  private Window windowLogin;
  private QueryBuilderChooser queryBuilder;
  private String bugEMailAddress;
  private QueryController queryController;
  private Refresher refresh;
  private String lastQueriedFragment;
  private InstanceConfig instanceConfig;
  private CSSInject css;
  
  public final static int CONTROL_PANEL_WIDTH = 360;

  
  
  @Override
  protected void init(VaadinRequest request)
  {  
    super.init(request);
    
    this.instanceConfig = getInstanceConfig(request);
    getPage().setTitle("ANNIS Corpus Search: " + instanceConfig.getInstanceDisplayName());
    
    queryController = new QueryController(this);
        
    refresh = new Refresher();
    // deactivate refresher by default
    refresh.setRefreshInterval(-1);
    refresh.addListener(queryController);
    addExtension(refresh);
    
    // always get the resize events directly
    setImmediate(true);
    
    VerticalLayout mainLayout = new VerticalLayout();
    setContent(mainLayout);
    
    mainLayout.setSizeFull();
    mainLayout.setMargin(false);
    
    final ScreenshotMaker screenshot = new ScreenshotMaker(this);
    addExtension(screenshot);
    
    css = new CSSInject(this);

    HorizontalLayout layoutToolbar = new HorizontalLayout();
    layoutToolbar.setWidth("100%");
    layoutToolbar.setHeight("-1px");
    
    mainLayout.addComponent(layoutToolbar);
    layoutToolbar.addStyleName("toolbar");
    layoutToolbar.addStyleName("border-layout");

    Button btAboutAnnis = new Button("About ANNIS");
    btAboutAnnis.addStyleName(ChameleonTheme.BUTTON_SMALL);
    btAboutAnnis.setIcon(new ThemeResource("info.gif"));
    btAboutAnnis.addClickListener(new AboutClickListener());
    
    btBugReport = new Button("Report Bug");
    btBugReport.addStyleName(ChameleonTheme.BUTTON_SMALL);
    btBugReport.setDisableOnClick(true);
    btBugReport.setIcon(new ThemeResource("../runo/icons/16/email.png"));
    btBugReport.addListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        screenshot.makeScreenshot();
        btBugReport.setCaption("bug report is initialized...");
      }
    });
     String bugmail = (String) VaadinSession.getCurrent().getAttribute("bug-e-mail");
    if(bugmail != null && !bugmail.isEmpty() 
      && !bugmail.startsWith("${")
      && new EmailValidator("").isValid(bugmail))
    {
      this.bugEMailAddress = bugmail;
    }
    btBugReport.setVisible(this.bugEMailAddress != null);
    
    updateUserInformation();
    
    lblUserName = new Label("not logged in");
    lblUserName.setWidth("-1px");
    lblUserName.setHeight("-1px");
    lblUserName.addStyleName("right-aligned-text");

    btLoginLogout = new Button("Login", new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        if (isLoggedIn())
        {
          // logout
          Helper.setUser(null);
          Notification.show("Logged out", Notification.Type.TRAY_NOTIFICATION);
          updateUserInformation();
        }
        else
        {
          showLoginWindow();
        }
      }
    });
    btLoginLogout.setSizeUndefined();
    btLoginLogout.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btLoginLogout.setIcon(new ThemeResource("../runo/icons/16/user.png"));

    Button btOpenSource = new Button("Help us to make ANNIS better!");
    btOpenSource.setStyleName(BaseTheme.BUTTON_LINK);
    btOpenSource.addListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
      Window w = new HelpUsWindow();
      w.setCaption("Help us to make ANNIS better!");
      w.setModal(true);
      w.setResizable(true);
      w.setWidth("600px");
      w.setHeight("500px");
      addWindow(w);
      w.center();
      }
    });
    
    
    layoutToolbar.addComponent(btAboutAnnis);
    layoutToolbar.addComponent(btBugReport);
    layoutToolbar.addComponent(btOpenSource);
    layoutToolbar.addComponent(lblUserName);
    layoutToolbar.addComponent(btLoginLogout);

    layoutToolbar.setSpacing(true);
    layoutToolbar.setComponentAlignment(btAboutAnnis, Alignment.MIDDLE_LEFT);
    layoutToolbar.setComponentAlignment(btBugReport, Alignment.MIDDLE_LEFT);
    layoutToolbar.setComponentAlignment(btOpenSource, Alignment.MIDDLE_CENTER);
    layoutToolbar.setComponentAlignment(lblUserName, Alignment.MIDDLE_RIGHT);
    layoutToolbar.setComponentAlignment(btLoginLogout, Alignment.MIDDLE_RIGHT);
    layoutToolbar.setExpandRatio(btOpenSource, 1.0f);
    
    HorizontalLayout hLayout = new HorizontalLayout();
    hLayout.setSizeFull();

    mainLayout.addComponent(hLayout);
    mainLayout.setExpandRatio(hLayout, 1.0f);

    controlPanel = new ControlPanel(queryController, instanceConfig);
    controlPanel.setWidth(CONTROL_PANEL_WIDTH, Layout.UNITS_PIXELS);
    controlPanel.setHeight(100f, Layout.UNITS_PERCENTAGE);
    hLayout.addComponent(controlPanel);

    tutorial = new TutorialPanel();
    tutorial.setHeight("99%");

    mainTab = new TabSheet();
    mainTab.setSizeFull();
    mainTab.addTab(tutorial, "Tutorial");

    queryBuilder = new QueryBuilderChooser(queryController, this, instanceConfig);
    mainTab.addTab(queryBuilder, "Query Builder");

    hLayout.addComponent(mainTab);
    hLayout.setExpandRatio(mainTab, 1.0f);

    addAction(new ShortcutListener("^Query builder")
    {

      @Override
      public void handleAction(Object sender, Object target)
      {
        mainTab.setSelectedTab(queryBuilder);
      }
    });
    addAction(new ShortcutListener("Tutor^eial")
    {

      @Override
      public void handleAction(Object sender, Object target)
      {
        mainTab.setSelectedTab(tutorial);
      }
    });
    
    getPage().addUriFragmentChangedListener(this);
    
    getSession().addRequestHandler(new RequestHandler() 
    {
      @Override
      public boolean handleRequest(VaadinSession session, VaadinRequest request,
        VaadinResponse response) throws IOException
      {
        checkCitation(request);
        
        if(request.getPathInfo() != null && request.getPathInfo().startsWith(
          "/vis-iframe-res/"))
        {
          String uuidString = StringUtils.removeStart(request.getPathInfo(), "/vis-iframe-res/");
          UUID uuid = UUID.fromString(uuidString);
          IFrameResourceMap map = 
            VaadinSession.getCurrent().getAttribute(IFrameResourceMap.class);
          if(map == null)
          {
            response.setStatus(404);
          }
          else
          {
            IFrameResource res = map.get(uuid);
            if(res != null)
            {
              response.setStatus(200);
              response.setContentType(res.getMimeType());
              response.getOutputStream().write(res.getData());
            }
          }
          return true;
        }
        
        return false;
      }
    });
    
    getSession().setAttribute(MediaController.class, new MediaControllerImpl());
    
    loadInstanceFonts();
    checkCitation(request);
    lastQueriedFragment = "";
    evaluateFragment(getPage().getUriFragment());
  }
  
  private void loadInstanceFonts()
  {
    if(instanceConfig != null && css != null && instanceConfig.getFont() != null)
    {
      FontConfig cfg = instanceConfig.getFont();
      css.setStyles(
        "@import url(" + cfg.getUrl() + ");\n"
        + ".corpus-font-force {font-family: '" + cfg.getName() + "', monospace !important;}\n"
        + ".corpus-font {font-family: '" + cfg.getName() + "', monospace;}\n"
        // this one is for the virtual keyboard
        + "#keyboardInputMaster tbody tr td table tbody tr td {\n"
        + "  font-family: '" + cfg.getName() + "', 'Lucida Console','Arial Unicode MS',monospace;"
        + "}");
    }
    else
    {
      css.setStyles(
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
    if(pathInfo != null && pathInfo.startsWith("/instance-"))
    {
      instance = pathInfo.substring("/instance-".length());
    }
    
    Map<String, InstanceConfig> allConfigs = loadInstanceConfig();
    if(instance != null && allConfigs.containsKey(instance))
    {
      // return the config that matches the parsed name
      return allConfigs.get(instance);
    }
    else if(allConfigs.containsKey("default"))
    {
      // return the default config
      return allConfigs.get("default");
    }
    else if(allConfigs.size() > 0)
    {
      // just return any existing config as a fallback
      log.warn("Instance config {} not found or null and default config is not available.", instance);
      return allConfigs.values().iterator().next();
    }
    
    // default to an empty instance config
    return new InstanceConfig();
  }

  @Override
  protected void addCustomUIPlugins(PluginManager pluginManager)
  {
    pluginManager.addPluginsFrom(new ClassURI(TigerQueryBuilderPlugin.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(PrecedenceQueryBuilderPlugin.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(ResourceServlet.class).toURI());
  }
  
  
  
  public void checkCitation(VaadinRequest request)
  {
    Object origURLRaw = VaadinSession.getCurrent().getSession().getAttribute(
      "citation");
    if(origURLRaw == null || !(origURLRaw instanceof String))
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
      Set<String> selectedCorpora = new HashSet<String>();
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
      LinkedList<String> userCorporaStrings = new LinkedList<String>();
      for(AnnisCorpus c : userCorpora)
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
        queryController.setQuery(
          new PagedResultQuery(cleft, cright, 0, 10, null, aql, selectedCorpora));
      }
      else
      {
        queryController.setQuery(new Query(aql, selectedCorpora));
      }
      
      // remove all currently openend sub-windows
      Set<Window> all = new HashSet<Window>(getWindows());
      for (Window w : all)
      {
        removeWindow(w);
      }
    }
    else
    {
      showNotification("Invalid citation", Notification.Type.WARNING_MESSAGE);
    }
    
  }

  public void updateUserInformation()
  {
    if (btLoginLogout == null || lblUserName == null)
    {
      return;
    }
    if (isLoggedIn())
    {
      AnnisUser user = Helper.getUser();
      if(user != null)
      {
        lblUserName.setValue("logged in as \"" + ((AnnisUser) user).getUserName() + "\"");
        btLoginLogout.setCaption("Logout");
      }
    }
    else
    {
      lblUserName.setValue("not logged in");
      btLoginLogout.setCaption("Login");
    }
    
    queryController.updateCorpusSetList();
  }
  
  private void showLoginWindow()
  {
    windowLogin = new LoginWindow();
    windowLogin.setModal(true);
    windowLogin.setSizeUndefined();

    addWindow(windowLogin);
    windowLogin.center();
  }

  @Override
  public void onLogin()
  {
    AnnisUser user = Helper.getUser();
    
    if(user != null)
    {
      Notification.show("Logged in as \"" + user.getUserName() + "\"",
        Notification.Type.TRAY_NOTIFICATION);
    }
    
    updateUserInformation();

  }
  

  public boolean isLoggedIn()
  {
    return Helper.getUser() != null;
  }
  
  public ControlPanel getControlPanel()
  {
    return controlPanel;
  }

  public InstanceConfig getInstanceConfig()
  {
    return instanceConfig;
  }
  
  @Override
  public void screenshotReceived(byte[] imageData, String mimeType)
  {
    btBugReport.setEnabled(true);
    btBugReport.setCaption("Report Bug");
    
    if(bugEMailAddress != null)
    {
      ReportBugWindow reportBugWindow = 
        new ReportBugWindow(bugEMailAddress, imageData, mimeType);
      
      reportBugWindow.setModal(true);
      reportBugWindow.setResizable(true);
      addWindow(reportBugWindow);
      reportBugWindow.center();
    }
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
    if(mimeType == null)
    {
      return;
    }
  
    if(mimeType.startsWith("audio/ogg") || mimeType.startsWith("video/web"))
    {
      String browserList = 
        "<ul>"
        + "<li>Mozilla Firefox: <a href=\"http://www.mozilla.org/firefox\" target=\"_blank\">http://www.mozilla.org/firefox</a></li>"
        + "<li>Google Chrome: <a href=\"http://www.google.com/chrome\" target=\"_blank\">http://www.google.com/chrome</a></li>"
        + "</ul>";
      
      WebBrowser browser = getPage().getWebBrowser();

      // IE9 users can install a plugin
      Set<String> supportedByIE9Plugin = new HashSet<String>();
      supportedByIE9Plugin.add("video/webm");
      supportedByIE9Plugin.add("audio/ogg");
      supportedByIE9Plugin.add("video/ogg");

      if (browser.isIE()
        && browser.getBrowserMajorVersion() >= 9 && supportedByIE9Plugin.contains(mimeType))
      {
        Notification.show("Media file type unsupported by your browser",
          "Please install the WebM plugin for Internet Explorer 9 from "
          + "<a href=\"https://tools.google.com/dlpage/webmmf\">https://tools.google.com/dlpage/webmmf</a> "
          + " or use a browser from the following list "
          + "(these are known to work with WebM or OGG files)<br/>"
          + browserList,
          Notification.Type.WARNING_MESSAGE);
      }
      else
      {
        Notification.show("Media file type unsupported by your browser",
          "Please use a browser from the following list "
          + "(these are known to work with WebM or OGG files)<br/>"
          + browserList,
          Notification.Type.WARNING_MESSAGE);
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
  
  private void evaluateFragment(String fragment)
  {
    // do nothing if not changed
    if (fragment == null || fragment.isEmpty() || fragment.equals(lastQueriedFragment))
    {
      return;
    }
    
    Map<String, String> args = Helper.parseFragment(fragment);

    Set<String> corpora = new TreeSet<String>();
    
    if (args.containsKey("c"))
    {
      String[] corporaSplitted = args.get("c").split("\\s*,\\s*");
      corpora.addAll(Arrays.asList(corporaSplitted));
    }

    if(args.containsKey("c") && args.size() == 1)
    {
      // special case: we were called from outside and should only select
      // our corpus
      Set<String> mappedCorpora = new HashSet<String>();
      // iterate over given corpora and map names if necessary
      for(String c : corpora)
      {
        if(instanceConfig.getCorpusMappings() != null 
          && instanceConfig.getCorpusMappings().containsKey(c))
        {
          mappedCorpora.add(instanceConfig.getCorpusMappings().get(c));
        }
        else
        {
          mappedCorpora.add(c);
        }
      }
      queryController.setQuery(new Query("tok", mappedCorpora));
    }
    else if(args.get("cl") != null && args.get("cr") != null)
    {
      // full query with given context
      queryController.setQuery(new PagedResultQuery(
        Integer.parseInt(args.get("cl")), 
        Integer.parseInt(args.get("cr")),
        Integer.parseInt(args.get("s")), Integer.parseInt(args.get("l")),
        args.get("seg"),
        args.get("q"), corpora));      
      queryController.executeQuery(true, true);
    }
    else
    {
      // use default context
      queryController.setQuery(new Query(args.get("q"), corpora));      
      queryController.executeQuery(true, true);
    }    
  }
  
  
  public void updateFragment(PagedResultQuery q)
  {
    List<String> args = Helper.citationFragment(q.getQuery(), q.getCorpora(), 
      q.getContextLeft(), q.getContextRight(), 
      q.getSegmentation(), q.getOffset(), q.getLimit());
      
    // set our fragment
    lastQueriedFragment = StringUtils.join(args, "&");
    UI.getCurrent().getPage().setUriFragment(lastQueriedFragment);
    
    // reset title
    getPage().setTitle("ANNIS Corpus Search: " + instanceConfig.getInstanceDisplayName());
    
  }
  
  public void setRefresherEnabled(boolean enabled)
  {
    if(refresh != null)
    {
      if(enabled)
      {
        refresh.setRefreshInterval(1000);
      }
      else
      {
        refresh.setRefreshInterval(-1);
      }
    }
  }

  private static class AboutClickListener implements ClickListener
  {

    public AboutClickListener()
    {
    }

    @Override
    public void buttonClick(ClickEvent event)
    {        
      Window w =  new AboutWindow();
      w.setCaption("About ANNIS");
      w.setModal(true);
      w.setResizable(true);
      w.setWidth("500px");
      w.setHeight("500px");
      UI.getCurrent().addWindow(w);
    }
  }

  private static class AnnisCorpusListType extends GenericType<List<AnnisCorpus>>
  {

    public AnnisCorpusListType()
    {
    }
  }
  
  
  
}
