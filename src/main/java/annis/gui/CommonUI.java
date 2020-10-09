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
package annis.gui;

import annis.gui.components.SettingsStorage;
import annis.gui.requesthandler.ResourceRequestHandler;
import annis.libgui.AnnisBaseUI;
import annis.libgui.Helper;
import annis.libgui.InstanceConfig;
import com.vaadin.server.VaadinRequest;
import java.util.Map;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public abstract ServletContext getServletContext();

}
