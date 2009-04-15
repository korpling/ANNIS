package de.deutschdiachrondigital.dddquery.sql.model.conditions;

import java.util.ArrayList;
import java.util.List;

import de.deutschdiachrondigital.dddquery.sql.InnerPathFormatter;
import de.deutschdiachrondigital.dddquery.sql.model.Column;
import de.deutschdiachrondigital.dddquery.sql.model.Condition;
import de.deutschdiachrondigital.dddquery.sql.model.Path;


public class OrCondition implements Condition {
	
	private Column lhs;
	private List<Path> alternatives;

	public OrCondition() {
		alternatives = new ArrayList<Path>();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(lhs);
		sb.append(" IN (\n\t\t");

		InnerPathFormatter formatter = new InnerPathFormatter();
		for (Path alternative : alternatives) {
			sb.append(formatter.format(alternative));
			sb.append("\n\n\t\tUNION ");
		}
		sb.setLength(sb.length() - "\tUNION ".length());
		sb.append(")");
		
		return sb.toString();
	}
	
	public void addAlternative(Path path) {
		alternatives.add(path);
	}

	public Column getLhs() {
		return lhs;
	}
	
	public void setLhs(Column lhs) {
		this.lhs = lhs;
	}
	
	public List<Path> getAlternatives() {
		return alternatives;
	}
	
	public void setAlternatives(List<Path> alternatives) {
		this.alternatives = alternatives;
	}

}
