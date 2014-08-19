/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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

import annis.gui.admin.CorpusAdminPanel;
import annis.gui.admin.GroupManagementPanel;
import annis.gui.admin.ImportPanel;
import annis.gui.admin.UserManagementPanel;
import annis.gui.admin.controller.CorpusController;
import annis.gui.admin.controller.GroupController;
import annis.gui.admin.controller.UserController;
import annis.gui.admin.model.GroupManagement;
import annis.gui.admin.model.UserManagement;
import annis.gui.admin.view.UIView;
import annis.gui.admin.model.CorpusManagement;
import annis.libgui.AnnisBaseUI;
import annis.libgui.Helper;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.annotations.Theme;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Theme("annis")
public class AdminUI extends AnnisBaseUI implements UIView, LoginListener,
  Page.UriFragmentChangedListener, TabSheet.SelectedTabChangeListener
{

  private VerticalLayout layout;

  private UserController userController;

  private GroupController groupManagementController;

  private CorpusController corpusController;

  private final List<UIView.Listener> listeners = new LinkedList<>();

  private TabSheet tabSheet;

  private ImportPanel importPanel;

  private CorpusAdminPanel corpusAdminPanel;

  private UserManagementPanel userManagementPanel;

  private GroupManagementPanel groupManagementPanel;

  @Override
  protected void init(VaadinRequest request)
  {
    super.init(request);

    getPage().setTitle("ANNIS Adminstration");
    
    WebResource rootResource = Helper.getAnnisWebResource();

    UserManagement userManagement = new UserManagement();
    userManagement.setRootResource(rootResource);
    GroupManagement groupManagement = new GroupManagement();
    groupManagement.setRootResource(rootResource);
    CorpusManagement corpusManagement = new CorpusManagement();
    corpusManagement.setRootResource(rootResource);

    corpusAdminPanel = new CorpusAdminPanel();
    corpusController = new CorpusController(corpusManagement, corpusAdminPanel,
      this);

    userManagementPanel = new UserManagementPanel();
    userController = new UserController(userManagement,
      userManagementPanel, this);

    groupManagementPanel = new GroupManagementPanel();
    groupManagementController = new GroupController(groupManagement,
      corpusManagement,
      groupManagementPanel, this, userManagementPanel);

    importPanel = new ImportPanel();

    tabSheet = new TabSheet();
    tabSheet.addTab(importPanel, "Import Corpus", new ThemeResource(
      "images/tango-icons/16x16/document-save.png"));
    tabSheet.addTab(corpusAdminPanel, "Corpus management", new ThemeResource(
      "images/tango-icons/16x16/system-file-manager.png"));
    tabSheet.addTab(userManagementPanel, "User management", new ThemeResource(
      "images/tango-icons/16x16/user-info.png"));
    tabSheet.addTab(groupManagementPanel, "Group management", new ThemeResource(
      "images/tango-icons/16x16/system-users.png"));
    tabSheet.setSizeFull();

    tabSheet.addSelectedTabChangeListener(this);

    MainToolbar toolbar = new MainToolbar(null);
    addExtension(toolbar.getScreenshotExtension());
    toolbar.addLoginListener(AdminUI.this);

    layout = new VerticalLayout(toolbar, tabSheet);
    layout.setSizeFull();

    layout.setExpandRatio(toolbar, 0.0f);
    layout.setExpandRatio(tabSheet, 1.0f);

    setContent(layout);

    getPage().addUriFragmentChangedListener(this);
    
    selectTabFromFragment(getPage().getUriFragment());

  }

  @Override
  public void uriFragmentChanged(Page.UriFragmentChangedEvent event)
  {
    selectTabFromFragment(event.getUriFragment());
  }
  
  private void selectTabFromFragment(String fragment)
  {
    if(fragment == null)
    {
      return;
    }
    switch (fragment)
    {
      case "import":
        tabSheet.setSelectedTab(importPanel);
        break;
      case "corpora":
        tabSheet.setSelectedTab(corpusAdminPanel);
        break;
      case "users":
        tabSheet.setSelectedTab(userManagementPanel);
        break;
      case "groups":
        tabSheet.setSelectedTab(groupManagementPanel);
        break;
    }
  }

  @Override
  public void selectedTabChange(TabSheet.SelectedTabChangeEvent event)
  {
    Component selected = event.getTabSheet().getSelectedTab();
    
    for (UIView.Listener l : listeners)
    {
      l.selectedTabChanged(selected);
    }
    
    if(selected == importPanel)
    {
      getPage().setUriFragment("import", false);
    }
    else if(selected == corpusAdminPanel)
    {
      getPage().setUriFragment("corpora", false);
    }
    else if(selected == userManagementPanel)
    {
      getPage().setUriFragment("users", false);
    }
    else if(selected == groupManagementPanel)
    {
      getPage().setUriFragment("groups", false);
    }
    
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
      l.loginChanged(Helper.getAnnisWebResource());
    }
  }

  @Override
  public void onLogout()
  {
    for (UIView.Listener l : listeners)
    {
      l.loginChanged(Helper.getAnnisWebResource());
    }
  }

}
