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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import annis.querymodel.QueryNode;
import annis.ql.parser.QueryData;

/**
 * Abstract base class for a complete SQL statement.
 * 
 * A SQL statement consists of a mandatory SELECT and FROM clauses 
 * and optional WHERE, GROUP BY, ORDER BY and LIMIT/OFFSET clauses. 
 * The individual clauses are generated using helper classes which 
 * are specified by properties.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 *
 * @param <T> Type into which the JDBC result set is transformed.
 */
public abstract class AbstractSqlGenerator<T>
	extends TableAccessStrategyFactory 
	implements SqlGenerator<QueryData, T> {

	// generators for different SQL statement clauses
	private WithClauseSqlGenerator withClauseSqlGenerator;
	private SelectClauseSqlGenerator<QueryData> selectClauseSqlGenerator;
	private List<FromClauseSqlGenerator<QueryData>> fromClauseSqlGenerators;
	private List<WhereClauseSqlGenerator<QueryData>> whereClauseSqlGenerators;
	private GroupByClauseSqlGenerator<QueryData> groupByClauseSqlGenerator;
	private OrderByClauseSqlGenerator<QueryData> orderByClauseSqlGenerator;
	private LimitOffsetClauseSqlGenerator<QueryData> limitOffsetClauseSqlGenerator;

	// controls indentation
	public final static String TABSTOP = "  ";

	@Override
	public String toSql(QueryData queryData) {
		return toSql(queryData, 0);
	}
	
  @Override
	public String toSql(QueryData queryData, int indentBy) {
		Assert.notEmpty(queryData.getAlternatives(), "BUG: no alternatives");
		
		// push alternative down
		List<QueryNode> alternative = queryData.getAlternatives().get(0);

		String indent = computeIndent(indentBy);
		StringBuffer sb = new StringBuffer();
		indent(sb, indent);
		sb.append(createSqlForAlternative(queryData, alternative, indent));
		appendOrderByClause(sb, queryData, alternative, indent);
		appendLimitOffsetClause(sb, queryData, alternative, indent);
		return sb.toString();
	}

	protected String createSqlForAlternative(QueryData queryData,
			List<QueryNode> alternative, String indent) {
		StringBuffer sb = new StringBuffer();
		appendSelectClause(sb, queryData, alternative, indent);
		appendFromClause(sb, queryData, alternative, indent);
		appendWhereClause(sb, queryData, alternative, indent);
		appendGroupByClause(sb, queryData, alternative, indent);
		return sb.toString();
	}

	protected String computeIndent(int indentBy) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indentBy; ++i) {
			sb.append(TABSTOP);
		}
		return sb.toString();
	}
	
	protected void indent(StringBuffer sb, String indent) {
		sb.append(indent);
	}

	protected void indent(StringBuilder sb, String indent) {
		sb.append(indent);
	}

	private void appendSelectClause(StringBuffer sb, QueryData queryData, List<QueryNode> alternative, String indent) {
		sb.append("SELECT ");
		sb.append(selectClauseSqlGenerator.selectClause(queryData, alternative, indent));
		sb.append("\n");
	}

	private void appendFromClause(StringBuffer sb, QueryData queryData, List<QueryNode> alternative, String indent) {
		indent(sb, indent);
		sb.append("FROM");
		List<String> fromTables = new ArrayList<String>();
		for (FromClauseSqlGenerator<QueryData> generator : fromClauseSqlGenerators) {
			fromTables.add(generator.fromClause(queryData, alternative, indent));
		}
		sb.append("\n");
		indent(sb, indent + TABSTOP);
		sb.append(StringUtils.join(fromTables, ",\n" + indent + TABSTOP));
		sb.append("\n");
	}

	private void appendWhereClause(StringBuffer sb, QueryData queryData, List<QueryNode> alternative, String indent) {

		// treat each condition as mutable string to remove last AND
		List<StringBuffer> conditions = new ArrayList<StringBuffer>();
		for (WhereClauseSqlGenerator<QueryData> generator : whereClauseSqlGenerators) {
			Set<String> whereConditions = generator.whereConditions(queryData, alternative, indent);
			for (String constraint : whereConditions)
				conditions.add(new StringBuffer(constraint));
		}

		// sort conditions, group by accessed table alias
		Collections.sort(conditions, new Comparator<StringBuffer>() {

			@Override
			public int compare(StringBuffer o1, StringBuffer o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null && o2 != null)
					return -1;
				if (o1 != null && o2 == null)
					return 1;
				return o1.toString().compareTo(o2.toString());
			}

		});

		// no conditions in WHERE clause? break out
		if (conditions.isEmpty())
			return;
		
		// append WHERE clause to query
		indent(sb, indent);
		sb.append("WHERE");
		sb.append("\n");
		indent(sb, indent + TABSTOP);
		sb.append(StringUtils.join(conditions, " AND\n" + indent + TABSTOP));
		sb.append("\n");
	}

	private void appendGroupByClause(StringBuffer sb, QueryData queryData, List<QueryNode> alternative, String indent) {
		if (groupByClauseSqlGenerator != null) {
			indent(sb, indent);
			sb.append("GROUP BY ");
			sb.append(groupByClauseSqlGenerator.groupByAttributes(queryData, alternative));
			sb.append("\n");
		}
	}

	protected void appendOrderByClause(StringBuffer sb, QueryData queryData, List<QueryNode> alternative, String indent) {
		if (orderByClauseSqlGenerator != null) {
			indent(sb, indent);
			sb.append("ORDER BY ");
			sb.append(orderByClauseSqlGenerator.orderByClause(queryData, alternative, indent));
			sb.append("\n");
		}
	}

	protected void appendLimitOffsetClause(StringBuffer sb, QueryData queryData, List<QueryNode> alternative, String indent) {
		if (limitOffsetClauseSqlGenerator != null) {
			indent(sb, indent);
			sb.append(limitOffsetClauseSqlGenerator.limitOffsetClause(queryData, alternative, indent));
			sb.append("\n");
		}
	}

	///// Getter / Setter

	public List<FromClauseSqlGenerator<QueryData>> getFromClauseSqlGenerators() {
		return fromClauseSqlGenerators;
	}

	public void setFromClauseSqlGenerators(
			List<FromClauseSqlGenerator<QueryData>> fromClauseSqlGenerators) {
		this.fromClauseSqlGenerators = fromClauseSqlGenerators;
	}

	public List<WhereClauseSqlGenerator<QueryData>> getWhereClauseSqlGenerators() {
		return whereClauseSqlGenerators;
	}

	public void setWhereClauseSqlGenerators(
			List<WhereClauseSqlGenerator<QueryData>> whereClauseSqlGenerators) {
		this.whereClauseSqlGenerators = whereClauseSqlGenerators;
	}

	public GroupByClauseSqlGenerator<QueryData> getGroupByClauseSqlGenerator() {
		return groupByClauseSqlGenerator;
	}

	public void setGroupByClauseSqlGenerator(
			GroupByClauseSqlGenerator<QueryData> groupByClauseSqlGenerator) {
		this.groupByClauseSqlGenerator = groupByClauseSqlGenerator;
	}

	public WithClauseSqlGenerator getWithClauseSqlGenerator() {
		return withClauseSqlGenerator;
	}

	public void setWithClauseSqlGenerator(
			WithClauseSqlGenerator withClauseSqlGenerator) {
		this.withClauseSqlGenerator = withClauseSqlGenerator;
	}

	public SelectClauseSqlGenerator<QueryData> getSelectClauseSqlGenerator() {
		return selectClauseSqlGenerator;
	}

	public void setSelectClauseSqlGenerator(
			SelectClauseSqlGenerator<QueryData> selectClauseSqlGenerator) {
		this.selectClauseSqlGenerator = selectClauseSqlGenerator;
	}

	public OrderByClauseSqlGenerator<QueryData> getOrderByClauseSqlGenerator() {
		return orderByClauseSqlGenerator;
	}

	public void setOrderByClauseSqlGenerator(
			OrderByClauseSqlGenerator<QueryData> orderByClauseSqlGenerator) {
		this.orderByClauseSqlGenerator = orderByClauseSqlGenerator;
	}

	public LimitOffsetClauseSqlGenerator<QueryData> getLimitOffsetClauseSqlGenerator() {
		return limitOffsetClauseSqlGenerator;
	}

	public void setLimitOffsetClauseSqlGenerator(
			LimitOffsetClauseSqlGenerator<QueryData> limitOffsetClauseSqlGenerator) {
		this.limitOffsetClauseSqlGenerator = limitOffsetClauseSqlGenerator;
	}

}