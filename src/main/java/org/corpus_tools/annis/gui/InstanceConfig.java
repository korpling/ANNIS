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
package org.corpus_tools.annis.gui;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Hold information about the configuration of a specific (sub-) instance of ANNIS.
 * 
 * Each physical installation (speak deployment) of ANNIS is able to have several instances which
 * behave differently. This is meant to provide a more specialized presentation for different
 * projects while still using only one ANNIS installation.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@XmlRootElement
public class InstanceConfig implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -1662935971268384730L;
  private String instanceName;
  private String instanceDisplayName;
  private String defaultQueryBuilder;
  private List<CorpusSet> corpusSets;
  private String defaultCorpusSet;
  private FontConfig font;
  private FontConfig frequencyFont;
  private String keyboardLayout;
  private boolean loginOnStart;
  private String helpUrl;

  public InstanceConfig() {
    instanceName = "";
    instanceDisplayName = "";
    defaultQueryBuilder = "";
    corpusSets = new LinkedList<>();
    defaultCorpusSet = "";
    loginOnStart = false;
  }

  /**
   * Get the corpus sets that are pre-defined by the instance.
   * 
   * @return
   */
  @XmlElement(name = "corpus-sets")
  public List<CorpusSet> getCorpusSets() {
    return corpusSets;
  }

  /**
   * Get the name of the corpus set that should be activated by default.
   * 
   * @return
   */
  @XmlElement(name = "default-corpusset")
  public String getDefaultCorpusSet() {
    return defaultCorpusSet;
  }

  /**
   * Get the short name of the query builder that should be selected by default.
   * 
   * @return
   */
  @XmlElement(name = "default-querybuilder")
  public String getDefaultQueryBuilder() {
    return defaultQueryBuilder;
  }

  /**
   * Get the special font used by this instance.
   * 
   * @return
   */
  @XmlElement(name = "font")
  public FontConfig getFont() {
    return font;
  }

  /**
   * Get a special font config that should be used in the frequency chart. Might return
   * {@code null}, in this case you should use the default font from {@link #getFont() }.
   * 
   * @return
   */
  @XmlElement(name = "frequency-font")
  public FontConfig getFrequencyFont() {
    return frequencyFont;
  }

  /**
   * The URL to use for the internal help page.
   * 
   * Must be located under the same domain as the ANNIS interface.
   * 
   * @return
   */
  @XmlElement(name = "help-url")
  public String getHelpUrl() {
    return helpUrl;
  }

  /**
   * Get the external display name (used e.g. in the user interface) of this instance.
   * 
   * @return
   */
  @XmlElement(name = "display-name")
  public String getInstanceDisplayName() {
    return instanceDisplayName;
  }

  /**
   * Get the internal short name of this instance.
   * 
   * @return
   */
  @XmlTransient
  public String getInstanceName() {
    return instanceName;
  }

  /**
   * Default keyboard layout used for the virtual keyboard.
   * 
   * Do not set {@code null} to disable virtual keyboards.
   * 
   * @return
   */
  @XmlElement(name = "keyboard-layout")
  public String getKeyboardLayout() {
    return keyboardLayout;
  }

  /**
   * If true the login window is shown at each startup automatically.
   * 
   * @return
   */
  @XmlElement(name = "login-on-start")
  public boolean isLoginOnStart() {
    return loginOnStart;
  }

  /**
   * @param corpusSets
   * @see #getCorpusSets()
   */
  public void setCorpusSets(List<CorpusSet> corpusSets) {
    this.corpusSets = corpusSets;
  }

  /**
   * @param defaultCorpusSet
   * @see #getDefaultCorpusSet()
   */
  public void setDefaultCorpusSet(String defaultCorpusSet) {
    this.defaultCorpusSet = defaultCorpusSet;
  }

  /**
   * @param defaultQueryBuilder
   * @see #getDefaultQueryBuilder()
   */
  public void setDefaultQueryBuilder(String defaultQueryBuilder) {
    this.defaultQueryBuilder = defaultQueryBuilder;
  }

  /**
   * @see #getFont()
   * @param font
   */
  public void setFont(FontConfig font) {
    this.font = font;
  }

  /**
   * @see #getFrequencyFont()
   * @param frequencyFont
   */
  public void setFrequencyFont(FontConfig frequencyFont) {
    this.frequencyFont = frequencyFont;
  }

  public void setHelpUrl(String helpUrl) {
    this.helpUrl = helpUrl;
  }

  /**
   * @param instanceDisplayName
   * @see #getInstanceDisplayName()
   */
  public void setInstanceDisplayName(String instanceDisplayName) {
    this.instanceDisplayName = instanceDisplayName;
  }

  /**
   * @param instanceName
   * @see #getInstanceName()
   */
  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  /**
   * @see #getKeyboardLayout()
   * @param keyboardLayout
   */
  public void setKeyboardLayout(String keyboardLayout) {
    this.keyboardLayout = keyboardLayout;
  }

  /**
   * @see #isLoginOnStart()
   * @param loginOnStart
   */
  public void setLoginOnStart(boolean loginOnStart) {
    this.loginOnStart = loginOnStart;
  }

}
