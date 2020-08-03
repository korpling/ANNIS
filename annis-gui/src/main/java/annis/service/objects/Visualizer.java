/*
 * Copyright 2014 SFB 632.
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

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.corpus_tools.annis.api.model.VisualizerRule.VisibilityEnum;

/**
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
@XmlRootElement
public class Visualizer implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 6429465444658974246L;

  private String type;

  private String displayName;

  private String mappings;

  private String namespace;

  public Visualizer() {}

  public Visualizer(VisualizerRule rule) {

    this.setDisplayName(rule.getDisplayName());
    this.setMappings(fromMappings(rule.getMappings()));
    this.setNamespace(rule.getLayer());
    this.setType(rule.getVisType());
  }


  /**
   * @return the displayName
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @return the mappings
   */
  public String getMappings() {
    return mappings;
  }

  /**
   * @return the namespace
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param displayName the displayName to set
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * @param mappings the mappings to set
   */
  public void setMappings(String mappings) {
    this.mappings = mappings;
  }

  /**
   * @param namespace the namespace to set
   */
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  public VisualizerRule toVisualizerRule() {
    VisualizerRule newVis = new VisualizerRule();
    newVis.setDisplayName(this.getDisplayName());
    newVis.setLayer(this.getNamespace());
    newVis.setMappings(parseMappings(this.getMappings()));
    newVis.setVisibility(VisibilityEnum.HIDDEN);
    newVis.setVisType(this.getType());
    return newVis;
  }


  private static Map<String, String> parseMappings(String mappings) {
    Map<String, String> result = new LinkedHashMap<>();


    if (mappings != null) {
      // split the entrys
      String[] entries = mappings.split(";");
      for (String e : entries) {
        // split key-value
        String[] keyvalue = e.split(":", 2);
        if (keyvalue.length == 2) {
          result.put(keyvalue[0].trim(), keyvalue[1].trim());
        }
      }
    }

    return result;
  }

  private static String fromMappings(Map<String, String> mappings) {
    if (mappings == null) {
      return null;
    } else {
      StringBuilder sb = new StringBuilder();
      Iterator<Map.Entry<String, String>> it = mappings.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, String> e = it.next();
        sb.append(e.getKey());
        sb.append(":");
        sb.append(e.getValue());
        if (it.hasNext()) {
          sb.append(";");
        }
      }
      return sb.toString();
    }
  }

}
