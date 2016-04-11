/*
 * Copyright 2012 SFB 632.
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
package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.DataAccessException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

import annis.exceptions.AnnisQLSemanticsException;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.service.objects.FrequencyTable;
import annis.service.objects.FrequencyTableEntry;
import annis.service.objects.FrequencyTableEntryType;
import annis.service.objects.FrequencyTableQuery;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FrequencySqlGenerator extends AbstractSqlGenerator
  implements WhereClauseSqlGenerator<QueryData>,
  SelectClauseSqlGenerator<QueryData>,
  GroupByClauseSqlGenerator<QueryData>, FromClauseSqlGenerator<QueryData>,
  SqlGeneratorAndExtractor<QueryData, FrequencyTable>
{

  private SolutionSqlGenerator solutionSqlGenerator;
  
  public static final Escaper escaper = Escapers.builder()
    .addEscape('\'', "''")
    .build();
  
  @Override
  public FrequencyTable extractData(ResultSet rs) throws SQLException, DataAccessException
  {
    FrequencyTable result = new FrequencyTable();

    ResultSetMetaData meta = rs.getMetaData();

    while (rs.next())
    {
      Validate.isTrue(meta.getColumnCount() > 1,
        "frequency table extractor needs at least 2 columns");

      Validate.isTrue(
        "count".equalsIgnoreCase(meta.getColumnName(meta.getColumnCount())),
        "last column name must be \"count\"");

      long count = rs.getLong("count");
      String[] tupel = new String[meta.getColumnCount() - 1];

      for (int i = 1; i <= tupel.length; i++)
      {
        String colVal = rs.getString(i);
        
        if(colVal == null)
        {
          tupel[i-1] = "";
        }
        else
        {
          String[] splitted = colVal.split(":", 3);
          if(splitted.length > 0)
          {
            colVal = splitted[splitted.length-1];
          }

          tupel[i - 1] = colVal;
        }
      } // end for each column (except last "count" column) 

      result.addEntry(new FrequencyTable.Entry(tupel, count));

    } // end for complete result

    return result;
  }

  @Override
  public String groupByAttributes(QueryData queryData,
    List<QueryNode> alternative)
  {
    FrequencyTableQuery ext;
    List<FrequencyTableQuery> freqQueryData = queryData.getExtensions(FrequencyTableQuery.class);
    Validate.notNull(freqQueryData);
    Validate.notEmpty(freqQueryData);
    ext = freqQueryData.get(0);

    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= ext.size(); i++)
    {
      sb.append("value").append(i);
      if (i < ext.size())
      {
        sb.append(", ");
      }
    }

    return sb.toString();
  }

  @Override
  public String fromClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    FrequencyTableQuery ext;
    List<FrequencyTableQuery> freqQueryData = queryData.getExtensions(FrequencyTableQuery.class);
    Validate.notNull(freqQueryData);
    Validate.notEmpty(freqQueryData);
    ext = freqQueryData.get(0);

    Multimap<FrequencyTableEntry, String> conditions = conditionsForEntries(ext, queryData, alternative);

    
    StringBuilder sb = new StringBuilder();

    sb.append(indent).append("(\n");
    sb.append(indent);

    sb.append(getSolutionSqlGenerator().toSql(queryData, indent + TABSTOP));
    sb.append(indent).append(") AS solutions\n");
    
    int i = 1;
    Iterator<FrequencyTableEntry> itEntry = ext.iterator();
    while (itEntry.hasNext())
    {
      FrequencyTableEntry e = itEntry.next();
      

      sb.append(indent).append(TABSTOP);
      sb.append("LEFT JOIN ");

      String tableSql;
      if(e.getType() == FrequencyTableEntryType.meta)
      {
        tableSql = "corpus_annotation";
      }
      else
      {
        tableSql = SelectedFactsFromClauseGenerator.selectedFactsSQL(
        queryData.getCorpusList(), indent);
      }
      
      sb.append(tableSql);
      sb.append(" AS v").append(i);
      
      sb.append(" ON (");
      sb.append(Joiner.on(" AND ").join(conditions.get(e)));
      sb.append(")\n");
      i++;
    }
    return sb.toString();
  }
  
  private Multimap<FrequencyTableEntry, String> conditionsForEntries(
    List<FrequencyTableEntry> frequencyEntries,
    QueryData queryData, List<QueryNode> alternative)
  {
    Multimap<FrequencyTableEntry, String> conditions = LinkedHashMultimap.create();
    int i = 1;

    ImmutableMap<String, QueryNode> idxNodeVariables = Maps.uniqueIndex(
      alternative.iterator(), new Function<QueryNode, String>()
      {
        @Override
        public String apply(QueryNode input)
        {
          return input.getVariable();
        }
      });

    for (FrequencyTableEntry e : frequencyEntries)
    {
      if (e.getType() == FrequencyTableEntryType.meta)
      {
        List<String> qName = Splitter.on(':').limit(2).omitEmptyStrings()
          .splitToList(e.getKey());
        if (qName.size() == 2)
        {
          conditions.put(e, "v" + i + ".namespace = '" + escaper.escape(qName.
            get(0))
            + "'");
          conditions.put(e, "v" + i + ".name = '" + escaper.escape(qName.get(1))
            + "'");
        }
        else
        {
          conditions.put(e, "v" + i + ".name = '" + escaper.escape(qName.get(0))
            + "'");
        }
        conditions.put(e, "v" + i + ".corpus_ref = solutions.corpus_ref");
      }
      else
      {
        // general partition restriction
        conditions.put(e, "v" + i + ".toplevel_corpus IN (" + StringUtils.join(
          queryData.getCorpusList(), ",") + ")");
        // specificly join on top level corpus
        conditions.put(e, "v" + i + ".toplevel_corpus = solutions.toplevel_corpus");

        // join on node ID
        QueryNode referencedNode = idxNodeVariables.get(e.getReferencedNode());
        if (referencedNode == null)
        {
          throw new AnnisQLSemanticsException("No such node \""
            + e.getReferencedNode() + "\". "
            + "Your query contains " + alternative.size()
            + " node(s), make sure no node definition numbers are greater than this number");
        }
        conditions.put(e, "v" + i + ".id = solutions.id" + referencedNode.getId());

        if (e.getType() == FrequencyTableEntryType.span)
        {
          conditions.put(e, "v" + i + ".n_sample IS TRUE");
        }
        else if (e.getType() == FrequencyTableEntryType.annotation)
        {
          // TODO: support namespaces
          // filter by selected key
          conditions.put(e, "v" + i + ".node_annotext LIKE '"
            + AnnotationConditionProvider.likeEscaper.escape(e.
              getKey())
            + ":%'");
          conditions.put(e, "v" + i + ".n_na_sample IS TRUE");
        }
      }
      i++;
    }
    return conditions;
  }

  @Override
  public String selectClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    TableAccessStrategy tas = tables(null);

    FrequencyTableQuery ext;
    List<FrequencyTableQuery> freqQueryData = queryData.getExtensions(FrequencyTableQuery.class);
    Validate.notNull(freqQueryData);
    Validate.notEmpty(freqQueryData);
    ext = freqQueryData.get(0);

    StringBuilder sb = new StringBuilder();
    int i = 1;
    for (FrequencyTableEntry e : ext)
    {
      if (e.getType() == FrequencyTableEntryType.annotation)
      {
        sb
          .append("v").append(i).append(".")
          .append(tas.columnName(NODE_ANNOTATION_TABLE, "qannotext"));
      }
      else if(e.getType() == FrequencyTableEntryType.span)
      {
        sb.append("('annis:tok:' || ").append("v").append(i).append(".").append(
          tas.columnName(NODE_TABLE,
            "span)"));
      }
      else if(e.getType() == FrequencyTableEntryType.meta)
      {
        sb.append("('annis_meta:'")
          .append(" || COALESCE(v").append(i).append(".").append("namespace").append(", '')")
          .append(" || v").append(i).append(".").append("\"name\"")
          .append(" || ':'")
          .append(" || v").append(i).append(".").append("\"value\")");
      }
      sb.append(" AS value").append(i).append(", ");
      i++;
    }

    sb.append("count(*) AS \"count\"");

    return sb.toString();
  }

  @Override
  public Set<String> whereConditions(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
//    Set<String> conditions = new LinkedHashSet<>();
//
//    FrequencyTableQuery ext;
//    List<FrequencyTableQuery> freqQueryData = queryData.getExtensions(FrequencyTableQuery.class);
//    Validate.notNull(freqQueryData);
//    Validate.notEmpty(freqQueryData);
//    ext = freqQueryData.get(0);
//
//    Set<String> conditions = conditionsForEntries(ext, queryData, alternative);

    return new HashSet<>();
  }

  public SolutionSqlGenerator getSolutionSqlGenerator()
  {
    return solutionSqlGenerator;
  }

  public void setSolutionSqlGenerator(SolutionSqlGenerator solutionSqlGenerator)
  {
    this.solutionSqlGenerator = solutionSqlGenerator;
  }

}
