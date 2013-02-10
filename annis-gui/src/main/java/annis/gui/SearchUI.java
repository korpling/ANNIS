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

import annis.gui.controlpanel.ControlPanel;
import annis.gui.media.MimeTypeErrorListener;
import annis.gui.querybuilder.QueryBuilderChooser;
import annis.gui.resultview.ResultViewPanel;
import annis.gui.tutorial.TutorialPanel;
import annis.security.AnnisUser;
import annis.service.objects.AnnisCorpus;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Page;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * GUI for searching in corpora.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PreserveOnRefresh
public class SearchUI extends AnnisBaseUI
  implements LoginForm.LoginListener,
  MimeTypeErrorListener, Page.UriFragmentChangedListener
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
  private ControlPanel control;
  private TutorialPanel tutorial;
  private TabSheet mainTab;
  private Window windowLogin;
  private ResultViewPanel resultView;
  private QueryBuilderChooser queryBuilder;
  private String bugEMailAddress;
  private String lastQueriedFragment;
  
  private boolean warnedAboutPossibleMediaFormatProblem = false;

  public final static int CONTROL_PANEL_WIDTH = 360;

  @Override
  protected void init(VaadinRequest request)
  {  
    super.init(request);
    
    InstanceConfig instanceConfig = getInstanceConfig(request);
    getPage().setTitle("ANNIS Corpus Searc: " + instanceConfig.getInstanceDisplayName());
    
    
    // always get the resize events directly
    setImmediate(true);
    
    VerticalLayout mainLayout = new VerticalLayout();
    setContent(mainLayout);
    
    mainLayout.setSizeFull();
    mainLayout.setMargin(false);

    HorizontalLayout layoutToolbar = new HorizontalLayout();
    layoutToolbar.setWidth("100%");
    layoutToolbar.setHeight("-1px");

    Panel panelToolbar = new Panel(layoutToolbar);
    panelToolbar.setWidth("100%");
    panelToolbar.setHeight("-1px");
    mainLayout.addComponent(panelToolbar);
    panelToolbar.addStyleName("toolbar");

    Button btAboutAnnis = new Button("About ANNIS");
    btAboutAnnis.addStyleName(ChameleonTheme.BUTTON_SMALL);
    btAboutAnnis.setIcon(new ThemeResource("info.gif"));
    btAboutAnnis.addListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        Window w =  new AboutWindow();
        w.setCaption("About ANNIS");
        w.setModal(true);
        w.setResizable(true);
        w.setWidth("500px");
        w.setHeight("500px");
        addWindow(w);
        w.center();
      }
    });

    // TODO: re-enable screenshots (vaadin7)
//    final Screenshot screenShot = new Screenshot();
//    screenShot.addListener(this);
//
//    addComponent(screenShot);

    btBugReport = new Button("Report Bug");
    btBugReport.addStyleName(ChameleonTheme.BUTTON_SMALL);
    btBugReport.setDisableOnClick(true);
    btBugReport.setIcon(new ThemeResource("../runo/icons/16/email.png"));
    btBugReport.addListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        btBugReport.setCaption("bug report is initialized...");
        
        //TODO make screenshot (vaadin7)
//        screenShot.makeScreenshot(finalThis);
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
          getSession().setAttribute(AnnisUser.class, null);
          Notification.show("Logged out", Notification.Type.TRAY_NOTIFICATION);
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

    Panel hPanel = new Panel(hLayout);
    hPanel.setSizeFull();
    hPanel.setStyleName(ChameleonTheme.PANEL_BORDERLESS);

    mainLayout.addComponent(hPanel);
    mainLayout.setExpandRatio(hPanel, 1.0f);

    control = new ControlPanel(this, instanceConfig);
    control.setWidth(CONTROL_PANEL_WIDTH, Layout.UNITS_PIXELS);
    control.setHeight(100f, Layout.UNITS_PERCENTAGE);
    hLayout.addComponent(control);

    tutorial = new TutorialPanel();

    mainTab = new TabSheet();
    mainTab.setSizeFull();
    mainTab.addTab(tutorial, "Tutorial");

    queryBuilder = new QueryBuilderChooser(control, this, instanceConfig);
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
    
    // TODO: re-enable parsing of the old style citation links (vaadin7)

