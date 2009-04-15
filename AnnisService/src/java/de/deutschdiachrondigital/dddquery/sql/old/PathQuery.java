package de.deutschdiachrondigital.dddquery.sql.old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.deutschdiachrondigital.dddquery.helper.Ast2String;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.sql.PathSqlGenerator;
import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.Column;
import de.deutschdiachrondigital.dddquery.sql.model.Condition;

@Deprecated
public class PathQuery {
	
	public enum TextValue { Element, Attribute };

	private List<Condition> conditions;
	private List<AliasSet> aliasSets;
	private AliasSet parentAliasSet;
	private AliasSet contextAliasSet;
	private AliasSet targetAliasSet;
	private Map<AliasSet, String> markings;
	private TextValue textValue;
	private String input;
	
	public PathQuery(Node node) {
		this((AliasSet) null);
		Ast2String ast2String = new Ast2String();
		node.apply(ast2String);
		input = ast2String.getResult();
	}
	
	public PathQuery() {
		this((AliasSet) null);
	}
	
	public PathQuery(AliasSet parent) {
		parentAliasSet = parent;
		aliasSets = new ArrayList<AliasSet>();
		markings = new HashMap<AliasSet, String>();
		conditions = new ArrayList<Condition>();
		textValue = TextValue.Element;
	}

	@Override
	public String toString() {
		return new PathSqlGenerator().format(null);
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

	public AliasSet getContextAliasSet() {
		return contextAliasSet;
	}

	public void setContextAliasSet(AliasSet contextAliasSet) {
		this.contextAliasSet = contextAliasSet;
	}

	public AliasSet getTargetAliasSet() {
		return targetAliasSet;
	}

	public void setTargetAliasSet(AliasSet targetAliasSet) {
		this.targetAliasSet = targetAliasSet;
	}
	
	public AliasSet newAliasSet() {
		return newAliasSet(null);
	}
	
	public AliasSet newAliasSet(AliasSet target) {
		contextAliasSet = targetAliasSet;
		targetAliasSet = target == null ? new AliasSet(aliasSets.size() + 1) : target;
		if ( ! aliasSets.contains(targetAliasSet) )
			aliasSets.add(targetAliasSet);
		return targetAliasSet;
	}

	public void markTargetAliasSet(String mark) {
		markings.put(targetAliasSet, mark == null ? "" : mark);
	}

	public Column contextAlias(String table, String column) {
		return getContextAliasSet().getColumn(table, column);
	}

	public Column targetAlias(String table, String column) {
		return getTargetAliasSet().getColumn(table, column);
	}

	public Map<AliasSet, String> getMarkings() {
		return markings;
	}

	public void setMarkings(Map<AliasSet, String> markings) {
		this.markings = markings;
	}

	public TextValue getTextValue() {
		return textValue;
	}

	public void setTextValue(TextValue textValue) {
		this.textValue = textValue;
	}

	public void merge(PathQuery toMerge) {
		int offset = aliasSets.size();
		for (AliasSet aliasSet : toMerge.getAliasSets()) {
			if (aliasSets.contains(aliasSet))
				--offset;
			else {
				aliasSet.setId(aliasSet.getId() + offset);
				aliasSets.add(aliasSet);
			}
		}
		
		for (Entry<AliasSet, String> mark : toMerge.getMarkings().entrySet())
			if ( ! markings.containsKey(mark.getKey()))
				markings.put(mark.getKey(), mark.getValue());
		
		for (Condition condition : toMerge.getConditions())
			if ( ! conditions.contains(condition) )
				conditions.add(condition);
	}

	public AliasSet getParentAliasSet() {
		return parentAliasSet;
	}

	public void setParentAliasSet(AliasSet parentAliasSet) {
		this.parentAliasSet = parentAliasSet;
	}
	
	public void addAliasSet(AliasSet aliasSet) {
		if (aliasSets.contains(aliasSet))
				return;
		aliasSets.add(aliasSet);
	}

	public String getInput() {
		return input;
	}
	
}
