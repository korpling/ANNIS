package de.deutschdiachrondigital.dddquery.sql.model.conditions;

import de.deutschdiachrondigital.dddquery.sql.model.Condition;
import de.deutschdiachrondigital.dddquery.sql.model.JoinField;
import de.deutschdiachrondigital.dddquery.sql.model.Literal;


public class IsNullCondition implements Condition {

	private JoinField field;
	
	public IsNullCondition(JoinField column) {
		this.field = column;
	}

	public IsNullCondition(String field) {
		this(new Literal(field));
	}

	@Override
	public String toString() {
		return field + " IS NULL";
	}
	
	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}
	
	@Override
	public int hashCode() {
		return field.hashCode();
	}

}
