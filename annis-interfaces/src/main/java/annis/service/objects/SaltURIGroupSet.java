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

import annis.CommonHelper;
import annis.model.QueryNode;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is only a wrapper to transport the salt ids with the
 * {@link QueryData} class as extension.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@XmlRootElement
public class SaltURIGroupSet implements Serializable
{
  private static final Logger log = LoggerFactory.getLogger(SaltURIGroupSet.class);
  
  private Map<Integer, SaltURIGroup> groups;
  
  
  public SaltURIGroupSet()
  {
    groups = new TreeMap<Integer, SaltURIGroup>();
  }

  public Map<Integer, SaltURIGroup> getGroups()
  {
    return groups;
  }

  public void setGroups(Map<Integer, SaltURIGroup> groups)
  {
    this.groups = groups;
  }
  
  /**
   * Parses a string representation of a {@link SaltURIGroupSet}. 
   * Each group is separated with a ";" and each Salt ID with a ",".
   * Example: 
   * {@code 
   * salt://id1,salt://id2,salt://id3;salt://id4,salt://id5;salt://id6
   * }
   * 
   * @param raw
   * @return 
   */
  public static SaltURIGroupSet fromString(String raw)
  {
    SaltURIGroupSet saltIDs = new SaltURIGroupSet();


    int i = 0;
    for (String group : raw.split("\\s*;\\s*"))
    {
      SaltURIGroup urisForGroup = new SaltURIGroup();

      for (String id : group.split("[,\\s]+"))
      {
        java.net.URI uri;
        try
        {
          uri = new java.net.URI(id);

          if (!"salt".equals(uri.getScheme()) || uri.getFragment() == null)
          {
            throw new URISyntaxException("not a Salt id", uri.toString());
          }
        }
        catch (URISyntaxException ex)
        {
          log.error("Invalid syntax for ID " + id, ex);
          continue;
        }
        urisForGroup.getUris().add(uri);
      }
      
      saltIDs.getGroups().put(++i, urisForGroup);
    }
    
    return saltIDs;
  }
  
  
}
