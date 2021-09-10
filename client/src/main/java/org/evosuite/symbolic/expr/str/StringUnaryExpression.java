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

package org.evosuite.symbolic.expr.str;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.dse.DSEStatistics;
import org.evosuite.symbolic.expr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * StringUnaryExpression class.
 * </p>
 *
 * @author krusev
 */
public final class StringUnaryExpression extends AbstractExpression<String> implements
        StringValue, UnaryExpression<String> {

    private static final long serialVersionUID = -384874147850376188L;

    protected static final Logger log = LoggerFactory.getLogger(StringUnaryExpression.class);

    // protected int conretIntValue;

    private final Operator op;

    private final Expression<String> expr;

    /**
     * <p>
     * Constructor for StringUnaryExpression.
     * </p>
     *
     * @param param a {@link org.evosuite.symbolic.expr.Expression} object.
     * @param op2   a {@link org.evosuite.symbolic.expr.Operator} object.
     * @param con   a {@link java.lang.String} object.
     */
    public StringUnaryExpression(Expression<String> param, Operator op2, String con) {
        super(con, 1 + param.getSize(), param.containsSymbolicVariable());
        this.expr = param;
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
    public Operator getOperator() {
        return op;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expression<String> getOperand() {
        return expr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return expr + "." + op.toString().trim() + "(" + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof StringUnaryExpression) {
            StringUnaryExpression other = (StringUnaryExpression) obj;
            return this.op.equals(other.op) && this.expr.equals(other.expr);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.op.hashCode() + this.expr.hashCode();
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
