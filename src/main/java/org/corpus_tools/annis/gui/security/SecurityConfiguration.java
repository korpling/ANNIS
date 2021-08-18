package org.corpus_tools.annis.gui.security;

import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
public class SecurityConfiguration {

  private static final String LOGOUT_URL = "/logout";
  private static final String LOGOUT_SUCCESS_URL = "/";

  public static final String ROLES_CLAIM = "https://corpus-tools.org/annis/roles";

  public static final String FRAGMENT_TO_RESTORE = "ANNIS_FRAGENT_TO_RESTORE";


  private static class NoClientsConfiguredCondition extends NoneNestedConditions {
    NoClientsConfiguredCondition() {
      super(ConfigurationPhase.REGISTER_BEAN);
    }

    @Conditional(ClientsConfiguredCondition.class)
    static class ClientsConfigured { // NO_UCD (unused code)
    }
  }

  @EnableWebSecurity
  @Conditional(ClientsConfiguredCondition.class)
  public static class OAuth2LoginSecurityConfig extends WebSecurityConfigurerAdapter { // NO_UCD
                                                                                       // (unused
                                                                                       // code)

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      configureHttpVaadinSecurity(http);
      // Configure logout
      http.logout().logoutUrl(LOGOUT_URL).logoutSuccessUrl(LOGOUT_SUCCESS_URL)
          // Configure OAuth2 for login
          .and().oauth2Login();
    }


    @Override
    public void configure(WebSecurity web) {
      ignoreVaadinWebSecurity(web);
    }
    
  }

  @EnableWebSecurity
  @Conditional(NoClientsConfiguredCondition.class)
  public static class NoLoginSecurityConfig extends WebSecurityConfigurerAdapter { // NO_UCD (unused
                                                                                   // code)

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      configureHttpVaadinSecurity(http);
    }

    @Override
    public void configure(WebSecurity web) {
      ignoreVaadinWebSecurity(web);
    }
  }

  private static void ignoreVaadinWebSecurity(WebSecurity web) {
    web.ignoring().antMatchers(
        // client-side JS code
        "/VAADIN/**",

        // the standard favicon URI
        "/favicon.ico",

        // web application manifest
        "/manifest.webmanifest", "/sw.js", "/offline-page.html",

        // icons and images
        "/icons/**", "/images/**");

  }

  private static void configureHttpVaadinSecurity(HttpSecurity http) throws Exception {
    http
        // Allow all internal Vaadin requests.
        .authorizeRequests().antMatchers("/PUSH/**").permitAll().antMatchers("/UIDL/**").permitAll()
        .antMatchers("/HEARTBEAT/**").permitAll().antMatchers("/**").permitAll()

        // Restrict access to our application.
        .and().authorizeRequests().anyRequest().authenticated()

        // Not using Spring CSRF here to be able to use plain HTML for the login page
        .and().csrf().disable();

    // We depend on IFrames embedded into the application
    http.headers().frameOptions().sameOrigin();
  }
}
