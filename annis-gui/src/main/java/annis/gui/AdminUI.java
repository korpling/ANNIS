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

import annis.gui.admin.GroupManagementPanel;
import annis.gui.admin.ImportPanel;
import annis.gui.admin.UserManagementPanel;
import annis.gui.admin.controller.GroupManagementController;
import annis.gui.admin.controller.UserManagementController;
import annis.gui.admin.model.GroupManagement;
import annis.gui.admin.model.UserManagement;
import annis.gui.admin.view.UIView;
import annis.gui.admin.view.UserManagementView;
import annis.libgui.AnnisBaseUI;
import annis.libgui.Helper;
import com.vaadin.annotations.Theme;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
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
public class AdminUI extends AnnisBaseUI implements UIView, LoginListener
{
  private VerticalLayout layout;
  
  private UserManagementController
     userController;
  private GroupManagementController
     groupManagementController;
  
  private final List<UIView.Listener> listeners = new LinkedList<>();
  
  @Override
  protected void init(VaadinRequest request)
  {
    super.init(request);
    
    UserManagementPanel userManagementPanel = new UserManagementPanel();
    UserManagement userManagement = new UserManagement();
    userManagement.setRootResource(Helper.getAnnisWebResource());
    userController = new UserManagementController(userManagement,
      userManagementPanel, this);
    
    GroupManagementPanel groupManagementPanel = new GroupManagementPanel();
    GroupManagement groupManagement = new GroupManagement();
    groupManagement.setRootResource(Helper.getAnnisWebResource());
    groupManagementController = new GroupManagementController(groupManagement,
      groupManagementPanel, this);
    
    
    TabSheet tabSheet = new TabSheet();
    tabSheet.addTab(new ImportPanel(), "Import Corpus", new ThemeResource("images/tango-icons/16x16/document-save.png"));
    tabSheet.addTab(userManagementPanel, "User management", new ThemeResource("images/tango-icons/16x16/user-info.png"));
    tabSheet.addTab(groupManagementPanel, "Group management", new ThemeResource("images/tango-icons/16x16/system-users.png"));
    tabSheet.setSizeFull();

    
    MainToolbar toolbar = new MainToolbar(null);
    addExtension(toolbar.getScreenshotExtension());
    toolbar.addLoginListener(AdminUI.this);
   
    layout = new VerticalLayout(toolbar, tabSheet);
    layout.setSizeFull();
    
    layout.setExpandRatio(toolbar, 0.0f);
    layout.setExpandRatio(tabSheet, 1.0f);
    
    setContent(layout);

  }

  @Override
  public void addListener(UIView.Listener listener)
  {
    listeners.add(listener);
  }
  

  @Override
  public void showInfo(String info)
  {
    Notification.show(info, Notification.Type.HUMANIZED_MESSAGE);
  }

  @Override
  public void showError(String error)
  {
    Notification.show(error, Notification.Type.ERROR_MESSAGE);
  }
  
  @Override
  public void onLogin()
  {
    for(UIView.Listener l : listeners)
    {
      l.loginChanged(Helper.getAnnisWebResource());
    }
  }

  @Override
  public void onLogout()
  {
    for(UIView.Listener l : listeners)
    {
      l.loginChanged(Helper.getAnnisWebResource());
    }
  }
  
}
