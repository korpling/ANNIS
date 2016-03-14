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

import annis.model.QueryAnnotation;
import annis.ql.parser.QueryData;
import annis.sqlgen.SubQueryCorpusSelectionStrategy;
import java.util.List;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;

/**
 *
 * @author thomas
 */
public class MetaDataFilter extends AbstractDao
{

  private SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy;

  /**
   * Will query the database which documents are matching according to the
   * given metadata
   * @param queryData QueryData from which the meta data will be extracted
   * @return The list of documents matching the meta data or null if no
   *        constraints need to be applied (all documents are matching)
   */
  public List<Long> getDocumentsForMetadata(QueryData queryData)
  {
    List<Long> corpusList = queryData.getCorpusList();

    if (!corpusList.isEmpty())
    {     
      List<QueryAnnotation> metaData = queryData.getMetaData();
      if (!metaData.isEmpty())
      {
        String documentsWithMetaDataSql = subQueryCorpusSelectionStrategy.buildSubQuery(corpusList, metaData);
        List<Long> documents = getJdbcTemplate()
          .query(documentsWithMetaDataSql, ParameterizedSingleColumnRowMapper.newInstance(Long.class));

        return documents;
      }

    }

    return null;
  }

  public SubQueryCorpusSelectionStrategy getSubQueryCorpusSelectionStrategy()
  {
    return subQueryCorpusSelectionStrategy;
  }

  public void setSubQueryCorpusSelectionStrategy(SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy)
  {
    this.subQueryCorpusSelectionStrategy = subQueryCorpusSelectionStrategy;
  }

  
}
