/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
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
package annis.service.objects;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a single match of an AQL query.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@XmlRootElement
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class Match implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 4550139902283358825L;

  private final static Splitter matchSplitter = Splitter.on(" ").trimResults().omitEmptyStrings();
  private final static Splitter annoIDSplitter = Splitter.on("::").trimResults().limit(3);

  private static final Escaper spaceEscaper =
      Escapers.builder().addEscape(' ', "%20").addEscape('%', "%25").build();

  public static Match parseFromString(String raw) {
    return parseFromString(raw, ' ');
  }

  private static Match parseFromString(String raw, char separator) {
    Match match = new Match();

    Splitter splitter = matchSplitter;
    if (separator != ' ') {
      splitter = Splitter.on(separator).trimResults().omitEmptyStrings();
    }

    for (String singleMatch : splitter.split(raw)) {
      parseSingleMatchComponent(singleMatch, match);
    }

    return match;
  }

  private static void parseSingleMatchComponent(String singleMatch, Match result) {

    String id = "";
    String anno = null;
    // split into the annotation namespace/name and the salt URI
    List<String> components = annoIDSplitter.splitToList(singleMatch);

    int componentsSize = components.size();
    if (components.size() == 1) {
      id = singleMatch;
    } else {
      Preconditions.checkArgument(componentsSize == 3 || componentsSize == 2, "A match containing "
          + "annotation information always has to have the form " + "ns::name::id  or name::id");

      String ns = "";
      String name = "";
      if (componentsSize == 3) {
        id = components.get(2);
        ns = components.get(0);
        name = components.get(1);
      } else if (componentsSize == 2) {
        id = components.get(1);
        name = components.get(0);
      }
      if (ns.isEmpty()) {
        anno = name;
      } else {
        anno = ns + "::" + name;
      }
      // undo any escaping for the annotation part
      anno = anno.replace("%20", " ").replace("%25", "%").replace("%2C", ",");
    }

    // Remove a possible legacy id prefix
    if (id.startsWith("salt:/")) {
      id = id.substring("salt:/".length());
    }

    result.addSaltId(id, anno);
  }

  private static String singleMatchToString(String id, String anno) {
    if (id != null) {
      String v = id;
      if (anno != null && !anno.isEmpty()) {
        v = spaceEscaper.escape(anno) + "::" + id;
      }
      return v;
    }
    return "";
  }

  private List<String> saltIDs;

  private List<String> annos;

  public Match() {
    saltIDs = new ArrayList<>();
    annos = new ArrayList<>();
  }

  public Match(Collection<String> originalIDs) {
    saltIDs = new ArrayList<>(originalIDs);
    annos = new ArrayList<>(saltIDs.size());
    for (int i = 0; i < saltIDs.size(); i++) {
      annos.add("");
    }
  }



  private void addSaltId(String id, String anno) {
    if (id != null) {
      saltIDs.add(id);
      if (anno == null) {
        annos.add("");
      } else {
        annos.add(anno);
      }
    }
  }

  private void createEmptyAnnoList() {
    if (saltIDs != null) {
      annos = new ArrayList<>(saltIDs.size());
      for (int i = 0; i < saltIDs.size(); i++) {
        annos.add("");
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Match other = (Match) obj;
    if (annos == null) {
      if (other.annos != null)
        return false;
    } else if (!annos.equals(other.annos))
      return false;
    if (saltIDs == null) {
      if (other.saltIDs != null)
        return false;
    } else if (!saltIDs.equals(other.saltIDs))
      return false;
    return true;
  }

  /**
   * Get the fully qualified annotation matched annotation names. This list must be the same size as
   * {@link #getSaltIDs() }. If no annotation is matched, the list contains an entry with an empty
   * string.
   * 
   * @return list of annotation names
   */
  @XmlElement(name = "anno")
  public List<String> getAnnos() {
    if (annos == null || annos.size() != saltIDs.size()) {
      createEmptyAnnoList();
    }
    return annos;
  }

  /**
   * Get Salt IDs of the nodes that are part of the match.
   * 
   * @return a list of IDs as URI
   */
  @XmlElement(name = "id")
  public List<String> getSaltIDs() {
    return saltIDs;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((annos == null) ? 0 : annos.hashCode());
    result = prime * result + ((saltIDs == null) ? 0 : saltIDs.hashCode());
    return result;
  }

  public void setAnnos(List<String> annos) {
    this.annos = annos;
  }

  /**
   * @see #getSaltIDs()
   * @param saltIDs the list of IDs as URI
   */
  public void setSaltIDs(List<String> saltIDs) {
    this.saltIDs = saltIDs;
  }

  /**
   * Returns a space separated list of all Salt IDs.
   * 
   * @return list of IDs as string
   */
  @Override
  public String toString() {
    if (saltIDs != null && annos != null) {
      Iterator<String> itID = saltIDs.iterator();
      Iterator<String> itAnno = annos.iterator();

      LinkedList<String> asString = new LinkedList<>();
      while (itID.hasNext() && itAnno.hasNext()) {
        String u = itID.next();
        String anno = itAnno.next();
        if (u != null) {
          asString.add(singleMatchToString(u, anno));
        }
      }
      return Joiner.on(" ").join(asString);
    }
    return "";
  }

}
