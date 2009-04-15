package de.deutschdiachrondigital.dddquery.sql;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.Condition;
import de.deutschdiachrondigital.dddquery.sql.model.Graph;
import de.deutschdiachrondigital.dddquery.sql.model.Path;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.ArbitraryCondition;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Join;


public class SqlGenerator {

	private Logger log = Logger.getLogger(this.getClass());
	
	private PathTranslator4 pathTranslator;
	
	public String translate(List<Long> corpora, Graph graph) {
		StringBuffer sb = new StringBuffer();
		
		int elements = 0;
		for (Path path : graph.getAlternatives()) {
			Map<AliasSet, String> markings = path.getMarkings();
			List<AliasSet> aliasSets = path.getAliasSets();
			int i = 0;
			for (AliasSet aliasSet : aliasSets)
				if (markings.containsKey(aliasSet))
					++i;
			if (i > elements)
				elements = i;
		}
		
		for (Path path : graph.getAlternatives()) {
			sb.append(translatePath(corpora, path, elements));
			sb.append("\n");
			sb.append("UNION ");
		}
		sb.setLength(sb.length() - "UNION ".length());
		
		String sql = sb.toString();
		
		log.debug("SQL query is:\n" + sql);
		
		return sql;
	}
	
	// FIXME: test
	String translatePath(List<Long> corpora, Path path, int elements) {
		StringBuffer sb = new StringBuffer();
		sb.append(selectClause(path, elements));
		sb.append(fromClause(path));
		sb.append(whereClause(corpora, path));
		return sb.toString();
	}

	String selectClause(Path path, int elements) {
		StringBuffer sb = new StringBuffer();
		
//		sb.append("SET statement_timeout to 60000;\n");
		sb.append("SELECT DISTINCT");
		
		Map<AliasSet, String> markings = path.getMarkings();
		int thisClause = 0;
		for (AliasSet aliasSet : path.getAliasSets()) {
			if ( ! markings.containsKey(aliasSet) )
				continue;

			String marker = markings.get(aliasSet);

			sb.append("\n\t");
			sb.append(aliasSet.getColumn(pathTranslator.getStructTable(), "id"));
			if ( ! marker.equals("") ) {
				sb.append(" AS ");
				sb.append(marker);
			}
			sb.append(", ");
			
			String[] fields = { "text_ref", "left_token", "right_token" };
			for (String field : fields) {
				sb.append(aliasSet.getColumn(pathTranslator.getStructTable(), field));
				sb.append(", ");
			}
			++thisClause;
		}
		for (int i = thisClause; i < elements; ++i)
			sb.append("\n\tNULL::numeric, NULL::numeric, NULL::numeric, NULL::numeric, ");
		
		sb.setLength(sb.length() - ", ".length());
		sb.append("\n");
		
		
		return sb.toString();
	}

	String fromClause(Path path) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("FROM\n");
		
