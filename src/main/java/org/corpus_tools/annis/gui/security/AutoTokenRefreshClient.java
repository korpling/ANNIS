package org.corpus_tools.annis.gui.security;

import java.util.Optional;
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.gui.CommonUI;
import org.corpus_tools.annis.gui.Helper;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class AutoTokenRefreshClient extends ApiClient {

  private final AuthenticationSuccessListener authListener;
  private final CommonUI ui;

  public AutoTokenRefreshClient(CommonUI ui, AuthenticationSuccessListener authListener) {
    super();
    this.ui = ui;
    this.authListener = authListener;

    // Use the configuration to allow changing the path to the web-service
    setBasePath(ui.getConfig().getWebserviceUrl());

    updateToken();
  }

  private void updateToken() {
    final Optional<OAuth2User> user = Helper.getUser(ui.getSecurityContext());
    if (user.isPresent()) {
      setBearerToken(authListener.getToken());
    } else {
      setBearerToken(null);
    }
  }

}
