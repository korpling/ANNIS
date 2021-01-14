package annis.gui.security;

import annis.libgui.Helper;
import java.io.IOException;
import java.util.Optional;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class JwtTokenInterceptor implements Interceptor {

  private final SecurityContext securityContext;

  public JwtTokenInterceptor(SecurityContext securityContext) {
    this.securityContext = securityContext;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    final Optional<OidcUser> user = Helper.getUser(securityContext);
    if (user.isPresent()) {
      String bearerToken = user.get().getIdToken().getTokenValue();
      Request requestWithAuth =
          chain.request().newBuilder().header("Authorization", "Bearer " + bearerToken).build();
      return chain.proceed(requestWithAuth);
    } else {
      return chain.proceed(chain.request());
    }
  }

}
