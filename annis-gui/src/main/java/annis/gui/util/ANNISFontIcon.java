/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package annis.gui.util;

import com.vaadin.server.FontIcon;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public enum ANNISFontIcon implements FontIcon
{

  SPACE(0xe600),
  RIGHT_ARROW(0xe601),
  LEFT_ARROW(0xe602),
  LAST(0xe603),
  FIRST(0xe604),
  MENU_RIGHT(0xe605),
  MENU_LEFT(0xe606),
  MENU_AUTO(0xe607),
  LOGO(0xe608);
  
  private final int codepoint;
  
  ANNISFontIcon(int codepoint)
  {
    this.codepoint = codepoint;
  }

  @Override
  public String getFontFamily()
  {
    return "ANNISFontIcon";
  }

  @Override
  public int getCodepoint()
  {
    return codepoint;
  }

  @Override
    public String getHtml() {
        return "<span class=\"v-icon\" style=\"font-family: " + getFontFamily()
                + ";\">&#x" + Integer.toHexString(codepoint) + ";</span>";
    }

  @Override
  public String getMIMEType()
  {
    throw new UnsupportedOperationException("Font icons don't have a MIME type");
  }
}
