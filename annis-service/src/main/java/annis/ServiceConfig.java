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
    
    @Key("max-context-left")
    @DefaultValue("20")
    int maxContextLeft();
    
    @Key("max-context-right")
    @DefaultValue("20")
    int maxContextRight();
    
    @Key("default-context")
    @DefaultValue("5")
    int defaultContext();
    
    @Key("context-steps")
    @DefaultValue("5")
    int contextSteps();
    
    @Key("default-context-segmentation")
    @DefaultValue("tok")
    String defaultContextSegmenation();

    @Key("default-base-text-segmentation")
    @DefaultValue("tok")
    String defaultBaseTextSegmentation();
    
    @Key("results-per-page")
    @DefaultValue("10")
    int resultsPerPage();

    @Key("browse-documents")
    @DefaultValue("true")
    boolean browseDocuments();
    
}
