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

import annis.libgui.CorpusSet;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration of an ANNIS user as stored as JSON in the database.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@XmlRootElement
public class UserConfig implements Serializable
{
  private List<CorpusSet> corpusSets = new LinkedList<>();
   
  @XmlElement(name="corpus-set")
  public List<CorpusSet> getCorpusSets()
  {
    return corpusSets;
  }

  public void setCorpusSets(List<CorpusSet> corpusSets)
  {
    this.corpusSets = corpusSets;
  } 
  
  
}
