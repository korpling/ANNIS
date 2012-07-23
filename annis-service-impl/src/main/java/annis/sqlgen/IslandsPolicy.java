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
package annis.sqlgen;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class IslandsPolicy
{

  private String defaultIslandsPolicy;

  public enum IslandPolicies
  {

    context, none
  }

  public IslandPolicies getMostRestrictivePolicy(List<Long> corpora,
    Map<Long, Properties> props)
  {
    if (corpora.isEmpty())
    {
      return IslandPolicies.valueOf(defaultIslandsPolicy);
    }

    IslandPolicies[] all =
      IslandPolicies.values();
    IslandPolicies result = all[all.length - 1];

    for (Long l : corpora)
    {
      IslandPolicies newPolicy = IslandPolicies.valueOf(defaultIslandsPolicy);

      if (props.get(l) != null)
      {
        newPolicy =
          IslandPolicies.valueOf(props.get(l).getProperty("islands-policy",
          defaultIslandsPolicy));
      }

      if (newPolicy.ordinal() < result.ordinal())
      {
        result = newPolicy;
      }
    }
    return result;
  }

  public String getDefaultIslandsPolicy()
  {
    return defaultIslandsPolicy;
  }

  public void setDefaultIslandsPolicy(String defaultIslandsPolicy)
  {
    this.defaultIslandsPolicy = defaultIslandsPolicy;
  }
}