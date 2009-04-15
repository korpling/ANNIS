package de.deutschdiachrondigital.dddquery.sql.model.conditions;



public class Alternative extends CompoundCondition {

	@Override
	protected String operator() {
		return "OR";
	}
	
}
