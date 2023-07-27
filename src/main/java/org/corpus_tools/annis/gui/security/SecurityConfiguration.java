package org.corpus_tools.annis.gui.security;

import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.corpus_tools.annis.gui.ServiceStarter;
import org.corpus_tools.annis.gui.UIConfig;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;


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

  @Bean
  WebClient webClient(UIConfig config,
      Optional<OAuth2AuthorizedClientManager> authorizedClientManager,
      ServiceStarter serviceStarter) throws IOException {


    // Use the provided service configuration to get the correct port of the graphANNIS service
    File serviceConfigFile = serviceStarter.getServiceConfig();
    TomlMapper mapper = new TomlMapper();
    Map<?, ?> parsedServiceConfig = mapper.readValue(serviceConfigFile, Map.class);
    String serviceURL = getServiceURL(parsedServiceConfig);

    WebClient.Builder builder = WebClient.builder().baseUrl(serviceURL);
    Optional<Authentication> desktopUserToken = serviceStarter.getDesktopUserToken();
    if(desktopUserToken.isPresent()) {
    	//  Use the static provided token to authenticate against the REST service 
        builder = builder.defaultHeader("Authorization",
            "Bearer " + desktopUserToken.get().getCredentials().toString());
    }
    else {
    	// Use the token that can be acquired by logging in
        Optional<ServletOAuth2AuthorizedClientExchangeFilterFunction> filter =
                authorizedClientManager.map(acm -> {
                  ServletOAuth2AuthorizedClientExchangeFilterFunction result =
                      new ServletOAuth2AuthorizedClientExchangeFilterFunction(acm);
//                  result.setDefaultClientRegistrationId("keycloak");
                  return result;
                });
        if (filter.isPresent()) {
          builder = builder.filter(filter.get());
        }
    }
    
    return builder.build();
  }

  private static String getServiceURL(Map<?, ?> serviceConfig) {
    long port = 5711l;
    Object bindSection = serviceConfig.get("bind");
    if (bindSection instanceof Map) {
      @SuppressWarnings("rawtypes")
      Object portRaw = ((Map) bindSection).get("port");
      if (portRaw instanceof Long) {
        port = (Long) portRaw;
      }
    }
    return "http://localhost:" + port + "/v1";
  }

  @Bean
  @Conditional(ClientsConfiguredCondition.class)
  public OAuth2AuthorizedClientManager authorizedClientManager(
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientRepository authorizedClientRepository) {

    OAuth2AuthorizedClientProvider authorizedClientProvider =
        OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();

    DefaultOAuth2AuthorizedClientManager authorizedClientManager =
        new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository,
            authorizedClientRepository);
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
    return authorizedClientManager;
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

        // Not using Spring CSRF protection here because Vaadin also has a
        // Cross-site request forgery protection running.
        // Spring will try to enforce an additional layer on the filtered resources, which conflicts
        // with the Vaadin CSRF protection and makes the frontend unusable.
        // Disabling Spring CSRF is therefore safe, as long as Vaadin CSRF protection is activated
        // (which it is per default).
        // Also see
        // https://vaadin.com/blog/filter-based-spring-security-in-vaadin-applications
        // for an explanation from the Vaadin developers.
        .and().csrf().disable();

    // We depend on IFrames embedded into the application
    http.headers().frameOptions().sameOrigin();
  }
}
