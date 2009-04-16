package annis.sqlgen;

import de.deutschdiachrondigital.dddquery.node.PComparison;

public abstract class AbstractExpression implements Expression {
	
	private Type type;
	
	public AbstractExpression(Type type) {
		this.type = type;
	}
	
	public void assign(PComparison op, Expression rhs) {
		// does this operation make sense
		if (canAssign(op, rhs))
			doAssign(op, rhs);
		
		// if not try switching sides
		else if (rhs.canAssign(op, this))
			((AbstractExpression) rhs).doAssign(op, this);
		
		// give up
		else
			throw new UnsupportedOperationException("can't assign expression of type " + rhs.getType());
	}

	public abstract boolean canAssign(PComparison op, Expression rhs);
	
	protected abstract void doAssign(PComparison op, Expression rhs);

	public Type getType() {
		return type;
	}

	public String toSql() {
		throw new UnsupportedOperationException("can't convert this expression to SQL");
	}

}
