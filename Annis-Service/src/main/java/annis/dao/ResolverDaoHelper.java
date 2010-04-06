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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @author thomas
 */
public class ResolverDaoHelper implements ResultSetExtractor
{

  public String createSqlQuery(long corpusId, String namespace, ResolverEntry.ElementType type)
  {
    //StringBuilder result = new StringBuilder();

    String select = "SELECT corpus.id, corpus.name, corpus.version, resolver_vis_map.namespace, resolver_vis_map.element, resolver_vis_map.vis_type, resolver_vis_map.display_name,resolver_vis_map.order, resolver_vis_map.mappings";
    String result = select +" FROM resolver_vis_map, corpus WHERE corpus.id = "+ corpusId +" AND corpus is NULL AND namespace is NULL AND element is NULL UNION "+select+" FROM resolver_vis_map  LEFT OUTER JOIN corpus ON (resolver_vis_map.corpus = corpus.name) WHERE corpus is NULL AND namespace = '"+namespace+"' AND namespace NOT IN (SELECT namespace FROM resolver_vis_map WHERE namespace = '"+namespace+"' AND corpus IS NOT NULL) UNION "+select+" FROM resolver_vis_map LEFT OUTER JOIN corpus ON (resolver_vis_map.corpus = corpus.name) WHERE corpus = corpus.name AND namespace = '"+namespace+"' AND element = '"+type+"' UNION "+select+" FROM resolver_vis_map LEFT OUTER JOIN corpus ON (resolver_vis_map.corpus = corpus.name) WHERE namespace IS NULL AND corpus IN (SELECT name from corpus WHERE id = '"+corpusId+"') ORDER BY \"order\";";

    // TODO: create the sql query for getting the resolver data
    
    //return result.toString();
       return result;
  }

  @Override
  public List<ResolverEntry> extractData(ResultSet rs) throws SQLException, DataAccessException
  {
    List<ResolverEntry> result = new LinkedList<ResolverEntry>();
    // TODO: implement extract resolver data from query

    while(rs.next())
    {
      //ResolverEntry e = new ResolverEntry(rs.getLong(1), null, null, null, ResolverEntry.ElementType.Node, null, null, null, order);
      //result.add(e);
    }

    return result;
  }

}
