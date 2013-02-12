/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.model;

import java.util.HashSet;
import java.util.Set;

/**
 * A POJO representing a query
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class Query
{
  private String query;
  private Set<String> corpora;

  public Query()
  {
    corpora = new HashSet<String>();
  }

  public Query(String query,
    Set<String> corpora)
  {
    this.query = query;
    this.corpora = corpora;
  }

  
  public String getQuery()
  {
    return query;
  }

  public void setQuery(String query)
  {
    this.query = query;
  }

  public Set<String> getCorpora()
  {
    return corpora;
  }

  public void setCorpora(Set<String> corpora)
  {
    this.corpora = corpora;
  }
  
  
}
