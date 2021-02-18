package org.corpus_tools.annis.gui;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.moandjiezana.toml.TomlWriter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import org.apache.commons.lang3.RandomStringUtils;
import org.corpus_tools.annis.gui.security.SecurityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

@Component
@Profile("desktop")
public class ServiceStarterDesktop extends ServiceStarter { // NO_UCD (unused code)

  private static final String USER_NAME = "desktop";
  private static final Logger log = LoggerFactory.getLogger(ServiceStarterDesktop.class);
  private final String secret = RandomStringUtils.randomAlphanumeric(50);
  private Optional<UsernamePasswordAuthenticationToken> desktopUserCredentials = Optional.empty();


  @Value("${server.port}")
  private String serverPort;

  @Autowired
  private Environment env;

  private List<Object> unpackToml(TomlArray orig) {
    if (orig == null) {
      return null;
    }
    ArrayList<Object> result = new ArrayList<>(orig.size());

    for (Object o : orig.toList()) {
      if (o instanceof TomlArray) {
        TomlArray tomlArray = (TomlArray) o;
        result.add(unpackToml(tomlArray));
      } else if (o instanceof TomlTable) {
        TomlTable tomlTable = (TomlTable) o;
        result.add(unpackToml(tomlTable));
      } else {
        result.add(o);
      }
    }

    return result;
  }

  private Map<String, Object> unpackToml(TomlTable orig) {
    if (orig == null) {
      return null;
    }

    LinkedHashMap<String, Object> result = new LinkedHashMap<>();

    for (Map.Entry<String, Object> e : orig.toMap().entrySet()) {
      if(e.getValue() instanceof TomlArray) {
        TomlArray tomlArray = (TomlArray) e.getValue();
        result.put(e.getKey(), unpackToml(tomlArray));
      } else if (e.getValue() instanceof TomlTable) {
        TomlTable tomlTable = (TomlTable) e.getValue();
        result.put(e.getKey(), unpackToml(tomlTable));
      } else {
        result.put(e.getKey(), e.getValue());
      }
    }
    
    return result;
  }

  /**
   * Overwrite the existing configuration file and add a temporary user for the desktop.
   */
  @Override
  protected File getServiceConfig() throws IOException {
    TomlParseResult tomlConfig = Toml.parse(super.getServiceConfig().toPath());
    Map<String, Object> config = unpackToml(tomlConfig);

    // Add it to the configuration
    Map<String, Object> tokenVerification = new LinkedHashMap<>();
    tokenVerification.put("type", "HS256");
    tokenVerification.put("secret", this.secret);

    // Add the new auth configuration (removing all existing settings in [auth])
    Map<String, Object> auth = new HashMap<>();
    auth.put("token_verification", tokenVerification);
    config.put("auth", auth);


    File temporaryFile = File.createTempFile("annis-service-config-desktop-", ".toml");
    TomlWriter writer = new TomlWriter();
    writer.write(config, temporaryFile);
    return temporaryFile;
  }

  private void showApplicationWindow(Desktop desktop) {
    try {
      UIManager.setLookAndFeel(new NimbusLookAndFeel());
    } catch (UnsupportedLookAndFeelException ex) {
      log.warn("Look and feel not supported", ex);
    }


    // Create a window where log messages and a link to the UI can be shown
    // This also allows to exit the application when no terminal is shown.
    JFrame mainFrame = new JFrame("ANNIS Desktop");
    BorderLayout mainLayout = new BorderLayout();
    mainFrame.getContentPane().setLayout(mainLayout);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setLocationByPlatform(true);


    final String webURL = "http://localhost:" + serverPort;
    JButton btLaunch = new JButton();

    btLaunch.setMnemonic('u');
    btLaunch.setForeground(Color.blue);
    btLaunch.setText("<html><u>Open " + webURL + " in browser</u></html>");
    btLaunch.setEnabled(true);
    btLaunch.setName("btLaunch");
    btLaunch.addActionListener((evt) -> {
      try {
        desktop.browse(new URI(webURL));
      } catch (IOException | URISyntaxException ex) {
        log.error("Could not open browser", ex);
      }
    });
    btLaunch.setPreferredSize(new Dimension(300, 60));
    mainFrame.getContentPane().add(btLaunch, BorderLayout.CENTER);

    JButton btExit = new JButton("Exit");
    btExit.addActionListener((evt) -> {
      System.exit(0);
    });
    mainFrame.getContentPane().add(btExit, BorderLayout.PAGE_END);

    mainFrame.pack();

    // Set icon for window
    Integer[] sizes = new Integer[] {192, 128, 64, 48, 32, 16, 14};
    List<Image> allImages = new LinkedList<Image>();

    for (int s : sizes) {
      try {
        BufferedImage imgIcon =
            ImageIO.read(ServiceStarterDesktop.class.getResource("logo/annis_" + s + ".png"));
        allImages.add(imgIcon);
      } catch (IOException ex) {
        log.error(null, ex);
      }
    }
    mainFrame.setIconImages(allImages);

    mainFrame.setVisible(true);
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    super.onApplicationEvent(event);

    List<String> roles = Arrays.asList("admin");
    Instant issuedAt = Instant.now();
    Instant expiresAt = Instant.now().plus(7l, ChronoUnit.DAYS);

    // Use the secret to sign a new JWT token with admin rights
    String signedToken = JWT.create().withSubject(USER_NAME)
        .withClaim(SecurityConfiguration.ROLES_CLAIM, roles).withExpiresAt(Date.from(expiresAt))
        .withIssuedAt(Date.from(issuedAt)).sign(Algorithm.HMAC256(this.secret));

    // Create the needed information for to represent this token as OIDC token in Spring
    // security
    List<? extends GrantedAuthority> grantedAuthorities =
        Arrays.asList(new SimpleGrantedAuthority("admin"));
    LinkedHashMap<String, Object> claims = new LinkedHashMap<>();
    claims.put(SecurityConfiguration.ROLES_CLAIM, roles);
    claims.put("sub", USER_NAME);
    OidcIdToken token = new OidcIdToken(signedToken, issuedAt, expiresAt, claims);
    DefaultOidcUser user = new DefaultOidcUser(grantedAuthorities, token);
    this.desktopUserCredentials =
        Optional.of(new UsernamePasswordAuthenticationToken(user, signedToken, grantedAuthorities));

    // Open the application in the browser
    String webURL = "http://localhost:" + serverPort;
    boolean isRunningHeadless =
        Arrays.stream(env.getActiveProfiles()).anyMatch(profile -> "headless".equals(profile));
    Desktop desktop =
        !isRunningHeadless && Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop == null) {
      log.warn(
          "ANNIS is running in desktop mode, but no desktop has been detected. You can open {} manually.",
          webURL);
    } else {
      showApplicationWindow(desktop);
      log.info("Opening {} in browser", webURL);
      try {
        desktop.browse(new URI(webURL));
      } catch (IOException | URISyntaxException ex) {
        log.error("Could not open " + webURL + " in browser.", ex);
      }
    }
  }

  @Override
  public Optional<UsernamePasswordAuthenticationToken> getDesktopUserToken() {
    return desktopUserCredentials;
  }

}
