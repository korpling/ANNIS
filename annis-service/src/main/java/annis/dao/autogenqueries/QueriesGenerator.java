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
package annis.dao.autogenqueries;

import annis.dao.AnnisDao;
import annis.examplequeries.ExampleQuery;
import annis.ql.parser.QueryData;
import annis.sqlgen.AnnotateQueryData;
import annis.sqlgen.LimitOffsetQueryData;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class QueriesGenerator
{

  AnnisDao annisDao;

  List<Long> corpusIds;

  String corpusName;

  private final Logger log = LoggerFactory.getLogger(QueriesGenerator.class);

  public QueriesGenerator(AnnisDao annisDao, long corpusId)
  {
    this.annisDao = annisDao;

    corpusIds = new ArrayList<Long>();
    corpusIds.add(corpusId);
    List<String> corpusNames = annisDao.mapCorpusIdsToNames(corpusIds);
    corpusName = corpusNames.get(0);
  }

  public ExampleQuery generateQuery(QueryBuilder queryBuilder)
  {
    log.info("generate auto query for {}", corpusName);
    String aql = queryBuilder.getAQL();
    QueryData queryData = annisDao.parseAQL(aql, this.corpusIds);
    queryData.addExtension(new LimitOffsetQueryData(5, 5));
    queryData.addExtension(new AnnotateQueryData(5, 5, null));
    SaltProject saltProject = annisDao.annotate(queryData);
    queryBuilder.analyzingQuery(saltProject);
    ExampleQuery exampleQuery = queryBuilder.getExampleQuery();
    exampleQuery.setCorpusName(corpusName);
    return exampleQuery;
  }

  public interface QueryBuilder
  {

    public String getAQL();

    public void analyzingQuery(SaltProject saltProject);

    public ExampleQuery getExampleQuery();
  }
}
