/**
 * 
 */
package de.deutschdiachrondigital.dddquery.helper;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.Column;
import de.deutschdiachrondigital.dddquery.sql.model.Condition;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Join;
import de.deutschdiachrondigital.dddquery.sql.old.PathQuery;
import de.deutschdiachrondigital.dddquery.sql.old.PathQueryTranslator;

public class AxisTest {
	
	private PathQueryTranslator translator;
	private PathQuery pathQuery;
	List<Condition> conditions;
	
	public AxisTest() {
		this.pathQuery = new PathQuery();
		this.translator = new PathQueryTranslator(pathQuery);
		conditions = new ArrayList<Condition>();
		setup();
	}

	public AxisTest setup() {
		pathQuery.newAliasSet();
		pathQuery.newAliasSet();
		return this;
	}
	
	private enum State { lhs, op, rhs, complete };
	
	private State state = State.complete;
	
	public AxisTest expect() {
		addCondition();
		return this;
	}

	private void addCondition() {
		if (state != State.complete)
			throw new RuntimeException("wrong state");
		if (lhs != null && op != null && rhs != null)
			conditions.add(new Join(op, lhs, rhs));
		state = State.lhs;
	}
	
	public AxisTest test() {
		addCondition();
		assertThat(pathQuery.getConditions(), is(conditions));
		return this;
	}
	
	private Column lhs;
	private Column rhs;
	
	public AxisTest context(String table, String column) {
		return setSide(context().getColumn(table, column));
	}

	private AxisTest setSide(Column column) {
		switch (state) {
		case lhs: lhs = column; state = State.op; return this;
		case rhs: rhs = column; state = State.complete; return this;
		default: throw new RuntimeException("wrong state");
		}
	}
	
	
	public AxisTest target(String table, String column) {
		return setSide(target().getColumn(table, column));
	}
	
	public AxisTest aliasSet(int reverseIndex, String table, String column) {
		return setSide(getAliasSet(reverseIndex).getColumn(table, column));
	}
	
	private String op;
	
	public AxisTest op(String op) {
		if (state != State.op)
			throw new RuntimeException("wrong state");
		this.op = op;
		state = State.rhs;
		return this;
	}
	
	private AliasSet target() {
		return getAliasSet(0);
	}

	private AliasSet context() {
		return getAliasSet(-1);
	}

	private AliasSet getAliasSet(int reverseIndex) {
		List<AliasSet> aliasSets = pathQuery.getAliasSets();
		return aliasSets.get(aliasSets.size() + reverseIndex - 1);
	}

	@SuppressWarnings("unchecked")
	public AxisTest runAxis(String axis) {
		try {
			Class clazz = Class.forName("de.deutschdiachrondigital.dddquery.node." + axis);
			Object instance = clazz.newInstance();
			Method method = translator.getClass().getMethod("in" + axis, clazz);
			method.invoke(translator, new Object[] { instance } );
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return this;
	}

}