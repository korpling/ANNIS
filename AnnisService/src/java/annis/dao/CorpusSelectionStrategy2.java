package annis.dao;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import annis.sqlgen.NodeSqlAdapter;
import annis.sqlgen.TableAccessStrategy;

public class CorpusSelectionStrategy2 extends BaseCorpusSelectionStrategy {

	private String structTable = TableAccessStrategy.NODE_TABLE;
	private String viewSuffix;
	
	public CorpusSelectionStrategy2() {
		setCorpusList(new ArrayList<Long>());
	}
	
	@Override
	public String createViewSql() {
		if (selectAll())
			return null;
		
		String template = "" +
				"CREATE VIEW :view_name " +
				"AS SELECT * " +
				"FROM :struct_table " +
				"WHERE corpus_ref " + corpusConstraint();
		String sql = template
			.replaceAll(":view_name", viewName(structTable))
			.replaceAll(":struct_table", structTable)
			.replaceAll(":corpusList", StringUtils.join(corpusList, ", "));
		return sql;
	}
	
//	// FIXME: TEST
//	@Override
//	public String dropViewSql() {
//		return "DROP VIEW :view_name".replaceAll(":view_name", viewName(structTable));
//	}
	
	@Override
	public String viewName(String table) {
		Validate.notNull(table, "BUG: table=null passed as argument");

		if (table.equals(structTable) && ! selectAll() )
			return table + viewSuffix;

		return table;
	}

	@Deprecated
	public void registerNodeAdapter(NodeSqlAdapter nodeSqlAdapter) {
//		if (isSelectEveryCorpus()) {
//			structTable = null;
//		} else {
//			structTable = nodeSqlAdapter.getStructTable();
//			nodeSqlAdapter.setStructTable(viewName());
//		}
	}

	///// Getter / Setter

	public String getStructTable() {
		return structTable;
	}

	public void setStructTable(String structTable) {
		this.structTable = structTable;
	}

	public String getViewSuffix() {
		return viewSuffix;
	}

	public void setViewSuffix(String viewSuffix) {
		this.viewSuffix = viewSuffix;
	}

}