//    addParameterHandler(new ParameterHandler()
//    {
//
//      @Override
//      public void handleParameters(Map<String, String[]> parameters)
//      {
//        if (parameters.containsKey("citation"))
//        {
//          HttpSession session =
//            ((WebApplicationContext) getApplication().getContext()).
//            getHttpSession();
//          String citation = (String) session.getAttribute("citation");
//          if (citation != null)
//          {
//            citation = StringUtils.removeStart(citation,
//              Helper.getContext(getApplication()) + "/Cite/");
//            evaluateCitation(citation);
//            session.removeAttribute("citation");
//          }
//
//        }
//      }
//    });
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
        get(new GenericType<List<AnnisCorpus>>(){});
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
        control.setQuery(aql, selectedCorpora, cleft, cright);
      }
      else
      {
        control.setQuery(aql, selectedCorpora);
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
      AnnisUser user = VaadinSession.getCurrent().getAttribute(AnnisUser.class);
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
  }

  public void showQueryResult(String aql, Set<String> corpora,
    int contextLeft,
    int contextRight, String segmentationLayer, int start, int pageSize)
  {
    warnedAboutPossibleMediaFormatProblem = false;
    
    // remove old result from view
    if (resultView != null)
    {
      mainTab.removeComponent(resultView);
    }

    updateFragment(aql, corpora, contextLeft, contextRight, segmentationLayer, 
      start, pageSize);
    
    resultView = new ResultViewPanel(this, aql, corpora, contextLeft, contextRight,
      segmentationLayer, start, pageSize, this);
    mainTab.addTab(resultView, "Query Result");
    mainTab.setSelectedTab(resultView);
    
  }
  
  

  public void updateQueryCount(int count)
  {
    if (resultView != null && count >= 0)
    {
      resultView.setCount(count);
    }
  }
  public void updateFragment(String aql, 
    Set<String> corpora, int contextLeft, int contextRight, String segmentation,
    int start, int limit)
  {
    List<String> args = Helper.citationFragmentParams(aql, corpora, 
      contextLeft, contextRight, 
      segmentation, start, limit);
      
    // set our fragment
    lastQueriedFragment = StringUtils.join(args, "&");
    getPage().setUriFragment(lastQueriedFragment);
    
  }

  @Override
  public void uriFragmentChanged(UriFragmentChangedEvent event)
  {
    
    String fragment = event.getUriFragment();
    // do nothing if not changed
    if(fragment.equals(lastQueriedFragment))
    {
      return;
    }
    
    Map<String, String> args = Helper.parseFragment(fragment);
    
    Set<String> corpora = new TreeSet<String>();
    if(args.containsKey("c"))
    {
      String[] corporaSplitted = args.get("c").split("\\s*,\\s*");
      corpora.addAll(Arrays.asList(corporaSplitted));
    }
    
    control.executeCount(args.get("q"), corpora);
    
    showQueryResult(args.get("q"), corpora, 
      Integer.parseInt(args.get("cl")), Integer.parseInt(args.get("cr")), 
      args.get("seg"), Integer.parseInt(args.get("s")), 
      Integer.parseInt(args.get("l")));
    
  }
  
  private void showLoginWindow()
  {

    if (windowLogin == null)
    {
      windowLogin = new LoginWindow();
      windowLogin.setModal(true);
      windowLogin.setSizeUndefined();
    }
    addWindow(windowLogin);
    windowLogin.center();
  }

  @Override
  public void onLogin(LoginEvent event)
  {
    

  }

  public boolean isLoggedIn()
  {
    return getSession().getAttribute(AnnisUser.class) != null;
  }
  
  public ControlPanel getControl()
  {
    return control;
  }

  // TODO: handle screenshot event (vaadin7)
//  @Override
//  public void screenshotReceived(byte[] imageData)
//  {
//    btBugReport.setEnabled(true);
//    btBugReport.setCaption("Report Bug");
//    
//    if(bugEMailAddress != null)
//    {
//      ReportBugPanel reportBugPanel = new ReportBugPanel(getApplication(),
//        bugEMailAddress, imageData);
//
//      // show bug report window
//
//      Window w = new Window("Report Bug", reportBugPanel);
//      w.setModal(true);
//      w.setResizable(true);
//      addWindow(w);
//      w.center();
//    }
//  }

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
  
  
  
}
