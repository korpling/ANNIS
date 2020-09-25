package annis.gui.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.impl.NullClaim;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.keycloak.KeycloakSecurityContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
        // This class only has static utility functions
    }

    public static boolean isUserLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return isUserLoggedIn(authentication);
    }

    public static boolean isUserLoggedIn(Authentication authentication) {
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();
    }

    public static Claim getClaim(String claim) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object credentials = authentication.getCredentials();
            if (credentials instanceof KeycloakSecurityContext) {
                KeycloakSecurityContext context = (KeycloakSecurityContext) credentials;
                DecodedJWT token = JWT.decode(context.getTokenString());
                return token.getClaim(claim);
            }
        }
        return new NullClaim();
    }
}
