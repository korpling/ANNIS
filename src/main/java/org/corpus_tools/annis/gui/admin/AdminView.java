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
package org.corpus_tools.annis.gui.admin;

import com.google.common.util.concurrent.FutureCallback;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.Background;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.LoginListener;
import org.corpus_tools.annis.gui.MainToolbar;
import org.corpus_tools.annis.gui.admin.controller.CorpusController;
import org.corpus_tools.annis.gui.admin.controller.GroupController;
import org.corpus_tools.annis.gui.admin.model.ApiClientProvider;
import org.corpus_tools.annis.gui.admin.model.CorpusManagement;
import org.corpus_tools.annis.gui.admin.model.GroupManagement;
import org.corpus_tools.annis.gui.admin.reflinks.MigrationPanel;
import org.corpus_tools.annis.gui.admin.reflinks.ReferenceLinkEditor;
import org.corpus_tools.annis.gui.admin.view.UIView;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class AdminView extends VerticalLayout
        implements View, UIView, LoginListener, TabSheet.SelectedTabChangeListener, ApiClientProvider {

    private static final long serialVersionUID = -5142632455076589645L;

    public static final String NAME = "admin";

    private final List<UIView.Listener> listeners = new LinkedList<>();

    private final TabSheet tabSheet;

    private final ImportPanel importPanel;

    private final CorpusAdminPanel corpusAdminPanel;

    private final GroupManagementPanel groupManagementPanel;

    private MainToolbar toolbar;

    private final AnnisUI ui;

    private final SecurityContext securityContext;

    private MigrationPanel migrationPanel;
    private ReferenceLinkEditor referenceLinkEditor;

    public AdminView(AnnisUI ui) {
        this.ui = ui;
        securityContext = SecurityContextHolder.getContext();
        Page.getCurrent().setTitle("ANNIS Adminstration");

        GroupManagement groupManagement = new GroupManagement();
        groupManagement.setWebResourceProvider(AdminView.this);
        CorpusManagement corpusManagement = new CorpusManagement();
        corpusManagement.setClientProvider(AdminView.this);

        boolean isLoggedIn = Helper.getUser(securityContext).isPresent();

        corpusAdminPanel = new CorpusAdminPanel();
        new CorpusController(corpusManagement, corpusAdminPanel, this, isLoggedIn);

        groupManagementPanel = new GroupManagementPanel();
        new GroupController(groupManagement, corpusManagement, groupManagementPanel, this, isLoggedIn);

        importPanel = new ImportPanel();

        migrationPanel = new MigrationPanel();
        referenceLinkEditor = new ReferenceLinkEditor();

        tabSheet = new TabSheet();
        tabSheet.addTab(importPanel, "Import Corpus", VaadinIcons.UPLOAD);
        tabSheet.addTab(corpusAdminPanel, "Corpus management", VaadinIcons.LIST);
        tabSheet.addTab(groupManagementPanel, "Group management", VaadinIcons.USERS);
        tabSheet.addTab(referenceLinkEditor, "Reference links", VaadinIcons.CONNECT);
        tabSheet.addTab(migrationPanel, "Reference link migration", VaadinIcons.CLOUD_DOWNLOAD_O);

        tabSheet.setSizeFull();

        tabSheet.addSelectedTabChangeListener(AdminView.this);

        addComponents(tabSheet);
        setSizeFull();
        setMargin(false);

        setExpandRatio(tabSheet, 1.0f);

        tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);

    }


    @Override
    public void addListener(UIView.Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void detach() {
        // inform the controllers that no tab is active any longer
        for (UIView.Listener l : listeners) {
            l.loadedTab(null);
        }

        super.detach();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {


        importPanel.updateMode(Helper.getUser(securityContext).isPresent());

        // group management and URL migrations are not visible when there is no security
        // (in desktop mode)
        tabSheet.getTab(groupManagementPanel).setVisible(!ui.isDesktopMode());
        tabSheet.getTab(migrationPanel).setVisible(!ui.isDesktopMode());
        tabSheet.getTab(referenceLinkEditor).setVisible(!ui.isDesktopMode());

        Component selectedTab = getComponentForFragment(event.getParameters());
        if (selectedTab != null && selectedTab != tabSheet.getSelectedTab()) {
            // Select the component given by the fragment, This will call
            // the selection change handler and thus we don't have
            // to call the listeners here.
            tabSheet.setSelectedTab(selectedTab);
        } else {
            // nothing to change in the tab selection, call the listeners manually
            selectedTab = tabSheet.getSelectedTab();
            for (UIView.Listener l : listeners) {
                l.loadedTab(selectedTab);
            }
            setFragmentParameter(getFragmentForComponent(selectedTab));
        }

    }


    private Component getComponentForFragment(String fragment) {
        if (fragment != null) {
            switch (fragment) {
            case "import":
                return importPanel;
            case "corpora":
                return corpusAdminPanel;
            case "groups":
                return groupManagementPanel;
              case "reference-link-migration":
                return migrationPanel;
              case "reference-link-editor":
                return referenceLinkEditor;
            default:
                break;
            }
        }
        return null;
    }

    private String getFragmentForComponent(Component c) {
      if (c == importPanel) {
        return "import";
      } else if (c == corpusAdminPanel) {
        return "corpora";
      } else if (c == groupManagementPanel) {
        return "groups";
      } else if (c == migrationPanel) {
        return "reference-link-migration";
      } else if (c == referenceLinkEditor) {
        return "reference-link-editor";
      }
        return "";
    }


    @Override
    public void invalidateClient() {
    }

    @Override
    public void onLogin() {
        for (UIView.Listener l : listeners) {
            l.loginChanged(true);
        }
        // TODO: make import panel a normal UI view listener
        if (importPanel != null) {
            importPanel.onLogin();
        }
    }

    @Override
    public void onLogout() {
        for (UIView.Listener l : listeners) {
            l.loginChanged(false);
        }
        // TODO: make import panel a normal UI view listener
        if (importPanel != null) {
            importPanel.onLogout();
        }
    }

    @Override
    public <T> void runInBackground(Callable<T> job, final FutureCallback<T> callback) {
        Background.runWithCallback(job, callback);
    }

    @Override
    public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
        Component selected = event.getTabSheet().getSelectedTab();

        for (UIView.Listener l : listeners) {
            l.loadedTab(selected);
        }
        setFragmentParameter(getFragmentForComponent(selected));
    }

    private void setFragmentParameter(String param) {
        Page.getCurrent().setUriFragment("!" + NAME + "/" + param, false);
    }

    public void setToolbar(MainToolbar newToolbar) {
        // remove old one if necessary
        if (this.toolbar != null) {
            removeComponent(this.toolbar);
            this.toolbar = null;
        }

        // add new toolbar
        if (newToolbar != null) {
            this.toolbar = newToolbar;
            addComponent(this.toolbar, 0);
            setExpandRatio(this.toolbar, 0.0f);
            this.toolbar.addLoginListener(this);
        }
    }

    @Override
    public void showBackgroundInfo(String info, String description) {
        Notification.show(info, description, Notification.Type.TRAY_NOTIFICATION);
    }

    @Override
    public void showError(String error, String description) {
        Notification.show(error, description, Notification.Type.ERROR_MESSAGE);
    }

    @Override
    public void showInfo(String info, String description) {
        Notification.show(info, description, Notification.Type.HUMANIZED_MESSAGE);
    }

    @Override
    public void showWarning(String error, String description) {
        Notification.show(error, description, Notification.Type.WARNING_MESSAGE);
    }

    @Override
    public ApiClient getClient() {
      return ui.getClient();
    }

}
