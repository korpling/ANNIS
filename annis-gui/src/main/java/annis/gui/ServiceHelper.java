/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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

import annis.exceptions.AnnisServiceFactoryException;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import com.vaadin.Application;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class ServiceHelper
{

  public static AnnisService getService(Application app, Window window)
  {
    AnnisService service = null;
    try
    {
      service = AnnisServiceFactory.getClient(app.getProperty("AnnisRemoteService.URL"));
    }
    catch(AnnisServiceFactoryException ex)
    {
      Logger.getLogger(ServiceHelper.class.getName()).log(Level.SEVERE, "Could not connect to service", ex);
      window.showNotification("Could not connect to service: " + ex.getMessage(),
        Notification.TYPE_TRAY_NOTIFICATION);
    }

    return service;
  }
}
