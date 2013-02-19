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

import java.io.Serializable;
import java.util.Set;

public abstract interface Expression<T extends Object> extends Serializable {

	/**
	 * <p>
	 * getParent
	 * </p>
	 * 
	 * @param <T>
	 *            a T object.
	 * @return a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public Expression<?> getParent();

	/**
	 * <p>
	 * setParent
	 * </p>
	 * 
	 * @param expr
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public void setParent(Expression<?> expr);

	/**
	 * <p>
	 * execute
	 * </p>
	 * 
	 * @return a {@link java.lang.Object} object.
	 */
	public T execute();

	/**
	 * <p>
	 * getConcreteValue
	 * </p>
	 * 
	 * @return a {@link java.lang.Object} object.
	 */
	public T getConcreteValue();

	/**
	 * <p>
	 * getSize
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getSize();

	/**
	 * Returns true iif
	 */
	public boolean containsSymbolicVariable();

	public Set<Variable<?>> getVariables();

	public Set<Object> getConstants();
}
