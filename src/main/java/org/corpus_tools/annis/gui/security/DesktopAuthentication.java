package org.corpus_tools.annis.gui.security;

import com.google.common.base.Preconditions;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

public class DesktopAuthentication implements Authentication {

  private static final long serialVersionUID = -7671167490817088267L;
  private final DefaultOAuth2User principal;
  private final String token;
  private boolean isAuthenticated;

  public DesktopAuthentication(DefaultOAuth2User principal, String token) {
    super();
    Preconditions.checkNotNull(principal);
    Preconditions.checkNotNull(token);
    this.principal = principal;
    this.token = token;
    this.isAuthenticated = true;
  }

  @Override
  public String getName() {
    return principal.getName();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return principal.getAuthorities();
  }

  @Override
  public Object getCredentials() {
    return token;
  }

  @Override
  public Object getDetails() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public boolean isAuthenticated() {
    return isAuthenticated;
  }

  @Override
  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    this.isAuthenticated = isAuthenticated;
  }

}
