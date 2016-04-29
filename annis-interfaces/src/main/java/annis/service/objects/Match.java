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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

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
 
  private static final Escaper spaceEscaper = Escapers.builder()
          .addEscape(' ', "%20")
          .addEscape('%', "%25") // also encode the percent itself
          .build();
  
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
    for(int i=0; i < saltIDs.size(); i++)
    {
      annos.add("");
    }
  }
  
  public Match(Collection<URI> originalIDs, Collection<String> originalAnnos)
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
      for(int i=0; i < saltIDs.size(); i++)
      {
        annos.add("");
      }
    }
  }
  
  public static Match parseFromString(String raw)
  {
    return parseFromString(raw, ' ');
  }
  
  public static Match parseFromString(String raw, char separator)
  {
    Match match = new Match();

    Splitter splitter = matchSplitter;
    if(separator != ' ')
    {
      splitter = Splitter.on(separator).trimResults().omitEmptyStrings();
    }
    
    for (String singleMatch : splitter.split(raw))
    {
      URI uri;
      
      // undo any escaping
      singleMatch = singleMatch.replace("%20", " ").replace("%25", "%");
      
      String id = "";
      String anno = null;
      if(singleMatch.startsWith("salt:/"))
      {
        id = singleMatch;
      }
      else
      {
        // split into the annotation namespace/name and the salt URI
        List<String> components = annoIDSplitter.splitToList(singleMatch);
        
        int componentsSize = components.size();
        
        
        Preconditions.checkArgument(componentsSize == 3 || componentsSize == 2, "A match containing "
          + "annotation information always has to have the form "
          + "ns::name::salt:/....  or name::salt:/....");
        
        String ns = "";
        String name = "";
        if(componentsSize == 3)
        {
          id = components.get(2);
          ns = components.get(0);
          name = components.get(1);
        }
        else if(componentsSize == 2)
        {
          id = components.get(1);
          name = components.get(0);
        }
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
        
        uri = new java.net.URI(id).normalize();
        
        if (!"salt".equals(uri.getScheme()) || uri.getFragment() == null)
        {
          throw new URISyntaxException("not a Salt id", uri.toString());
        }
        // check if the path ends with "/" (which was wrongly used by older ANNIS versions)
        String path = uri.getPath();
        if(path.endsWith("/"))
        {
          path = path.substring(0, path.length()-1);
          uri = new URI(uri.getScheme(), uri.getHost(), path, uri.getFragment());
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
          asString.add(singleMatchToString(u, anno));
        }
      }
      return Joiner.on(" ").join(asString);
    }
    return "";
  }
  
  public static String singleMatchToString(URI uri, String anno)
  {
    if(uri != null)
    {
      String v = uri.toASCIIString();
      if(anno != null && !anno.isEmpty())
      {
        v = spaceEscaper.escape(anno) + "::" + uri;
      }
      return v;
    }
    return "";
  }
  
  
}
