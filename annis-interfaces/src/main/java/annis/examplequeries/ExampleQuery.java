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
package annis.examplequeries;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wraps example queries.
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
@XmlRootElement
public class ExampleQuery implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 7523398041841117842L;

  private String description;

  private String exampleQuery;

  private String corpusName;



  /**
   * @return the corpusName
   */
  public String getCorpusName() {
    return corpusName;
  }

  public String getDescription() {
    return description;
  }

  public String getExampleQuery() {
    return exampleQuery;
  }

  /**
   * @param corpusName the corpusName to set
   */
  public void setCorpusName(String corpusName) {
    this.corpusName = corpusName;
  }


  public void setDescription(String description) {
    this.description = description;
  }

  public void setExampleQuery(String exampleQuery) {
    this.exampleQuery = exampleQuery;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("example query: ").append(exampleQuery).append("\n");
    sb.append("example corpusName: ").append(corpusName).append("\n");
    sb.append("description: ").append(description).append("\n");
    return sb.toString();
  }

}
