package de.deutschdiachrondigital.dddquery.sql.old2;

import java.util.List;

import annis.dao.Match;
import annis.dao.Span;
import de.deutschdiachrondigital.dddquery.sql.old2.AnnotationRetriever.SqlGenerator;

public class AnnotationRetrieverSqlGenerator4 implements SqlGenerator {

	public String generateSql(List<Match> matches, int left, int right) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT DISTINCT\n"); 
		sb.append("	graph.key, graph.pre, graph.post, annotations.*\n");
		
		sb.append("FROM\n");            
		sb.append(tokenRelation(matches, left, right));
		sb.append(",\n");
		sb.append("	annotations\n");

		sb.append("WHERE\n");
		sb.append("	graph.struct_ref = annotations.struct\n");
		sb.append("ORDER BY graph.key, graph.pre");
		
		return sb.toString();
	}
	
	String tokenRelation(List<Match> matches, int left, int right) {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("\t(\n");
		
		for (List<Span> match : matches) {

			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			int textRef = match.get(0).getTextRef();
			
			for (Span node : match) {
				min = Math.min(min, node.getTokenLeft());
				max = Math.max(max, node.getTokenRight());
			}
			
			sb.append("\t\tSELECT * FROM ( SELECT '{");
	
			for (Span node : match) {
				sb.append(node.getStructId());
				sb.append(", ");
			}
			sb.setLength(sb.length() - ", ".length());
			sb.append("}'::numeric[] AS key ) AS key, graph_over_tokens(");
			
			sb.append(textRef);
			sb.append(", ");
			
			sb.append(min - left);
			sb.append(", ");
			
			sb.append(max + right);
			sb.append(") AS graph UNION\n");
		}
		sb.setLength(sb.length() - " UNION\n".length());
		sb.append("\n");

		sb.append("\t) AS graph");

		return sb.toString();
	}

}
