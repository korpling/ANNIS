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

import annis.model.QueryNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;


public class TableAccessStrategy {

	// default table names
	public final static String NODE_TABLE = "node";
  public final static String RANK_TABLE = "rank";
  public final static String COMPONENT_TABLE = "component";
  public final static String NODE_ANNOTATION_TABLE = "node_annotation";
  public final static String EDGE_ANNOTATION_TABLE = "edge_annotation";
  public final static String ANNOTATION_POOL_TABLE = "annotation_pool";
  public final static String FACTS_TABLE = "facts";
  public final static String CORPUS_TABLE = "corpus";
  public final static String CORPUS_ANNOTATION_TABLE = "corpus_annotation";
  public final static String TEXT_TABLE = "text";

	// the wrapped node
	private QueryNode node;
	
	// table aliases
	private Map<String, String> tableAliases;
	
	// aliased column names
	private Map<String, Map<String, String>> columnAliases;
  
  private Map<String, Boolean> tablePartitioned;
	
	public TableAccessStrategy() {
		this.tableAliases = new HashMap<>();
		this.columnAliases = new HashMap<>();
		
	}
	
	public TableAccessStrategy(QueryNode node) {
		this();
		this.node = node;
	}
  
  /** Copy constructor */
  public TableAccessStrategy(TableAccessStrategy tas)
  {
    this.tableAliases = new HashMap<>(tas.getTableAliases());
    this.columnAliases = new HashMap<>(tas.getColumnAliases());
    this.tablePartitioned = new HashMap<>();
    this.node = tas.getNode();
  }

	///// table and column aliases
	
	public String tableName(String table) 
  {
    return tableName(tableAliases, table);
	}
  
  public String partitionTableName(String table, List<Long> corpusList) 
  {
    return partitionTableName(tableAliases, tablePartitioned, table, corpusList);
	}
  
  private static String tableName(Map<String, String> tableAliases, String table)
  {
    return tableAliases.containsKey(table) ? tableAliases.get(table) : table;
  }
  
   private static String  partitionTableName(Map<String, String> tableAliases,
     Map<String, Boolean> tablePartitioned,
     String table, List<Long> corpusList) 
  {
    String result = tableName(tableAliases, table);
    
    /* when we only have one corpus and the table partitions are used, 
     optimize the query by directly querying the partition table */
    if(corpusList != null && corpusList.size() == 1 && isPartitioned(tablePartitioned, table))
    {
      result = result + "_" + corpusList.get(0);
    }
    return result;
	}
  

  public String columnName(String table, String column) 
  {
    return columnName(columnAliases, table, column);
  }
  
	public static String columnName(Map<String, Map<String,String>> columnAliases, 
    String table, String column) 
  {
		if (columnAliases.containsKey(table)) 
    {
			Map<String, String> columns = columnAliases.get(table);
			if (columns.containsKey(column)) {
				return columns.get(column);
			}
		}
		return column;
	}
	
	public String aliasedTable(String table, int count) 
  {
		if (node != null) 
    {
			// sanity checks
//			if (table.equals(NODE_ANNOTATION_TABLE) && count > node.getNodeAnnotations().size())
//				throw new IllegalArgumentException("access to node annotation table out of range: " + count);
			if (table.equals(EDGE_ANNOTATION_TABLE) && count > node.getEdgeAnnotations().size())
				throw new IllegalArgumentException("access to edge annotation table out of range: " + count);
			if (table.equals(NODE_TABLE) && count > 1)
				throw new IllegalArgumentException("access to struct table out of range: " + count);
			if (table.equals(RANK_TABLE) && count > 1)
				throw new IllegalArgumentException("access to rank table out of range: " + count);
			
			// offset table count for edge annotations if node and edge annotations are the same table
			if (table.equals(EDGE_ANNOTATION_TABLE) && isMaterialized(EDGE_ANNOTATION_TABLE, NODE_ANNOTATION_TABLE))
				count = count + node.getNodeAnnotations().size() - 1;
		}
		
		if (count == 0) {
			count = 1;
		}
		
		// compute table counts
		Bag tables = computeSourceTables(node, tableAliases);

		String aliasedName = tableName(table);
		String aliasCount = node != null ? String.valueOf(node.getId()) : "";
		String countSuffix = tables.getCount(aliasedName) > 1 ? "_" + count : "";

		return aliasedName + aliasCount + countSuffix;
	}
  
  public static String aliasedTable(QueryNode node, Map<String, String> tableAliases, 
    String table, int count)
  {
    if (node != null)
    {
      // sanity checks
//			if (table.equals(NODE_ANNOTATION_TABLE) && count > node.getNodeAnnotations().size())
//				throw new IllegalArgumentException("access to node annotation table out of range: " + count);
      if (table.equals(EDGE_ANNOTATION_TABLE) && count > node.getEdgeAnnotations().size())
      {
        throw new IllegalArgumentException("access to edge annotation table out of range: " + count);
      }
      if (table.equals(NODE_TABLE) && count > 1)
      {
        throw new IllegalArgumentException("access to struct table out of range: " + count);
      }
      if (table.equals(RANK_TABLE) && count > 1)
      {
        throw new IllegalArgumentException("access to rank table out of range: " + count);
      }

      // offset table count for edge annotations if node and edge annotations are the same table
      if (table.equals(EDGE_ANNOTATION_TABLE) && isMaterialized(tableAliases, 
        EDGE_ANNOTATION_TABLE, NODE_ANNOTATION_TABLE))
      {
        count = count + node.getNodeAnnotations().size() - 1;
      }
    }

    if (count == 0)
    {
      count = 1;
    }

    // compute table counts
    Bag tables = computeSourceTables(node, tableAliases);

    String aliasedName = tableName(tableAliases, table);
    String aliasCount = node != null ? String.valueOf(node.getId()) : "";
    String countSuffix = tables.getCount(aliasedName) > 1 ? "_" + count : "";

		return aliasedName + aliasCount + countSuffix;
  }

