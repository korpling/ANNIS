/*
 *  Copyright 2010 Collaborative Research Centre SFB 632.
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

import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @author thomas
 */
public class ResolverDaoHelper implements ResultSetExtractor, PreparedStatementCreator
{

  private int requestCount;

  public ResolverDaoHelper(int requestCount)
  {
    this.requestCount = requestCount;
  }

  @Override
  public PreparedStatement createPreparedStatement(Connection cnctn) throws SQLException
  {
    String select = "SELECT corpus.id, corpus.name, "
      + "corpus.version, "
      + "resolver_vis_map.namespace, "
      + "resolver_vis_map.element, "
      + "resolver_vis_map.vis_type, "
      + "resolver_vis_map.display_name, "
      + "resolver_vis_map.order, "
      + "resolver_vis_map.mappings";

    String firstUnion =
      select
      + " FROM resolver_vis_map, corpus WHERE corpus.id = "
      + "?"  // corpusId
      + " AND corpus is NULL AND namespace is NULL AND element is NULL";

    String secondUnion =
      select
      + " FROM resolver_vis_map  LEFT OUTER JOIN corpus ON (resolver_vis_map.corpus = corpus.name) WHERE corpus is NULL AND namespace = "
      + "?" // namespace
      + " AND namespace NOT IN (SELECT namespace FROM resolver_vis_map WHERE namespace = "
      +  "?" //namespace
      + " AND corpus IS NOT NULL)";

    String thirdUnion =
      select
      + " FROM resolver_vis_map LEFT OUTER JOIN corpus ON (resolver_vis_map.corpus = corpus.name) WHERE corpus = corpus.name AND namespace = "
      + "?" //namespace
      + " AND element = "
      + "?"; //type

    String fourthUnion =
      select
      + " FROM resolver_vis_map LEFT OUTER JOIN corpus ON (resolver_vis_map.corpus = corpus.name) WHERE namespace IS NULL AND corpus IN (SELECT name from corpus WHERE id = "
      + "?" //corpusId
      + ")";

    StringBuffer result = new StringBuffer();
    for(int i=0; i < requestCount; i++)
    {
      if(i > 0)
      {
        result.append(" \nUNION \n");
      }
      result.append(firstUnion);
      result.append(" \nUNION \n");
      result.append(secondUnion);
      result.append(" \nUNION \n");
      result.append(thirdUnion);
      result.append(" \nUNION \n");
      result.append(fourthUnion);
    }
    result.append(" \nORDER BY \"order\" ;");

    return cnctn.prepareStatement(result.toString());
  }

  public void fillPreparedStatement(SingleResolverRequest[] resolverRequest, PreparedStatement stmt) throws SQLException
  {
    for(int offset=0; offset < requestCount; offset++)
    {
      if(offset < resolverRequest.length)
      {
         stmt.setLong((offset*6) + 1, resolverRequest[offset].getCorpusId());
         stmt.setString((offset*6) + 2, resolverRequest[offset].getNamespace());
         stmt.setString((offset*6) + 3, resolverRequest[offset].getNamespace());
         stmt.setString((offset*6) + 4, resolverRequest[offset].getNamespace());
         stmt.setString((offset*6) + 5, resolverRequest[offset].getType().name());
         stmt.setLong((offset*6) + 6, resolverRequest[offset].getCorpusId());
      }
    }
  }

  @Override
  public List<ResolverEntry> extractData(ResultSet rs) throws SQLException, DataAccessException
  {
    List<ResolverEntry> result = new LinkedList<ResolverEntry>();


    while (rs.next())
    {
      // TODO: fill these properties with resolver_vis_map.mappings
      Properties props = new Properties();

      String element = rs.getString("element");

      ResolverEntry e = new ResolverEntry(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("version"),
        rs.getString("namespace"),
        element == null ? null : ResolverEntry.ElementType.valueOf(element),
        rs.getString("vis_type"),
        rs.getString("display_name"),
        props,
        rs.getInt("order"));
      result.add(e);

    }

    return result;
  }
}
