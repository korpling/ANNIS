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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui;

import annis.gui.controlpanel.ControlPanel;
import annis.gui.media.MimeTypeErrorListener;
import annis.gui.querybuilder.TigerQueryBuilder;
import annis.gui.resultview.ResultViewPanel;
import annis.gui.tutorial.TutorialPanel;
import annis.security.AnnisSecurityManager;
import annis.security.AnnisUser;
import annis.security.SimpleSecurityManager;
import annis.service.objects.AnnisCorpus;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.AuthenticationException;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.netomi.vaadin.screenshot.Screenshot;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class SearchWindow extends Window
  implements LoginForm.LoginListener, Screenshot.ScreenshotListener,
  MimeTypeErrorListener
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SearchWindow.class);
    
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
  private AnnisSecurityManager securityManager;
  private PluginSystem ps;
  private TigerQueryBuilder queryBuilder;
  private String bugEMailAddress;
  
  private boolean warnedAboutMediaFormat = false;

  public SearchWindow(PluginSystem ps)
  {
    super("ANNIS Corpus Search");

    this.ps = ps;
    
    setName("search");

    getContent().setSizeFull();
    ((VerticalLayout) getContent()).setMargin(false);

    HorizontalLayout layoutToolbar = new HorizontalLayout();
    layoutToolbar.setWidth("100%");
    layoutToolbar.setHeight("-1px");

    Panel panelToolbar = new Panel(layoutToolbar);
    panelToolbar.setWidth("100%");
    panelToolbar.setHeight("-1px");
    addComponent(panelToolbar);
    panelToolbar.addStyleName("toolbar");

    Button btAboutAnnis = new Button("About ANNIS");
    btAboutAnnis.addStyleName(ChameleonTheme.BUTTON_SMALL);
    btAboutAnnis.setIcon(new ThemeResource("info.gif"));
    btAboutAnnis.addListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        Window w = new Window("About ANNIS", new AboutPanel(getApplication()));
        w.setModal(true);
        w.setResizable(true);
        w.setWidth("500px");
        w.setHeight("500px");
        addWindow(w);
        w.center();
      }
    });

    final SearchWindow finalThis = this;
    final Screenshot screenShot = new Screenshot();
    screenShot.addListener(finalThis);

    addComponent(screenShot);

    btBugReport = new Button("Report Bug");
    btBugReport.addStyleName(ChameleonTheme.BUTTON_SMALL);
    btBugReport.setIcon(new ThemeResource("../runo/icons/16/email.png"));
    btBugReport.addListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {

        // make screenshot
        screenShot.makeScreenshot(finalThis);

      }
    });

    lblUserName = new Label("not logged in");
    lblUserName.setWidth("100%");
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
          getApplication().setUser(null);
          showNotification("Logged out",
            Window.Notification.TYPE_TRAY_NOTIFICATION);
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

    layoutToolbar.addComponent(btAboutAnnis);
    
    layoutToolbar.addComponent(btBugReport);
    layoutToolbar.addComponent(lblUserName);
    layoutToolbar.addComponent(btLoginLogout);

    layoutToolbar.setSpacing(true);
    layoutToolbar.setComponentAlignment(btAboutAnnis, Alignment.MIDDLE_LEFT);
    layoutToolbar.setComponentAlignment(lblUserName, Alignment.MIDDLE_RIGHT);
    layoutToolbar.setComponentAlignment(btLoginLogout, Alignment.MIDDLE_RIGHT);
    layoutToolbar.setExpandRatio(lblUserName, 1.0f);

    HorizontalLayout hLayout = new HorizontalLayout();
    hLayout.setSizeFull();

    Panel hPanel = new Panel(hLayout);
    hPanel.setSizeFull();
    hPanel.setStyleName(ChameleonTheme.PANEL_BORDERLESS);

    addComponent(hPanel);
    ((VerticalLayout) getContent()).setExpandRatio(hPanel, 1.0f);

    control = new ControlPanel(this);
    control.setWidth(30f, Layout.UNITS_EM);
    control.setHeight(100f, Layout.UNITS_PERCENTAGE);
    hLayout.addComponent(control);

    tutorial = new TutorialPanel();

    mainTab = new TabSheet();
    mainTab.setSizeFull();
    mainTab.addTab(tutorial, "Tutorial", null);

    queryBuilder = new TigerQueryBuilder(control);
    mainTab.addTab(queryBuilder, "Query Builder", null);

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

    addParameterHandler(new ParameterHandler()
    {

      @Override
      public void handleParameters(Map<String, String[]> parameters)
      {
        if (parameters.containsKey("citation"))
        {
          HttpSession session =
            ((WebApplicationContext) getApplication().getContext()).
            getHttpSession();
          String citation = (String) session.getAttribute("citation");
          if (citation != null)
          {
            citation = StringUtils.removeStart(citation,
              Helper.getContext(getApplication()) + "/Cite/");
            evaluateCitation(citation);
            session.removeAttribute("citation");
          }

        }
      }
    });

  }

  @Override
  public void attach()
  {
    super.attach();

    this.bugEMailAddress = getApplication().getProperty("bug-e-mail");
    if("".equals(this.bugEMailAddress))
    {
      this.bugEMailAddress = null;
    }
    btBugReport.setVisible(this.bugEMailAddress != null);
    
    initSecurityManager();
    updateUserInformation();

  }

  public void evaluateCitation(String relativeUri)
  {

    AnnisUser user = (AnnisUser) getApplication().getUser();
    if (user == null)
    {
      return;
    }

    Map<String, AnnisCorpus> userCorpora = user.getCorpusList();

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
      HashMap<String, AnnisCorpus> selectedCorpora =
        new HashMap<String, AnnisCorpus>();
      if (m.group(2) != null)
      {
        String[] cids = m.group(2).split(",");
        for (String name : cids)
        {
          AnnisCorpus c = userCorpora.get(name);
          if (c != null)
          {
            selectedCorpora.put(c.getName(), c);
          }
        }
      }

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
      Set<Window> all = new HashSet<Window>(getChildWindows());
      for (Window w : all)
      {
        removeWindow(w);
      }
    }
    else
    {
      showNotification("Invalid citation", Notification.TYPE_WARNING_MESSAGE);
    }
  }

  private void initSecurityManager()
  {
    securityManager = new SimpleSecurityManager();

    Enumeration<?> parameterNames = getApplication().getPropertyNames();
    Properties properties = new Properties();
    while (parameterNames.hasMoreElements())
    {
      String name = (String) parameterNames.nextElement();
      properties.put(name, getApplication().getProperty(name));
    }
    securityManager.setProperties(properties);
    getApplication().setUser(null);
  }

  public void updateUserInformation()
  {
    AnnisUser user = (AnnisUser) getApplication().getUser();
    if (btLoginLogout == null || lblUserName == null || user == null)
    {
      return;
    }
    if (isLoggedIn())
    {
      lblUserName.setValue("logged in as \"" + user.getUserName() + "\"");
      btLoginLogout.setCaption("Logout");
    }
    else
    {
      lblUserName.setValue("not logged in");
      btLoginLogout.setCaption("Login");
    }
  }

  public void showQueryResult(String aql, Map<String, AnnisCorpus> corpora,
    int contextLeft,
    int contextRight, String segmentationLayer, int pageSize)
  {
    warnedAboutMediaFormat = false;
    
    // remove old result from view
    if (resultView != null)
    {
      mainTab.removeComponent(resultView);
    }
    resultView = new ResultViewPanel(aql, corpora, contextLeft, contextRight,
      segmentationLayer, pageSize, ps);
    mainTab.addTab(resultView, "Query Result", null);
    mainTab.setSelectedTab(resultView);
  }

  public void updateQueryCount(int count)
  {
    if (resultView != null && count >= 0)
    {
      resultView.setCount(count);
    }
  }

  private void showLoginWindow()
  {

    if (windowLogin == null)
    {
      LoginForm login = new LoginForm();
      login.addListener((LoginForm.LoginListener) this);

      windowLogin = new Window("Login");
      windowLogin.addComponent(login);
      windowLogin.setModal(true);
      windowLogin.setSizeUndefined();
      login.setSizeUndefined();
      ((VerticalLayout) windowLogin.getContent()).setSizeUndefined();
    }
    addWindow(windowLogin);
    windowLogin.center();
  }

  @Override
  public void onLogin(LoginEvent event)
  {
    try
    {
      AnnisUser newUser = securityManager.login(event.getLoginParameter(
        "username"),
        event.getLoginParameter("password"), true);
      getApplication().setUser(newUser);
      showNotification("Logged in as \"" + newUser.getUserName() + "\"",
        Window.Notification.TYPE_TRAY_NOTIFICATION);
    }
    catch (AuthenticationException ex)
    {
      showNotification("Authentification error: " + ex.getMessage(),
        Window.Notification.TYPE_ERROR_MESSAGE);
    }
    catch (Exception ex)
    {
      log.error(null, ex);

      showNotification("Unexpected exception: " + ex.getMessage(),
        Window.Notification.TYPE_ERROR_MESSAGE);
    }
    finally
    {
      // hide login window
      removeWindow(windowLogin);
    }

  }

  public boolean isLoggedIn()
  {
    AnnisUser u = (AnnisUser) getApplication().getUser();
    if (u == null || AnnisSecurityManager.FALLBACK_USER.equals(u.getUserName()))
    {
      return false;
    }
    else
    {
      return true;
    }
  }

  @Override
  public String getName()
  {
    return "Search";
  }

  public AnnisSecurityManager getSecurityManager()
  {
    return securityManager;
  }

  public ControlPanel getControl()
  {
    return control;
  }

  @Override
  public void screenshotReceived(byte[] imageData)
  {
    if(bugEMailAddress != null)
    {
      ReportBugPanel reportBugPanel = new ReportBugPanel(getApplication(),
        bugEMailAddress, imageData);

      // show bug report window

      Window w = new Window("Report Bug", reportBugPanel);
      w.setModal(true);
      w.setResizable(true);
      addWindow(w);
      w.center();
    }
  }

  @Override
  public void notifyCannotPlayMimeType(String mimeType)
  {
    if(!warnedAboutMediaFormat)
    {
      String browserList = 
        "<ul>"
        + "<li>Mozilla Firefox: <a href=\"http://www.mozilla.org/firefox\" target=\"_blank\">http://www.mozilla.org/firefox</a></li>"
        + "<li>Google Chrome: <a href=\"http://www.google.com/chrome\" target=\"_blank\">http://www.google.com/chrome</a></li>"
        + "<li>Opera: <a href=\"http://www.opera.com/\" target=\"_blank\">http://www.opera.com/</a></li>"
        + "</ul>";
      WebApplicationContext context = ((WebApplicationContext) getApplication()
        .getContext());
      WebBrowser browser = context.getBrowser();

      // IE9 users can install a plugin
      Set<String> supportedByIE9Plugin = new HashSet<String>();
      supportedByIE9Plugin.add("video/webm");
      supportedByIE9Plugin.add("audio/ogg");
      supportedByIE9Plugin.add("video/ogg");

      if (browser.isIE()
        && browser.getBrowserMajorVersion() >= 9 && supportedByIE9Plugin.contains(mimeType))
      {
        getWindow().showNotification("Media file type unsupported by your browser",
          "Please install the WebM plugin for Internet Explorer 9 from "
          + "<a href=\"https://tools.google.com/dlpage/webmmf\">https://tools.google.com/dlpage/webmmf</a> "
          + " or use a browser from the following list "
          + "(these are known to work with WebM or OGG files)<br/>"
          + browserList,
          Window.Notification.TYPE_ERROR_MESSAGE, true);
      }
      else
      {
        getWindow().showNotification("Media file type unsupported by your browser",
          "Please use a browser from the following list "
          + "(these are known to work with WebM or OGG files)<br/>"
          + browserList,
          Window.Notification.TYPE_ERROR_MESSAGE, true);
      }

      
      warnedAboutMediaFormat=true;
    }
  }
}
