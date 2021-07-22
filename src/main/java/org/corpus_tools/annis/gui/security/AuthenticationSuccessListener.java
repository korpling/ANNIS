package org.corpus_tools.annis.gui.security;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener implements 
ApplicationListener<AuthenticationSuccessEvent> {

	private OAuth2AccessToken token;
	
	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		if(event.getAuthentication() instanceof OAuth2LoginAuthenticationToken) {
			OAuth2LoginAuthenticationToken auth = (OAuth2LoginAuthenticationToken) event.getAuthentication();
			this.token = auth.getAccessToken();
		}
	}
	
	public OAuth2AccessToken getToken() {
		return token;
	}
}
