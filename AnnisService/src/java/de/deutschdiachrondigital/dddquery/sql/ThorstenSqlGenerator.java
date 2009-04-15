package de.deutschdiachrondigital.dddquery.sql;

import de.deutschdiachrondigital.dddquery.sql.model.Graph;
import de.deutschdiachrondigital.dddquery.sql.model.Path;


public class ThorstenSqlGenerator {

	PathSqlGenerator pathSqlGenerator;
	
	public String translate(Graph graph) {
		StringBuffer sb = new StringBuffer();
		
		for (Path path : graph.getAlternatives()) {
			sb.append(pathSqlGenerator.format(path));
			sb.append("\n");
			sb.append("UNION ");
		}
		sb.setLength(sb.length() - "UNION ".length());
		sb.append("ORDER BY pre;\n");
		
		return sb.toString();
	}
	
	public PathSqlGenerator getPathSqlGenerator() {
		return pathSqlGenerator;
	}

	public void setPathSqlGenerator(PathSqlGenerator formatter) {
		this.pathSqlGenerator = formatter;
	}
	
}
