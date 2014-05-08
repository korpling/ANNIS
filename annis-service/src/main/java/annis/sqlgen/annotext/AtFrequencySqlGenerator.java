/*
 * Copyright 2014 SFB 632.
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

package annis.sqlgen.annotext;

import annis.exceptions.AnnisQLSemanticsException;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.service.objects.FrequencyTableEntry;
import annis.service.objects.FrequencyTableEntryType;
import static annis.sqlgen.AbstractSqlGenerator.TABSTOP;
import annis.sqlgen.FrequencySqlGenerator;
import annis.sqlgen.TableAccessStrategy;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import annis.sqlgen.extensions.FrequencyTableQueryData;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AtFrequencySqlGenerator extends FrequencySqlGenerator
{
  @Override
  public String fromClause(QueryData queryData, List<QueryNode> alternative, String indent)
  {
    TableAccessStrategy tas = tables(null);
    
    FrequencyTableQueryData ext;
    List<FrequencyTableQueryData> freqQueryData = queryData.getExtensions(FrequencyTableQueryData.class);
    Validate.notNull(freqQueryData);
    Validate.notEmpty(freqQueryData);    
    ext = freqQueryData.get(0);
    
    StringBuilder sb = new StringBuilder();

    sb.append(indent).append("(\n");
    sb.append(indent);

    sb.append(getInnerQuerySqlGenerator().toSql(queryData, indent + TABSTOP));
    sb.append(indent).append(") AS solutions,\n");

    int i = 1;
    Iterator<FrequencyTableEntry> itEntry = ext.iterator();
    while(itEntry.hasNext())
    {
      FrequencyTableEntry e = itEntry.next();
      
      sb.append(indent).append(TABSTOP);
      
      sb.append(TableAccessStrategy.partitionTableName(
        tas.getTableAliases(), tas.getTablePartitioned(), 
        NODE_TABLE, queryData.getCorpusList()));
      sb.append(" AS v").append(i);
      
      if(itEntry.hasNext())
      {
        sb.append(",");
      }
      
      sb.append("\n");

      i++;
    }
    return sb.toString();
  }
  
  @Override
  public String selectClause(QueryData queryData, List<QueryNode> alternative, String indent)
  {
    TableAccessStrategy tas = tables(null);
    
    FrequencyTableQueryData ext;
    List<FrequencyTableQueryData> freqQueryData = queryData.getExtensions(FrequencyTableQueryData.class);
    Validate.notNull(freqQueryData);
    Validate.notEmpty(freqQueryData);    
    ext = freqQueryData.get(0);
    
    StringBuilder sb = new StringBuilder();
    int i=1;
    for(FrequencyTableEntry e : ext)
    {
      if(e.getType() == FrequencyTableEntryType.annotation)
      {
        sb
          .append("(splitanno(")
          .append("v").append(i).append(".")
          .append(tas.columnName(NODE_ANNOTATION_TABLE, "qannotext"))
          .append("))[3]");
      }
      else
      {
        sb.append("v").append(i).append(".").append(
          tas.columnName(NODE_TABLE,
          "span"));
      }
      sb.append(" AS value").append(i).append(", ");
      i++;
    }
    
    sb.append("count(*) AS \"count\"");
    
    return sb.toString();
  }
  
  @Override
  public Set<String> whereConditions(QueryData queryData, List<QueryNode> alternative, String indent)
  {
    Set<String> conditions = new LinkedHashSet<String>();
    
    FrequencyTableQueryData ext;
    List<FrequencyTableQueryData> freqQueryData = queryData.getExtensions(FrequencyTableQueryData.class);
    Validate.notNull(freqQueryData);
    Validate.notEmpty(freqQueryData);    
    ext = freqQueryData.get(0);
    
    ImmutableMap<String, QueryNode> idxNodeVariables = Maps.uniqueIndex(
      alternative.iterator(), new Function<QueryNode, String>()
    {
      @Override
      public String apply(QueryNode input)
      {
        return input.getVariable();
      }
    });
    
    int i=1;
    for(FrequencyTableEntry e : ext)
    {      
      // general partition restriction
      conditions.add("v" + i + ".toplevel_corpus IN (" + StringUtils.join(
        queryData.getCorpusList(), ",") + ")" );
      // specificly join on top level corpus
      conditions.add("v" + i + ".toplevel_corpus = solutions.toplevel_corpus" );
      // join on node ID
      QueryNode referencedNode = idxNodeVariables.get(e.getReferencedNode());
      if(referencedNode == null)
      {
        throw new AnnisQLSemanticsException("No such node \"" 
          + e.getReferencedNode() + "\". "
          + "Your query contains " + alternative.size() + " node(s), make sure no node definition numbers are greater than this number");
      }
      conditions.add("v" + i + ".id = solutions.id" + referencedNode.getId());
      
      if(e.getType() == FrequencyTableEntryType.span)
      {
        conditions.add("v" + i + ".n_sample IS TRUE" );
      }
      else
      {
        // filter by selected key
        conditions.add("(splitanno(v" + i + ".node_qannotext))[2] = '" + e.getKey().replaceAll("'",
          "''") + "'");
        conditions.add("v" + i + ".n_na_sample IS TRUE" );
        
      }
      i++;
    }
    
    return conditions;
  }
}
