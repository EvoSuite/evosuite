/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.expr;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractExpression<T> implements Expression<T> {

    private static final long serialVersionUID = 2896502683190522448L;

    private Expression<?> parent = null;

    protected T concreteValue;

    private final int size;

    public AbstractExpression(T concreteValue, int size, boolean containsSymbolicVariable) {
        this.concreteValue = concreteValue;
        this.size = size;
        this.containsSymbolicVariable = containsSymbolicVariable;
    }

    @Override
    public final int getSize() {
        return size;
    }

    /**
     * <p>
     * Getter for the field <code>parent</code>.
     * </p>
     *
     * @return a {@link org.evosuite.symbolic.expr.Expression} object.
     */
    @Override
    public final Expression<?> getParent() {
        return this.parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setParent(Expression<?> expr) {
        this.parent = expr;
    }

    private final boolean containsSymbolicVariable;

    @Override
    public final boolean containsSymbolicVariable() {
        return containsSymbolicVariable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final T getConcreteValue() {
        return concreteValue;
    }

    @Override
    public Set<Object> getConstants() {
        Set<Object> result = new HashSet<>();
        result.add(this.concreteValue);
        return result;
    }

}
