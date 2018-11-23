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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * Describes a group of matches for an AQL query.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@XmlRootElement(name = "match-group")
public class MatchGroup implements Serializable
{
  private List<Match> matches;
  
  public final static Splitter lineSplitter = Splitter.on('\n').trimResults().omitEmptyStrings();
  
  private final static Joiner lineJoiner = Joiner.on('\n');
  
  public MatchGroup()
  {
    matches = new ArrayList<>();
  }
  
  public MatchGroup(Collection<Match> orig)
  {
    matches = new ArrayList<>(orig);
  }
  
  @XmlElement(name = "match")
  public List<Match> getMatches()
  {
    return matches;
  }
  
 

  public void setMatches(List<Match> matches)
  {
    this.matches = matches;
  }

  
  
  /**
   * Parses a string representation of a {@link MatchGroup}. 
   * Each group is separated with a new line and each Salt ID with a space.
   * Example: 
   * {@code 
   * salt://id1,salt://id2 salt://id3
   * salt://id4
   * salt://id5 salt://id6
   * }
   * 
   * @param raw
   * @return 
   */
  public static MatchGroup parseString(String raw)
  {
    MatchGroup saltIDs = new MatchGroup();
    for (String group : lineSplitter.split(raw))
    {
      saltIDs.matches.add(Match.parseFromString(group));
    }
    
    return saltIDs;
  }

  /**
   * Generates a string where each group is separated by new line  and each ID inside a group by space.
   * This string can be parsed by {@link #parseString(java.lang.String) }.
   * @return 
   */
  @Override
  public String toString()
  {
    List<String> lines = new LinkedList<>();
    
    for(Match m : this.matches)
    {
      lines.add(m.toString());
    }
    
    return lineJoiner.join(lines);
  }
  
  
  
  
}
