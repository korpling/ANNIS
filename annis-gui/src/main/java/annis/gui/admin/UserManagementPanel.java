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

package annis.gui.admin;

import annis.gui.admin.view.UserManagementView;
import annis.security.User;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class UserManagementPanel extends Panel
  implements UserManagementView
{
  
  private final VerticalLayout layout;
  private final Table userList;
  private final BeanContainer<String, User> container;
  
  public UserManagementPanel()
  {
    layout = new VerticalLayout();
    layout.setSizeFull();
    setContent(layout);
    setSizeFull();
    
    container = new BeanContainer<>(User.class);
    container.setBeanIdProperty("name");
    
    userList = new Table();
    userList.setSizeFull();
    userList.setContainerDataSource(container);
    
    layout.addComponent(userList);
  }


  @Override
  public void setUserList(Collection<User> users)
  {
    container.removeAllItems();
    container.addAll(users);
  }

  
  
  
}
