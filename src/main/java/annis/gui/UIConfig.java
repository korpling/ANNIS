package annis.gui;

import java.io.Serializable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "annis")
public class UIConfig implements Serializable {

  private static final long serialVersionUID = 8667972535616579418L;

  private String bugEMail;
  private String mailHost;
  private String mailUser;
  private String mailPassword;
  private int mailPort;
  private boolean mailTLS;
  private String mailFrom;

  private boolean disableRTL;


  private String webserviceUrl;
  private String webserviceConfig;

  private boolean shortenReferenceLinks;
  
  public String getBugEmail() {
    return bugEMail;
  }

  public void setBugEmail(String bugEmail) {
    this.bugEMail = bugEmail;
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

  public String getMailHost() {
    return mailHost;
  }

  public void setMailHost(String mailHost) {
    this.mailHost = mailHost;
  }

  public String getMailUser() {
    return mailUser;
  }

  public void setMailUser(String mailUser) {
    this.mailUser = mailUser;
  }

  public String getMailPassword() {
    return mailPassword;
  }

  public void setMailPassword(String mailPassword) {
    this.mailPassword = mailPassword;
  }

  public int getMailPort() {
    return mailPort;
  }

  public void setMailPort(int mailPort) {
    this.mailPort = mailPort;
  }

  public boolean isMailTLS() {
    return mailTLS;
  }

  public void setMailTLS(boolean mailTLS) {
    this.mailTLS = mailTLS;
  }

  public String getMailFrom() {
    return mailFrom;
  }

  public void setMailFrom(String mailFrom) {
    this.mailFrom = mailFrom;
  }  
}
