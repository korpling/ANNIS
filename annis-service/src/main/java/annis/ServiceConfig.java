package annis;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

import annis.administration.AdministrationDao;

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
    
    @Key("annis.mail-sender")
    String mailSender();
    
    @Key("generateExampleQueries")
    @DefaultValue("IF_MISSING")
    AdministrationDao.EXAMPLE_QUERIES_CONFIG generateExampleQueries();
}
