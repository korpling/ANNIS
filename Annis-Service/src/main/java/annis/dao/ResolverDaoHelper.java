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

    String defaultFromWhere = 
      " FROM resolver_vis_map, corpus as corpus, corpus as c2 WHERE ";
    String corpusNameJoin = " resolver_vis_map.corpus = c2.name AND corpus.pre >= c2.pre AND corpus.post <= c2.post ";

    // If (corp=null && ns=null && element=null) => show this visualisation no matter what
    String firstUnion =
      select
      + defaultFromWhere
      + " resolver_vis_map.corpus is NULL AND resolver_vis_map.namespace is NULL" 
      + " AND resolver_vis_map.element is NULL"
      + " AND corpus.id="
      + "?"; // corpus

    // if (not_exists(my_corp+my_ns) && exists(corp=null && ms=my_ns && element=my_element.type)) => show this visulization for this hit;
    String secondUnion =
      select
      + defaultFromWhere
      + " resolver_vis_map.corpus is NULL AND resolver_vis_map.namespace = "
      + "?" // namespace
      + " AND resolver_vis_map.element = "
      + "?" // type
      + " AND resolver_vis_map.namespace NOT IN ("
          + "SELECT resolver_vis_map.namespace FROM resolver_vis_map, corpus as corpus, corpus as c2 WHERE " + corpusNameJoin
          + " AND resolver_vis_map.namespace = "
          +  "?" //namespace
          + " AND corpus.id = "
          + "?" // corpus
      + ")"
      + " AND corpus.id="
      + "?"; // corpus

    // if (corp=my_corp && ns=my_ns && element=my_element.type) => show this visulization for this hit;
    String thirdUnion =
      select
      + defaultFromWhere
      + corpusNameJoin
      + "AND resolver_vis_map.namespace = "
      + "?" //namespace
      + " AND resolver_vis_map.element = "
      + "?" //type
      + " AND corpus.id = "
      + "?"; //corpus

    // if (corp=my_corp && ns=null) => always show this visualization for this corpus;
    String fourthUnion =
      select
      +  defaultFromWhere
      + corpusNameJoin
      + " AND resolver_vis_map.namespace IS NULL "
      + " AND corpus.id = "
      + "?"; // corpus


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
         stmt.setLong((offset*10) + 1, resolverRequest[offset].getCorpusId());
         stmt.setString((offset*10) + 2, resolverRequest[offset].getNamespace());
         stmt.setString((offset*10) + 3, resolverRequest[offset].getType().name());
         stmt.setString((offset*10) + 4, resolverRequest[offset].getNamespace());
         stmt.setLong((offset*10) + 5, resolverRequest[offset].getCorpusId());
         stmt.setLong((offset*10) + 6, resolverRequest[offset].getCorpusId());
         stmt.setString((offset*10) + 7, resolverRequest[offset].getNamespace());
         stmt.setString((offset*10) + 8, resolverRequest[offset].getType().name());
         stmt.setLong((offset*10) + 9, resolverRequest[offset].getCorpusId());
         stmt.setLong((offset*10) + 10, resolverRequest[offset].getCorpusId());
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
