/*
 * Copyright 2019 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui;

import com.vaadin.annotations.Widgetset;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.declarative.Design;
import javax.servlet.ServletContext;
import org.corpus_tools.annis.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.web.reactive.function.client.WebClient;

@SpringUI(path = "/unsupported-query")
@Widgetset("org.corpus_tools.annis.gui.widgets.gwt.AnnisWidgetSet")
public class UnsupportedQueryUI extends CommonUI { // NO_UCD (test only)

  public class UnsupportedQueryPanel extends Panel {

    /**
     * 
     */
    private static final long serialVersionUID = 5891948595136418081L;
    private Button btExecute;

    private String url;

    public UnsupportedQueryPanel() {
      Page.getCurrent().setTitle("ANNIS: Unsupported query for citation link");

      Design.read("UnsupportedQueryPanel.html", this);

      btExecute.addClickListener(event -> {
        if (url != null) {
          getPage().setLocation(servletContext.getContextPath() + url);
        }
      });
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }
  }

  private static final long serialVersionUID = 3022711576267350004L;

  public static final String URL_PREFIX = "/unsupported-query";


  @Autowired
  private transient ServletContext servletContext;


  @Autowired(required = false)
  private transient OAuth2ClientProperties oauth2Clients;

  @Autowired
  private UIConfig config;
 
  @Autowired
  private WebClient webClient;
  

  private UnsupportedQueryPanel panel;

  protected Page overwrittenPage;

  public UnsupportedQueryUI(ServiceStarter serviceStarter) {
    super(URL_PREFIX, serviceStarter);
  }

  @Override
  protected void init(VaadinRequest request) {
    panel = new UnsupportedQueryPanel();
    panel.setUrl(request.getParameter("url"));
    setContent(panel);
  }


  @Override
  public ServletContext getServletContext() {
    return servletContext;
  }

  @Override
  public OAuth2ClientProperties getOauth2ClientProperties() {
    return this.oauth2Clients;
  }

  @Override
  public UIConfig getConfig() {
    return this.config;
  }


  public UnsupportedQueryPanel getPanel() {
    return panel;
  }

  @Override
  public Page getPage() {
    if (overwrittenPage == null) {
      return super.getPage();
    } else {
      return overwrittenPage;
    }
  }
  
	@Override
	public ApiClient getClient() {
		ApiClient result = new ApiClient(webClient);
		result.setBasePath(getConfig().getWebserviceUrl());
		return result;
	}
}
