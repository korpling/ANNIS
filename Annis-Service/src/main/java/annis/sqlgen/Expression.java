/**
 * 
 */
package annis.sqlgen;

import de.deutschdiachrondigital.dddquery.node.PComparison;

interface Expression {
	
	public enum Type {
		STRING,
		REGEXP,
		SPAN,
		ATTRIBUTE
	}
	
	public Type getType();
	public boolean canAssign(PComparison op, Expression rhs);
	public void assign(PComparison op, Expression rhs);
	public String toSql();
}