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

import static annis.gui.MainToolbar.LOGIN_MAXIMIZED_KEY;

import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.Window;

import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.api.AuthentificationApi;
import org.corpus_tools.annis.api.model.InlineObject1;

import annis.gui.components.ExceptionDialog;
import annis.libgui.AnnisBaseUI;
import annis.libgui.AnnisUser;
import annis.libgui.Helper;

/**
 * A window for logging in.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class LoginWindow extends Window implements LoginForm.LoginListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1567905636551177878L;
    private QueryController queryController;
    private boolean executeSearchAfterClose;

    private final LoginForm form;

    public LoginWindow() {
        super("ANNIS Login");

        setModal(true);

        setWidth("400px");
        setHeight("250px");

        form = new LoginForm();
    }

    @Override
    public void attach() {
        super.attach();

        form.addLoginListener(this);
        setContent(form);

        String loginMaximizedRaw = (String) getSession().getAttribute(LOGIN_MAXIMIZED_KEY);
        if (Boolean.parseBoolean(loginMaximizedRaw)) {
            setWindowMode(WindowMode.MAXIMIZED);
        }
    }

    @Override
    public void onLogin(LoginEvent event) {
        String password = event.getLoginParameter("password");
        String username = event.getLoginParameter("username");
        if (username != null && password != null) {
            // forget any old user information
            VaadinSession.getCurrent().getSession().removeAttribute(AnnisBaseUI.USER_KEY);

            // Attempt to get a JWT bearer token for this user.
            // Since we want to authenticate, we use an anonymous API client
            final ApiClient client = Configuration.getDefaultApiClient();
            AnnisUI ui = null;
            if (getUI() instanceof AnnisUI) {
                ui = (AnnisUI) getUI();
                client.setBasePath(ui.getConfig().getWebserviceURL());
            }
            AuthentificationApi api = new AuthentificationApi(client);
            
            InlineObject1 credentials = new InlineObject1();
            credentials.setUserId(username);
            credentials.setPassword(password);
            try {
                String token = api.localLogin(credentials);
                Helper.setUser(new AnnisUser(username, password, token));
                if (ui != null) {
                    ui.getToolbar().onLogin();
                }
            } catch (ApiException ex) {
                ExceptionDialog.show(ex, "Could not login", getUI());
            }

        } // end if login attempt

    }

    public void close(boolean loginSuccessful) {
        if (executeSearchAfterClose && loginSuccessful && queryController != null
                && !queryController.getState().getSelectedCorpora().isEmpty()) {
            queryController.executeSearch(true, true);
        }

        super.close();

    }

    public QueryController getQueryController() {
        return queryController;
    }

    public boolean isExecuteSearchAfterClose() {
        return executeSearchAfterClose;
    }

    public void setExecuteSearchAfterClose(boolean executeSearchAfterClose) {
        this.executeSearchAfterClose = executeSearchAfterClose;
    }

    public void setQueryController(QueryController queryController) {
        this.queryController = queryController;
    }

}
