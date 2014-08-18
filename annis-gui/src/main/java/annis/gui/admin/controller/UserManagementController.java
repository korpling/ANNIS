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
import annis.gui.admin.view.UserManagementView;
import annis.security.User;
import com.google.common.base.Joiner;
import com.sun.jersey.api.client.WebResource;
import java.util.LinkedList;
import java.util.Set;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class UserManagementController
  implements UserManagementView.Listener, UIView.Listener
{
  
  private final UserManagement model;
  private final UserManagementView view;
  private final UIView uiView;

  public UserManagementController(UserManagement model, UserManagementView view, UIView uiView)
  {
    this.model = model;
    this.view = view;    
    this.uiView = uiView;
    view.addListener(UserManagementController.this);
    uiView.addListener(UserManagementController.this);
  }
  
  private void updateUserList()
  {
    if(model.fetchUsers())
    {
      view.setUserList(model.getUsers());
    }
    
    else
    {
      uiView.showError("Cannot get the user list");
      view.setUserList(new LinkedList<User>());
    }
  }

  @Override
  public void attached()
  {
    updateUserList();
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
      uiView.showError("User name is empty");
    }
    else if(model.getUser(userName) != null)
    {
      uiView.showError("User already exists");
    }
    else
    {
      // create new user with empty password
      User u = new User(userName);
      model.createOrUpdateUser(u);
      view.askForPasswordChange(userName); 
      view.setUserList(model.getUsers());
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
      uiView.showInfo("User \"" + userName.iterator().next() +  "\" was deleted");
    }
    else
    {
      uiView.showInfo("Deleted users: " + Joiner.on(", ").join(userName));
    }
  }

  @Override
  public void loginChanged(WebResource annisRootResource)
  {
    model.setRootResource(annisRootResource);
    updateUserList();
  }
  
  
}
