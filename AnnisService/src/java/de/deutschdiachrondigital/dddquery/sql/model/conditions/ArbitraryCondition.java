/**
 * 
 */
package de.deutschdiachrondigital.dddquery.sql.model.conditions;

import de.deutschdiachrondigital.dddquery.sql.model.Condition;


public class ArbitraryCondition implements Condition {
	
	private String condition;
	
	public ArbitraryCondition(String condition) {
		this.condition = condition;
	}
	
	@Override
	public String toString() {
		return condition;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( ! (obj instanceof ArbitraryCondition) )
			return false;
		
		ArbitraryCondition c = (ArbitraryCondition) obj;
		return condition.equals(c.condition);
	}
	
	@Override
	public int hashCode() {
		return condition.hashCode();
	}
}