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

package annis.gui.admin.controller;

import annis.gui.admin.model.UserManagement;
import annis.gui.admin.view.UIView;
import annis.gui.admin.view.UserListView;
import annis.security.User;
import com.google.common.base.Joiner;
import com.sun.jersey.api.client.WebResource;
import java.util.LinkedList;
import java.util.Set;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class UserController
  implements UserListView.Listener, UIView.Listener
{
  
  private final UserManagement model;
  private final UserListView view;
  private final UIView uiView;

  public UserController(UserManagement model,
    UserListView view, UIView uiView)
  {
    this.model = model;
    this.view = view;    
    this.uiView = uiView;
    view.addListener(UserController.this);
    uiView.addListener(UserController.this);
  }
  
  private void fetchFromService()
  {
    if(model.fetchFromService())
    {
      view.setUserList(model.getUsers());
    }
    
    else
    {
      uiView.showWarning("Cannot get the user list", null);
      view.setUserList(new LinkedList<User>());
    }
  }

  @Override
  public void userUpdated(User user)
  {
    model.createOrUpdateUser(user);
  }

  @Override
  public void passwordChanged(String userName, String newPassword)
  {
    model.setPassword(userName, newPassword);
  }

  @Override
  public void addNewUser(String userName)
  {
    if(userName == null || userName.isEmpty())
    {
      uiView.showError("User name is empty", null);
    }
    else if(model.getUser(userName) != null)
    {
      uiView.showError("User already exists", null);
    }
    else
    {
      // create new user with empty password
      User u = new User(userName);
      model.createOrUpdateUser(u);
      view.askForPasswordChange(userName); 
      view.setUserList(model.getUsers());
      view.emptyNewUserNameTextField();
    }
  }

  @Override
  public void deleteUsers(Set<String> userName)
  {
    for(String u : userName)
    {
      model.deleteUser(u);
    }
    view.setUserList(model.getUsers());
    
    if(userName.size() == 1)
    {
      uiView.showInfo("User \"" + userName.iterator().next() +  "\" was deleted", null);
    }
    else
    {
      uiView.showInfo("Deleted users: " + Joiner.on(", ").join(userName), null);
    }
  }

  @Override
  public void loginChanged(WebResource annisRootResource)
  {
    model.setRootResource(annisRootResource);
    fetchFromService();
  }

  @Override
  public void selectedTabChanged(Object selectedTab)
  {
    if(selectedTab == view)
    {
      fetchFromService();
    }
  }
  
  
  
}
