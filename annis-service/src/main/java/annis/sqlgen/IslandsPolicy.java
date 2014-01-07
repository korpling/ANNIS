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
 * Matched nodes that are too far apart to be shown in the same context 
 * (distance > context_left+context_right) normally create islands 
 * ("..." is displayed between the tokens).
 * 
 * This class contains an enum which is used to configure the behavior of
 * ANNIS in this case.
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class IslandsPolicy
{

  private String defaultIslandsPolicy;

  /**
   * Define behavior for matched nodes that are too far away.
   */
  public enum IslandPolicies
  {
    
    /** 
     * Use the context as the base to determine how far a node can be away from
     * a matched node in order to be included in the result.
     */
    context,
    /** Never produce islands, always include all nodes between the matched nodes. */
    none
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