/*
 * Copyright 2013 SFB 632.
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
package annis.visualizers.htmlvis;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class VisualizationDefinition
{
  private String matchingElement;
  private String matchingValue;
  
  private String outputElement;
  
  private String style;

  public String getMatchingElement()
  {
    return matchingElement;
  }

  public void setMatchingElement(String matchingElement)
  {
    this.matchingElement = matchingElement;
  }

  public String getMatchingValue()
  {
    return matchingValue;
  }

  public void setMatchingValue(String matchingValue)
  {
    this.matchingValue = matchingValue;
  }

  public String getOutputElement()
  {
    return outputElement;
  }

  public void setOutputElement(String outputElement)
  {
    this.outputElement = outputElement;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash =
      37 * hash +
      (this.matchingElement != null ? this.matchingElement.hashCode() : 0);
    hash =
      37 * hash +
      (this.matchingValue != null ? this.matchingValue.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final VisualizationDefinition other = (VisualizationDefinition) obj;
    if ((this.matchingElement == null) ? (other.matchingElement != null)
      : !this.matchingElement.equals(other.matchingElement))
    {
      return false;
    }
    if ((this.matchingValue == null) ? (other.matchingValue != null)
      : !this.matchingValue.equals(other.matchingValue))
    {
      return false;
    }
    return true;
  }
  
  
}