		for (AliasSet aliasSet : path.getAliasSets()) {
			
			// skip alias sets with no used tables
			if (aliasSet.getUsedTables().isEmpty())
				continue;
			
			if (aliasSet.usesTable("anno_attribute")) {
				aliasSet.useTable("anno");
				aliasSet.useTable("struct");
			}
			
//			if (aliasSet.usesTable(pathTranslator.getStructTable()))
//				aliasSet.useTable("documents");
			
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

	String whereClause(List<Long> corpora, Path path) {
		List<Condition> conditions = path.getConditions();
		
		for (AliasSet aliasSet : path.getAliasSets()) {
			conditions.addAll(missingJoins(corpora, aliasSet));
		}
			
		if (conditions.isEmpty())
			return "";
		
		StringBuffer sb = new StringBuffer();
		sb.append("WHERE\n");
		for (Condition condition : conditions) {			
			sb.append("\t" + condition + " AND\n");
		}
		sb.setLength(sb.length() - " AND\n".length());
		sb.append("\n");
		
		return sb.toString();
	}
	
	Set<Condition> missingJoins(List<Long> corpora, AliasSet aliasSet) {
		Set<Condition> missing = new HashSet<Condition>();
		
		String structTable = pathTranslator.getStructTable();
		if (aliasSet.usesTable(structTable)) {
			StringBuffer docConstraint = new StringBuffer();
			docConstraint.append(aliasSet.getColumn(structTable, "doc_ref"));
			docConstraint.append(" IN ( SELECT DISTINCT doc_id FROM doc_2_corp d, corpus c1, corpus c2 WHERE d.corpus_ref = c1.id AND c1.pre >= c2.pre AND c1.post <= c2.post AND c2.id in ( ");
//			docConstraint.append(aliasSet.getColumn("documents", "corpus_id"));
//			docConstraint.append(" IN ( ");
			for (Long doc : corpora) {
				docConstraint.append(doc);
				docConstraint.append(", ");
			}
			docConstraint.setLength(docConstraint.length() - ", ".length());
//			docConstraint.append(" )");
			docConstraint.append(" ) )");
			missing.add(new ArbitraryCondition(docConstraint.toString()));
//			missing.add(Join.eq(aliasSet.getColumn(structTable, "doc_ref"), aliasSet.getColumn("documents", "doc_id")));
		}
		
//		if (aliasSet.usesTable("struct") && aliasSet.usesTable("annos"))
//			missing.add(Join.eq(aliasSet.getColumn("annos", "struct_ref"), aliasSet.getColumn("struct", "id")));
//		
//		if (aliasSet.usesTable("rank_struct") && aliasSet.usesTable("annos"))
//			missing.add(Join.eq(aliasSet.getColumn("annos", "struct_ref"), aliasSet.getColumn("rank_struct", "id")));
//		
//		if (aliasSet.usesTable("rank") && aliasSet.usesTable("annotations"))
//			missing.add(Join.eq(aliasSet.getColumn("rank", "struct_ref"), aliasSet.getColumn("annotations", "struct")));
		
		String struct = structTable;
		String rank = pathTranslator.getRankTable();
		String rankAnno = pathTranslator.getRankAnnoTable();
		String anno = pathTranslator.getAnnoTable();
		String annoAttribute = pathTranslator.getAnnoAttributeTable();
		
		aliasSet.useTable(struct);
		
//		if (aliasSet.usesTable(rankAnno))
//			aliasSet.useTable(rank);
		if (aliasSet.usesTable(annoAttribute))
			aliasSet.useTable(anno);
		
		if (aliasSet.usesTable(rankAnno) && aliasSet.usesTable(rank) && ( ! rankAnno.equals(rank) ))
			missing.add(Join.eq(aliasSet.getColumn(rankAnno, "rank_ref"), aliasSet.getColumn(rank, "pre")));
		if (aliasSet.usesTable(rank) && aliasSet.usesTable(struct) && ( ! rank.equals(struct) ))
			missing.add(Join.eq(aliasSet.getColumn(rank, "struct_ref"), aliasSet.getColumn(struct, "id")));
		if (aliasSet.usesTable(anno) && aliasSet.usesTable(struct) && ( ! anno.equals(struct) ))
			missing.add(Join.eq(aliasSet.getColumn(anno, "struct_ref"), aliasSet.getColumn(struct, "id")));
		if (aliasSet.usesTable(annoAttribute) && aliasSet.usesTable(anno) && ( ! annoAttribute.equals(anno) ))
			missing.add(Join.eq(aliasSet.getColumn(annoAttribute, "anno_ref"), aliasSet.getColumn(anno, "id")));
		
		
		
//		if (aliasSet.usesTable("rank_anno"))
//			missing.add(Join.eq(aliasSet.getColumn("rank_anno", "rank_ref"), aliasSet.getColumn("rank", "pre")));
//		if ( aliasSet.usesTable("rank") && aliasSet.usesTable("struct") )
//			missing.add(Join.eq(aliasSet.getColumn("rank", "struct_ref"), aliasSet.getColumn("struct", "id")));
//		if ( aliasSet.usesTable("struct") && aliasSet.usesTable("anno") )
//			missing.add(Join.eq(aliasSet.getColumn("anno", "struct_ref"), aliasSet.getColumn("struct", "id")));
//		if ( aliasSet.usesTable("anno") && aliasSet.usesTable("anno_attribute") )
//			missing.add(Join.eq(aliasSet.getColumn("anno_attribute", "anno_ref"), aliasSet.getColumn("anno", "id")));
		
		return missing;
//		return new ArrayList<Condition>();
	}

	public PathTranslator4 getPathTranslator() {
		return pathTranslator;
	}

	public void setPathTranslator(PathTranslator4 pathTranslator) {
		this.pathTranslator = pathTranslator;
	}

}
