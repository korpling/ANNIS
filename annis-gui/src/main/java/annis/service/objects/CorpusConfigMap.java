/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package annis.service.objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import org.corpus_tools.annis.api.model.CorpusConfiguration;

/**
 * Maps corpus names to corpus configurations.
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
@XmlRootElement
public class CorpusConfigMap implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -7045818041525225347L;
  private Map<String, CorpusConfiguration> corpusConfigs = new HashMap<>();

  /**
   * Checks if a corpus configuration is defined for a specific corpus name.
   *
   * @param corpusName The corpus name, for which the config is lookup.
   * @return True if corpus configuration is defined for this corpus.
   */
  public boolean containsConfig(String corpusName) {
    if (corpusConfigs != null) {
      return corpusConfigs.containsKey(corpusName);
    }

    return false;
  }

  public CorpusConfiguration get(String k) {
    return corpusConfigs.get(k);
  }

  /**
   * @return the corpusConfigs
   */
  public Map<String, CorpusConfiguration> getCorpusConfigs() {
    return (corpusConfigs == null) ? new HashMap<>() : corpusConfigs;
  }

  public boolean isEmpty() {
    return corpusConfigs == null || corpusConfigs.isEmpty();
  }

  public CorpusConfiguration put(String k, CorpusConfiguration corpusConfig) {
    return corpusConfigs.put(k, corpusConfig);
  }

  /**
   * @param corpusConfigs the corpusConfigs to set
   */
  public void setCorpusConfigs(Map<String, CorpusConfiguration> corpusConfigs) {
    this.corpusConfigs = corpusConfigs;
  }
}
