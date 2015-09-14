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
package annis.gui.admin;

import annis.security.User;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.declarative.Design;

/**
 * UI to edit the properties of a single user.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@DesignRoot
public class EditSingleUser extends Panel
{

  Label lblUser;

  Button btSave;

  Button btCancel;

  PopupTwinColumnSelect groupSelector;
  PopupTwinColumnSelect permissionSelector;

  private User user = new User();

  public EditSingleUser()
  {
    Design.read(EditSingleUser.this);
    
    btSave.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        Notification.show("Clicked");
      }
    });
  }

  @Override
  public void attach()
  {
    super.attach();
  }

  public User getUser()
  {
    return user;
  }

  public void setUser(User user)
  {
    this.user = user;
    lblUser.setValue(user.getName());
    groupSelector.setPropertyDataSource(new ObjectProperty(user.getGroups()));
    permissionSelector.setPropertyDataSource(new ObjectProperty(user.getPermissions()));
  }

  public void setGroupsContainer(IndexedContainer groupsContainer)
  {
    groupSelector.setSelectableContainer(groupsContainer);
  }

}
