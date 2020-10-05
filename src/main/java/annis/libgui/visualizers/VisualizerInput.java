/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin
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
package annis.libgui.visualizers;

import annis.gui.CommonUI;
import annis.gui.FontConfig;
import annis.libgui.MatchedNodeColors;
import annis.service.objects.RawTextWrapper;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.core.SNode;

/**
 * Contains all needed data for a visualizer to perform the visualization.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class VisualizerInput implements Serializable {
  private static final long serialVersionUID = 2L;

  private SDocument document = SaltFactory.createSDocument();

  private String namespace = "";

  private Map<SNode, Long> markedAndCovered = new HashMap<>();

  private String id = "";

  private String contextPath;

  private CommonUI ui;

  private Map<String, String> mappings;

  private String resourcePathTemplate = "%s";

  private Set<String> tokenAnnos;

  private String segmentationName;

  private FontConfig font;

  private RawTextWrapper rawText;

  /**
   * Gets the context path of this Annis installation.
   *
   * @return The context path, beginning with an "/" but *not* ending with it.
   */
  public String getContextPath() {
    return contextPath;
  }

  public SDocument getDocument() {
    return document;
  }

  /**
   * Get the properties of the (web-) font in which this visualizer should render the output. The
   * visualizer is self-responsible for declaring a backup font that is used when this font is not
   * available.
   *
   * @return
   */
  public FontConfig getFont() {
    return font;
  }

  /**
   * Gets an optional result id to be used by {@link #writeOutput(Writer)}
   *
   * @return
   */
  public String getId() {
    return id;
  }

  /**
   * Get the mappings for a visualizers. Mappings are visualizer specific properties, that can be
   * configured in the resolver table of the database
   *
   * @return The mappings as properties.
   */
  public Map<String, String> getMappings() {
    return mappings;
  }

  /**
   * This map is used for calculating the colors of a matching node.
   *
   * All Nodes, which are not included, should not be colorized. For getting HEX-values according to
   * html or css standards, it is possible to use {@link MatchedNodeColors}.
   *
   * @return
   */
  public Map<SNode, Long> getMarkedAndCovered() {
    return this.markedAndCovered;
  }

  /**
   * Gets the namespace to be processed by {@link #writeOutput(Writer)}.
   *
   * @return
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * Gets the original text from the relAnnis text.tab file represented as a String.
   *
   * <p>
   * This is a convenient and very fast method for extracting the whole text of a document, since
   * this method simply reads database tupel and does not map anything to salt.
   * </p>
   *
   * @return
   *         <ul>
   *         <li>null - if the {@link VisualizerPlugin#isUsingRawText()} method false for this
   *         visualizer.</li>
   *
   *         <li>empty list - if there are only segmentations and the cachedToken layer is
   *         empty</li>
   *
   */
  public RawTextWrapper getRawText() {
    return rawText;
  }

  /**
   * Returns a valid URL/path for which a relative (from the class package) resource can be
   * accessed.
   *
   * @param resource
   * @return
   */
  public String getResourcePath(String resource) {
    return String.format(resourcePathTemplate, resource);
  }

  public String getResourcePathTemplate() {
    return resourcePathTemplate;
  }

  /**
   * @return the segmentationName
   */
  public String getSegmentationName() {
    return segmentationName;
  }

  /**
   * Alias for {@link VisualizerInput#getDocument()}
   */
  public SDocument getSResult() {
    return getDocument();
  }

  public CommonUI getUI() {
    return ui;
  }

  /**
   * This cachedToken annotation should displayed by the visualizer and is selected by the user in
   * the annis gui.
   */
  public Set<String> getVisibleTokenAnnos() {
    return this.tokenAnnos;
  }

  /**
   * Sets the context path of this Annis installation.
   *
   * @param contextPath The context path, beginning with an "/" but *not* ending with it.
   */
  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  public void setDocument(SDocument document) {
    this.document = document;
  }

  public void setFont(FontConfig font) {
    this.font = font;
  }

  /**
   * Sets an optional result id to be used by {@link #writeOutput(Writer)}
   *
   * @param id result id to be used in output
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Set the mappings for a visualizers. Mappings are visualizer specific properties, that can be
   * configured in the resolver table of the database
   *
   * @param mappings The new mappings.
   */
  public void setMappings(Map<String, String> mappings) {
    this.mappings = mappings;
  }

  /**
   * This map is used for calculating the colors of a matching node.
   *
   * All Nodes, which are not included, should not be colorized. For getting HEX-values according to
   * html or css standards, it is possible to use {@link MatchedNodeColors}.
   *
   */
  public void setMarkedAndCovered(Map<SNode, Long> markedAndCovered) {
    this.markedAndCovered = markedAndCovered;
  }

  /**
   * Sets the namespace to be processed by {@link #writeOutput(Writer)}.
   *
   * @param namespace Namespace to be processed
   */
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   * Sets the raw text. This should only be done, if {@link VisualizerPlugin#isUsingText()} return
   * true.
   *
   * @param rawText the original text from the text.tab file in relAnnis. Therefore could be an
   *        empty string.
   */
  public void setRawText(RawTextWrapper rawText) {
    this.rawText = rawText;
  }

  public void setResourcePathTemplate(String resourcePathTemplate) {
    this.resourcePathTemplate = resourcePathTemplate;
  }

  /**
   * Alias for
   * {@link VisualizerInput#setDocument(de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument)
   */
  public void setResult(SDocument document) {
    setDocument(document);
  }

  public void setSegmentationName(String segmentationName) {
    this.segmentationName = segmentationName;
  }

  public void setUI(CommonUI ui) {
    this.ui = ui;
  }

  /**
   * Set all cachedToken annotations which should be displayed by the visualizer and correspondands
   * to the annos choosen by the user in the annis gui.
   */
  public void setVisibleTokenAnnos(Set<String> tokenAnnos) {
    this.tokenAnnos = tokenAnnos;
  }

}
