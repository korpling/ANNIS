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

import static annis.sqlgen.TableAccessStrategy.CORPUS_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.FACTS_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.TEXT_TABLE;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import annis.dao.AnnotatedMatch;
import annis.dao.AnnotatedSpan;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.ql.parser.QueryData;

/**
 *
 * @author thomas
 */
public class MatrixSqlGenerator 
	extends AbstractSqlGenerator<List<AnnotatedMatch>>
	implements SelectClauseSqlGenerator, FromClauseSqlGenerator,
	WhereClauseSqlGenerator, GroupByClauseSqlGenerator
{

	@Deprecated
  private String matchedNodesViewName;

	private SqlGenerator<?> innerQuerySqlGenerator;
	private TableJoinsInFromClauseSqlGenerator 
		tableJoinsInFromClauseGenerator;

	
  @Override
  public List<AnnotatedMatch> extractData(ResultSet resultSet) 
		  throws SQLException, DataAccessException
  {
    List<AnnotatedMatch> matches = new ArrayList<AnnotatedMatch>();

    Map<List<Long>, AnnotatedSpan[]> matchesByGroup = 
    		new HashMap<List<Long>, AnnotatedSpan[]>();

    while (resultSet.next())
    {
      long id = resultSet.getLong("id");
      String coveredText = resultSet.getString("span");
      
      Array arrayAnnotation = resultSet.getArray("annotations");
      Array arrayMeta = resultSet.getArray("metadata");
      
      List<Annotation> annotations =  extractAnnotations(arrayAnnotation);
      List<Annotation> metaData = extractAnnotations(arrayMeta);

      // create key
      Array sqlKey = resultSet.getArray("key");
      Validate.isTrue(!resultSet.wasNull(), 
    		  "Match group identifier must not be null");
      Validate.isTrue(sqlKey.getBaseType() == Types.BIGINT,
        "Key in database must be from the type \"bigint\" but was \"" + 
      sqlKey.getBaseTypeName() + "\"");

      Long[] keyArray = (Long[]) sqlKey.getArray();
      int matchWidth = keyArray.length;
      List<Long> key = Arrays.asList(keyArray);
      
      if (!matchesByGroup.containsKey(key))
      {
        matchesByGroup.put(key, new AnnotatedSpan[matchWidth]);
      }
      
      // set annotation spans for *all* positions of the id
      // (node could have matched several times)
      for(int posInMatch=0; posInMatch < key.size(); posInMatch++)
      {
        if(key.get(posInMatch) == id)
        {
          matchesByGroup.get(key)[posInMatch] = 
        		  new AnnotatedSpan(id, coveredText, annotations, metaData);
        }
      }
    }

    for(AnnotatedSpan[] match : matchesByGroup.values())
    {
      matches.add(new AnnotatedMatch(Arrays.asList(match)));
    }

    return matches;

  }

  @Override
	public String selectClause(QueryData queryData,
			List<AnnisNode> alternative, String indent) {

	  	StringBuilder sb = new StringBuilder();
	  	sb.append("\n");

	  	// key
	  	indent(sb, indent + TABSTOP);
	  	sb.append("ARRAY[");
	  	List<String> ids = new ArrayList<String>();
	  	for (int i = 1; i <= alternative.size(); ++i) {
	  		ids.add("solutions.id" + String.valueOf(i));
	  	}
	  	sb.append(StringUtils.join(ids, ", "));
	  	sb.append("] AS key,\n");
	
	  	// fields
		TableAccessStrategy tables = tables(null);
		indent(sb, indent + TABSTOP);
		sb.append(tables.aliasedColumn(NODE_TABLE, "id"));
		sb.append(" AS id,\n");
		
		indent(sb, indent + TABSTOP);
		sb.append("min(substr(");
		sb.append(tables.aliasedColumn(TEXT_TABLE, "text"));
		sb.append(", ");
		sb.append(tables.aliasedColumn(NODE_TABLE, "left"));
		sb.append(" + 1, ");
		sb.append(tables.aliasedColumn(NODE_TABLE, "right"));
		sb.append(" - ");
		sb.append(tables.aliasedColumn(NODE_TABLE, "left"));
		sb.append(")) AS span,\n");

		indent(sb, indent + TABSTOP);
		sb.append("array_agg(DISTINCT coalesce(");
		sb.append(tables.aliasedColumn(NODE_ANNOTATION_TABLE, "namespace"));
		sb.append(" || ':', '') || ");
		sb.append(tables.aliasedColumn(NODE_ANNOTATION_TABLE, "name"));
		sb.append(" || ':' || encode(");
		sb.append(tables.aliasedColumn(NODE_ANNOTATION_TABLE, "value"));
		sb.append("::bytea, 'base64')) AS annotations,\n");

		indent(sb, indent + TABSTOP);
		sb.append("array_agg(DISTINCT coalesce(");
		sb.append(tables.aliasedColumn(CORPUS_ANNOTATION_TABLE, "namespace"));
		sb.append(" || ':', '') || ");
		sb.append(tables.aliasedColumn(CORPUS_ANNOTATION_TABLE, "name"));
		sb.append(" || ':' || encode(");
		sb.append(tables.aliasedColumn(CORPUS_ANNOTATION_TABLE, "value"));
		sb.append("::bytea, 'base64')) AS metadata");
		
	  	return sb.toString();
	}
  
  
	@Override
	public String fromClause(QueryData queryData, 
			List<AnnisNode> alternative, String indent) {
		StringBuffer sb = new StringBuffer();
		
		indent(sb, indent);
		sb.append("(\n");
		indent(sb, indent);
		int indentBy = indent.length() / 2 + 2;
		sb.append(innerQuerySqlGenerator.toSql(queryData, indentBy));
		indent(sb, indent + TABSTOP);
		sb.append(") AS solutions,\n");
		
		indent(sb, indent + TABSTOP);
		// really ugly
		sb.append(
				tableJoinsInFromClauseGenerator
				.fromClauseForNode(null, true));

		sb.append("\n");

		TableAccessStrategy tables = tables(null);
		indent(sb, indent + TABSTOP);
		sb.append("LEFT OUTER JOIN ");
		sb.append(CORPUS_ANNOTATION_TABLE);
		sb.append(" ON (");
		sb.append(tables.aliasedColumn(CORPUS_ANNOTATION_TABLE, "corpus_ref"));
		sb.append(" = ");
		sb.append(tables.aliasedColumn(NODE_TABLE, "corpus_ref"));
		sb.append("),\n");
		
		indent(sb, indent + TABSTOP);
		sb.append(TEXT_TABLE);
		
		return sb.toString();
	}

	@Override
	public Set<String> whereConditions(QueryData queryData,
			List<AnnisNode> alternative, String indent) {
		
		Set<String> conditions = new HashSet<String>();
		StringBuilder sb = new StringBuilder();
		TableAccessStrategy tables = tables(null);
		
		// corpus selection
		List<Long> corpusList = queryData.getCorpusList();
		if (corpusList != null && ! corpusList.isEmpty() ) {
			sb.append(tables.aliasedColumn(NODE_TABLE, "toplevel_corpus"));
			sb.append(" IN (");
			sb.append(StringUtils.join(corpusList, ", "));
			sb.append(")");
			conditions.add(sb.toString());
		}
		
		// text table table joining (FIXME: why not in from clause)
		sb.setLength(0);
		sb.append(tables.aliasedColumn(TEXT_TABLE, "id"));
		sb.append(" = ");
		sb.append(tables.aliasedColumn(NODE_TABLE, "text_ref"));
		conditions.add(sb.toString());
		
		// nodes selected by id
		sb.setLength(0);
		sb.append("(\n");
		indent(sb, indent + TABSTOP + TABSTOP);
		List<String> ors = new ArrayList<String>();
		for (int i = 1; i <= queryData.getMaxWidth(); ++i) {
			ors.add(
					tables.aliasedColumn(NODE_TABLE, "id") +
					" = solutions.id" + String.valueOf(i));
		}
		sb.append(StringUtils.join(ors, " OR\n" + indent + TABSTOP + TABSTOP));
		sb.append("\n");
		indent(sb, indent + TABSTOP);
		sb.append(")");
		conditions.add(sb.toString());		
		
		return conditions;
		
	}
	
	@Override
	public String groupByAttributes(QueryData queryData,
			List<AnnisNode> alternative) {
		return "key, " + tables(null).aliasedColumn(NODE_TABLE, "id") + ", span";
	}
	
	@Deprecated
  public String getMatrixQuery(List<Long> corpusList, int maxWidth)
  {
    StringBuilder keySb = new StringBuilder();
    keySb.append("ARRAY[matches.id1");
    for (int i = 2; i <= maxWidth; ++i)
    {
      keySb.append(",");
      keySb.append("matches.id");
      keySb.append(i);
    }
    keySb.append("] AS key");
    String key = keySb.toString();

    StringBuilder sb = new StringBuilder();

    sb.append("SELECT \n");
    sb.append("\t");
    sb.append(key);
    sb.append(",\nfacts.id AS id,\n");
    sb.append("min(substr(text.text, facts.left+1,facts.right-facts.left)) " +
    		"AS span,\n");
    sb.append("array_agg(DISTINCT coalesce(facts.node_annotation_namespace " +
    		"|| ':', '') "
      + "|| facts.node_annotation_name || ':' "
      + "|| encode(facts.node_annotation_value::bytea, 'base64')) " +
      "AS annotations,\n");
    sb.append("array_agg(DISTINCT coalesce(ca.namespace || ':', '') "
      + "|| ca.name || ':' "
      + "|| encode(ca.value::bytea, 'base64')) AS metadata\n");
    
    sb.append("FROM\n");
    sb.append("\t");
    sb.append(matchedNodesViewName);
    sb.append(" AS matches,\n");

    sb.append("\t\"text\" AS \"text\",\n");
    
    sb.append("\t");
    sb.append(FACTS_TABLE);
    sb.append(" AS facts\n");
    sb.append("\t LEFT OUTER JOIN corpus_annotation AS ca " +
    		"ON (ca.corpus_ref = facts.corpus_ref)\n");

    sb.append("WHERE\n");

    if (corpusList != null)
    {
      sb.append("facts.toplevel_corpus IN (");
      sb.append(corpusList.isEmpty() 
    		  ? "NULL" : StringUtils.join(corpusList, ","));
      sb.append(") AND\n");
    }
    sb.append("facts.text_ref = text.id AND \n");

    sb.append("(");
    for (int i = 1; i <= maxWidth; i++)
    {
      sb.append("facts.id = matches.id").append(i);
      if (i < maxWidth)
      {
        sb.append(" OR ");
      }
    }
    sb.append(")\n");
    sb.append("GROUP BY key, facts.id, span");

    Logger.getLogger(MatrixSqlGenerator.class)
    	.debug("generated SQL for matrix:\n" + sb.toString());

    return sb.toString();
  }
  
  private List<Annotation> extractAnnotations(Array array) throws SQLException
  {
    List<Annotation> result = new ArrayList<Annotation>();
    
    if(array != null)
    {
      String[] arrayLines = (String[]) array.getArray();
      
      for(String line : arrayLines)
      {
        if(line != null)
        {
          String namespace = null;
          String name = null;
          String value = null;

          String[] split = line.split(":");
          if(split.length > 2)
          {
            namespace = split[0];
            name = split[1];
            value = split[2];
          }
          else if(split.length > 1)
          {
            name = split[0];
            value = split[1];
          }
          else
          {
            name = split[0];
          }

          if(value != null)
          {
            value = new String(Base64.decodeBase64(value));
          }

          result.add(new Annotation(namespace, name, value));
        } // if line not null
      }
    }
    
    return result;
  }

  public List<AnnotatedMatch> queryMatrix(JdbcTemplate jdbcTemplate, 
		  List<Long> corpusList, int maxWidth)
  {
    return (List<AnnotatedMatch>) 
    		jdbcTemplate.query(getMatrixQuery(corpusList, maxWidth), this);
  }

  public String getMatchedNodesViewName()
  {
    return matchedNodesViewName;
  }

  public void setMatchedNodesViewName(String matchedNodesViewName)
  {
    this.matchedNodesViewName = matchedNodesViewName;
  }

public SqlGenerator<?> getInnerQuerySqlGenerator() {
	return innerQuerySqlGenerator;
}

public void setInnerQuerySqlGenerator(
		SqlGenerator<?> innerQuerySqlGenerator) {
	this.innerQuerySqlGenerator = innerQuerySqlGenerator;
}

public TableJoinsInFromClauseSqlGenerator getTableJoinsInFromClauseGenerator() {
	return tableJoinsInFromClauseGenerator;
}

public void setTableJoinsInFromClauseGenerator(
		TableJoinsInFromClauseSqlGenerator tableJoinsInFromClauseGenerator) {
	this.tableJoinsInFromClauseGenerator = tableJoinsInFromClauseGenerator;
}
}
