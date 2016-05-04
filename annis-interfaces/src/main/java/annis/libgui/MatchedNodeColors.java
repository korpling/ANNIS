/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.libgui;

import java.awt.Color;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

/**
 * Colors for matched nodes. The names and color values correspond to the
 * CSS standard.
 * <br/><br/>
 * These colors must always be synchronized with the "match-color"
 * definitions in the annis.scss style sheet.
 * @author Thomas Krause <krauseto@hu-berlin.>
 */
public enum MatchedNodeColors
{

  Red(new Color(255, 0, 0)),
  MediumVioletRed(new Color(199, 21, 133)),
  LimeGreen(new Color(50, 205, 50)),
  Peru(new Color(205, 133, 63)),
  SkyBlue(new Color(135, 206, 235)),
  IndianRed(new Color(205, 92, 92)),
  YellowGreen(new Color(173, 255, 47)),
  DarkRed(new Color(139, 0, 0)),
  OrangeRed(new Color(255, 69, 0)),
  Gold(new Color(255, 215, 0)),
  Tan(new Color(210, 180, 140)),
  Navy(new Color(0, 0, 128)),
  MediumSpringGreen(new Color(0, 250, 154)),
  DodgerBlue(new Color(30, 144, 255)),
  OliveDrab(new Color(107, 142,  35)),
  LightPink(new Color(255, 182, 193))
  ;
  
  private final Color color;

  private MatchedNodeColors(Color color)
  {
    this.color = color;
  }

  public Color getColor()
  {
    return color;
  }

  /**
   * Returns the hexadecimal RGB representation beginning with a hash-sign.
   * @return the color in the format "#rrggbb"
   */
  public String getHTMLColor()
  {
    StringBuilder result = new StringBuilder("#");
    result.append(twoDigitHex(color.getRed()));
    result.append(twoDigitHex(color.getGreen()));
    result.append(twoDigitHex(color.getBlue()));
    return result.toString();
  }
  
  /**
   * Returns the hexadecimal RGB representation beginning with a hash-sign.
   * @param match
   * @return the color in the format "#rrggbb"
   */
  public static String getHTMLColorByMatch(Long match)
  {
    if(match == null)
    {
      return null;
    }
    int m = ((int) ((long) match))-1;
    m = Math.min(m, MatchedNodeColors.values().length);
    MatchedNodeColors c = MatchedNodeColors.values()[m];
    return c.getHTMLColor();
  }

  private String twoDigitHex(int i)
  {
    String result = Integer.toHexString(i).toLowerCase(new Locale("en"));

    if(result.length() > 2)
    {
      result = result.substring(0, 2);
    }
    else if(result.length() < 2)
    {
      result = StringUtils.leftPad(result, 2, '0');
    }
    return result;
  }

  public static String colorClassByMatch(Long match)
  {
    if(match == null)
    {
      return null;
    }
    long m = match;
    m = Math.min(m, MatchedNodeColors.values().length);
    return "match_" + m;
  }
}
