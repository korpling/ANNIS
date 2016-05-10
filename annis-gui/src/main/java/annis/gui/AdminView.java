/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.common.util.concurrent.FutureCallback;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import annis.gui.admin.CorpusAdminPanel;
import annis.gui.admin.GroupManagementPanel;
import annis.gui.admin.ImportPanel;
import annis.gui.admin.UserManagementPanel;
import annis.gui.admin.controller.CorpusController;
import annis.gui.admin.controller.GroupController;
import annis.gui.admin.controller.UserController;
import annis.gui.admin.model.CorpusManagement;
import annis.gui.admin.model.GroupManagement;
import annis.gui.admin.model.UserManagement;
import annis.gui.admin.model.WebResourceProvider;
import annis.gui.admin.view.UIView;
import annis.libgui.Background;
import annis.libgui.Helper;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AdminView extends VerticalLayout implements View,
  UIView, LoginListener, TabSheet.SelectedTabChangeListener, WebResourceProvider
{

  public static final String NAME = "admin";

  private final List<UIView.Listener> listeners = new LinkedList<>();

  private final TabSheet tabSheet;

  private final ImportPanel importPanel;

  private final CorpusAdminPanel corpusAdminPanel;

  private final UserManagementPanel userManagementPanel;

  private final GroupManagementPanel groupManagementPanel;

  private MainToolbar toolbar;

  private transient WebResource webResource;

  private transient AsyncWebResource asyncWebResource;
  
  public AdminView(AnnisUI ui)
  {
    Page.getCurrent().setTitle("ANNIS Adminstration");

    UserManagement userManagement = new UserManagement();
    userManagement.setWebResourceProvider(AdminView.this);
    GroupManagement groupManagement = new GroupManagement();
    groupManagement.setWebResourceProvider(AdminView.this);
    CorpusManagement corpusManagement = new CorpusManagement();
    corpusManagement.setWebResourceProvider(AdminView.this);

    boolean isLoggedIn = Helper.getUser() != null;

    corpusAdminPanel = new CorpusAdminPanel();
    new CorpusController(corpusManagement, corpusAdminPanel,
      this, isLoggedIn);

    userManagementPanel = new UserManagementPanel();
    new UserController(userManagement,
      userManagementPanel, this, isLoggedIn);

    groupManagementPanel = new GroupManagementPanel();
    new GroupController(groupManagement,
      corpusManagement,
      groupManagementPanel, this, userManagementPanel, isLoggedIn);

    importPanel = new ImportPanel();

    tabSheet = new TabSheet();
    tabSheet.addTab(importPanel, "Import Corpus", FontAwesome.UPLOAD);
    tabSheet.addTab(corpusAdminPanel, "Corpus management", FontAwesome.LIST_ALT);
    tabSheet.addTab(userManagementPanel, "User management", FontAwesome.USER);
    tabSheet.addTab(groupManagementPanel, "Group management",
      FontAwesome.USERS);

    tabSheet.setSizeFull();

    tabSheet.addSelectedTabChangeListener(AdminView.this);

    addComponents(tabSheet);
    setSizeFull();

    setExpandRatio(tabSheet, 1.0f);

    tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);

  }

  public void setToolbar(MainToolbar newToolbar)
  {
    // remove old one if necessary
    if (this.toolbar != null)
    {
      removeComponent(this.toolbar);
      this.toolbar = null;
    }

    // add new toolbar
    if (newToolbar != null)
    {
      this.toolbar = newToolbar;
      addComponent(this.toolbar, 0);
      setExpandRatio(this.toolbar, 0.0f);
      this.toolbar.addLoginListener(this);
    }
  }

  @Override
  public void enter(ViewChangeListener.ViewChangeEvent event)
  {

    boolean kickstarter = Helper.isKickstarter(getSession());

    importPanel.updateMode(kickstarter, Helper.getUser() != null);

    // group and user management are not applicable in kickstarter
    tabSheet.getTab(groupManagementPanel).setVisible(!kickstarter);
    tabSheet.getTab(userManagementPanel).setVisible(!kickstarter);

    Component selectedTab = getComponentForFragment(event.getParameters());
    if(selectedTab != null && selectedTab != tabSheet.getSelectedTab())
    {
      // Select the component given by the fragment, This will call
      // the selection change handler and thus we don't have
      // to call the listeners here. 
      tabSheet.setSelectedTab(selectedTab);
    }
    else
    {
      // nothing to change in the tab selection, call the listeners manually
      selectedTab = tabSheet.getSelectedTab();
      for (UIView.Listener l : listeners)
      {
        l.loadedTab(selectedTab);
      } 
      setFragmentParameter(getFragmentForComponent(selectedTab));
    }

  }

  @Override
  public void detach()
  {
    // inform the controllers that no tab is active any longer
    for(UIView.Listener l : listeners)
    {
      l.loadedTab(null);
    }
    
    super.detach();
  }
  
  

  private Component getComponentForFragment(String fragment)
  {
    if (fragment != null)
    {
      switch (fragment)
      {
        case "import":
          return importPanel;
        case "corpora":
          return corpusAdminPanel;
        case "users":
          return userManagementPanel;
        case "groups":
          return groupManagementPanel;
        default:
          break;
      }
    }
    return null;
  }
  
  private String getFragmentForComponent(Component c)
  {
    if (c == importPanel)
    {
      return "import";
    }
    else if (c == corpusAdminPanel)
    {
      return "corpora";
    }
    else if (c == userManagementPanel)
    {
      return "users";
    }
    else if (c == groupManagementPanel)
    {
      return  "groups";
    }
    return "";
  }

  @Override
  public void selectedTabChange(TabSheet.SelectedTabChangeEvent event)
  {
    Component selected = event.getTabSheet().getSelectedTab();
    
    for (UIView.Listener l : listeners)
    {
      l.loadedTab(selected);
    }
    setFragmentParameter(getFragmentForComponent(selected));
  }

  private void setFragmentParameter(String param)
  {
    Page.getCurrent().setUriFragment("!" + NAME + "/" + param, false);
  }

  @Override
  public void addListener(UIView.Listener listener)
  {
    listeners.add(listener);
  }

  @Override
  public void showInfo(String info, String description)
  {
    Notification.show(info, description, Notification.Type.HUMANIZED_MESSAGE);
  }

  @Override
  public void showBackgroundInfo(String info, String description)
  {
    Notification.show(info, description, Notification.Type.TRAY_NOTIFICATION);
  }

  @Override
  public void showWarning(String error, String description)
  {
    Notification.show(error, description, Notification.Type.WARNING_MESSAGE);
  }

  @Override
  public void showError(String error, String description)
  {
    Notification.show(error, description, Notification.Type.ERROR_MESSAGE);
  }

  @Override
  public void onLogin()
  {
    for (UIView.Listener l : listeners)
    {
      l.loginChanged(true);
    }
    // TODO: make import panel a normal UI view listener
    if (importPanel != null)
    {
      importPanel.onLogin();
    }
  }

  @Override
  public void onLogout()
  {
    for (UIView.Listener l : listeners)
    {
      l.loginChanged(false);
    }
    // TODO: make import panel a normal UI view listener
    if (importPanel != null)
    {
      importPanel.onLogout();
    }
  }

  @Override
  public <T> void runInBackground(Callable<T> job,
    final FutureCallback<T> callback)
  {
    Background.runWithCallback(job, callback);
  }

  @Override
  public WebResource getWebResource()
  {
    if (webResource == null)
    {
      webResource = Helper.getAnnisWebResource();
    }
    return webResource;
  }

  @Override
  public AsyncWebResource getAsyncWebResource()
  {
    if (asyncWebResource == null)
    {
      asyncWebResource = Helper.getAnnisAsyncWebResource();
    }
    return asyncWebResource;
  }

  @Override
  public void invalidateWebResource()
  {
    asyncWebResource = null;
    webResource = null;
  }

}
