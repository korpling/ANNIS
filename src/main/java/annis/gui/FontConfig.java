/*
 * Copyright 2013 SFB 632.
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
package annis.gui;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A configuration for an external web-font.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@XmlRootElement
public class FontConfig implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 6417420124768278215L;
  private String name;
  private String url;
  private String size;

  public FontConfig() {
    name = "";
    url = "";
  }

  @XmlElement(name = "name")
  public String getName() {
    return name;
  }

  @XmlElement(name = "size")
  public String getSize() {
    return size;
  }

  @XmlElement(name = "url")
  public String getUrl() {
    return url;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
