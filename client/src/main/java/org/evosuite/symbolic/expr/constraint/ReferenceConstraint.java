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
package org.evosuite.symbolic.expr.constraint;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.dse.DSEStatistics;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a constraint of references.
 *
 * It could be either "equals" or "not equals", which should be created
 * through {@link org.evosuite.symbolic.vm.ConstraintFactory}.
 *
 * @author Ignacio Lebrero
 */
public class ReferenceConstraint extends Constraint<Object> {

    static Logger log = LoggerFactory.getLogger(ReferenceConstraint.class);

    private static final long serialVersionUID = 4018615724972375366L;

    private final Comparator cmp;
    private final Expression<Object> left;
    private final Expression<Object> right;

    /**
     * <p>
     * Constructor for ReferenceConstraint.
     * </p>
     *
     * @param left  a {@link org.evosuite.symbolic.expr.Expression} object.
     * @param cmp   a {@link org.evosuite.symbolic.expr.Comparator} object.
     * @param right a {@link org.evosuite.symbolic.expr.Expression} object.
     */
    public ReferenceConstraint(Expression<Object> left, Comparator cmp,
                               Expression<Object> right) {
        super();

        this.cmp = cmp;
        this.left = left;
        this.right = right;

        if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
            DSEStatistics.getInstance().reportConstraintTooLong(getSize());
            throw new ConstraintTooLongException(getSize());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparator getComparator() {
        return cmp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expression<Object> getLeftOperand() {
        return left;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expression<Object> getRightOperand() {
        return right;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return left + cmp.toString() + right;
    }

    @Override
    public Constraint<Object> negate() {
        return new ReferenceConstraint(this.left, this.cmp.not(), this.right);
    }

    @Override
    public <K, V> K accept(ConstraintVisitor<K, V> v, V arg) {
        return v.visit(this, arg);
    }

}