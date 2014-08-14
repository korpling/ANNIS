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
import annis.gui.admin.view.UserManagementView;
import annis.security.User;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class UserManagementController
  implements UserManagementView.Listener
{
  
  private final UserManagement model;
  private final UserManagementView view;

  public UserManagementController(UserManagement model, UserManagementView view)
  {
    this.model = model;
    this.view = view;
    
    model.fetchUsers();
    view.setUserList(model.getUsers());
    view.addListener(UserManagementController.this);
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
      view.showError("User name is empty");
    }
    else if(model.getUser(userName) != null)
    {
      view.showError("User already exists");
    }
    else
    {
      // create new user with empty password
      User u = new User(userName);
      model.createOrUpdateUser(u);
      view.askForPasswordChange(userName); 
      
      model.fetchUsers();
      view.setUserList(model.getUsers());
    }
  }
  
}
