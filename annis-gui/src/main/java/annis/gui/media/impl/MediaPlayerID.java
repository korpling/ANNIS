/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.media.impl;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class MediaPlayerID implements Comparable<MediaPlayerID>
{
  private String sessionID;
  private String resultID;

  
  public MediaPlayerID(String sessionID, String resultID)
  {
    this.sessionID = sessionID;
    this.resultID = resultID;
  }

  public String getSessionID()
  {
    return sessionID;
  }

  public void setSessionID(String sessionID)
  {
    this.sessionID = sessionID;
  }

  public String getResultID()
  {
    return resultID;
  }

  public void setResultID(String resultID)
  {
    this.resultID = resultID;
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 97 * hash + (this.sessionID != null ? this.sessionID.hashCode() : 0);
    hash = 97 * hash + (this.resultID != null ? this.resultID.hashCode() : 0);
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
    final MediaPlayerID other = (MediaPlayerID) obj;
    if ((this.sessionID == null) ? (other.sessionID != null) : !this.sessionID.equals(other.sessionID))
    {
      return false;
    }
    if ((this.resultID == null) ? (other.resultID != null) : !this.resultID.equals(other.resultID))
    {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(MediaPlayerID o)
  {
    int result = sessionID.compareTo(o.sessionID);
    if(result == 0)
    {
      result = resultID.compareTo(o.resultID);
    }
    
    return result;
  }

}
