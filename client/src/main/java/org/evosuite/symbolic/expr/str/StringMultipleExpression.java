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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * StringMultipleExpression class.
 * </p>
 *
 * @author krusev
 */
public final class StringMultipleExpression extends AbstractExpression<String> implements
        StringValue, MultipleExpression<String> {

    private static final long serialVersionUID = 7172041118401792672L;

    private final ArrayList<Expression<?>> other_v;

    private final Operator op;

    private final Expression<String> left;

    private final Expression<?> right;

    protected static final Logger log = LoggerFactory.getLogger(StringMultipleExpression.class);

    /**
     * <p>
     * Constructor for StringMultipleExpression.
     * </p>
     *
     * @param _left  a {@link org.evosuite.symbolic.expr.Expression} object.
     * @param _op    a {@link org.evosuite.symbolic.expr.Operator} object.
     * @param _right a {@link org.evosuite.symbolic.expr.Expression} object.
     * @param _other a {@link java.util.ArrayList} object.
     * @param con    a {@link java.lang.String} object.
     */
    public StringMultipleExpression(Expression<String> _left, Operator _op,
                                    Expression<?> _right, ArrayList<Expression<?>> _other, String con) {
        super(con, 1 + _left.getSize() + _right.getSize() + countSizes(_other),
                _left.containsSymbolicVariable() || _right.containsSymbolicVariable()
                        || containsSymbolicVariable(_other));

        this.op = _op;
        this.left = _left;
        this.right = _right;
        this.other_v = _other;

        if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
            DSEStatistics.getInstance().reportConstraintTooLong(getSize());
            throw new ConstraintTooLongException(getSize());
        }
    }

    /**
     * <p>
     * getOther
     * </p>
     *
     * @return the other
     */
    @Override
    public ArrayList<Expression<?>> getOther() {
        return other_v;
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
        String str_other_v = "";
        for (Expression<?> expression : this.other_v) {
            str_other_v += " " + expression.toString();
        }

        return "(" + left + op.toString() + (right == null ? "" : right) + str_other_v
                + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof StringMultipleExpression) {
            StringMultipleExpression other = (StringMultipleExpression) obj;

            return this.op.equals(other.op) && this.left.equals(other.left)
                    && this.right.equals(other.right)
                    && this.other_v.equals(other.other_v);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.op.hashCode() + this.left.hashCode() + this.right.hashCode()
                + this.other_v.hashCode();
    }

    private static boolean containsSymbolicVariable(ArrayList<Expression<?>> list) {

        for (Expression<?> e : list) {
            if (e.containsSymbolicVariable()) {
                return true;
            }
        }

        return false;
    }

    private static int countSizes(ArrayList<Expression<?>> list) {
        int retVal = 0;
        for (Expression<?> e : list) {
            retVal += e.getSize();
        }
        return retVal;
    }

    @Override
    public Set<Variable<?>> getVariables() {
        Set<Variable<?>> variables = new HashSet<>();
        variables.addAll(this.left.getVariables());
        variables.addAll(this.right.getVariables());
        for (Expression<?> other_e : this.other_v) {
            variables.addAll(other_e.getVariables());
        }
        return variables;
    }

    @Override
    public Set<Object> getConstants() {
        Set<Object> result = new HashSet<>();
        result.addAll(this.left.getConstants());
        result.addAll(this.right.getConstants());
        for (Expression<?> other_e : this.other_v) {
            result.addAll(other_e.getConstants());
        }
        return result;
    }

    @Override
    public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
        return v.visit(this, arg);
    }

}
