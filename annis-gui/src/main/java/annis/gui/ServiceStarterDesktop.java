package annis.gui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.vaadin.ui.UI;

import org.apache.commons.lang3.RandomStringUtils;
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.AuthentificationApi;
import org.corpus_tools.annis.api.model.InlineObject1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import annis.libgui.AnnisUser;
import annis.libgui.Helper;

@Component
@Profile("desktop")
public class ServiceStarterDesktop extends ServiceStarter {

    private static final String USER_NAME = "desktop";
    private static final Logger log = LoggerFactory.getLogger(ServiceStarterDesktop.class);
    private String password;
    private String basePath;
    private Optional<AnnisUser> desktopUserCredentials = Optional.empty();


    /**
     * Overwrite the existing configuration file and add a temporary user for the desktop.
     */
    @Override
    protected File getServiceConfig() throws IOException {
        Toml tomlConfig = new Toml().read(super.getServiceConfig());
        Map<String, Object> config = tomlConfig.toMap();

        this.basePath = getServiceURL(tomlConfig);

        // Generate a random password
        this.password = RandomStringUtils.random(50);
        // Hash the password with bcrypt and add it to the configuration
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Map<String, Object> desktopUser = new LinkedHashMap<>();
        desktopUser.put("password", passwordEncoder.encode(this.password));
        // The generated user is an adminstrator
        desktopUser.put("admin", true);

        // Add the temporary user to the configuration (removing all existing users)
        Map<String, Object> users = new HashMap<>();
        users.put(USER_NAME, desktopUser);
        config.put("users", users);


        File temporaryFile = File.createTempFile("annis-service-config-desktop-", ".toml");
        TomlWriter writer = new TomlWriter();
        writer.write(config, temporaryFile);
        return temporaryFile;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        super.onApplicationEvent(event);

        // Get a JWT token for the logged in user from the started service
        // Since we want to authenticate, we use an anonymous API client
        ApiClient client = new ApiClient();
        client.setBasePath(this.basePath);
        AuthentificationApi api = new AuthentificationApi(client);
        
        InlineObject1 credentials = new InlineObject1();
        credentials.setUserId(USER_NAME);
        credentials.setPassword(password);
        try {
            String token = api.localLogin(credentials);
            this.desktopUserCredentials = Optional.of(new AnnisUser(USER_NAME, password, token));
        } catch (ApiException ex) {
            log.error("Could not login with temporary desktop user", ex);
            // Since this error is unrecoverable, we stop the application
            SpringApplication.exit(event.getApplicationContext(), () -> 401);
        }
    }

    @Override
    public Optional<AnnisUser> getDesktopUserCredentials() {
      return desktopUserCredentials;
    }

}
