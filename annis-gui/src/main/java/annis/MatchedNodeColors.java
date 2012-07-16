package annis;

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


import java.awt.Color;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;

/**
 * Colors for matched nodes. The names and color values correspond to the
 * CSS standard.
 * @author thomas
 */
public enum MatchedNodeColors
{

  Red(new Color(255, 0, 0)),
  MediumVioletRed(new Color(199, 21, 133)),
  LimeGreen(new Color(50, 205, 50)),
  Peru(new Color(205, 133, 63)),
  IndianRed(new Color(205, 92, 92)),
  YellowGreen(new Color(173, 255, 47)),
  DarkRed(new Color(139, 0, 0)),
  OrangeRed(new Color(255, 69, 0));
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

  private String twoDigitHex(int i)
  {
    String result = Integer.toHexString(i).toLowerCase(Locale.ENGLISH);

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
    m = Math.min(m, 8);
    return "match_" + m;
  }
}
