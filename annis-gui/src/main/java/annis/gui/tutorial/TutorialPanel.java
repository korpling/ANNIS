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
package annis.gui.tutorial;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author thomas
 */
public class TutorialPanel extends VerticalLayout
{

  private static final Logger log = LoggerFactory.getLogger(TutorialPanel.class);

  private BrowserFrame embedded;

  public TutorialPanel()
  {
    setSizeFull();

    URI appURI = UI.getCurrent().getPage().getLocation();
    URI tutorialURI;
    
    String relativeFile = "/VAADIN/help/index.html";
    
    try
    {
      String oldPath = VaadinService.getCurrentRequest().getContextPath();
      if (oldPath == null)
      {
        oldPath = "";
      }
      if (oldPath.endsWith("/"))
      {
        oldPath = oldPath.substring(0, oldPath.length() - 1);
      }
      tutorialURI = new URI(appURI.getScheme(),
        appURI.getUserInfo(),
        appURI.getHost(),
        appURI.getPort(),
        oldPath + relativeFile,
        null,
        null);
      embedded = new BrowserFrame(null, new ExternalResource(tutorialURI.toURL()));
      embedded.setSizeFull();
      addComponent(embedded);

    }
    catch (URISyntaxException | MalformedURLException ex)
    {
      log.error("Invalid tutorial URI", ex);
    }

  }
}
