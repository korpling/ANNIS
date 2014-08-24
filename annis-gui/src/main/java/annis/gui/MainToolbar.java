/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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

import static annis.gui.SidebarState.AUTO_HIDDEN;
import static annis.gui.SidebarState.AUTO_VISIBLE;
import static annis.gui.SidebarState.HIDDEN;
import static annis.gui.SidebarState.VISIBLE;
import annis.gui.components.ScreenshotMaker;
import annis.libgui.AnnisBaseUI;
import static annis.libgui.AnnisBaseUI.USER_LOGIN_ERROR;
import annis.libgui.AnnisUser;
import annis.libgui.Helper;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ValoTheme;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.LoggerFactory;

/**
 * The ANNIS main toolbar.
 * Handles login, showing the sidebar (if it exists), the screenshot making
 * and some information windows.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class MainToolbar extends HorizontalLayout
  implements LoginListener, ScreenshotMaker.ScreenshotCallback
{
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    MainToolbar.class);


  private Button btSidebar;

  private final Button btLogin;

  private final Button btLogout;

  private final Button btBugReport;

  private final Label lblUserName;

  private final String bugEMailAddress;

  private Window windowLogin;

  private SidebarState sidebarState = SidebarState.VISIBLE;
  
  private final List<LoginListener> loginListeners = new LinkedList<>();

  private Throwable lastBugReportCause;
  
  private Sidebar sidebar;
    
  private ScreenshotMaker screenshotExtension;
  
  public MainToolbar(Sidebar sidebar)
  {
    this.sidebar = sidebar;
    
    String bugmail = (String) VaadinSession.getCurrent().getAttribute(
      "bug-e-mail");
    if (bugmail != null && !bugmail.isEmpty()
      && !bugmail.startsWith("${")
      && new EmailValidator("").isValid(bugmail))
    {
      this.bugEMailAddress = bugmail;
    }
    else
    {
      this.bugEMailAddress = null;
    }
    
    regenerateStateFromCookies();

    setWidth("100%");
    setHeight("-1px");

    addStyleName("toolbar");
    addStyleName("border-layout");

    Button btAboutAnnis = new Button("About ANNIS");
    btAboutAnnis.addStyleName(ValoTheme.BUTTON_SMALL);
    btAboutAnnis.setIcon(new ThemeResource("images/annis_16.png"));
    btAboutAnnis.addClickListener(new AboutClickListener());

    btSidebar = new Button();
    btSidebar.setDisableOnClick(true);
    btSidebar.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
    btSidebar.addStyleName(ValoTheme.BUTTON_SMALL);
    btSidebar.setDescription("Show and hide search sidebar");
    btSidebar.setIconAlternateText(btSidebar.getDescription());

    btBugReport = new Button("Report Problem");
    btBugReport.addStyleName(ValoTheme.BUTTON_SMALL);
    btBugReport.setDisableOnClick(true);
    btBugReport.setIcon(new ThemeResource("../runo/icons/16/email.png"));
    btBugReport.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        reportBug();
      }
    });
    btBugReport.setVisible(this.bugEMailAddress != null);

    lblUserName = new Label("not logged in");
    lblUserName.setWidth("-1px");
    lblUserName.setHeight("-1px");
    lblUserName.addStyleName("right-aligned-text");

    btLogin = new Button("Login", new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        BrowserFrame frame = new BrowserFrame("login", new ExternalResource(
          Helper.getContext() + "/login"));
        frame.setWidth("100%");
        frame.setHeight("200px");

        windowLogin = new Window("ANNIS Login", frame);
        windowLogin.setModal(true);
        windowLogin.setWidth("400px");
        windowLogin.setHeight("250px");

        UI.getCurrent().addWindow(windowLogin);
        windowLogin.center();
      }
    });

    btLogout = new Button("Logout", new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        // logout
        Helper.setUser(null);
        for(LoginListener l : loginListeners)
        {
          l.onLogout();
        }
        Notification.show("Logged out", Notification.Type.TRAY_NOTIFICATION);
        updateUserInformation();
      }
    });

    btLogin.setSizeUndefined();
    btLogin.setStyleName(ValoTheme.BUTTON_SMALL);
    btLogin.setIcon(new ThemeResource("../runo/icons/16/user.png"));

    btLogout.setSizeUndefined();
    btLogout.setStyleName(ValoTheme.BUTTON_SMALL);
    btLogout.setIcon(new ThemeResource("../runo/icons/16/user.png"));

    Button btOpenSource = new Button("Help us to make ANNIS better!");
    btOpenSource.setStyleName(BaseTheme.BUTTON_LINK);
    btOpenSource.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        Window w = new HelpUsWindow();
        w.setCaption("Help us to make ANNIS better!");
        w.setModal(true);
        w.setResizable(true);
        w.setWidth("600px");
        w.setHeight("500px");
        UI.getCurrent().addWindow(w);
        w.center();
      }
    });

    if(sidebar != null)
    {
      addComponent(btSidebar);
    }
    addComponent(btAboutAnnis);
    addComponent(btBugReport);
    addComponent(btOpenSource);

    setSpacing(true);
    setComponentAlignment(btAboutAnnis, Alignment.MIDDLE_LEFT);
    setComponentAlignment(btBugReport, Alignment.MIDDLE_LEFT);
    setComponentAlignment(btOpenSource, Alignment.MIDDLE_CENTER);
    setExpandRatio(btOpenSource, 1.0f);

    addLoginButton();

    btSidebar.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        btSidebar.setEnabled(true);

        // decide new state
        switch (sidebarState)
        {
          case VISIBLE:
            if (event.isCtrlKey())
            {
              sidebarState = SidebarState.AUTO_VISIBLE;
            }
            else
            {
              sidebarState = SidebarState.HIDDEN;
            }
            break;
          case HIDDEN:
            if (event.isCtrlKey())
            {
              sidebarState = SidebarState.AUTO_HIDDEN;
            }
            else
            {
              sidebarState = SidebarState.VISIBLE;
            }
            break;

          case AUTO_VISIBLE:
            if (event.isCtrlKey())
            {
              sidebarState = SidebarState.VISIBLE;
            }
            else
            {
              sidebarState = SidebarState.AUTO_HIDDEN;
            }
            break;
          case AUTO_HIDDEN:
            if (event.isCtrlKey())
            {
              sidebarState = SidebarState.HIDDEN;
            }
            else
            {
              sidebarState = SidebarState.AUTO_VISIBLE;
            }
            break;
        }
        
        updateSidebarState();
      }
    });
    
    screenshotExtension = new ScreenshotMaker(this);
    
    JavaScript.getCurrent().addFunction("annis.gui.logincallback",
      new LoginCloseCallback());
    
    updateSidebarState();
    MainToolbar.this.updateUserInformation();
  }
  
  
  
  private void updateSidebarState()
  {
    if(sidebar != null && btSidebar != null)
    {
      btSidebar.setIcon(sidebarState.getIcon());
      sidebar.updateSidebarState(sidebarState);
    }
  }
  
  public void notifiyQueryStarted()
  {
    if(sidebarState == SidebarState.AUTO_VISIBLE)
    {
      sidebarState = SidebarState.AUTO_HIDDEN;
    }
    
    updateSidebarState();
  }
  
  public void addLoginListener(LoginListener listener)
  {
    this.loginListeners.add(listener);
  }

  /**
   * Adds the login button + login text to the toolbar. This is only happened,
   * when the gui is not started via the kickstarter.
   *
   * <p>
   * The Kickstarter overrides the "kickstarterEnvironment" context parameter
   * and set it to "true", so the gui can detect, that is not necessary to offer
   * a login button.</p>
   *
   * component.
   */
  private void addLoginButton()
  {
    VaadinSession session = VaadinSession.getCurrent();
    if(session != null)
    {
      DeploymentConfiguration configuration = session.getConfiguration();

      boolean kickstarter = Boolean.parseBoolean(
        configuration.getInitParameters().getProperty("kickstarterEnvironment",
          "false"));

      if (!kickstarter)
      {
        addComponent(lblUserName);
        setComponentAlignment(lblUserName, Alignment.MIDDLE_RIGHT);
        addComponent(btLogin);
        setComponentAlignment(btLogin, Alignment.MIDDLE_RIGHT);

      }
    }
  }
  
  private void regenerateStateFromCookies()
  {
    Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
    if(cookies != null)
    {
      for(Cookie c : cookies )
      {
        if("annis-sidebar-state".equals(c.getName()))
        {
          try
          {
            sidebarState = SidebarState.valueOf(c.getValue());
            // don't be invisible
            if(sidebarState == SidebarState.AUTO_HIDDEN)
            {
              sidebarState = SidebarState.AUTO_VISIBLE;
            }
            else if(sidebarState == SidebarState.HIDDEN)
            {
              sidebarState = SidebarState.VISIBLE;
            }
          }
          catch(IllegalArgumentException ex)
          {
            log.debug("Invalid cookie for sidebar state", ex);
          }
        }
      }
    }
    updateSidebarState();
  }

  public void updateUserInformation()
  {
    if (lblUserName == null)
    {
      return;
    }
    if (isLoggedIn())
    {
      AnnisUser user = Helper.getUser();
      if (user != null)
      {
        lblUserName.setValue("logged in as \"" + ((AnnisUser) user).
          getUserName() + "\"");
        if (getComponentIndex(btLogin) > -1)
        {
          replaceComponent(btLogin, btLogout);
          setComponentAlignment(btLogout, Alignment.MIDDLE_RIGHT);
        }
        // do not show the logout button if the user cannot logout using ANNIS
        btLogout.setVisible(!user.isRemote());
      }
    }
    else
    {
      lblUserName.setValue("not logged in");
      if (getComponentIndex(btLogout) > -1)
      {
        replaceComponent(btLogout, btLogin);
        setComponentAlignment(btLogin, Alignment.MIDDLE_RIGHT);
      }
    }

  }

  @Override
  public void onLogin()
  {
    if (windowLogin != null)
    {
      UI.getCurrent().removeWindow(windowLogin);
    }

    AnnisUser user = Helper.getUser();

    if (user == null)
    {
      Object loginErrorOject = VaadinSession.getCurrent().getSession().
        getAttribute(USER_LOGIN_ERROR);
      if (loginErrorOject != null && loginErrorOject instanceof String)
      {
        Notification.show((String) loginErrorOject,
          Notification.Type.WARNING_MESSAGE);
      }
      VaadinSession.getCurrent().getSession().removeAttribute(
        AnnisBaseUI.USER_LOGIN_ERROR);
    }
    else if (user.getUserName() != null)
    {
      Notification.show("Logged in as \"" + user.getUserName() + "\"",
        Notification.Type.TRAY_NOTIFICATION);
    }
    updateUserInformation();
  }

  @Override
  public void onLogout()
  {
    updateUserInformation();
  }
  
  
  
  public boolean canReportBugs()
  {
    return this.bugEMailAddress != null;
  }

  public void reportBug()
  {
    reportBug(null);
  }

  public void reportBug(Throwable cause)
  {
    lastBugReportCause = cause;
    if(screenshotExtension.isAttached())
    {
      screenshotExtension.makeScreenshot();
      btBugReport.setCaption("problem report is initialized...");
    }
    else
    {
      Notification.show("This user interface does not allow screenshots. Can't report bug.", 
        Notification.Type.ERROR_MESSAGE);
    }
  }
  
  @Override
  public void screenshotReceived(byte[] imageData, String mimeType)
  {
    btBugReport.setEnabled(true);
    btBugReport.setCaption("Report Problem");

    if (bugEMailAddress != null)
    {
      ReportBugWindow reportBugWindow =
        new ReportBugWindow(bugEMailAddress, imageData, mimeType,
        lastBugReportCause);

      reportBugWindow.setModal(true);
      reportBugWindow.setResizable(true);
      UI.getCurrent().addWindow(reportBugWindow);
      reportBugWindow.center();
      lastBugReportCause = null;
    }
  }

  public ScreenshotMaker getScreenshotExtension()
  {
    return screenshotExtension;
  }
  
  

  private static class AboutClickListener implements Button.ClickListener
  {

    public AboutClickListener()
    {
    }

    @Override
    public void buttonClick(Button.ClickEvent event)
    {
      Window w = new AboutWindow();
      w.setCaption("About ANNIS");
      w.setModal(true);
      w.setResizable(true);
      w.setWidth("500px");
      w.setHeight("500px");
      UI.getCurrent().addWindow(w);
    }
  }

  public boolean isLoggedIn()
  {
    return Helper.getUser() != null;
  }
  
  private class LoginCloseCallback implements JavaScriptFunction
  {

    @Override
    public void call(JSONArray arguments) throws JSONException
    {
      for(LoginListener l : loginListeners)
      {
        l.onLogin();
      }
      onLogin();
    }
  }

}
