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

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@XmlRootElement
public class AqlParseError implements Serializable
{

  private ParsedEntityLocation location;

  private String message;

  public AqlParseError()
  {
    this.location = new ParsedEntityLocation();
    this.message = "";
  }

  public AqlParseError(ParsedEntityLocation location, String message)
  {
    this.location = location;
    this.message = message;
  }

  public AqlParseError(QueryNode n, String message)
  {
    if (n != null && n.getParseLocation() != null)
    {
      this.location = n.getParseLocation();
    }
    else
    {
      this.location = new ParsedEntityLocation();
    }
    this.message = message;
  }

  public AqlParseError(String message)
  {
    this.message = message;
    this.location = new ParsedEntityLocation();
  }

  @Override
  public String toString()
  {
    return "line " + location.toString() + " " + message;
  }

  public ParsedEntityLocation getLocation()
  {
    return location;
  }

  public String getMessage()
  {
    return message;
  }

  public void setLocation(ParsedEntityLocation location)
  {
    this.location = location;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }
  
}
