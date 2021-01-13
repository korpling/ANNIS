package annis.gui.security;

import annis.libgui.Helper;
import java.io.IOException;
import java.util.Optional;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class JwtTokenRefreshAuthenticator implements Authenticator {

  private final SecurityContext securityContext;
  private final OAuth2AuthorizedClientService authorizedClientService;

  public JwtTokenRefreshAuthenticator(SecurityContext securityContext,
      OAuth2AuthorizedClientService authorizedClientService) {
    this.securityContext = securityContext;
    this.authorizedClientService = authorizedClientService;
  }

  @Override
  public Request authenticate(Route route, Response response) throws IOException {
    String existingAuthHeader = response.request().header("Authorization");
    if (existingAuthHeader != null && existingAuthHeader.startsWith("Bearer")) {
      Optional<OidcUser> optionalUser = Helper.getUser(securityContext);
      if (optionalUser.isPresent()) {
        OidcUser user = optionalUser.get();
        // TODO: how to get the correct clientRegistrationId?
        OAuth2AuthorizedClient client =
            authorizedClientService.loadAuthorizedClient("keycloak", user.getName());

        response.request().newBuilder().header("Authorization",
            "Bearer " + client.getAccessToken());

      }
    }
    return null;
  }

}
