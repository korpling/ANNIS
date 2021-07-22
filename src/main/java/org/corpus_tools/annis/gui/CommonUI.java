/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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

import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletContext;
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.auth.HttpBearerAuth;
import org.corpus_tools.annis.gui.components.SettingsStorage;
import org.corpus_tools.annis.gui.requesthandler.ResourceRequestHandler;
import org.corpus_tools.annis.gui.security.SecurityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public abstract class CommonUI extends AnnisBaseUI {
    private static final long serialVersionUID = -1304604048896817844L;

    private static final Logger log = LoggerFactory.getLogger(CommonUI.class);

    private SettingsStorage settings;

    private final String urlPrefix;

    private InstanceConfig instanceConfig;

    private SecurityContext securityContext;

    protected CommonUI(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }


    public InstanceConfig getInstanceConfig() {
        return instanceConfig;
    }

    private InstanceConfig getInstanceConfig(VaadinRequest request) {
        String instance = null;
        String pathInfo = request.getPathInfo();

        if (pathInfo != null && pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1);
        }
        if (pathInfo != null && pathInfo.endsWith("/")) {
            pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
        }

        Map<String, InstanceConfig> allConfigs = loadInstanceConfig();

        if (pathInfo != null && !pathInfo.isEmpty()) {
            instance = pathInfo;
        }

        if (instance != null && allConfigs.containsKey(instance)) {
            // return the config that matches the parsed name
            return allConfigs.get(instance);
        } else if (allConfigs.containsKey("default")) {
            // return the default config
            return allConfigs.get("default");
        } else if (allConfigs.size() > 0) {
            // just return any existing config as a fallback
            log.warn("Instance config {} not found or null and default config is not available.",
                    instance);
            return allConfigs.values().iterator().next();
        }

        // default to an empty instance config
        return new InstanceConfig();
    }

    public FontConfig getInstanceFont() {
        if (instanceConfig != null && instanceConfig.getFont() != null) {
            return instanceConfig.getFont();
        }
        return null;
    }

    public SettingsStorage getSettings() {
        if (settings == null) {
            settings = new SettingsStorage(this);
        }
        return settings;
    }

    @Override
    protected void init(VaadinRequest request) {
        super.init(request);

        getSession().addRequestHandler(new ResourceRequestHandler(urlPrefix));

        settings = new SettingsStorage(this);

        this.instanceConfig = getInstanceConfig(request);
    }

    protected void loadInstanceFonts() {
        if (getInstanceConfig() != null && getInstanceConfig().getFont() != null) {
            FontConfig cfg = getInstanceConfig().getFont();
            String url = cfg.getUrl() == null || cfg.getUrl().isEmpty() ? ""
                    : "@import url(" + cfg.getUrl() + ");\n";
            if (cfg.getSize() == null || cfg.getSize().isEmpty()) {
                injectUniqueCSS( // this one is for the virtual keyboard
                        url + "." + Helper.CORPUS_FONT_FORCE + " {font-family: '" + cfg.getName()
                                + "', monospace !important; }\n" + "." + Helper.CORPUS_FONT
                                + " {font-family: '" + cfg.getName() + "', monospace; }\n" + "div."
                                + Helper.CORPUS_FONT + " .CodeMirror pre {font-family: '"
                                + cfg.getName() + "', monospace; }\n"
                                + "#keyboardInputMaster tbody tr td table tbody tr td {\n"
                                + "  font-family: '" + cfg.getName()
                                + "', 'Lucida Console','Arial Unicode MS',monospace; " + "}");
            } else {
                injectUniqueCSS( // this one is for the virtual keyboard
                        url + "." + Helper.CORPUS_FONT_FORCE + " {\n" + "  font-family: '"
                                + cfg.getName() + "', monospace !important;\n" + "  font-size: "
                                + cfg.getSize() + " !important;\n" + "}\n" + "."
                                + Helper.CORPUS_FONT + " {\n" + "  font-family: '" + cfg.getName()
                                + "', monospace;\n" + "  font-size: " + cfg.getSize() + ";\n"
                                + "}\n" + "div." + Helper.CORPUS_FONT + " .CodeMirror pre" + " {\n"
                                + "  font-family: '" + cfg.getName() + "', monospace;\n"
                                + "  font-size: " + cfg.getSize() + ";\n" + "}\n" + "."
                                + Helper.CORPUS_FONT + " .v-table-table {\n" + "    font-size: "
                                + cfg.getSize() + ";\n" + "}\n"
                                + "#keyboardInputMaster tbody tr td table tbody tr td {\n"
                                + "  font-family: '" + cfg.getName()
                                + "', 'Lucida Console','Arial Unicode MS',monospace; " + "}");
            }
        } else {
            injectUniqueCSS( // use original font definition from keyboard.css if no font given
                    "#keyboardInputMaster tbody tr td table tbody tr td {\n"
                            + "  font-family: 'Lucida Console','Arial Unicode MS',monospace;"
                            + "}");
        }
    }

    public void setInstanceConfig(InstanceConfig instanceConfig) {
        this.instanceConfig = instanceConfig;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public ApiClient getClient() {
      
      final ApiClient client = Configuration.getDefaultApiClient();
      // Use the configuration to allow changing the path to the web-service
      client.setBasePath(getConfig().getWebserviceUrl());

      final Optional<OAuth2User> user = Helper.getUser(getSecurityContext());
      String bearerToken = null;
      if (user.isPresent()) {
        // TODO implement bearer token extraction or switch to a different client implementation
        throw new UnsupportedOperationException("Bearer token extraction not implemented yet");
      }
      final org.corpus_tools.annis.auth.Authentication auth =
          client.getAuthentication("bearerAuth");
      if (auth instanceof HttpBearerAuth) {
        final HttpBearerAuth bearerAuth = (HttpBearerAuth) auth;
        bearerAuth.setBearerToken(bearerToken);
      }
      return client;
    }

    /**
     * Handle common errors like database/service connection problems and display a unified error
     * message.
     * 
     * This will not log the exception, only display information to the user.
     * 
     * @param ex exception to handle
     * @return True if error was handled, false otherwise.
     */
    public boolean handleCommonError(Throwable ex, String action) {

      if (ex != null) {
        Throwable rootCause = ex;
        while (rootCause.getCause() != null) {
          rootCause = rootCause.getCause();
        }

        if (rootCause instanceof ApiException) {
          ApiException apiEx = (ApiException) rootCause;

          if (apiEx.getCode() == 503) {
            // database connection error
            Notification n = new Notification(
                "Can't execute " + (action == null ? "" : "\"" + action + "\"")
                    + " action because database server is not responding.<br/>"
                    + "There might be too many users using this service right now.",
                Notification.Type.WARNING_MESSAGE);
            n.setDescription(
                "<p><strong>Please try again later.</strong> If the error persists inform the administrator of this server.</p>"
                    + "<p>Click on this message to close it.</p>"
                    + "<p style=\"font-size:9pt;color:gray;\">Pinguin picture by Polar Cruises [CC BY 2.0 (http://creativecommons.org/licenses/by/2.0)], via Wikimedia Commons</p>");
            n.setIcon(AnnisBaseUI.PINGUIN_IMAGE);
            n.setHtmlContentAllowed(true);
            n.setDelayMsec(15000);

            n.show(this.getPage());
            return true;
          } else if (apiEx.getCode() == 401) {
            redirectToLogin();
            return true;
          }
        }
      }
      return false;
    }

    public abstract ServletContext getServletContext();

    public SecurityContext getSecurityContext() {
      if (this.securityContext == null) {
        this.securityContext = SecurityContextHolder.getContext();
      }
      return securityContext;
    }

    public abstract OAuth2ClientProperties getOauth2ClientProperties();

    public abstract UIConfig getConfig();
    
    public void redirectToLogin() {
     
      OAuth2ClientProperties oauth2Clients = getOauth2ClientProperties();
      if (oauth2Clients != null) {

        // Store the current fragment so it can be restored after login was successful
        String oldFragment = Page.getCurrent().getUriFragment();
        VaadinSession.getCurrent().setAttribute(SecurityConfiguration.FRAGMENT_TO_RESTORE,
            oldFragment);

        VaadinRequest currentRequest = VaadinRequest.getCurrent();
        final String contextPath = currentRequest == null ? "" : currentRequest.getContextPath();
        // Determine if there is only one or several clients
        Collection<String> providers = oauth2Clients.getProvider().keySet();
        if (providers.size() == 1) {
          // Directly login with the single provider
          Page.getCurrent()
              .setLocation(contextPath + "/oauth2/authorization/" + providers.iterator().next());
        } else {
          // Show general login selection page
          Page.getCurrent().setLocation(contextPath + "/login");
        }
      }
    }

}
