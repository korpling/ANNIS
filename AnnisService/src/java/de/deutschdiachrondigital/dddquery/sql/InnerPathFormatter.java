package de.deutschdiachrondigital.dddquery.sql;

import java.util.List;
import java.util.Set;

import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.Condition;
import de.deutschdiachrondigital.dddquery.sql.model.Path;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Join;


public class InnerPathFormatter {

	public String format(Path path) {
		String sql = "" +
		selectClause(path) +
		fromClause(path) +
		whereClause(path) +
		whereClauseAliasJoins(path) +
		whereClauseOutputSelection(path);

		return sql;
	}
	
	String selectClause(Path path) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT\n");
		sb.append("\t\t\toutput.pre AS PRE\n");
		
		return sb.toString();
	}
	
	String fromClause(Path path) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("\t\tFROM\n");
		sb.append("\t\t\trank output");
		
		if (path.getAliasSets().isEmpty()) {
			sb.append("\n");
			return sb.toString();
		}
		
		sb.append(",\n");
		
		for (AliasSet aliasSet : path.getAliasSets()) {
			sb.append("\t\t\t");
			for (String table : aliasSet.getUsedTables()) {
				sb.append(table + " " + aliasSet.getTable(table) + ", ");
			}
			sb.setLength(sb.length() - " ".length());
			sb.append("\n");
		}
		sb.setLength(sb.length() - ",\n".length());
		sb.append("\n");
		
		return sb.toString();
	}
	
	private String whereClause(Path path) {
		List<Condition> conditions = path.getConditions();
		
		if (conditions.isEmpty())
			return "";
		
		StringBuffer sb = new StringBuffer();
		sb.append("\t\tWHERE\n");
		for (Condition condition : conditions) {
			sb.append("\t\t\t" + condition + " AND\n");
		}
		
		return sb.toString();
	}

	private String whereClauseAliasJoins(Path path) {
		StringBuffer sb = new StringBuffer();
		for (AliasSet aliasSet : path.getAliasSets()) {
			Set<String> usedTables = aliasSet.getUsedTables();
			if (usedTables.contains("rank") && usedTables.contains("struct"))
				sb.append("\t\t\t" + Join.eq(aliasSet.getColumn("rank", "struct_ref"), aliasSet.getColumn("struct", "id")) + " AND\n");
			if (usedTables.contains("struct") && usedTables.contains("anno"))
				sb.append("\t\t\t" + Join.eq(aliasSet.getColumn("anno", "struct_ref"), aliasSet.getColumn("struct", "id")) + " AND\n");
			if (usedTables.contains("anno") && usedTables.contains("anno_attribute"))
				sb.append("\t\t\t" + Join.eq(aliasSet.getColumn("anno_attribute", "anno_ref"), aliasSet.getColumn("anno", "id")) + " AND\n");
		}
		return sb.toString();
	}

	private String whereClauseOutputSelection(Path path) {
		AliasSet output = path.getAliasSets().get(0);
		return "\t\t\toutput.pre = " + output.getColumn("rank", "pre");
	}

}
