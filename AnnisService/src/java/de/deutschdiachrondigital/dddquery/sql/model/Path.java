package de.deutschdiachrondigital.dddquery.sql.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class Path {
	
	private String input;
	private List<AliasSet> aliasSets;
	private List<Condition> conditions;
	private Map<AliasSet, String> markings;
	
	public Path() {
		aliasSets = new ArrayList<AliasSet>();
		conditions = new ArrayList<Condition>();
		markings = new HashMap<AliasSet, String>();
	}
	
	public void setInput(String input) {
		this.input = input;
	}

	public String getInput() {
		return input;
	}
	
	@Override
	public String toString() {
		return input;
	}
	
	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public void addCondition(Condition condition) {
		conditions.add(condition);
	}

	public List<AliasSet> getAliasSets() {
		return aliasSets;
	}

	public void setAliasSets(List<AliasSet> aliasSets) {
		this.aliasSets = aliasSets;
	}

	public void addAliasSet(AliasSet aliasSet) {
		aliasSets.add(aliasSet);
	}

	public Map<AliasSet, String> getMarkings() {
		return markings;
	}

	public void setMarkings(Map<AliasSet, String> markings) {
		this.markings = markings;
	}

	public void markAliasSet(AliasSet aliasSet, String marker) {
		markings.put(aliasSet, marker);
	}

	
	public void merge(Path path) {
		aliasSets.addAll(path.getAliasSets());
		conditions.addAll(path.getConditions());
		markings.putAll(path.getMarkings());
	}


}
