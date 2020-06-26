package annis;

import annis.administration.AdministrationDao;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({ "file:${annis.home}/conf/annis-service.properties" })
public interface ServiceConfig extends Config {

    @Key("browse-documents")
    @DefaultValue("true")
    boolean browseDocuments();

    @Key("context-steps")
    @DefaultValue("5")
    int contextSteps();

    @Key("annis.data-path")
    String dataPath();

    @Key("default-base-text-segmentation")
    @DefaultValue("tok")
    String defaultBaseTextSegmentation();

    @Key("default-context")
    @DefaultValue("5")
    int defaultContext();

    @Key("default-context-segmentation")
    @DefaultValue("tok")
    String defaultContextSegmenation();

    @Key("generateExampleQueries")
    @DefaultValue("IF_MISSING")
    AdministrationDao.EXAMPLE_QUERIES_CONFIG generateExampleQueries();

    @Key("annis.mail-sender")
    String mailSender();

    @Key("max-context-left")
    @DefaultValue("20")
    int maxContextLeft();

    @Key("max-context-right")
    @DefaultValue("20")
    int maxContextRight();

    @Key("max-corpus-cache-size")
    @DefaultValue("-1")
    long maxCorpusCacheSize();

    @Key("parallel-query-execution")
    @DefaultValue("true")
    boolean parallelQueryExecution();

    @Key("prefer-disk-based")
    @DefaultValue("false")
    boolean preferDiskBased();

    @Key("results-per-page")
    @DefaultValue("10")
    int resultsPerPage();

    @Key("annis.sql-timeout")
    @DefaultValue("60000")
    int timeout();

    @Key("annis.webservice-port")
    @DefaultValue("5711")
    int webservicePort();

}
