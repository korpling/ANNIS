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

import annis.gui.admin.view.UserListView;
import com.vaadin.data.Validator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import java.util.List;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class NewPasswordWindow extends Window
{ 
  public NewPasswordWindow(final String userName, final List<UserListView.Listener> listeners)
  {
    setCaption("Set new password for user \"" + userName + "\"");
    setModal(true);
    
    FormLayout layout = new FormLayout();
    setContent(layout);
    
    final PasswordField txtPassword1 = new PasswordField("Enter new password");
    final PasswordField txtPassword2 = new PasswordField("Repeat new password");
    
    txtPassword1.setValidationVisible(true);
    txtPassword1.setRequired(true);
    
    txtPassword2.addValidator(new Validator()
    {

      @Override
      public void validate(Object value) throws Validator.InvalidValueException
      {
        String asString = (String)  value;
        if(asString != null && !asString.equals(txtPassword1.getValue()))
        {
          throw new InvalidValueException("Passwords are not the same");
        }
      }
    });
    txtPassword2.setRequired(true);
    txtPassword2.setValidationVisible(true);
    
    Button btOk = new Button("Ok");
    btOk.setClickShortcut(ShortcutAction.KeyCode.ENTER);
    btOk.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(Button.ClickEvent event)
      { 
        try
        {
          txtPassword1.validate();
          txtPassword2.validate();
        
          if(txtPassword1.isValid() && txtPassword2.isValid())
          {
            for(UserListView.Listener l : listeners)
            {
              l.passwordChanged(userName, txtPassword1.getValue());
            }
            UI.getCurrent().removeWindow(NewPasswordWindow.this);
            Notification.show("Password for user \"" + userName + "\" was changed");
          }
          else
          {
            
          }
        }
        catch(Validator.InvalidValueException ex)
        {
          Notification n = new Notification("Validation failed", ex.getHtmlMessage(),
            Type.ERROR_MESSAGE, true);
          n.show(Page.getCurrent());
        }
      }
    });
    
    Button btCancel = new Button("Cancel");
    btCancel.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        UI.getCurrent().removeWindow(NewPasswordWindow.this);
      }
    });
    
    HorizontalLayout actionLayout = new HorizontalLayout(btOk, btCancel);
    
    layout.addComponent(txtPassword1);
    layout.addComponent(txtPassword2);
    layout.addComponent(actionLayout);
  }
}
