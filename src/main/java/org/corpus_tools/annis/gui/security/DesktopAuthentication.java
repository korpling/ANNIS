package org.corpus_tools.annis.gui.security;

import java.util.Collection;
import java.util.LinkedList;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class DesktopAuthentication implements Authentication {

  private OAuth2User principal;
  private String token;
  private boolean isAuthenticated;

  public DesktopAuthentication(OAuth2User principal, String token) {
    super();
    this.principal = principal;
    this.token = token;
    this.isAuthenticated = principal != null && token != null;
  }

  @Override
  public String getName() {
    return principal == null ? null : principal.getName();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return principal == null ? new LinkedList<>() : principal.getAuthorities();
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
