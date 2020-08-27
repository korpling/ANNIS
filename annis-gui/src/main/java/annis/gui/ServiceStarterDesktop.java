package annis.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import org.apache.commons.lang3.RandomStringUtils;
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.AuthentificationApi;
import org.corpus_tools.annis.api.model.InlineObject1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import annis.libgui.AnnisUser;

@Component
@Profile("desktop")
public class ServiceStarterDesktop extends ServiceStarter {

    private static final String USER_NAME = "desktop";
    private static final Logger log = LoggerFactory.getLogger(ServiceStarterDesktop.class);
    private String password;
    private String basePath;
    private Optional<AnnisUser> desktopUserCredentials = Optional.empty();


    @Value("${server.port}")
    private String serverPort;

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

        // Open the application in the browser
        String webURL = "http://localhost:" + serverPort;
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop == null) {
            log.warn(
                    "ANNIS is running in desktop mode, but no desktop has been detected. You can open {} manually.",
                    webURL);
        } else {
            log.info("Opening {} in browser", webURL);
            try {
                Desktop.getDesktop().browse(new URI(webURL));
            } catch (IOException | URISyntaxException ex) {
                log.error("Could not open " + webURL + " in browser.", ex);
            }
        }
    }

    @Override
    public Optional<AnnisUser> getDesktopUserCredentials() {
        return desktopUserCredentials;
    }

}
