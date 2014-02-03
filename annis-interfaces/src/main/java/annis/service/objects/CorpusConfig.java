/*
 * Copyright 2012 SFB 632.
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
import java.util.Properties;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds the config of a corpus.
 *
 * <p>A {@link CorpusConfig} object wraps a {@link Properties} object. This
 * Properties object stores the corpus configuration as simple key-value
 * pairs.</p>
 *
 * @author thomas
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@XmlRootElement
public class CorpusConfig implements Serializable
{

  private Properties config;

  /**
   * Returns the underlying {@link Properties} object.
   *
   * @return {@link Properties} in case, their was never set a configuration
   * with {@link #setConfig(java.util.Properties)} or {@link #setConfig(java.lang.String, java.lang.String)
   * }
   */
  public Properties getConfig()
  {
    return config;
  }

  public void setConfig(Properties config)
  {
    this.config = config;
  }

  /**
   * Add a new configuration. If the config name already exists, the config
   * value is overwritten.
   *
   * @param configName The key of the config.
   * @param configValue The value of the new config.
   */
  public void setConfig(String configName, String configValue)
  {
    if (config == null)
    {
      config = new Properties();
    }

    if(configValue == null)
    {
      config.remove(configName);
    }
    else
    {
      config.setProperty(configName, configValue);
    }
  }

  /**
   * Returns a configuration from the underlying property object.
   *
   * @param configName The name of the configuration.
   * @return Can be null if the config name does not exists.
   */
  public String getConfig(String configName)
  {
    if (config != null)
    {
      return config.getProperty(configName);
    }
    else
    {
      return null;
    }
  }
  
  /**
   * Returns a configuration from the underlying property object.
   *
   * @param configName The name of the configuration.
   * @param def the default value, if no config is found.
   * @return Can be null if the config name does not exists.
   */
  public String getConfig(String configName, String def)
  {
    if (config != null)
    {
      return config.getProperty(configName, def);
    }
    else
    {
      return def;
    }
  }

  /**
   * Checks whether a key exists in the properties.
   *
   */
  public boolean containsKey(String key)
  {
    return (config == null) ? false : config.containsKey(key);
  }

  public boolean isEmpty()
  {
    return this.config == null ? true : config.isEmpty();
  }
}
