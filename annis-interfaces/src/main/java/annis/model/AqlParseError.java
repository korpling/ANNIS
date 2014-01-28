/*
 * Copyright 2014 SFB 632.
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

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@XmlRootElement
public class AqlParseError
{
  public int startLine;
  public int startColumn;
  public int endLine;
  public int endColumn;
  
  public String message;

  public AqlParseError()
  {
    this.startLine = 1;
    this.endLine = 1;
    this.startColumn = 0;
    this.endColumn = 0;
    this.message = "";
  }

  public AqlParseError(int startLine, int startColumn, int endLine,
    int endColumn, String message)
  {
    this.startLine = startLine;
    this.startColumn = startColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;
    this.message = message;
  }

  public AqlParseError(String message)
  {
    this.message = message;
    this.startLine = 1;
    this.endLine = 1;
    this.startColumn = 0;
    this.endColumn = 0;
  }

  @Override
  public String toString()
  {
    return "line " + startLine + ":" + startColumn + " " + message;
  }
  
  
  
}
