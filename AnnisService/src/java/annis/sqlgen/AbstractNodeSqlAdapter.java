package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import annis.dao.CorpusSelectionStrategy;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnisNode.TextMatching;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.Inclusion;
import annis.sqlgen.model.Join;
import annis.sqlgen.model.LeftAlignment;
import annis.sqlgen.model.LeftDominance;
import annis.sqlgen.model.LeftOverlap;
import annis.sqlgen.model.Overlap;
import annis.sqlgen.model.PointingRelation;
import annis.sqlgen.model.Precedence;
import annis.sqlgen.model.RangedJoin;
import annis.sqlgen.model.RankTableJoin;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightDominance;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.SameSpan;
import annis.sqlgen.model.Sibling;

public abstract class AbstractNodeSqlAdapter implements NodeSqlAdapter {

	// the adapted node
	protected AnnisNode node;

	// helper for corpus selection
	protected CorpusSelectionStrategy corpusSelectionStrategy;

	// helper for table aliases
	private TableAccessStrategy tableAccessStrategy;

	// pluggable factory for TableAccessStrategy classes
	protected TableAccessStrategyFactory tableAccessStrategyFactory;

	public String selectClause() {
		String[] columns = { "id", "text_ref", "left_token", "right_token" };
		for (int i = 0; i < columns.length; ++i) {
			columns[i] = getTableAccessStrategy().aliasedColumn(NODE_TABLE, columns[i]);
		}
		
		return StringUtils.join(Arrays.asList(columns), ", ");
	}

	public String selectClauseNullValues() {
		return "NULL, NULL, NULL, NULL";
	}

	public List<String> whereClause() {
		List<String> conditions = new ArrayList<String>();
		
		addCorpusSelectionCondition(conditions);

		if (node.getSpannedText() != null) {
			TextMatching textMatching = node.getSpanTextMatching();
			conditions.add(join(textMatching.sqlOperator(), getTableAccessStrategy().aliasedColumn(NODE_TABLE, "span"), sqlString(node.getSpannedText(), textMatching)));
		}

		if (node.isToken())
			conditions.add(getTableAccessStrategy().aliasedColumn(NODE_TABLE, "token_index") + " IS NOT NULL");

		if (node.isRoot())
			conditions.add(getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "parent") + " IS NULL");

