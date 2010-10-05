/**
 * 
 */
package de.deutschdiachrondigital.dddquery.parser;

import de.deutschdiachrondigital.dddquery.node.PComparison;


interface Expression {
	
	public enum Type {
		STRING,
		REGEXP,
		SPAN,
		ATTRIBUTE
	}
	
	public Type getType();
  
  @Deprecated
	public boolean canAssign(PComparison op, Expression rhs);
  @Deprecated
	public void assign(PComparison op, Expression rhs);
	public String toSql();
}