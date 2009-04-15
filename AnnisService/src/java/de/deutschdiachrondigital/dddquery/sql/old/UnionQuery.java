package de.deutschdiachrondigital.dddquery.sql.old;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class UnionQuery extends PathQuery {

	private List<PathQuery> alternatives;
	
	public UnionQuery() {
		alternatives = new ArrayList<PathQuery>();
	}
	
	public void addAlternative(PathQuery pathQuery) {
		alternatives.add(pathQuery);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		for (PathQuery alternative : alternatives) {
			sb.append("(\n\n");
			sb.append(alternative);
			sb.setLength(sb.length() - ";\n".length());
			sb.append("\n\n) UNION ");
		}
		sb.setLength(sb.length() - " UNION ".length());
		sb.append(";\n");
		
		return sb.toString();
	}
	
}
