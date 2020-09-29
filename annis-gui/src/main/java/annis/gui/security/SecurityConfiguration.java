package annis.gui.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String LOGOUT_URL = "/logout";
    private static final String LOGOUT_SUCCESS_URL = "/";

    public static final String ROLES_CLAIM = "https://corpus-tools.org/annis/roles";

    /**
     * Registers our UserDetailsService and the password encoder to be used on login attempts.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http

                // Allow all internal Vaadin requests.
                .authorizeRequests().antMatchers("/PUSH/**").permitAll().antMatchers("/UIDL/**")
                .permitAll().antMatchers("/HEARTBEAT/**").permitAll().antMatchers("/**").permitAll()

                // Restrict access to our application.
                .and().authorizeRequests().anyRequest().authenticated()

                // Not using Spring CSRF here to be able to use plain HTML for the login page
                .and().csrf().disable()

                // Configure logout
                .logout().logoutUrl(LOGOUT_URL).logoutSuccessUrl(LOGOUT_SUCCESS_URL)

                // Configure the login page.
                .and().oauth2Login();

    }

    /**
     * Allows access to static resources, bypassing Spring Security.
     */
    @Override
    public void configure(WebSecurity web) {
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
}
