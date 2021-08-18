package org.corpus_tools.annis.gui.security;

import java.io.Serializable;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener implements 
    ApplicationListener<AuthenticationSuccessEvent>, Serializable {

  private static final long serialVersionUID = 108867628342416834L;
  private String token;
	
	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		if(event.getAuthentication() instanceof OAuth2LoginAuthenticationToken) {
			OAuth2LoginAuthenticationToken auth = (OAuth2LoginAuthenticationToken) event.getAuthentication();
            this.token = auth.getAccessToken().getTokenValue();
		}
	}
	
    public String getToken() {
		return token;
	}

    public void setToken(String token) {
      this.token = token;
    }
}
