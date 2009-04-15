package de.deutschdiachrondigital.dddquery.sql;

import java.util.HashMap;
import java.util.Stack;

import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;


public class AliasSetProvider {
	
	private Stack<HashMap<String, AliasSet>> boundSetsStack;
	private int count;
	
	private HashMap<String, AliasSet> boundSets;
	
	public AliasSetProvider() {
		reset();
	}

	public void reset() {
		count = 0;
		boundSets = new HashMap<String, AliasSet>();
		boundSetsStack = new Stack<HashMap<String,AliasSet>>();
	}

	public AliasSet getAliasSet() {
		return newAliasSet();
	}
	
	public AliasSet getAliasSet(String name) {
		if ( ! isBound(name) )
			boundSets.put(name, newAliasSet());

		return boundSets.get(name);
	}

	public boolean isBound(String name) {
		return boundSets.containsKey(name);
	}

	@SuppressWarnings("unchecked")
	public void startUnion() {
		boundSetsStack.push((HashMap<String, AliasSet>) boundSets.clone());
	}

	@SuppressWarnings("unchecked")
	public void newAlternative() {
		if (boundSetsStack.isEmpty())
			return;
		boundSets = (HashMap<String, AliasSet>) boundSetsStack.peek().clone();
	}

	public void endUnion() {
		if ( ! isInUnion() )
			throw new RuntimeException("start union must be called before endUnion");
		boundSets = boundSetsStack.pop();
	}

	private boolean isInUnion() {
		return ! boundSetsStack.isEmpty();
	}

	private AliasSet newAliasSet() {
		return new AliasSet(++count);
	}

}
