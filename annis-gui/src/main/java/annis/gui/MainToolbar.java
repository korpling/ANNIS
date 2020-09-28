/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui;

import annis.gui.components.ScreenshotMaker;
import annis.gui.components.SettingsStorage;
import annis.libgui.AnnisBaseUI;
import annis.libgui.Helper;
import annis.libgui.IDGenerator;
import annis.libgui.LoginDataLostException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.validator.EmailValidator;
import com.vaadin.v7.ui.themes.BaseTheme;
import java.util.LinkedHashSet;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * The ANNIS main toolbar. Handles login, showing the sidebar (if it exists), the screenshot making
 * and some information windows.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class MainToolbar extends HorizontalLayout
    implements ScreenshotMaker.ScreenshotCallback, SettingsStorage.LoadedListener {

  private static class AboutClickListener implements Button.ClickListener {

    /**
     * 
     */
    private static final long serialVersionUID = 7147113799757433869L;

    public AboutClickListener() {}

    @Override
    public void buttonClick(Button.ClickEvent event) {
      Window w = new AboutWindow();
      w.setCaption("About ANNIS");
      w.setModal(true);
      w.setResizable(true);
      w.setWidth("500px");
      w.setHeight("500px");
      UI.getCurrent().addWindow(w);

    }
  }

  public enum NavigationTarget {

    SEARCH(SearchView.NAME, "Search interface", FontAwesome.SEARCH), ADMIN(AdminView.NAME,
        "Administration", FontAwesome.WRENCH);

    private final String caption;

    private final String state;

    private final Resource icon;

    private NavigationTarget(String state, String caption, Resource icon) {
      this.caption = caption;
      this.state = state;
      this.icon = icon;
    }

  }

  /**
   * 
   */
  private static final long serialVersionUID = -6033428470667608345L;

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(MainToolbar.class);

  public static final String BUG_MAIL_KEY = "bug-e-mail";

  public static final String LOGIN_URL_KEY = "login-url";

  public static final String LOGIN_MAXIMIZED_KEY = "login-window-maximized";

  private Button btSidebar;

  private final Button btNavigate;

  private NavigationTarget navigationTarget;

  private final Button btLogin;

  private final Button btLogout;

  private final Button btBugReport;

  private final Button btAboutAnnis;

  private final Button btOpenSource;

  private final Label lblUserName;

  private final String bugEMailAddress;

  private SidebarState sidebarState = SidebarState.VISIBLE;

  private final LinkedHashSet<LoginListener> loginListeners = new LinkedHashSet<>();

  private Throwable lastBugReportCause;

  private Sidebar sidebar;

  private ScreenshotMaker screenshotExtension;

  private QueryController queryController;

  public MainToolbar() {

    String bugmail = null;
    if (UI.getCurrent() instanceof AnnisUI) {
      bugmail = ((AnnisUI) UI.getCurrent()).getConfig().getBugEmail();
    }
    if (bugmail != null && !bugmail.isEmpty() && !bugmail.startsWith("${")
        && new EmailValidator("").isValid(bugmail)) {
      this.bugEMailAddress = bugmail;
    } else {
      this.bugEMailAddress = null;
    }

    UI ui = UI.getCurrent();
    if (ui instanceof CommonUI) {
      ((CommonUI) ui).getSettings().addedLoadedListener(MainToolbar.this);
    }

    setWidth("100%");
    setHeight("-1px");

    addStyleName("toolbar");
    addStyleName("border-layout");

    btAboutAnnis = new Button("About ANNIS");
    btAboutAnnis.addStyleName(ValoTheme.BUTTON_SMALL);
    btAboutAnnis.setIcon(new ThemeResource("images/annis_16.png"));
    btAboutAnnis.addClickListener(new AboutClickListener());

    btSidebar = new Button();
    btSidebar.setDisableOnClick(true);
    btSidebar.addStyleName(ValoTheme.BUTTON_SMALL);
    btSidebar.setDescription("Show and hide search sidebar");
    btSidebar.setIconAlternateText(btSidebar.getDescription());

    btBugReport = new Button("Report Problem");
    btBugReport.addStyleName(ValoTheme.BUTTON_SMALL);
    btBugReport.setDisableOnClick(true);
    btBugReport.setIcon(FontAwesome.ENVELOPE_O);
    btBugReport.addClickListener(event -> reportBug());
    btBugReport.setVisible(this.bugEMailAddress != null);

    btNavigate = new Button();
    btNavigate.setVisible(false);
    btNavigate.setDisableOnClick(true);
    btNavigate.addClickListener(event -> {
      btNavigate.setEnabled(true);
      if (navigationTarget != null) {
        UI.getCurrent().getNavigator().navigateTo(navigationTarget.state);
      }
    });
    lblUserName = new Label("not logged in");
    lblUserName.setWidth("-1px");
    lblUserName.setHeight("-1px");
    lblUserName.addStyleName("right-aligned-text");

    btLogin = new Button("Login", (ClickListener) event -> showLoginWindow(false));

    btLogout = new Button("Logout", (ClickListener) event -> {
      // logout
      Page.getCurrent().setLocation("logout");
      UI.getCurrent().getSession().close();

      for (LoginListener l : loginListeners) {
        l.onLogout();
      }
      Notification.show("Logged out", Notification.Type.TRAY_NOTIFICATION);
      updateUserInformation();
    });

    btLogin.setSizeUndefined();
    btLogin.setStyleName(ValoTheme.BUTTON_SMALL);
    btLogin.setIcon(FontAwesome.USER);

    btLogout.setSizeUndefined();
    btLogout.setStyleName(ValoTheme.BUTTON_SMALL);
    btLogout.setIcon(FontAwesome.USER);

    btOpenSource = new Button("Help us make ANNIS better!");
    btOpenSource.setStyleName(BaseTheme.BUTTON_LINK);
    btOpenSource.addClickListener(event -> {
      Window w = new HelpUsWindow();
      w.setCaption("Help us make ANNIS better!");
      w.setModal(true);
      w.setResizable(true);
      w.setWidth("600px");
      w.setHeight("500px");
      UI.getCurrent().addWindow(w);
      w.center();
    });

    addComponent(btSidebar);
    setComponentAlignment(btSidebar, Alignment.MIDDLE_LEFT);

    addComponent(btAboutAnnis);
    addComponent(btBugReport);
    addComponent(btNavigate);

    addComponent(btOpenSource);

    setSpacing(true);
    setComponentAlignment(btAboutAnnis, Alignment.MIDDLE_LEFT);
    setComponentAlignment(btBugReport, Alignment.MIDDLE_LEFT);
    setComponentAlignment(btNavigate, Alignment.MIDDLE_LEFT);

    setComponentAlignment(btOpenSource, Alignment.MIDDLE_CENTER);
    setExpandRatio(btOpenSource, 1.0f);

    addLoginButton();

    btSidebar.addClickListener(event -> {
      btSidebar.setEnabled(true);

      // decide new state
      switch (sidebarState) {
        case VISIBLE:
          if (event.isCtrlKey()) {
            sidebarState = SidebarState.AUTO_VISIBLE;
          } else {
            sidebarState = SidebarState.HIDDEN;
          }
          break;
        case HIDDEN:
          if (event.isCtrlKey()) {
            sidebarState = SidebarState.AUTO_HIDDEN;
          } else {
            sidebarState = SidebarState.VISIBLE;
          }
          break;

        case AUTO_VISIBLE:
          if (event.isCtrlKey()) {
            sidebarState = SidebarState.VISIBLE;
          } else {
            sidebarState = SidebarState.AUTO_HIDDEN;
          }
          break;
        case AUTO_HIDDEN:
          if (event.isCtrlKey()) {
            sidebarState = SidebarState.HIDDEN;
          } else {
            sidebarState = SidebarState.AUTO_VISIBLE;
          }
          break;
      }

      updateSidebarState();
      updateUserInformation();
    });

    screenshotExtension = new ScreenshotMaker(this);

    updateSidebarState();
  }

  /**
   * Adds the login button + login text to the toolbar. This is only happened, when the gui is not
   * started in desktop mode.
   *
   * <p>
   * The Kickstarter overrides the "desktopMode" application parameter and set it to "true", so the
   * gui can detect, that is not necessary to offer a login button.
   * </p>
   *
   * component.
   */
  private void addLoginButton() {

    boolean desktopMode = false;
    UI ui = UI.getCurrent();
    if (ui instanceof AnnisUI) {
      desktopMode = ((AnnisUI) ui).isDesktopMode();
    }

    if (!desktopMode) {
      addComponent(lblUserName);
      setComponentAlignment(lblUserName, Alignment.MIDDLE_RIGHT);
      addComponent(btLogin);
      setComponentAlignment(btLogin, Alignment.MIDDLE_RIGHT);

    }

  }

  public void addLoginListener(LoginListener listener) {
    this.loginListeners.add(listener);
  }

  @Override
  public void attach() {
    super.attach();

    UI ui = UI.getCurrent();

    MainToolbar.this.updateUserInformation();

    if (ui instanceof AnnisBaseUI) {
      ((AnnisBaseUI) ui).getLoginDataLostBus().register(this);
    }

    IDGenerator.assignIDForFields(MainToolbar.this, btAboutAnnis, btOpenSource);
  }

  public boolean canReportBugs() {
    return this.bugEMailAddress != null;
  }

  @Override
  public void detach() {
    UI ui = UI.getCurrent();
    if (ui instanceof AnnisBaseUI) {
      ((AnnisBaseUI) ui).getLoginDataLostBus().unregister(this);
    }

    super.detach();
  }

  public QueryController getQueryController() {
    return queryController;
  }

  public ScreenshotMaker getScreenshotExtension() {
    return screenshotExtension;

  }

  public Sidebar getSidebar() {
    return sidebar;
  }

  @Subscribe
  public void handleLoginDataLostException(LoginDataLostException ex) {

    Notification.show("Login data was lost, please login again.",
        "Due to a server misconfiguration the login-data was lost. Please contact the adminstrator of this ANNIS instance.",
        Notification.Type.WARNING_MESSAGE);

    for (LoginListener l : loginListeners) {
      try {
        l.onLogout();
      } catch (Exception loginEx) {
        log.error("exception thrown while notifying login listeners", loginEx);
      }
    }
    updateUserInformation();

  }

  public boolean isLoggedIn() {
    return Helper.getUser().isPresent();
  }

  public void notifiyQueryStarted() {
    if (sidebarState == SidebarState.AUTO_VISIBLE) {
      sidebarState = SidebarState.AUTO_HIDDEN;
    }

    updateSidebarState();
  }

  public void onLogin() {
    for (LoginListener l : loginListeners) {
      try {
        l.onLogin();
      } catch (Exception ex) {
        log.error("exception thrown while notifying login listeners", ex);
      }
    }

    updateUserInformation();
  }

  public void onLogout() {

    for (LoginListener l : loginListeners) {
      try {
        l.onLogout();;
      } catch (Exception ex) {
        log.error("exception thrown while notifying login listeners", ex);
      }
    }

    updateUserInformation();
  }

  @Override
  public void onSettingsLoaded(SettingsStorage settings) {
    String sidebarStateSetting = settings.get("annis-sidebar-state");
    if (sidebarStateSetting != null) {
      try {
        sidebarState = SidebarState.valueOf(sidebarStateSetting);
        // don't be invisible
        if (sidebarState == SidebarState.AUTO_HIDDEN) {
          sidebarState = SidebarState.AUTO_VISIBLE;
        } else if (sidebarState == SidebarState.HIDDEN) {
          sidebarState = SidebarState.VISIBLE;
        }
      } catch (IllegalArgumentException ex) {
        log.debug("Invalid cookie for sidebar state", ex);
      }
    }
    updateSidebarState();
  }

  public void removeLoginListener(LoginListener listener) {
    this.loginListeners.remove(listener);
  }

  public void reportBug() {
    reportBug(null);
  }

  public void reportBug(Throwable cause) {
    lastBugReportCause = cause;
    if (screenshotExtension.isAttached()) {
      screenshotExtension.makeScreenshot();
      btBugReport.setCaption("problem report is initialized...");
    } else {
      Notification.show("This user interface does not allow screenshots. Can't report bug.",
          Notification.Type.ERROR_MESSAGE);
    }
  }

  @Override
  public void screenshotReceived(byte[] imageData, String mimeType) {
    btBugReport.setEnabled(true);
    btBugReport.setCaption("Report Problem");

    if (bugEMailAddress != null) {
      ReportBugWindow reportBugWindow =
          new ReportBugWindow(bugEMailAddress, imageData, mimeType, lastBugReportCause);

      reportBugWindow.setModal(true);
      reportBugWindow.setResizable(true);
      UI.getCurrent().addWindow(reportBugWindow);
      reportBugWindow.center();
      lastBugReportCause = null;
    }
  }

  public void setNavigationTarget(NavigationTarget target, UI ui) {
    if (target == this.navigationTarget) {
      // nothing changed, return
    }

    this.navigationTarget = target;
    btNavigate.setVisible(false);

    if (target == NavigationTarget.ADMIN) {
      // check in background if display is necessary
      updateAdministratorButtonVisibility(Helper.getUser());
    } else if (target != null) {
      btNavigate.setVisible(true);
      btNavigate.setCaption(target.caption);
      btNavigate.setIcon(target.icon);
    }

  }

  public void setQueryController(QueryController queryController) {
    this.queryController = queryController;
  }

  public void setSidebar(Sidebar sidebar) {
    this.sidebar = sidebar;
    btSidebar.setVisible(sidebar != null);
    updateSidebarState();
  }

  public void showLoginWindow(boolean executeQueryAfterLogin) {
    Page.getCurrent().setLocation("login");
    // TODO: handle the case we need to execute a query after login
  }

  private void updateSidebarState() {
    if (sidebar != null && btSidebar != null) {
      btSidebar.setIcon(sidebarState.getIcon());
      sidebar.updateSidebarState(sidebarState);
    }
  }

  private void updateAdministratorButtonVisibility(Optional<OidcUser> user) {
    // We don't verify the provided token, this is the job of the backend.
    // This only decides if the Administrator button is visible
    if (user.isPresent() && user.get().containsClaim("roles")
        && user.get().getClaimAsStringList("roles").contains("admin")) {
      // make the administration button visible
      btNavigate.setCaption(NavigationTarget.ADMIN.caption);
      btNavigate.setIcon(NavigationTarget.ADMIN.icon);
      btNavigate.setVisible(true);
    }

  }

  private void updateUserInformation() {
    if (lblUserName == null) {
      return;
    }

    if (navigationTarget == NavigationTarget.ADMIN) {
      // don't show administration link per default
      btNavigate.setVisible(false);
    }

    Optional<OidcUser> user = Helper.getUser();
    if (user.isPresent()) {
      // logged in
      String displayName = Helper.getDisplayName(user.get());
      Notification.show("Logged in as \"" + displayName + "\"",
          Notification.Type.TRAY_NOTIFICATION);

      lblUserName.setValue("logged in as \"" + displayName + "\"");


      if (getComponentIndex(btLogin) > -1) {
        replaceComponent(btLogin, btLogout);
        setComponentAlignment(btLogout, Alignment.MIDDLE_RIGHT);
      }

      if (navigationTarget == NavigationTarget.ADMIN) {
        updateAdministratorButtonVisibility(user);
      }

    } else {
      lblUserName.setValue("not logged in");
      if (getComponentIndex(btLogout) > -1) {
        replaceComponent(btLogout, btLogin);
        setComponentAlignment(btLogin, Alignment.MIDDLE_RIGHT);
      }
    }

  }
}
