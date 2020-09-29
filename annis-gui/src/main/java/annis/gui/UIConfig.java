package annis.gui;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "annis")
public class UIConfig {

  private String bugEMail;

  private boolean disableRTL;

  private boolean loginWindowMaxmized;

  private String loginURL;

  private String webserviceUrl;
  private String webserviceConfig;

  private boolean shortenReferenceLinks;
  
  public String getBugEmail() {
    return bugEMail;
  }

  public void setBugEmail(String bugEmail) {
    this.bugEMail = bugEmail;
  }

  public boolean isLoginWindowMaxmized() {
    return loginWindowMaxmized;
  }

  public void setLoginWindowMaxmized(boolean loginWindowMaxmized) {
    this.loginWindowMaxmized = loginWindowMaxmized;
  }

  public String getLoginURL() {
    return loginURL;
  }

  public void setLoginURL(String loginURL) {
    this.loginURL = loginURL;
  }

  public String getWebserviceUrl() {
    return webserviceUrl;
  }

  public void setWebserviceUrl(String webserviceUrl) {
    this.webserviceUrl = webserviceUrl;
  }

  public boolean isShortenReferenceLinks() {
    return shortenReferenceLinks;
  }

  public void setShortenReferenceLinks(boolean shortenReferenceLinks) {
    this.shortenReferenceLinks = shortenReferenceLinks;
  }

  public boolean isDisableRTL() {
    return disableRTL;
  }

  public void setDisableRTL(boolean disableRTL) {
    this.disableRTL = disableRTL;
  }

  public String getWebserviceConfig() {
    return webserviceConfig;
  }

  public void setWebserviceConfig(String webserviceConfig) {
    this.webserviceConfig = webserviceConfig;
  }  
}
