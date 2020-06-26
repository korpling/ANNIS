/*
 * Copyright 2012 SFB 632.
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
package annis.service.objects;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the result of a COUNT MATCHES AND DOCUMENTS query in ANNIS.
 * 
 * It provides the number of query matches and the number of distinct documents where these matches
 * occurred.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@XmlRootElement
public class MatchAndDocumentCount implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 8675390944849954490L;
  private long matchCount;
  private long documentCount;

  public MatchAndDocumentCount() {

  }

  public MatchAndDocumentCount(long matchCount, long documentCount) {
    super();
    this.matchCount = matchCount;
    this.documentCount = documentCount;
  }

  public long getDocumentCount() {
    return documentCount;
  }

  public long getMatchCount() {
    return matchCount;
  }

  public void setDocumentCount(long documentsMatched) {
    this.documentCount = documentsMatched;
  }

  public void setMatchCount(long tupelMatched) {
    this.matchCount = tupelMatched;
  }

  @Override
  public String toString() {
    return "" + matchCount + " matches in " + documentCount + " documents";
  }



}
