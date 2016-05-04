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

package annis.gui.admin.view;

import annis.security.User;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * Definition of interactions for a view displaying the user list.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface UserListView extends Serializable
{
  public void setUserList(Collection<User> users);
  
  public void setLoadingAnimation(boolean show);
  
  public void addListener(Listener listener);
  
  public void askForPasswordChange(String userName);
  
  public void emptyNewUserNameTextField();
  
  public void addAvailableGroupNames(Collection<String> groupNames);
  public void addAvailablePermissions(Collection<String> permissions);
  
  public interface Listener
  {
    public void userUpdated(User user);
    public void passwordChanged(String userName, String newPassword);
    
    public void addNewUser(String userName);
    public void deleteUsers(Set<String> userName);
  }
  
}
