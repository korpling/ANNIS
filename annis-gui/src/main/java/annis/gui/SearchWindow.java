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
import annis.gui.resultview.ResultViewPanel;
import annis.gui.tutorial.TutorialPanel;
import annis.security.AnnisSecurityManager;
import annis.security.AnnisUser;
import annis.security.SimpleSecurityManager;
import annis.service.ifaces.AnnisCorpus;
import com.vaadin.addon.chameleon.ChameleonTheme;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.AuthenticationException;

/**
 *
 * @author thomas
 */
public class SearchWindow extends Window implements LoginForm.LoginListener
{

  private Label lblUserName;
  private Button btLoginLogout;
  private ControlPanel control;
  private TutorialPanel tutorial;
  private TabSheet mainTab;
  private Window windowLogin;
  private ResultViewPanel resultView;
  private AnnisSecurityManager securityManager;
  private PluginSystem ps;
  
  public SearchWindow(PluginSystem ps)
  {
    super("Annis² Corpus Search");
    this.ps = ps;
    
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

    lblUserName = new Label("not logged in");
    lblUserName.setWidth("100%");
    lblUserName.setHeight("-1px");
    lblUserName.addStyleName("right-aligned-text");

    btLoginLogout = new Button("Login", new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        if(isLoggedIn())
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


    layoutToolbar.addComponent(lblUserName);
    layoutToolbar.addComponent(btLoginLogout);

    layoutToolbar.setSpacing(true);
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

    hLayout.addComponent(mainTab);
    hLayout.setExpandRatio(mainTab, 1.0f);


  }

  @Override
  public void attach()
  {
    super.attach();
    
    initSecurityManager();    
    updateUserInformation();
    
  }

  
  
  private void initSecurityManager()
  {
    securityManager = new SimpleSecurityManager();

    Enumeration<?> parameterNames = getApplication().getPropertyNames();
    Properties properties = new Properties();
    while(parameterNames.hasMoreElements())
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
    if(btLoginLogout == null || lblUserName == null || user == null)
    {
      return;
    }
    if(isLoggedIn())
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

  public void showQueryResult(String aql, Map<Long, AnnisCorpus> corpora, int contextLeft,
    int contextRight, int pageSize)
  {
    // remove old result from view
    if(resultView != null)
    {
      mainTab.removeComponent(resultView);
    }
    resultView = new ResultViewPanel(aql, corpora, contextLeft, contextRight,
      pageSize, ps);
    mainTab.addTab(resultView, "Query Result", null);
    mainTab.setSelectedTab(resultView);
  }

  public void updateQueryCount(int count)
  {
    if(resultView != null && count >= 0)
    {
      resultView.setCount(count);
    }
  }

  private void showLoginWindow()
  {
    LoginForm login = new LoginForm();

    login.addListener((LoginForm.LoginListener) this);

    if(windowLogin == null)
    {
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
      AnnisUser newUser = securityManager.login(event.getLoginParameter("username"),
        event.getLoginParameter("password"), true);
      getApplication().setUser(newUser);
      showNotification("Logged in as \"" + newUser.getUserName() + "\"",
        Window.Notification.TYPE_TRAY_NOTIFICATION);
    }
    catch(AuthenticationException ex)
    {
      showNotification("Authentification error: " + ex.getMessage(),
        Window.Notification.TYPE_ERROR_MESSAGE);
    }
    catch(Exception ex)
    {
      Logger.getLogger(SearchWindow.class.getName()).log(Level.SEVERE, null, ex);

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
    if(u == null || AnnisSecurityManager.FALLBACK_USER.equals(u.getUserName()))
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

  
  
}