  public String aliasedColumn(String table, String column) 
  {
    return aliasedColumn(tableAliases, columnAliases, node, table, column);
  }
  
  public String aliasedColumn(String table, String column, int count) 
  {
    return aliasedColumn(tableAliases, columnAliases, node, table, column, count);
  }
  
  
	public static String aliasedColumn(Map<String, String> tableAliases,
    Map<String, Map<String,String>> columnAliases, QueryNode node, String table, String column) 
  {
		return aliasedColumn(tableAliases, columnAliases, node, table, column, 1);
	}
	
	public static String aliasedColumn(Map<String, String> tableAliases, 
    Map<String, Map<String,String>> columnAliases,
    QueryNode node, 
    String table, String column, int count) 
  {
		return column(aliasedTable(node, tableAliases, table, count), 
      columnName(columnAliases, table, column));
	}
	
	protected static String column(String table, String column) 
  {
		return table + "." + column;
	}
	
	///// table usage
	
  protected Bag computeSourceTables() 
  {
    return computeSourceTables(node, tableAliases);
  }
  
	protected static Bag computeSourceTables(QueryNode node, Map<String, String> tableAliases) {
		Bag tables = new HashBag();
		
		// hack to support table selections for ANNOTATE query
		if (node == null) {
			String[] tableNames = {NODE_TABLE, RANK_TABLE, COMPONENT_TABLE, NODE_ANNOTATION_TABLE, EDGE_ANNOTATION_TABLE};
			for (String table : tableNames)
				tables.add(table);
			return tables;
		}
		
		tables.add(tableName(tableAliases, NODE_ANNOTATION_TABLE), node.getNodeAnnotations().size());
		if (node.getNodeAnnotations().isEmpty() && node.getNodeAnnotations().size() > 0)
			tables.add(tableName(tableAliases, NODE_ANNOTATION_TABLE));
		
		tables.add(tableName(tableAliases, EDGE_ANNOTATION_TABLE), node.getEdgeAnnotations().size());
		
		if ( tables.getCount(tableName(tableAliases, RANK_TABLE)) == 0 && usesRankTable(node) )
			tables.add(tableName(tableAliases, RANK_TABLE));
		if ( tables.getCount(tableName(tableAliases, COMPONENT_TABLE)) == 0 && usesRankTable(node) )
			tables.add(tableName(tableAliases, COMPONENT_TABLE));
		
		if (tables.getCount(tableName(tableAliases, NODE_TABLE)) == 0)
			tables.add(tableName(tableAliases, NODE_TABLE));
		
		return tables;
	}
	
	public boolean usesNodeAnnotationTable() 
  {
    return usesNodeAnnotationTable(node);
	}
  
  public static boolean usesNodeAnnotationTable(QueryNode node) {
		return node == null || ! node.getNodeAnnotations().isEmpty();
	}
	
	public boolean usesRankTable() 
  {
    return usesRankTable(node);
	}
  
  public static boolean usesRankTable(QueryNode node)
  {
    return node == null || usesComponentTable(node) || node.isRoot() || node.getArity() != null;
  }
	
	public boolean usesComponentTable() {
		return node == null || node.isPartOfEdge() || usesEdgeAnnotationTable();
	}
  
  public static boolean usesComponentTable(QueryNode node) {
		return node == null || node.isPartOfEdge() || useEdgeAnnotationTable(node);
	}
	
	public boolean usesEdgeAnnotationTable() {
		return node == null || ! node.getEdgeAnnotations().isEmpty();
	}
  
  public static boolean useEdgeAnnotationTable(QueryNode node) {
		return node == null || ! node.getEdgeAnnotations().isEmpty();
	}
	
  public boolean isMaterialized(String table, String otherTable) 
  {
    return isMaterialized(tableAliases, table, otherTable);
  }
  
	public static boolean isMaterialized(Map<String, String> tableAliases, 
    String table, String otherTable) 
  {
		return tableName(tableAliases, table).equals(tableName(tableAliases, otherTable));
	}
  
  public boolean isPartitioned(String table)
  {
    return isPartitioned(tablePartitioned, table);
  }
  
  public static boolean isPartitioned(Map<String, Boolean> tablePartitioned, 
    String table)
  {
    if(tablePartitioned != null)
    {
      Boolean result = tablePartitioned.get(table);
      if(result != null)
      {
        return result;
      }
    }
    return false;
  }
	
	///// delegates
	
	public void addTableAlias(String table, String alias) {
		tableAliases.put(table, alias);
	}
	
	public void addColumnAlias(String table, String column, String alias) {
		if ( ! columnAliases.containsKey(table) )
			columnAliases.put(table, new HashMap<String, String>());
		
		Map<String, String> aliases = columnAliases.get(table);
		aliases.put(column, alias);
	}
	
	///// Getter / Setter
	
	public QueryNode getNode() {
		return node;
	}

	public void setNode(QueryNode node) {
		this.node = node;
	}

	public Map<String, String> getTableAliases() {
		return tableAliases;
	}

	public void setTableAliases(Map<String, String> tableAliases) {
		this.tableAliases = tableAliases;
	}
	

	public Map<String, Map<String, String>> getColumnAliases() {
		return columnAliases;
	}

	public void setColumnAliases(Map<String, Map<String, String>> columnAliases) {
		this.columnAliases = columnAliases;
	}

  public Map<String, Boolean> getTablePartitioned()
  {
    return tablePartitioned;
  }

  public void setTablePartitioned(Map<String, Boolean> tablePartitioned)
  {
    this.tablePartitioned = tablePartitioned;
  }
  
}
