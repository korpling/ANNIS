/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin
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
package annis.libgui.visualizers;

import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SNode;

import annis.CommonHelper;
import annis.gui.FontConfig;
import annis.libgui.MatchedNodeColors;
import annis.service.ifaces.AnnisResult;
import annis.service.objects.AnnisResultImpl;
import annis.service.objects.RawTextWrapper;
import annis.utils.LegacyGraphConverter;

/**
 * Contains all needed data for a visualizer to perform the visualization.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class VisualizerInput implements Serializable
{
  private static final long serialVersionUID = 2L;
  
  private SDocument document = SaltFactory.createSDocument();

  private String namespace = "";

  private Map<String, Long> markedAndCovered = new HashMap<String, Long>();
  private transient Map<SNode, Long> cachedMarkedAndCoveredNodes;
  
  private Map<String, String> markableMap = new HashMap<String, String>();

  private Map<String, String> markableExactMap = new HashMap<String, String>();

  private String id = "";

  private String contextPath;

  private String annisWebServiceURL;

  private String dotPath;

  private AnnisResult result;

  private Properties mappings;

  private String resourcePathTemplate = "%s";

  private transient List<SToken> cachedToken;

  private Set<String> tokenAnnos;

  private String segmentationName;

  private FontConfig font;

  private RawTextWrapper rawText;

  public String getAnnisWebServiceURL()
  {
    return annisWebServiceURL;
  }

  /**
   * Set the URL which is configured for the Annis installation.
   *
   * @param annisRemoteServiceURL
   */
  public void setAnnisWebServiceURL(String annisRemoteServiceURL)
  {
    this.annisWebServiceURL = annisRemoteServiceURL;
  }

  /**
   * Gets the context path of this Annis installation.
   *
   * @return The context path, beginning with an "/" but *not* ending with it.
   */
  public String getContextPath()
  {
    return contextPath;
  }

  /**
   * Sets the context path of this Annis installation.
   *
   * @param contextPath The context path, beginning with an "/" but *not* ending
   * with it.
   */
  public void setContextPath(String contextPath)
  {
    this.contextPath = contextPath;
  }

  /**
   * Get the path to the dot graph layout generator program.
   *
   * @deprecated For configuration of visualizers please use the more general
   * {@link #getMappings()} .
   */
  @Deprecated
  public String getDotPath()
  {
    return dotPath;
  }

  /**
   * Set the path to the dot graph layout generator program.
   *
   * @param dotPath
   * @deprecated For configuration of visualizers please use the more general
   * {@link #setMappings(Properties)} .
   */
  @Deprecated
  public void setDotPath(String dotPath)
  {
    this.dotPath = dotPath;
  }

  /**
   * Gets an optional result id to be used by {@link #writeOutput(Writer)}
   *
   * @return
   */
  public String getId()
  {
    return id;
  }

  /**
   * Sets an optional result id to be used by {@link #writeOutput(Writer)}
   *
   * @param id result id to be used in output
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * Get the mappings for a visualizers. Mappings are visualizer specific
   * properties, that can be configured in the resolver table of the database
   *
   * @return The mappings as properties.
   */
  public Properties getMappings()
  {
    return mappings;
  }

  /**
   * Set the mappings for a visualizers. Mappings are visualizer specific
   * properties, that can be configured in the resolver table of the database
   *
   * @param mappings The new mappings.
   */
  public void setMappings(Properties mappings)
  {
    this.mappings = mappings;
  }

  /**
   * Same as {@link #getMarkableMap() } except that this only includes the
 really matched nodes and not covered cachedToken.
   *
   * @return
   */
  @Deprecated
  public Map<String, String> getMarkableExactMap()
  {
    return markableExactMap;
  }

  @Deprecated
  public void setMarkableExactMap(Map<String, String> markableExactMap)
  {
    this.markableExactMap = markableExactMap;
  }

  /**
   * Gets the map of markables used by {@link #writeOutput(Writer)}. The key of
 this map must be the corresponding node id of annotations or cachedTokens. The
 values must be HTML compatible color definitions like #000000 or red. For
 detailed information on HTML color definition refer to
 {@link http://www.w3schools.com/HTML/html_colornames.asp}
   *
   * @return
   */
  @Deprecated
  public Map<String, String> getMarkableMap()
  {
    return markableMap;
  }

  /**
   * Sets the map of markables used by {@link #writeOutput(Writer)}. The key of
 this map must be the corresponding node id of annotations or cachedTokens. The
 values must be HTML compatible color definitions like #000000 or red. For
 detailed information on HTML color definition refer to
 {@link http://www.w3schools.com/HTML/html_colornames.asp}
   *
   * @param markableMap
   */
  @Deprecated
  public void setMarkableMap(Map<String, String> markableMap)
  {
    this.markableMap = markableMap;
  }

  /**
   * This map is used for calculating the colors of a matching node.
   *
   * All Nodes, which are not included, should not be colorized. For getting
   * HEX-values according to html or css standards, it is possible to use
   * {@link MatchedNodeColors}.
   *
   */
  public void setMarkedAndCovered(Map<String, Long> markedAndCovered)
  {
    this.cachedMarkedAndCoveredNodes = null;
    this.markedAndCovered = markedAndCovered;
  }

  /**
   * This map is used for calculating the colors of a matching node.
   *
   * All Nodes, which are not included, should not be colorized. For getting
   * HEX-values according to html or css standards, it is possible to use
   * {@link MatchedNodeColors}.
   *
   * @return 
   */
  public Map<SNode, Long> getMarkedAndCovered()
  {
    if(cachedMarkedAndCoveredNodes == null)
    {      
      if (document != null)
      {
        cachedMarkedAndCoveredNodes = CommonHelper.createSNodeMapFromIDs(
          markedAndCovered, document.getDocumentGraph());
      }
    }
    
    return cachedMarkedAndCoveredNodes;
  }

  /**
   * Gets the namespace to be processed by {@link #writeOutput(Writer)}.
   *
   * @return
   */
  public String getNamespace()
  {
    return namespace;
  }

  /**
   * Sets the namespace to be processed by {@link #writeOutput(Writer)}.
   *
   * @param namespace Namespace to be processed
   */
  public void setNamespace(String namespace)
  {
    this.namespace = namespace;
  }

  @Deprecated
  public AnnisResult getResult()
  {
    if (result == null)
    {
      result
        = new AnnisResultImpl(LegacyGraphConverter.convertToAnnotationGraph(
            document));
    }
    return result;
  }

  public SDocument getDocument()
  {
    return document;
  }

  public void setDocument(SDocument document)
  {
    this.document = document;
  }

  /**
   * Alias for {@link VisualizerInput#setDocument(de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument)
   */
  public void setResult(SDocument document)
  {
    setDocument(document);
  }

  /**
   * Alias for {@link VisualizerInput#getDocument()}
   */
  public SDocument getSResult()
  {
    return getDocument();
  }

  public String getResourcePathTemplate()
  {
    return resourcePathTemplate;
  }

  public void setResourcePathTemplate(String resourcePathTemplate)
  {
    this.resourcePathTemplate = resourcePathTemplate;
  }

  /**
   * Returns a valid URL/path for which a relative (from the class package)
   * resource can be accessed.
   *
   * @param resource
   * @return
   */
  public String getResourcePath(String resource)
  {
    return String.format(resourcePathTemplate, resource);
  }

  /**
   * should contains a list of all cachedToken of a the which is available with
 {@link VisualizerInput#getSResult()}.
   *
   * @return TODO at the moment it's not certain, that cachedToken are nodes of the
 {@link VisualizerInput#getSResult()}.
   *
   */
  @Deprecated
  public List<SToken> getToken()
  {
    if(this.cachedToken == null)
    {
      this.cachedToken = getSResult().getDocumentGraph().getSortedTokenByText();
    }
    return this.cachedToken;
  }

  /**
   * Set all cachedToken annotations which should be displayed by the visualizer and
 correspondands to the annos choosen by the user in the annis gui.
   */
  public void setVisibleTokenAnnos(Set<String> tokenAnnos)
  {
    this.tokenAnnos = tokenAnnos;
  }

  /**
   * This cachedToken annotation should displayed by the visualizer and is selected by
 the user in the annis gui.
   */
  public Set<String> getVisibleTokenAnnos()
  {
    return this.tokenAnnos;
  }

  public void setSegmentationName(String segmentationName)
  {
    this.segmentationName = segmentationName;
  }

  /**
   * @return the segmentationName
   */
  public String getSegmentationName()
  {
    return segmentationName;
  }

  /**
   * Get the properties of the (web-) font in which this visualizer should
   * render the output. The visualizer is self-responsible for declaring a
   * backup font that is used when this font is not available.
   *
   * @return
   */
  public FontConfig getFont()
  {
    return font;
  }

  public void setFont(FontConfig font)
  {
    this.font = font;
  }

  /**
   * Gets the original text from the relAnnis text.tab file represented as a
   * String.
   *
   * <p>
   * This is a convenient and very fast method for extracting the whole text of
   * a document, since this method simply reads database tupel and does not map
   * anything to salt.</p>
   *
   * @return <ul><li>null - if the {@link VisualizerPlugin#isUsingRawText()}
   * method false for this visualizer.</li>
   *
   * <li>empty list - if there are only segmentations and the cachedToken layer is
 empty</li>
   *
   */
  public RawTextWrapper getRawText()
  {
    return rawText;
  }

  /**
   * Sets the raw text. This should only be done, if
   * {@link VisualizerPlugin#isUsingText()} return true.
   *
   * @param rawText the original text from the text.tab file in relAnnis.
   * Therefore could be an empty string.
   */
  public void setRawText(RawTextWrapper rawText)
  {
    this.rawText = rawText;
  }

}
