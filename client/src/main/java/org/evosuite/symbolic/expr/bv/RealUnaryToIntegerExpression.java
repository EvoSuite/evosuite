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
package org.evosuite.symbolic.expr.bv;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.dse.DSEStatistics;
import org.evosuite.symbolic.expr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public final class RealUnaryToIntegerExpression extends AbstractExpression<Long>
        implements IntegerValue, UnaryExpression<Double> {

    private static final long serialVersionUID = 9086637495150131445L;

    protected static final Logger log = LoggerFactory.getLogger(RealUnaryToIntegerExpression.class);

    private final Operator op;

    private final Expression<Double> expr;

    /**
     * <p>
     * Constructor for RealUnaryExpression.
     * </p>
     *
     * @param e   a {@link org.evosuite.symbolic.expr.Expression} object.
     * @param op2 a {@link org.evosuite.symbolic.expr.Operator} object.
     * @param con a {@link java.lang.Double} object.
     */
    public RealUnaryToIntegerExpression(Expression<Double> e, Operator op2, Long con) {
        super(con, 1 + e.getSize(), e.containsSymbolicVariable());
        this.expr = e;
        this.op = op2;

        if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
            DSEStatistics.getInstance().reportConstraintTooLong(getSize());
            throw new ConstraintTooLongException(getSize());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expression<Double> getOperand() {
        return expr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Operator getOperator() {
        return op;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return op.toString() + "(" + expr + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RealUnaryToIntegerExpression) {
            RealUnaryToIntegerExpression v = (RealUnaryToIntegerExpression) obj;
            return this.op.equals(v.op) && this.getSize() == v.getSize()
                    && this.expr.equals(v.expr);
        }
        return false;
    }

    @Override
    public Set<Variable<?>> getVariables() {
        Set<Variable<?>> variables = new HashSet<>(this.expr.getVariables());
        return variables;
    }

    @Override
    public Set<Object> getConstants() {
        return expr.getConstants();
    }

    @Override
    public int hashCode() {
        return this.op.hashCode() + this.getSize() + this.expr.hashCode();
    }

    @Override
    public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
        return v.visit(this, arg);
    }
}
