package de.deutschdiachrondigital.dddquery.sql.model.conditions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import de.deutschdiachrondigital.dddquery.sql.model.Condition;


public abstract class CompoundCondition implements Condition {

	private static int indent = 1;
	
	private List<Condition> conditions;

	public CompoundCondition() {
		conditions = new ArrayList<Condition>();
	}

	protected abstract String operator();

	public CompoundCondition addCondition(Condition condition) {
		conditions.add(condition);
		return this;
	}

	public String sqlString() {
		if (conditions.size() == 0)
			throw new RuntimeException("need at least one condition in " + operator() + " statement");
		
		String operator = " " + operator() + " ";
		
		StringBuffer sb = new StringBuffer();
		sb.append("(\n");
		++indent;
		for (Condition condition : conditions) {
			for (int i = 0; i < indent; ++i)
				sb.append("\t");
			sb.append(condition);
			sb.append(operator);
			sb.append("\n");
		}
		sb.setLength(sb.length() - operator.length() - "\n".length());
		sb.append("\n");
		--indent;
		for (int i = 0; i < indent; ++i)
			sb.append("\t");
		sb.append(")");
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		try {
			return sqlString();
		} catch (RuntimeException e) {
			return "empty " + operator() + " statement";
		}
	}

	@Override
	public boolean equals(Object obj) {
		if ( ! (obj instanceof CompoundCondition) )
			return false;
		
		CompoundCondition c = (CompoundCondition) obj;
		
		return new EqualsBuilder()
			.append(this.operator(), c.operator())
			.append(this.conditions, c.conditions)
			.isEquals();
	}
	
	public int hashCode() {
		return new HashCodeBuilder().append(conditions).toHashCode();
	}

	
}