package de.deutschdiachrondigital.dddquery.sql.model.conditions;



public class Conjunction extends CompoundCondition {

	@Override
	protected String operator() {
		return "AND";
	}

}
