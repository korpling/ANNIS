package annis.libgui;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;

@LoadPolicy(LoadType.MERGE)
@Sources({ "file:${user.home}/.annis/annis-gui.properties", "file:${ANNIS_CFG}/annis-gui.properties",
        "file:/etc/annis/annis-gui.properties", "classpath:annis-gui.properties" })
public interface UIConfig extends Config {

    @Key("AnnisWebService.URL")
    @DefaultValue("http://localhost:5711/annis/")
    String webserviceURL();

    @Key("disable-rtl")
    @DefaultValue("false")
    boolean disableRTL();

    @Key("bug-e-mail")
    @DefaultValue("")
    String bugEMail();

    @Key("login-url")
    String loginURL();

    @Key("login-window-maximized")
    boolean isLoginWindowMaximized();

}