		if (node.getNamespace() != null)
			conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "namespace"), sqlString(node.getNamespace())));

		if (node.getName() != null)
			conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "name"), sqlString(node.getName())));

		addAnnotationConditions(conditions, node.getNodeAnnotations(), NODE_ANNOTATION_TABLE);

		addAnnotationConditions(conditions, node.getEdgeAnnotations(), EDGE_ANNOTATION_TABLE);

		addJoinConditions(conditions);

		return conditions;
	}
	
	private void addJoinConditions(List<String> conditions) {
		for (Join join : node.getJoins()) {
			AnnisNode target = join.getTarget();
			TableAccessStrategy targetTableAccessStrategy = tableAccessStrategyFactory.createTableAccessStrategy(target, corpusSelectionStrategy);
		
			if (join instanceof SameSpan) {
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "text_ref"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "left"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "left")));
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "right"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "right")));
			
			} else if (join instanceof LeftAlignment) {
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "text_ref"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "left"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "left")));
			
			} else if (join instanceof RightAlignment) {
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "text_ref"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "right"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "right")));
			
			} else if (join instanceof Inclusion) {
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "text_ref"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join("<=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "left"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "left")));
				conditions.add(join(">=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "right"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "right")));
			
			} else if (join instanceof Overlap) {
				throw new NotImplementedException("_o_ nicht implementiert, geht das ohne SQL OR?");
			
			} else if (join instanceof LeftOverlap) {
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "text_ref"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join("<=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "left"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "left")));
				conditions.add(join("<", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "left"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "right")));
				conditions.add(join("<", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "right"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "right")));
			
			} else if (join instanceof RightOverlap) {
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "text_ref"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "text_ref")));
				conditions.add(join("<", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "left"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "left")));
				conditions.add(join("<=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "left"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "right")));
				conditions.add(join(">=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "right"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "right")));
			
			} else if (join instanceof Precedence) {
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "text_ref"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "text_ref")));
				
				RangedJoin precedence = (RangedJoin) join;
				int min = precedence.getMinDistance();
				int max = precedence.getMaxDistance();
			
				// indirect
				if (min == 0 && max == 0) {
					conditions.add(join("<", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "right_token"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "left_token")));
			
					// exact distance
				} else if (min == max) {
					conditions.add(numberJoin("=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "right_token"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "left_token"), -min));
			
					// ranged distance
				} else {
					conditions.add(numberJoin("<=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "right_token"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "left_token"), -min));
					conditions.add(numberJoin(">=", getTableAccessStrategy().aliasedColumn(NODE_TABLE, "right_token"), targetTableAccessStrategy.aliasedColumn(NODE_TABLE, "left_token"), -max));
				}
			
			} else if (join instanceof Sibling) {
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "parent"), targetTableAccessStrategy.aliasedColumn(EDGE_TABLE, "parent")));
			
			} else if (join instanceof LeftDominance) {
				conditions.add(numberJoin("=", getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "pre"), targetTableAccessStrategy.aliasedColumn(EDGE_TABLE, "pre"), -1));
			
			} else if (join instanceof RightDominance) {
				conditions.add(numberJoin("=", getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "post"), targetTableAccessStrategy.aliasedColumn(EDGE_TABLE, "post"), 1));
			
			} else if (join instanceof Dominance) {
				addEdgeConditions(conditions, join, targetTableAccessStrategy, "d");

			} else if (join instanceof PointingRelation) {
				addEdgeConditions(conditions, join, targetTableAccessStrategy, "p");

			}
		}
	}

	private void addEdgeConditions(List<String> conditions, Join join, TableAccessStrategy targetTableAccessStrategy, final String edgeType) {
		conditions.add(join("=", getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "zshg"), targetTableAccessStrategy.aliasedColumn(EDGE_TABLE, "zshg")));				
		conditions.add(join("=", targetTableAccessStrategy.aliasedColumn(EDGE_TABLE, "edge_type"), sqlString(edgeType)));				
		
		RankTableJoin rankTableJoin = (RankTableJoin) join;
		if (rankTableJoin.getName() != null)
			conditions.add(join("=", targetTableAccessStrategy.aliasedColumn(EDGE_TABLE, "name"), sqlString(rankTableJoin.getName())));

		int min = rankTableJoin.getMinDistance();
		int max = rankTableJoin.getMaxDistance();

		// direct
		if (min == 1 && max == 1) {
			conditions.add(join("=", getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "pre"), targetTableAccessStrategy.aliasedColumn(EDGE_TABLE, "parent")));

		// indirect
		} else {
			conditions.add(join("<", getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "pre"), targetTableAccessStrategy.aliasedColumn(EDGE_TABLE, "pre")));
			conditions.add(join(">", getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "post"), targetTableAccessStrategy.aliasedColumn(EDGE_TABLE, "post")));

			// exact
			if (min > 0 && min == max) {
				conditions.add(numberJoin("=", getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "level"), targetTableAccessStrategy.aliasedColumn(EDGE_TABLE, "level"), -min));

			// range
			} else if (min > 0 && min < max) {
				conditions.add(numberJoin("<=", getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "level"), targetTableAccessStrategy.aliasedColumn(EDGE_TABLE, "level"), -min));
				conditions.add(numberJoin(">=", getTableAccessStrategy().aliasedColumn(EDGE_TABLE, "level"), targetTableAccessStrategy.aliasedColumn(EDGE_TABLE, "level"), -max));
			}
		}
	}

	private void addCorpusSelectionCondition(List<String> conditions) {
		String docRefColumn = getTableAccessStrategy().aliasedColumn(NODE_TABLE, "corpus_ref");
		String corpusSelectionCondition = corpusSelectionStrategy.whereClauseForNode(docRefColumn);
		if (corpusSelectionCondition != null)
			conditions.add(corpusSelectionCondition);
	}
	
	private void addAnnotationConditions(List<String> conditions, Set<Annotation> annotations, String table) {
		int i = 0;
		for (Annotation annotation : annotations) {
			++i;
			if (annotation.getNamespace() != null)
				conditions.add(join("=", getTableAccessStrategy().aliasedColumn(table, "namespace", i), sqlString(annotation.getNamespace())));
			conditions.add(join("=", getTableAccessStrategy().aliasedColumn(table, "name", i), sqlString(annotation.getName())));
			if (annotation.getValue() != null) {
				TextMatching textMatching = annotation.getTextMatching();
				conditions.add(join(textMatching.sqlOperator(), getTableAccessStrategy().aliasedColumn(table, "value", i), sqlString(annotation.getValue(), textMatching)));
			}
		}
	}
	
	protected String join(String op, String lhs, String rhs) {
		return lhs + " " + op + " " + rhs;
	}

	protected String numberJoin(String op, String lhs, String rhs,
			int offset) {
				String plus = offset >= 0 ? " + " : " - ";
				return join(op, lhs, rhs) + plus + String.valueOf(Math.abs(offset));
			}

	protected String sqlString(String string) {
		return "'" + string + "'";
	}

	protected String sqlString(String string, TextMatching textMatching) {
		if (textMatching == TextMatching.REGEXP)
			string = "^" + string + "$";
		return sqlString(string);
	}

	protected String tableAliasDefinition(String table, int count) {
		StringBuffer sb = new StringBuffer();
	
		sb.append(getTableAccessStrategy().tableName(table));
		sb.append(" AS ");
		sb.append(getTableAccessStrategy().aliasedTable(table, count));
	
		return sb.toString();
	}

	public AnnisNode getNode() {
		return node;
	}

	public void setNode(AnnisNode node) {
		this.node = node;
	}

	public CorpusSelectionStrategy getCorpusSelectionStrategy() {
		return corpusSelectionStrategy;
	}

	public void setCorpusSelectionStrategy(CorpusSelectionStrategy corpusSelectionStrategy) {
		this.corpusSelectionStrategy = corpusSelectionStrategy;
	}

	public TableAccessStrategyFactory getTableAccessStrategyFactory() {
		return tableAccessStrategyFactory;
	}

	public void setTableAccessStrategyFactory(TableAccessStrategyFactory tableAccessStrategyFactory) {
		this.tableAccessStrategyFactory = tableAccessStrategyFactory;
	}

	protected TableAccessStrategy getTableAccessStrategy() {
		if (tableAccessStrategy == null)
			tableAccessStrategy = tableAccessStrategyFactory.createTableAccessStrategy(node, corpusSelectionStrategy);
		return tableAccessStrategy;
	}

}
