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
package annis.gui;

import static annis.gui.MainToolbar.LOGIN_MAXIMIZED_KEY;
import static annis.gui.MainToolbar.LOGIN_URL_KEY;
import annis.libgui.Helper;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Window;

/**
 * A window for logging in.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class LoginWindow extends Window
{

  private String loginURL;
  private QueryController queryController;
  private boolean executeSearchAfterClose;
  
  public LoginWindow()
  {
    super("ANNIS Login");

    setModal(true);

    setWidth("400px");
    setHeight("250px");
  }

  @Override
  public void attach()
  {
    super.attach();

    this.loginURL = (String) VaadinSession.getCurrent().getAttribute(
      LOGIN_URL_KEY);

    Resource loginRes;
    if (loginURL == null || loginURL.isEmpty())
    {
      loginRes = new ExternalResource(
        Helper.getContext() + "/login");
    }
    else
    {
      loginRes = new ExternalResource(loginURL);
    }

    BrowserFrame frame = new BrowserFrame("login", loginRes);
    frame.setWidth("100%");
    frame.setHeight("100%");

    setContent(frame);

    String loginMaximizedRaw = (String) getSession().getAttribute(
      LOGIN_MAXIMIZED_KEY);
    if (Boolean.parseBoolean(loginMaximizedRaw))
    {
      setWindowMode(WindowMode.MAXIMIZED);
    }
  }
  
  public void close(boolean loginSuccessful)
  {
    if(executeSearchAfterClose && loginSuccessful && queryController != null
      && !queryController.getState().getSelectedCorpora().getValue().isEmpty())
    {
      queryController.executeSearch(true, true);
    }
    
    super.close();
    
  }

  public QueryController getQueryController()
  {
    return queryController;
  }

  public void setQueryController(QueryController queryController)
  {
    this.queryController = queryController;
  }

  public boolean isExecuteSearchAfterClose()
  {
    return executeSearchAfterClose;
  }

  public void setExecuteSearchAfterClose(boolean executeSearchAfterClose)
  {
    this.executeSearchAfterClose = executeSearchAfterClose;
  }
  
  

}
