package org.corpus_tools.annis.gui.security;

import java.lang.reflect.Type;
import java.util.Optional;
import okhttp3.Call;
import org.corpus_tools.annis.ApiCallback;
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.ApiResponse;
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

    setReadTimeout(0);

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

  @Override
  public <T> ApiResponse<T> execute(Call call, Type returnType) throws ApiException {
    updateToken();
    return super.execute(call, returnType);
  }

  @Override
  public <T> void executeAsync(Call call, Type returnType, ApiCallback<T> callback) {
    updateToken();
    super.executeAsync(call, returnType, callback);
  }
}
