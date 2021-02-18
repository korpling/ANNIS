/*
 * Copyright 2012 SFB 632.
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
package org.corpus_tools.annis.gui.objects;

import com.google.common.base.Joiner;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Describes a group of matches for an AQL query.
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@XmlRootElement(name = "match-group")
public class MatchGroup implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -439541347794416714L;
  private final static Joiner lineJoiner = Joiner.on('\n');

  private List<Match> matches;

  public MatchGroup() {
    matches = new ArrayList<>();
  }

  @XmlElement(name = "match")
  public List<Match> getMatches() {
    return matches;
  }



  public void setMatches(List<Match> matches) {
    this.matches = matches;
  }

  /**
   * Generates a string where each group is separated by new line and each ID inside a group by
   * space. This string can be parsed by {@link #parseString(java.lang.String) }.
   * 
   * @return a string representation
   */
  @Override
  public String toString() {
    List<String> lines = new LinkedList<>();

    for (Match m : this.matches) {
      lines.add(m.toString());
    }

    return lineJoiner.join(lines);
  }



}
