
/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.symbolic.expr;
public abstract class IntegerExpression implements Expression<Long> {

	private static final long serialVersionUID = 2896502683190522448L;

	protected int hash = 0;

	
	private Expression<?> parent = null;
	
	/**
	 * <p>Getter for the field <code>parent</code>.</p>
	 *
	 * @return a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public Expression<?> getParent() {
		return this.parent;
	}
	
	/** {@inheritDoc} */
	public void setParent(Expression<?> expr) {
		this.parent = expr;
	}

	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (hash == 0) {
			if (this.getConcreteValue() != null) {
				hash = this.getConcreteValue().hashCode();
			} else {
				hash = 1;
			}
		}
		return hash;

	}

}
