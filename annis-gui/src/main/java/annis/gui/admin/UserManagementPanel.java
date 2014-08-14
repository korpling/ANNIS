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
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.vaadin.tokenfield.TokenField;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class UserManagementPanel extends Panel
  implements UserManagementView
{
  
  private final VerticalLayout layout;
  private final Table userList;
  private final BeanContainer<String, User> userContainer;
  private final List<UserManagementView.Listener> listeners = new LinkedList<>();
  
  public UserManagementPanel()
  {
    layout = new VerticalLayout();
    layout.setSizeFull();
    setContent(layout);
    setSizeFull();
    
    userContainer = new BeanContainer<>(User.class);
    userContainer.setBeanIdProperty("name");
    
    
    userList = new Table();
    userList.setEditable(true);
    userList.setSizeFull();
    userList.setContainerDataSource(userContainer);
    userList.addGeneratedColumn("changepassword", new PasswordChangeColumnGenerator());
    
    userList.setVisibleColumns("name", "groups", "permissions", "changepassword");
    userList.setColumnHeaders("Username", "Groups", "Additional permissions", "");
    
    userList.setTableFieldFactory(new FieldFactory());
    
    
    layout.addComponent(userList);
  }

  @Override
  public void addListener(UserManagementView.Listener listener)
  {
    listeners.add(listener);
  }

  


  @Override
  public void setUserList(Collection<User> users)
  {
    userContainer.removeAllItems();
    userContainer.addAll(users);
  }
  
  public class PasswordChangeColumnGenerator implements Table.ColumnGenerator
  {

    @Override
    public Object generateCell(Table source, final Object itemId, Object columnId)
    {
     PasswordField txtNewPassword = new PasswordField();
      txtNewPassword.setInputPrompt("New password");
      Button btChangePassword = new Button("Change password");
      btChangePassword.addClickListener(new Button.ClickListener()
      {

        @Override
        public void buttonClick(Button.ClickEvent event)
        {
          NewPasswordWindow w = new NewPasswordWindow((String) itemId, listeners);
          UI.getCurrent().addWindow(w);
          w.center();
        }
      });
      return btChangePassword;
    }
    
  }

  public class FieldFactory implements TableFieldFactory
  {

    @Override
    public Field<?> createField(Container container, final Object itemId,
      Object propertyId, Component uiContext)
    {
      if("groups".equals(propertyId) || "permissions".equals(propertyId))
      {
        TokenField field = new TokenField();
        field.setPropertyDataSource(container.getContainerProperty(itemId,
          propertyId));
        
        field.addValueChangeListener(new Property.ValueChangeListener()
        {

          @Override
          public void valueChange(Property.ValueChangeEvent event)
          {
            for(UserManagementView.Listener l : listeners) 
            {
              l.userUpdated(userContainer.getItem(itemId).getBean());
            }
          }
        });

        
        return field;
      }
      else
      {
        TextField field = new TextField(container.getContainerProperty(itemId,
          propertyId));
        
        if("name".equals(propertyId))
        {
          field.setReadOnly(true);
        }
        
        return field;
      }
    }
    
  }
  
  
}
