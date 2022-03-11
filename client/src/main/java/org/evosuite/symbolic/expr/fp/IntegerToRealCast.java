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
package org.evosuite.symbolic.expr.fp;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.dse.DSEStatistics;
import org.evosuite.symbolic.expr.*;
import org.evosuite.symbolic.expr.bv.IntegerValue;

import java.util.HashSet;
import java.util.Set;

public final class IntegerToRealCast extends AbstractExpression<Double> implements
        RealValue, Cast<Long> {
    private static final long serialVersionUID = -3070453617714122236L;

    private final IntegerValue expr;

    /**
     * <p>
     * Constructor for IntegerToRealCast.
     * </p>
     *
     * @param _expr      a {@link org.evosuite.symbolic.expr.Expression} object.
     * @param _concValue a {@link java.lang.Double} object.
     */
    public IntegerToRealCast(IntegerValue _expr, Double _concValue) {
        super(_concValue, 1 + _expr.getSize(), _expr.containsSymbolicVariable());
        this.expr = _expr;

        if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
            DSEStatistics.getInstance().reportConstraintTooLong(getSize());
            throw new ConstraintTooLongException(getSize());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expression<Long> getArgument() {
        return expr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "((REAL)" + expr + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof IntegerToRealCast) {
            IntegerToRealCast other = (IntegerToRealCast) obj;
            return this.expr.equals(other.expr);
            // && this.getSize() == other.getSize();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.expr.hashCode();
    }

    @Override
    public Set<Variable<?>> getVariables() {
        Set<Variable<?>> variables = new HashSet<>(this.expr.getVariables());
        return variables;
    }

    @Override
    public Set<Object> getConstants() {
        return this.expr.getConstants();
    }

    @Override
    public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
        return v.visit(this, arg);
    }
}
