/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.service.objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Maps corpus names to corpus configurations.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@XmlRootElement
public class CorpusConfigMap implements Serializable
{

  private Map<String, CorpusConfig> corpusConfigs = new HashMap<String, CorpusConfig>();

  /**
   * @return the corpusConfigs
   */
  public Map<String, CorpusConfig> getCorpusConfigs()
  {
    return (corpusConfigs == null) ? new HashMap<String, CorpusConfig>() : corpusConfigs;
  }

  /**
   * @param corpusConfigs the corpusConfigs to set
   */
  public void setCorpusConfigs(
    Map<String, CorpusConfig> corpusConfigs)
  {
    this.corpusConfigs = corpusConfigs;
  }

  public CorpusConfig put(String k, CorpusConfig corpusConfig)
  {
    return corpusConfigs.put(k, corpusConfig);
  }

  public CorpusConfig get(String k)
  {
    return corpusConfigs.get(k);
  }

  /**
   * Checks if a corpus configuration is defined for a specific corpus name.
   *
   * @param corpusName The corpus name, for which the config is lookup.
   */
  public boolean containsConfig(String corpusName)
  {
    if (corpusConfigs != null)
    {
      return corpusConfigs.containsKey(corpusName);
    }

    return false;
  }

  public boolean isEmpty()
  {
    return (corpusConfigs == null)? true : corpusConfigs.isEmpty();
  }
}
