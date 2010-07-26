/*
 *  Copyright 2010 thomas.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package annis.dao;

import annis.ql.parser.QueryAnalysis;
import annis.sqlgen.SqlGenerator;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;
import java.util.List;

/**
 *
 * @author thomas
 */
public class MatchViewGenerator
{

  private DddQueryParser dddQueryParser;
  private SqlGenerator sqlGenerator;
  private QueryAnalysis queryAnalysis;
  private String nodeTableViewName;
  private String matchedNodesViewName;

  public String createSqlQuery(List<Long> corpusList, String dddQuery, long offset, long limit, int left, int right)
  {
    // parse query
    Start statement = dddQueryParser.parse(dddQuery);

    // get number of nodes in match
    int nodeCount = queryAnalysis.analyzeQuery(statement, corpusList).getMaxWidth();

    // key for annotation graph matches
    StringBuilder selectClause = new StringBuilder();
    selectClause.append("id1");
    for (int i = 2; i <= nodeCount; ++i)
    {
      selectClause.append(",");
      selectClause.append("id");
      selectClause.append(i);
    }

    // sql for matches
    StringBuilder matchSb = new StringBuilder();
    matchSb.append("CREATE TEMPORARY VIEW \"");
    matchSb.append(matchedNodesViewName);
    matchSb.append("\" AS\n");
    matchSb.append("\t SELECT\n");
    matchSb.append(selectClause);
    matchSb.append("\n\tFROM\n(\n");
    matchSb.append(sqlGenerator.toSql(statement, corpusList));
    matchSb.append("\n) as matched_ids");

    return matchSb.toString();
  }

  public String getNodeTableViewName()
  {
    return nodeTableViewName;
  }

  public void setNodeTableViewName(String nodeTableViewName)
  {
    this.nodeTableViewName = nodeTableViewName;
  }

  public SqlGenerator getSqlGenerator()
  {
    return sqlGenerator;
  }

  public void setSqlGenerator(SqlGenerator sqlGenerator)
  {
    this.sqlGenerator = sqlGenerator;
  }

  public DddQueryParser getDddQueryParser()
  {
    return dddQueryParser;
  }

  public void setDddQueryParser(DddQueryParser dddQueryParser)
  {
    this.dddQueryParser = dddQueryParser;
  }

  public QueryAnalysis getQueryAnalysis()
  {
    return queryAnalysis;
  }

  public void setQueryAnalysis(QueryAnalysis queryAnalysis)
  {
    this.queryAnalysis = queryAnalysis;
  }

  public String getMatchedNodesViewName()
  {
    return matchedNodesViewName;
  }

  public void setMatchedNodesViewName(String matchedNodesViewName)
  {
    this.matchedNodesViewName = matchedNodesViewName;
  }
}
