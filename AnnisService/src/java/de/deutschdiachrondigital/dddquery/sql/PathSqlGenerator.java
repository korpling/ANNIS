package de.deutschdiachrondigital.dddquery.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.Condition;
import de.deutschdiachrondigital.dddquery.sql.model.Path;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Join;


public class PathSqlGenerator {

	public String format(Path path) {
		String sql = "" +
			selectClause(path) +
			selectClauseMarkings(path) +
			fromClause(path) +
			whereClause(path) +
			whereClauseAliasJoins(path) +
			whereClauseOutputSelection(path);

		return sql;
	}

	String selectClause(Path path) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT DISTINCT\n");
		sb.append("\toutput.pre AS PRE,\n");
		sb.append("\toutput.post AS POST,\n");
		sb.append("\toutput.struct_id AS STRUCT,\n");
		sb.append("\toutput.name AS NAME,\n");
		sb.append("\toutput.text_ref AS TEXT_ID,\n");
		sb.append("\toutput.left AS LEFT,\n");
		sb.append("\toutput.right AS RIGHT,\n");
		sb.append("\toutput.span AS SPAN,\n");
		sb.append("\toutput.anno_level || ':' || output.anno_name AS ATTRIBUTE,\n");
		sb.append("\toutput.anno_value AS VALUE,\n");
		
		
		return sb.toString();
	}

	public String selectClauseMarkings(Path path) {
		Map<AliasSet, String> markings = path.getMarkings();
		
		if (markings.size() == 0)
			throw new RuntimeException("no alias set marked for output");
		
		if (markings.size() == 1)
			return "\t'" + markings.values().iterator().next() + "' AS MARKER\n";
		
		// sort alias sets for better readability
		List<AliasSet> aliasSets = new ArrayList<AliasSet>();
		aliasSets.addAll(markings.keySet());
		Collections.sort(aliasSets, new Comparator<AliasSet>() {

			public int compare(AliasSet o1, AliasSet o2) {
				return Integer.signum(o1.getId() - o2.getId());
			}
			
		});
		
		StringBuffer sb = new StringBuffer();

		sb.append("\tCASE\n");
		for (AliasSet aliasSet : aliasSets) {
			sb.append("\t\tWHEN output.pre = " + aliasSet.getColumn("rank", "pre") + " THEN '" + markings.get(aliasSet) + "'\n");
		}
		sb.append("\tEND AS MARKER\n");
		
		return sb.toString();
	}
	
	String fromClause(Path path) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("FROM\n");
		sb.append("\t( (rank AS rank0 INNER JOIN struct AS struct0 (struct_id, text_ref, doc_id, name, \"left\", \"right\", token_count, cont, span) ON rank0.struct_ref = struct0.struct_id) LEFT OUTER JOIN (anno AS anno0 (anno_id, struct_ref, anno_level) INNER JOIN anno_attribute AS anno_attribute0 (anno_ref, anno_name, anno_value) ON anno_attribute0.anno_ref = anno0.anno_id) ON anno0.struct_ref = struct0.struct_id ) output");
		
		if (path.getAliasSets().isEmpty()) {
			sb.append("\n");
			return sb.toString();
		}
		
		sb.append(",\n");
		
		for (AliasSet aliasSet : path.getAliasSets()) {
			sb.append("\t");
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
	
	String whereClause(Path path) {
		List<Condition> conditions = path.getConditions();
		
		if (conditions.isEmpty())
			return "";
		
		StringBuffer sb = new StringBuffer();
		sb.append("WHERE\n");
		for (Condition condition : conditions) {			
			sb.append("\t" + condition + " AND\n");
		}
		
		return sb.toString();
	}
	
	String whereClauseAliasJoins(Path path) {
		StringBuffer sb = new StringBuffer();
		for (AliasSet aliasSet : path.getAliasSets()) {
			Set<String> usedTables = aliasSet.getUsedTables();
			if (usedTables.contains("rank") && usedTables.contains("struct"))
				sb.append("\t" + Join.eq(aliasSet.getColumn("rank", "struct_ref"), aliasSet.getColumn("struct", "id")) + " AND\n");
			if (usedTables.contains("struct") && usedTables.contains("anno"))
				sb.append("\t" + Join.eq(aliasSet.getColumn("anno", "struct_ref"), aliasSet.getColumn("struct", "id")) + " AND\n");
			if (usedTables.contains("anno") && usedTables.contains("anno_attribute"))
				sb.append("\t" + Join.eq(aliasSet.getColumn("anno_attribute", "anno_ref"), aliasSet.getColumn("anno", "id")) + " AND\n");
		}
		return sb.toString();
	}

	String whereClauseOutputSelection(Path path) {
		Map<AliasSet, String> markings = path.getMarkings();
		
		if (markings.size() == 0)
			throw new RuntimeException("no alias set marked for output");
		
		if (markings.size() == 1)
			return "\toutput.pre = " + markings.keySet().iterator().next().getColumn("rank", "pre") + "\n";
		
		// sort alias sets for better readability
		List<AliasSet> aliasSets = new ArrayList<AliasSet>();
		aliasSets.addAll(markings.keySet());
		Collections.sort(aliasSets, new Comparator<AliasSet>() {

			public int compare(AliasSet o1, AliasSet o2) {
				return Integer.signum(o1.getId() - o2.getId());
			}
			
		});
		
		StringBuffer sb = new StringBuffer();

		sb.append("\t(\n");
		for (AliasSet aliasSet : aliasSets) {
			sb.append("\t\toutput.pre = " + aliasSet.getColumn("rank", "pre") + " OR\n");
		}
		sb.setLength(sb.length() - " OR\n".length());
		sb.append("\n\t)\n");
		
		return sb.toString();
	}

}
