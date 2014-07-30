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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single match of an AQL query.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@XmlRootElement
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class Match implements Serializable
{
  
  private final static Logger log = LoggerFactory.getLogger(Match.class);

  private final static Splitter matchSplitter = Splitter.on(" ").trimResults().omitEmptyStrings();
  private final static Splitter annoIDSplitter = Splitter.on("::").trimResults().limit(3);
  
  private List<URI> saltIDs;
  private List<String> annos;

  public Match()
  {
    saltIDs = new ArrayList<>();
    annos = new ArrayList<>();
  }
  
  public Match(Collection<URI> originalIDs)
  {
    saltIDs = new ArrayList<>(originalIDs);
    annos = new ArrayList<>(saltIDs.size());
    for (URI saltID : saltIDs)
    {
      annos.add("");
    }
  }
  
  public Match(Collection<URI> originalIDs, List<String> originalAnnos)
  {
    saltIDs = new ArrayList<>(originalIDs);
    annos = new ArrayList<>(originalAnnos);
  }

  public void addSaltId(URI id)
  {
    addSaltId(id, null);
  }
  
  public void addSaltId(URI id, String anno)
  {
    if(id != null)
    {
      saltIDs.add(id);
      if(anno == null)
      {
        annos.add("");
      }
      else
      {
        annos.add(anno);
      }
    }
  }

  /**
   * Get Salt IDs of the nodes that are part of the match.
   * @return 
   */
  @XmlElement(name="id")
  public List<URI> getSaltIDs()
  {
    return saltIDs;
  }

  /**
   * @see #getSaltIDs() 
   * @param saltIDs 
   */
  public void setSaltIDs(List<URI> saltIDs)
  {
    this.saltIDs = saltIDs;
  }

  /**
   * Get the fully qualified annotation matched annotation names.
   * This list must be the same size as {@link #getSaltIDs() }.
   * If no annotation is matched, the list contains an entry with an empty string.
   * @return 
   */
  @XmlElement(name="anno")
  public List<String> getAnnos()
  {
    if(annos == null || annos.size() != saltIDs.size())
    {
      createEmptyAnnoList();
    }
    return annos;
  }

  public void setAnnos(List<String> annos)
  {
    this.annos = annos;
  }
  
  private void createEmptyAnnoList()
  {
    if(saltIDs != null)
    {
      annos = new ArrayList<>(saltIDs.size());
      for (URI saltID : saltIDs)
      {
        annos.add("");
      }
    }
  }
  
  public static Match parseFromString(String raw)
  {
    Match match = new Match();

    for (String singleMatch : matchSplitter.split(raw))
    {
      URI uri;
      
      String id;
      String anno = null;
      if(singleMatch.startsWith("salt:/"))
      {
        id = singleMatch;
      }
      else
      {
        // split into the annotation namespace/name and the salt URI
        List<String> components = annoIDSplitter.splitToList(singleMatch);
        Preconditions.checkArgument(components.size() == 3, "A match containing "
          + "annotation information always has to have the form "
          + "'ns::name::salt:/....");
        
        id = components.get(2);
        String ns = components.get(0);
        String name = components.get(1);
        if(ns.isEmpty())
        {
          anno = name;
        }
        else
        {
          anno = ns + "::" + name;
        }
      }
      
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
        log.error("Invalid syntax for ID " + singleMatch, ex);
        continue;
      }
      match.addSaltId(uri, anno);
    }

    return match;
  }

  /**
   * Returns a space seperated list of all Salt IDs.
   * @return 
   */
  @Override
  public String toString()
  {
    if(saltIDs != null && annos != null)
    {
      Iterator<URI> itID = saltIDs.iterator();
      Iterator<String> itAnno = annos.iterator();

      LinkedList<String> asString = new LinkedList<>();
      while(itID.hasNext() && itAnno.hasNext())
      {
        URI u = itID.next();
        String anno = itAnno.next();
        if(u != null)
        {
          String v = u.toASCIIString();
          if(anno != null && !anno.isEmpty())
          {
            v = anno + "::" + u;
          }
          asString.add(v);
        }
      }
      return Joiner.on(" ").join(asString);
    }
    return "";
  }
  
  
  
}
