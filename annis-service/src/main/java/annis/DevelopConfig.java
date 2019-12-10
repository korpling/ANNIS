package annis;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({"file:${annis.home}/conf/develop.properties"})
public interface DevelopConfig extends Config {
    
    @Key("annis.script-path")
    @DefaultValue("${annis.home}/sql")
    String scriptPath();
}
