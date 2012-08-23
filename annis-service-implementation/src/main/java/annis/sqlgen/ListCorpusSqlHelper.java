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
package annis.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import annis.service.objects.AnnisCorpus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListCorpusSqlHelper implements ParameterizedRowMapper<AnnisCorpus>
{
  
  private static final Logger log = LoggerFactory.getLogger(ListCorpusSqlHelper.class);

  public String createSqlQuery()
  {
    return "SELECT * FROM corpus_stats";
  }

  public AnnisCorpus mapRow(ResultSet rs, int rowNum) throws SQLException
  {
    AnnisCorpus corpus = new AnnisCorpus();
    corpus.setId(rs.getLong("id"));
    corpus.setName(rs.getString("name"));
    corpus.setTextCount(rs.getInt("text"));
    corpus.setTokenCount(rs.getInt("tokens"));
    try
    {
      corpus.setSourcePath(rs.getString("source_path"));
    }
    catch (SQLException ex)
    {
      log.debug(null, ex);    
    }
    return corpus;
  }
}