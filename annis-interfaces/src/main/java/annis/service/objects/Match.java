/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.service.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("serial")
@XmlRootElement
public class Match implements Serializable
{

  private List<String> saltIDs;

  public Match()
  {
    saltIDs = new ArrayList<String>();
  }

  public void setSaltId(String id)
  {
    saltIDs.add(id);
  }

  public String getSaltId(int i)
  {
    return saltIDs.get(i);
  }

  @XmlElementWrapper(name="salt-ids")
  @XmlElement(name="id")
  public List<String> getSaltIDs()
  {
    return saltIDs;
  }

  public void setSaltIDs(List<String> saltIDs)
  {
    this.saltIDs = saltIDs;
  }

  /**
   * Returns a comma seperated list of all Salt IDs.
   * @return 
   */
  @Override
  public String toString()
  {
    return StringUtils.join(saltIDs, ",");
  }
  
  
  
}
