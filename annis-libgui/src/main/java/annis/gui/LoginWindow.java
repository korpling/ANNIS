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

import annis.libgui.Helper;
import annis.security.AnnisUser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple login window.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class LoginWindow extends Window implements Button.ClickListener
{
  private final static Logger log = LoggerFactory.getLogger(LoginWindow.class);
  
  private TextField txtUser = new TextField("Username");
  private  PasswordField txtPassword = new PasswordField("Password");
  private  Button btLogin = new Button("Login");

  public LoginWindow()
  {
    super("Login");
    
    btLogin.addClickListener(this);
    
    VerticalLayout layout = new VerticalLayout(txtUser, txtPassword, btLogin);
    setContent(layout);
    layout.setMargin(true);
    
    btLogin.setClickShortcut(ShortcutAction.KeyCode.ENTER);
    
    txtUser.focus();
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    try
    {
      // forget old user information
      Helper.setUser(null);
      
      String userName = txtUser.getValue();
      
      Client client = Helper.createRESTClient(userName, txtPassword.getValue());
      
      // check if this is valid user/password combination
      String webserviceURL = (String) VaadinSession.getCurrent()
        .getAttribute(Helper.KEY_WEB_SERVICE_URL);
      WebResource res = client.resource(webserviceURL)
        .path("admin").path("is-authenticated");
      if("true".equalsIgnoreCase(res.get(String.class)))
      {
        // everything ok, save this user configuration for re-use
        Helper.setUser(new AnnisUser(userName, client));
        
        Notification.show("Logged in as \"" + userName + "\"",
          Notification.Type.TRAY_NOTIFICATION);
      }
    }
    catch (ClientHandlerException ex)
    {
      Notification.show("Authentification error: " + ex.getMessage(),
        Notification.Type.WARNING_MESSAGE);
    }
    catch(UniformInterfaceException ex)
    {
      if(ex.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode())
      {
        Notification.show("Username or password wrong", ex.getMessage(), 
          Notification.Type.WARNING_MESSAGE);
      }
      else
      {
        log.error(null, ex);
        Notification.show("Unexpected exception: " + ex.getMessage(),
          Notification.Type.WARNING_MESSAGE);
      }
    }
    catch (Exception ex)
    {
      log.error(null, ex);

      Notification.show("Unexpected exception: " + ex.getMessage(),
        Notification.Type.WARNING_MESSAGE);
    }
    finally
    {
      // hide login window
      getUI().removeWindow(this);
      
      if(UI.getCurrent() instanceof LoginListener)
      {
        ((LoginListener) UI.getCurrent()).onLogin();
      }
    }
  }
  
  public static interface LoginListener 
  {
    public void onLogin();
  }
  
}
