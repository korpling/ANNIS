package annis.gui.security;

import annis.gui.CommonUI;
import com.vaadin.ui.Notification;
import java.io.IOException;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class ReloginAuthenticator implements Authenticator {

  private final CommonUI ui;

  public ReloginAuthenticator(CommonUI ui) {
    this.ui = ui;
  }

  @Override
  public Request authenticate(Route route, Response response) throws IOException {
    ui.access(() -> {
      Notification.show("Login session expired", Notification.Type.WARNING_MESSAGE);
      ui.redirectToLogin();
    });
    return null;
  }

}
