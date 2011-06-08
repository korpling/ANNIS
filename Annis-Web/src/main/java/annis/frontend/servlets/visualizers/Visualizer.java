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
package annis.frontend.servlets.visualizers;

import annis.service.ifaces.AnnisResult;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public abstract class Visualizer
{

  private String namespace = "";
  private String paula = null;
  private Map<String, String> markableMap = new HashMap<String, String>();
  private Map<String, String> markableExactMap = new HashMap<String, String>();
  private String id = "";
  private String contextPath;
  private String annisRemoteServiceURL;
  private String dotPath;
  private Document paulaJDOM = null;
  private AnnisResult result;
  private Properties mappings;

  // BEGIN properties

  /**
   * Gets the namespace to be processed by {@link #writeOutput(Writer)}.
   * @return
   */
  public String getNamespace()
  {
    return namespace;
  }

  /**
   * Sets the namespace to be processed by {@link #writeOutput(Writer)}.
   * @param namespace Namespace to be processed
   */
  public void setNamespace(String namespace)
  {
    this.namespace = namespace;
  }

  /**
   * Gets the context path of this Annis installation.
   * @return The context path, beginning with an "/" but *not* ending with it.
   */
  public String getContextPath()
  {
    return contextPath;
  }

  /**
   * Sets the context path of this Annis installation.
   * @param contextPath The context path, beginning with an "/" but *not* ending with it.
   */
  public void setContextPath(String contextPath)
  {
    this.contextPath = contextPath;
  }

  /**
   * Get the URL which is configured for the Annis installation.
   * @return
   */
  public String getAnnisRemoteServiceURL()
  {
    return annisRemoteServiceURL;
  }

  /**
   * Set the URL which is configured for the Annis installation.
   * @param annisRemoteServiceURL
   */
  public void setAnnisRemoteServiceURL(String annisRemoteServiceURL)
  {
    this.annisRemoteServiceURL = annisRemoteServiceURL;
  }



  /**
   * Gets the map of markables used by {@link #writeOutput(Writer)}. The key of this map must be the corresponding node id of annotations or tokens.
   * The values must be HTML compatible color definitions like #000000 or red. For detailed information on HTML color definition refer to {@link http://www.w3schools.com/HTML/html_colornames.asp}
   * @return
   */
  public Map<String, String> getMarkableMap()
  {
    return markableMap;
  }

  /**
   * Sets the map of markables used by {@link #writeOutput(Writer)}. The key of this map must be the corresponding node id of annotations or tokens.
   * The values must be HTML compatible color definitions like #000000 or red. For detailed information on HTML color definition refer to {@link http://www.w3schools.com/HTML/html_colornames.asp}
   * @param markableMap
   */
  public void setMarkableMap(Map<String, String> markableMap)
  {
    this.markableMap = markableMap;
  }

  /**
   * Same as {@link #getMarkableMap() } except that this only includes the really
   * matched nodes and not covered token.
   * @return
   */
  public Map<String, String> getMarkableExactMap()
  {
    return markableExactMap;
  }

  public void setMarkableExactMap(Map<String, String> markableExactMap)
  {
    this.markableExactMap = markableExactMap;
  }



  /**
   * Gets an optional result id to be used by {@link #writeOutput(Writer)}
   * @return
   */
  public String getId()
  {
    return id;
  }

  /**
   * Sets an optional result id to be used by {@link #writeOutput(Writer)}
   * @param id result id to be used in output
   */
  public void setId(String id)
  {
    this.id = id;
  }

  public AnnisResult getResult()
  {
    return result;
  }

  public void setResult(AnnisResult result)
  {
    this.result = result;
  }

  /**
   * Gets the private paula String property that will be uses by {@link #writeOutput(Writer)}.
   * @return
   * @deprecated
   */
  @Deprecated
  public String getPaula()
  {
    if(paula == null)
    {
      // construct Paula from result
      paula = result.getPaula();
    }
    return paula;
  }

//  /**
//   * Sets the private paula String property that will be uses by {@link #writeOutput(Writer)}.
//   * @param paula
//   */
//  public void setPaula(String paula)
//  {
//    this.paula = paula;
//    this.paulaJDOM = null;
//  }


  /**
   * Get a JDOM Document representing paula. Will be generated only once.
   * @deprecated
   */
  @Deprecated
  protected Document getPaulaJDOM()
  {
    if(paulaJDOM == null)
    {
      try
      {
        paulaJDOM = new SAXBuilder().build(new InputSource(new StringReader(getPaula())));
      }
      catch(Exception ex)
      {
        Logger.getLogger(Visualizer.class.getName()).log(Level.SEVERE, null, ex);
        
        // never return null
        paulaJDOM = new Document();
      }
    }
    
    return paulaJDOM;
  }

  /**
   * Set the path to the dot graph layout generator program.
   * @param dotPath
   * @deprecated For configuration of visualizers please use the more general
   *             {@link #setMappings(Properties)} .
   */
  @Deprecated
  public void setDotPath(String dotPath)
  {
    this.dotPath = dotPath;
  }

  /**
   * Get the path to the dot graph layout generator program.
   * @deprecated For configuration of visualizers please use the more general
   *             {@link #getMappings()} .
   */
  @Deprecated
  public String getDotPath()
  {
    return dotPath;
  }

  /**
   * Get the mappings for a visualizers. Mappings are visualizer specific
   * properties, that can be configured in the resolver table of the database
   * @return The mappings as properties.
   */
  public Properties getMappings()
  {
    return mappings;
  }

  /**
   * Set the mappings for a visualizers. Mappings are visualizer specific
   * properties, that can be configured in the resolver table of the database
   * @param mappings The new mappings.
   */
  public void setMappings(Properties mappings)
  {
    this.mappings = mappings;
  }

  // END properties

  /**
   * Writes the final output to passed OutputStream. The stream should remain open.
   * @param outstream the OutputStream to be used
   */
  public abstract void writeOutput(OutputStream outstream);

  /**
   * Returns the content-type for this particular Visualizer output. For more information see {@link javax.servlet.ServletResponse#setContentType(String)}.
   * Must be overridden to change default "text/html".
   * @return the ContentType
   */
  public String getContentType()
  {
    return "text/html";
  }

  /**
   * Returns the character endocing for this particular Visualizer output. For more information see {@link javax.servlet.ServletResponse#setCharacterEncoding(String)}.
   * Must be overridden to change default "utf-8".
   * @return the CharacterEncoding
   */
  public String getCharacterEncoding()
  {
    return "utf-8";
  }


}
