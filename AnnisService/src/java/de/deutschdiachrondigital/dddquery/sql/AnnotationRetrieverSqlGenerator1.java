package de.deutschdiachrondigital.dddquery.sql;

import java.util.List;

import de.deutschdiachrondigital.dddquery.sql.AnnotationRetriever.SqlGenerator;

public class AnnotationRetrieverSqlGenerator1 implements SqlGenerator {

	public String generateSql(List<Match> matches, int left, int right) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT DISTINCT\n"); 
		sb.append("	tokens.key, rank2.pre, rank2.post, annotations.*\n");
		sb.append("FROM\n");            
		
		sb.append(tokenRelation(matches, left, right));
		sb.append("\n");
		
		sb.append("	JOIN struct ON (tokens.text_ref = struct.text_ref AND tokens.min <= struct.token_count AND tokens.max >= struct.token_count)\n");
		sb.append("	JOIN rank rank1 ON (rank1.struct_ref = struct.id)\n");           
		sb.append("	JOIN rank2 rank2 ON (rank1.post > rank2.pre AND rank2.post >= rank1.post)\n");
		sb.append("	JOIN annotations ON (rank2.struct_ref = annotations.struct)\n");
		sb.append("ORDER BY tokens.key, rank2.pre");
		
		return sb.toString();
	}

	String tokenRelation(List<Match> matches, int left, int right) {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("\t(\n");
		
		for (List<Node> match : matches) {

			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			int textRef = match.get(0).getTextRef();
			
			for (Node node : match) {
				min = Math.min(min, node.getTokenLeft());
				max = Math.max(max, node.getTokenRight());
			}
			
			sb.append("\t\tSELECT '{");
	
			for (Node node : match) {
				sb.append(node.getStructId());
				sb.append(", ");
			}
			sb.setLength(sb.length() - ", ".length());
			sb.append("}'::numeric[] AS key, ");
			
			sb.append(textRef);
			sb.append(" AS text_ref, ");
			
			sb.append(min - left);
			sb.append(" AS min, ");
			
			sb.append(max + right);
			sb.append(" AS max UNION\n");
		}
		sb.setLength(sb.length() - " UNION\n".length());
		sb.append("\n");

		sb.append("\t) AS tokens");

		return sb.toString();
	}

}
