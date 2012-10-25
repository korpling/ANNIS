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
package annis.security;

import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Properties of an ANNIS user as stored in the configuration.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@XmlRootElement
public class AnnisUserConfig
{
  private String name;
  
  private Map<String, List<String>> corpusSets;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  @XmlElementWrapper(name="corpusSets")
  public Map<String, List<String>> getCorpusSets()
  {
    return corpusSets;
  }

  public void setCorpusSets(Map<String, List<String>> corpusSets)
  {
    this.corpusSets = corpusSets;
  }
  
  
}
