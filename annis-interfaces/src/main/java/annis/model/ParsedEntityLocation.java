/*
 * Copyright 2015 SFB 632.
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
package annis.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The location of some entity in the query when it was parsed.
 * Can be used to define e.g. error locations.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@XmlRootElement
public class ParsedEntityLocation implements Serializable
{
  private int startLine;
  private int startColumn;
  private int endLine;
  private  int endColumn;
  
  public ParsedEntityLocation()
  {
    this.startLine = 1;
    this.endLine = 1;
    this.startColumn = 0;
    this.endColumn = 0;
  }

  public ParsedEntityLocation(int startLine, int startColumn, int endLine, int endColumn)
  {
    this.startLine = startLine;
    this.startColumn = startColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;
  }

  public int getStartLine()
  {
    return startLine;
  }

  public int getStartColumn()
  {
    return startColumn;
  }

  public int getEndLine()
  {
    return endLine;
  }

  public int getEndColumn()
  {
    return endColumn;
  }

  public void setStartLine(int startLine)
  {
    this.startLine = startLine;
  }

  public void setStartColumn(int startColumn)
  {
    this.startColumn = startColumn;
  }

  public void setEndLine(int endLine)
  {
    this.endLine = endLine;
  }

  public void setEndColumn(int endColumn)
  {
    this.endColumn = endColumn;
  }
  
  @Override
  public String toString()
  {
    if (startLine == endLine)
    {
      if (startColumn == endColumn)
      {
        return startLine + ":" + startColumn;
      }
      else
      {
        return startLine + ":" + startColumn + "-" + endColumn;
      }
    }
    else
    {
      if (startColumn == endColumn)
      {
        return startLine + "-" + endLine + ":" + startColumn;
      }
      else
      {
        return startLine + "-" + endLine + ":" + startColumn + "-" + endColumn;
      }
    }
  }
  
  
  
  
}
