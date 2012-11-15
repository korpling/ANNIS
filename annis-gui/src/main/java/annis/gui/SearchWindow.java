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
import annis.security.AnnisUser;
import annis.service.objects.AnnisCorpus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.themes.ChameleonTheme;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
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
  private PluginSystem ps;
  private TigerQueryBuilder queryBuilder;
  private String bugEMailAddress;
  
  private boolean warnedAboutPossibleMediaFormatProblem = false;

  public final static int CONTROL_PANEL_WIDTH = 360;
  
  public SearchWindow(PluginSystem ps)
  {
    super("ANNIS Corpus Search");

    this.ps = ps;
    
    setName("search");
    // always get the resize events directly
    setImmediate(true);
    
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
        Window w = new Window("About ANNIS", new AboutPanel());
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
    btBugReport.setDisableOnClick(true);
    btBugReport.setIcon(new ThemeResource("../runo/icons/16/email.png"));
    btBugReport.addListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        btBugReport.setCaption("bug report is initialized...");
        
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
    control.setWidth(CONTROL_PANEL_WIDTH, Layout.UNITS_PIXELS);
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

    String bugmail = getApplication().getProperty("bug-e-mail");
    if(bugmail != null && !bugmail.isEmpty() 
      && !bugmail.startsWith("${")
      && new EmailValidator("").isValid(bugmail))
    {
      this.bugEMailAddress = bugmail;
    }
    
    
    btBugReport.setVisible(this.bugEMailAddress != null);
    
    updateUserInformation();

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
      WebResource res = Helper.getAnnisWebResource(getApplication());
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

  public void updateUserInformation()
  {
    if (btLoginLogout == null || lblUserName == null)
    {
      return;
    }
    if (isLoggedIn())
    {
      Object user = getApplication().getUser();
      if(user instanceof AnnisUser)
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
    int contextRight, String segmentationLayer, int pageSize)
  {
    warnedAboutPossibleMediaFormatProblem = false;
    
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
      LoginForm login = new LoginForm()
      {

        /**
         * Custom implementation which uses a more distinctive name for the password field
         */
        @Override
        protected byte[] getLoginHTML()
        {
           String appUri = getApplication().getURL().toString()
            + getWindow().getName() + "/";

          try
          {
            return ("<!DOCTYPE html PUBLIC \"-//W3C//DTD "
              + "XHTML 1.0 Transitional//EN\" "
              + "\"http://www.w3.org/TR/xhtml1/"
              + "DTD/xhtml1-transitional.dtd\">\n" + "<html>"
              + "<head><script type='text/javascript'>"
              + "var setTarget = function() {" + "var uri = '"
              + appUri
              + "loginHandler"
              + "'; var f = document.getElementById('loginf-annis');"
              + "document.forms[0].action = uri;document.forms[0].username.focus();};"
              + ""
              + "var styles = window.parent.document.styleSheets;"
              + "for(var j = 0; j < styles.length; j++) {\n"
              + "if(styles[j].href) {"
              + "var stylesheet = document.createElement('link');\n"
              + "stylesheet.setAttribute('rel', 'stylesheet');\n"
              + "stylesheet.setAttribute('type', 'text/css');\n"
              + "stylesheet.setAttribute('href', styles[j].href);\n"
              + "document.getElementsByTagName('head')[0].appendChild(stylesheet);\n"
              + "}"
              + "}\n"
              + "function submitOnEnter(e) { var keycode = e.keyCode || e.which;"
              + " if (keycode == 13) {document.forms[0].submit();}  } \n"
              + "</script>"
              + "</head><body onload='setTarget();' style='margin:0;padding:0; background:transparent;' class=\""
              + ApplicationConnection.GENERATED_BODY_CLASSNAME
              + "\">"
              + "<div class='v-app v-app-loginpage' style=\"background:transparent;\">"
              + "<iframe name='logintarget' style='width:0;height:0;"
              + "border:0;margin:0;padding:0;'></iframe>"
              + "<form id='loginf-annis' target='logintarget' onkeypress=\"submitOnEnter(event)\" method=\"post\">"
              + "<div>"
              + getUsernameCaption()
              + "</div><div >"
              + "<input class='v-textfield' style='display:block;' type='text' name='username'></div>"
              + "<div>"
              + getPasswordCaption()
              + "</div>"
              + "<div><input class='v-textfield' style='display:block;' type='password' name='annis-gui-password'></div>"
              + "<div><div onclick=\"document.forms[0].submit();\" tabindex=\"0\" class=\"v-button\" role=\"button\" ><span class=\"v-button-wrap\"><span class=\"v-button-caption\">"
              + getLoginButtonCaption()
              + "</span></span></div></div></form></div>" + "</body></html>")
              .getBytes("UTF-8");
          }
          catch (UnsupportedEncodingException e)
          {
            throw new RuntimeException("UTF-8 encoding not avalable", e);
          }
        }
        
      };
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
      // forget old user information
      getApplication().setUser(null);
      
      String userName = event.getLoginParameter("username");
      
      Client client = Helper.createRESTClient(userName, 
        event.getLoginParameter("annis-gui-password"));
      
      // check if this is valid user/password combination
      WebResource res = client.resource(getApplication()
        .getProperty(Helper.KEY_WEB_SERVICE_URL))
        .path("admin").path("is-authenticated");
      if("true".equalsIgnoreCase(res.get(String.class)))
      {
        // everything ok, save this user configuration for re-use
        getApplication().setUser(new AnnisUser(userName, client));
        
        showNotification("Logged in as \"" + userName + "\"",
          Window.Notification.TYPE_TRAY_NOTIFICATION);
      }
    }
    catch (ClientHandlerException ex)
    {
      showNotification("Authentification error: " + ex.getMessage(),
        Window.Notification.TYPE_WARNING_MESSAGE);
    }
    catch(UniformInterfaceException ex)
    {
      if(ex.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode())
      {
        getWindow().showNotification("Username or password wrong", ex.getMessage(), 
          Notification.TYPE_WARNING_MESSAGE);
      }
      else
      {
        log.error(null, ex);
        showNotification("Unexpected exception: " + ex.getMessage(),
          Window.Notification.TYPE_WARNING_MESSAGE);
      }
    }
    catch (Exception ex)
    {
      log.error(null, ex);

      showNotification("Unexpected exception: " + ex.getMessage(),
        Window.Notification.TYPE_WARNING_MESSAGE);
    }
    finally
    {
      // hide login window
      removeWindow(windowLogin);
    }

  }

  public boolean isLoggedIn()
  {
    return getApplication().getUser() != null;
  }

  @Override
  public String getName()
  {
    return "Search";
  }
  public ControlPanel getControl()
  {
    return control;
  }

  @Override
  public void screenshotReceived(byte[] imageData)
  {
    btBugReport.setEnabled(true);
    btBugReport.setCaption("Report Bug");
    
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
          Window.Notification.TYPE_WARNING_MESSAGE, true);
      }
      else
      {
        getWindow().showNotification("Media file type unsupported by your browser",
          "Please use a browser from the following list "
          + "(these are known to work with WebM or OGG files)<br/>"
          + browserList,
          Window.Notification.TYPE_WARNING_MESSAGE, true);
      }
    }
    else
    {
      getWindow().showNotification(
        "Media file type \"" + mimeType + "\" unsupported by your browser!",
        "Try to check your browsers documentation how to enable "
        + "support for the media type or inform the corpus creator about this problem.",
        Window.Notification.TYPE_WARNING_MESSAGE, true);
    }

  }

  @Override
  public void notifyMightNotPlayMimeType(String mimeType)
  {
    if(!warnedAboutPossibleMediaFormatProblem)
    {
      Notification notify = new Notification("Media file type \"" + mimeType  + "\" might be unsupported by your browser!",
          "This means you might get errors playing this file.<br/><br /> "
        + "<em>If you have problems with this media file:</em><br /> Try to check your browsers "
        + "documentation how to enable "
        + "support for the media type or inform the corpus creator about this problem.",
          Window.Notification.TYPE_TRAY_NOTIFICATION, true);
      notify.setDelayMsec(15000);
      showNotification(notify);
      warnedAboutPossibleMediaFormatProblem = true;
    }
  }
  
  
  
}
