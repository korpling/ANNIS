package annis.gui.security;

import annis.libgui.Helper;
import java.io.IOException;
import java.util.Optional;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class JwtTokenRefreshAuthenticator implements Authenticator {

  private final SecurityContext securityContext;
  private final OAuth2AuthorizedClientRepository clientRepo;

  public JwtTokenRefreshAuthenticator(SecurityContext securityContext,
      OAuth2AuthorizedClientRepository clientRepo) {
    this.securityContext = securityContext;
    this.clientRepo = clientRepo;
  }

  @Override
  public Request authenticate(Route route, Response response) throws IOException {
    String existingAuthHeader = response.request().header("Authorization");
    if (existingAuthHeader != null && existingAuthHeader.startsWith("Bearer")) {
      Optional<OidcUser> user = Helper.getUser(securityContext);
      if(user.isPresent()) {
        // TODO Refresh the token and update header
        // clientRepo.loadAuthorizedClient(user.get().g, principal, request)(clientRegistrationId,
        // principal, request)
      }
    }
    return null;
  }

}
