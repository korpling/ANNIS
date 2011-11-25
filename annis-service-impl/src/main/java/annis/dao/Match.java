/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Properties;


@SuppressWarnings("serial")
public class Match extends ArrayList<Long>
{
  
  private long toplevelCorpusId;
  private HashMap<Long, Properties> corpusConfiguration;
  
  public Match()
  {
    corpusConfiguration = new HashMap<Long, Properties>();
  }

  public Match(List<Long> nodes)
  {
    corpusConfiguration = new HashMap<Long, Properties>();
    addAll(nodes);
  }

  public long getToplevelCorpusId()
  {
    return toplevelCorpusId;
  }

  public void setToplevelCorpusId(long toplevelCorpusId)
  {
    this.toplevelCorpusId = toplevelCorpusId;
  }

  public HashMap<Long, Properties> getCorpusConfiguration()
  {
    return corpusConfiguration;
  }

  public void setCorpusConfiguration(HashMap<Long, Properties> corpusConfiguration)
  {
    this.corpusConfiguration = corpusConfiguration;
  }
  
}
