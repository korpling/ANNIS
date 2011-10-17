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
import annis.gui.querybuilder.TigerQueryBuilder;
import annis.gui.resultview.ResultViewPanel;
import annis.gui.tutorial.TutorialPanel;
import annis.security.AnnisSecurityManager;
import annis.security.AnnisUser;
import annis.security.SimpleSecurityManager;
import annis.service.ifaces.AnnisCorpus;
import com.vaadin.ui.themes.ChameleonTheme;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.AuthenticationException;

/**
 *
 * @author thomas
 */
public class SearchWindow extends Window implements LoginForm.LoginListener
{

  // regular expression matching, CLEFT and CRIGHT are optional
  // indexes: AQL=1, CIDS=2, CLEFT=4, CRIGHT=6
  private Pattern citationPattern =
    Pattern.compile("AQL\\((.*)\\),CIDS\\(([^)]*)\\)(,CLEFT\\(([^)]*)\\),)?(CRIGHT\\(([^)]*)\\))?");

  
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

    mainTab.addTab(new TigerQueryBuilder(), "Query Builder", null);
    
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
  

  public void evaluateCitation(String relativeUri)
  {
    
    AnnisUser user = (AnnisUser) getApplication().getUser();
    if(user == null)
    {
      return;
    }

    Map<Long, AnnisCorpus> userCorpora = user.getCorpusList();
    Map<String, AnnisCorpus> name2Corpus = Helper.calculateName2Corpus(userCorpora);

    Matcher m = citationPattern.matcher(relativeUri);
    if(m.matches())
    {
      // AQL
      String aql = "";
      if(m.group(1) != null)
      {
        aql = m.group(1);
      }

      // CIDS      
      HashMap<Long, AnnisCorpus> selectedCorpora = new HashMap<Long, AnnisCorpus>();
      if(m.group(2) != null)
      {
        String[] cids = m.group(2).split(",");
        for(String name : cids)
        {
          AnnisCorpus c = name2Corpus.get(name);
          if(c != null)
          {
            selectedCorpora.put(c.getId(), c);
          }
        }
      }

      // CLEFT and CRIGHT
      if(m.group(4) != null && m.group(6) != null)
      {
        int cleft = 0;
        int cright = 0;
        try
        {
          cleft = Integer.parseInt(m.group(4));
          cright = Integer.parseInt(m.group(6));
        }
        catch(NumberFormatException ex)
        {
          Logger.getLogger(SearchWindow.class.getName()).log(Level.SEVERE, 
            "could not parse context value", ex);
        }
        control.setQuery(aql, selectedCorpora, cleft, cright);
      }
      else
      {
        control.setQuery(aql, selectedCorpora);
      }
      
    }
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
