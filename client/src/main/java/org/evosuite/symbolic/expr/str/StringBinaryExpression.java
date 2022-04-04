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
 * StringBinaryExpression class.
 * </p>
 *
 * @author krusev
 */
public final class StringBinaryExpression extends AbstractExpression<String> implements
        StringValue, BinaryExpression<String> {

    private static final long serialVersionUID = -986689442489666986L;

    protected static final Logger log = LoggerFactory.getLogger(StringBinaryExpression.class);

    private final Expression<String> left;
    private final Operator op;
    private final Expression<?> right;

    /**
     * <p>
     * Constructor for StringBinaryExpression.
     * </p>
     *
     * @param left2  a {@link org.evosuite.symbolic.expr.Expression} object.
     * @param op2    a {@link org.evosuite.symbolic.expr.Operator} object.
     * @param right2 a {@link org.evosuite.symbolic.expr.Expression} object.
     * @param con    a {@link java.lang.String} object.
     */
    public StringBinaryExpression(Expression<String> left2, Operator op2,
                                  Expression<?> right2, String con) {
        super(con, 1 + left2.getSize() + right2.getSize(),
                left2.containsSymbolicVariable() || right2.containsSymbolicVariable());
        this.left = left2;
        this.op = op2;
        this.right = right2;

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
    public Expression<String> getLeftOperand() {
        return left;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expression<?> getRightOperand() {
        return right;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(" + left + op.toString() + right + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof StringBinaryExpression) {
            StringBinaryExpression other = (StringBinaryExpression) obj;
            return this.op.equals(other.op) && this.left.equals(other.left)
                    && this.right.equals(other.right);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.left.hashCode() + this.op.hashCode() + this.right.hashCode();
    }

    @Override
    public Set<Variable<?>> getVariables() {
        Set<Variable<?>> variables = new HashSet<>();
        variables.addAll(this.left.getVariables());
        variables.addAll(this.right.getVariables());
        return variables;
    }

    @Override
    public Set<Object> getConstants() {
        Set<Object> result = new HashSet<>();
        result.addAll(this.left.getConstants());
        result.addAll(this.right.getConstants());
        return result;
    }

    @Override
    public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
        return v.visit(this, arg);
    }

}
