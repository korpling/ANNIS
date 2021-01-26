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
package org.corpus_tools.annis.gui.components.medialement;

import com.vaadin.shared.ui.JavaScriptComponentState;

/**
 * A state of the {@link MediaElementPlayer}
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class MediaState extends JavaScriptComponentState {
  /**
   * 
   */
  private static final long serialVersionUID = 7341733202953802626L;
  private MediaElement elementType = MediaElement.video;
  private String resourceURL;
  private String mimeType;

  public MediaElement getElementType() {
    return elementType;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getResourceURL() {
    return resourceURL;
  }

  public void setElementType(MediaElement elementType) {
    this.elementType = elementType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public void setResourceURL(String resourceURL) {
    this.resourceURL = resourceURL;
  }

}
