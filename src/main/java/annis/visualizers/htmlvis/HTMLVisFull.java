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
package annis.visualizers.htmlvis;

import org.springframework.stereotype.Component;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@Component
public class HTMLVisFull extends HTMLVis {

  /**
   * 
   */
  private static final long serialVersionUID = 2801586778957236360L;

  @Override
  public String getShortName() {
    return "htmldoc";
  }

  @Override
  public boolean isUsingText() {
    return true;
  }



}
