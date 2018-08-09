package annis;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({"file:${annis.home}/conf/annis-service.properties"})
public interface ServiceConfig extends Config {
    
    @Key("annis.webservice-port")
    @DefaultValue("5711")
    int webservicePort();
    
    @Key("annis.sql-timeout")
    @DefaultValue("60000")
    int timeout();
    
    @Key("annis.external-data-path")
    String externalDataPath();
}
