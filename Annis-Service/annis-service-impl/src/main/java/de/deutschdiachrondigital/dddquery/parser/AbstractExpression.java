/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.deutschdiachrondigital.dddquery.parser;

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
