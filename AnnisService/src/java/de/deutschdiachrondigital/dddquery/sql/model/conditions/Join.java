package de.deutschdiachrondigital.dddquery.sql.model.conditions;

import de.deutschdiachrondigital.dddquery.sql.model.Condition;
import de.deutschdiachrondigital.dddquery.sql.model.JoinField;
import de.deutschdiachrondigital.dddquery.sql.model.Literal;



public class Join implements Condition {

	private String operator;
	private JoinField lhs;
	private JoinField rhs;
	
	public Join(String operator, JoinField lhs, JoinField rhs) {
		this.operator = operator;
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	public Join(String operator, JoinField lhs, String rhs) {
		this(operator, lhs, new Literal(rhs));
	}
	
	public Join(String operator, String lhs, JoinField rhs) {
		this(operator, new Literal(lhs), rhs);
	}
	
	public Join(String operator, String lhs, String rhs) {
		this(operator, new Literal(lhs), new Literal(rhs));
	}
	
	public static Join eq(String lhs, String rhs) {
		return new Join("=", lhs, rhs);
	}

	public static Join eq(JoinField lhs, String rhs) {
		return new Join("=", lhs, rhs);
	}

	public static Join eq(String lhs, JoinField rhs) {
		return new Join("=", lhs, rhs);
	}

	public static Join eq(JoinField lhs, JoinField rhs) {
		return new Join("=", lhs, rhs);
	}

	public static Join ne(String lhs, String rhs) {
		return new Join("!=", lhs, rhs);
	}

	public static Join ne(JoinField lhs, String rhs) {
		return new Join("!=", lhs, rhs);
	}

	public static Join ne(String lhs, JoinField rhs) {
		return new Join("!=", lhs, rhs);
	}

	public static Join ne(JoinField lhs, JoinField rhs) {
		return new Join("!=", lhs, rhs);
	}

	public static Join lt(String lhs, String rhs) {
		return new Join("<", lhs, rhs);
	}

	public static Join lt(JoinField lhs, String rhs) {
		return new Join("<", lhs, rhs);
	}

	public static Join lt(String lhs, JoinField rhs) {
		return new Join("<", lhs, rhs);
	}

	public static Join lt(JoinField lhs, JoinField rhs) {
		return new Join("<", lhs, rhs);
	}

	public static Join le(String lhs, String rhs) {
		return new Join("<=", lhs, rhs);
	}

	public static Join le(JoinField lhs, String rhs) {
		return new Join("<=", lhs, rhs);
	}

	public static Join le(String lhs, JoinField rhs) {
		return new Join("<=", lhs, rhs);
	}

	public static Join le(JoinField lhs, JoinField rhs) {
		return new Join("<=", lhs, rhs);
	}

	public static Join gt(String lhs, String rhs) {
		return new Join(">", lhs, rhs);
	}

	public static Join gt(JoinField lhs, String rhs) {
		return new Join(">", lhs, rhs);
	}

	public static Join gt(String lhs, JoinField rhs) {
		return new Join(">", lhs, rhs);
	}

	public static Join gt(JoinField lhs, JoinField rhs) {
		return new Join(">", lhs, rhs);
	}

	public static Join ge(String lhs, String rhs) {
		return new Join(">=", lhs, rhs);
	}
	
	public static Join ge(JoinField lhs, String rhs) {
		return new Join(">=", lhs, rhs);
	}
	
	public static Join ge(String lhs, JoinField rhs) {
		return new Join(">=", lhs, rhs);
	}
	
	public static Join ge(JoinField lhs, JoinField rhs) {
		return new Join(">=", lhs, rhs);
	}
	
	@Override
	public String toString() {
		return lhs + " " + operator + " " + rhs;
	}
	
	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
