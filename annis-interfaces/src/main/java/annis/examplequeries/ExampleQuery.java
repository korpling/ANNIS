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
package annis.examplequeries;

import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps example queries.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@XmlRootElement
public class ExampleQuery
{

  private static final Logger log = LoggerFactory.getLogger(ExampleQuery.class);

  private String type;

  private String description;

  private String exampleQuery;

  private String used_operators;

  private String corpusName;

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getExampleQuery()
  {
    return exampleQuery;
  }

  public void setExampleQuery(String exampleQuery)
  {
    this.exampleQuery = exampleQuery;
  }

  public String getUsedOperators()
  {
    return used_operators;
  }

  public void setUsedOperators(String used_operators)
  {
    this.used_operators = used_operators;
  }

  /**
   * @return the corpusName
   */
  public String getCorpusName()
  {
    return corpusName;
  }

  /**
   * @param corpusName the corpusName to set
   */
  public void setCorpusName(String corpusName)
  {
    this.corpusName = corpusName;
  }
}
