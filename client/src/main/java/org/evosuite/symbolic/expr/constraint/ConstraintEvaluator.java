/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with EvoSuite. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.expr.constraint;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstraintEvaluator implements ConstraintVisitor<Object, Void> {

    static Logger log = LoggerFactory.getLogger(ConstraintEvaluator.class);

    @Override
    public Object visit(IntegerConstraint n, Void arg) {

        ExpressionEvaluator visitor = new ExpressionEvaluator();
        long left = (Long) n.getLeftOperand().accept(visitor, null);
        long right = (Long) n.getRightOperand().accept(visitor, null);

        Comparator cmpr = n.getComparator();

        switch (cmpr) {
            case EQ:
                return left == right;
            case NE:
                return left != right;
            case LT:
                return left < right;
            case LE:
                return left <= right;
            case GT:
                return left > right;
            case GE:
                return left >= right;
            default:
                throw new IllegalArgumentException("unimplemented comparator");
        }
    }

    @Override
    public Object visit(RealConstraint n, Void arg) {
        ExpressionEvaluator visitor = new ExpressionEvaluator();
        double left = (Double) n.getLeftOperand().accept(visitor, null);
        double right = (Double) n.getRightOperand().accept(visitor, null);

        Comparator cmpr = n.getComparator();

        switch (cmpr) {
            case EQ:
                return left == right;
            case NE:
                return left != right;
            case LT:
                return left < right;
            case LE:
                return left <= right;
            case GT:
                return left > right;
            case GE:
                return left >= right;
            default:
                throw new IllegalArgumentException("unimplemented comparator");
        }

    }

    @Override
    public Object visit(StringConstraint n, Void arg) {

        ExpressionEvaluator visitor = new ExpressionEvaluator();
        long left = (Long) n.getLeftOperand().accept(visitor, null);
        long right = (Long) n.getRightOperand().accept(visitor, null);
        Comparator cmpr = n.getComparator();

        switch (cmpr) {
            case EQ:
                return left == right;
            case NE:
                return left != right;
            case LT:
                return left < right;
            case LE:
                return left <= right;
            case GT:
                return left > right;
            case GE:
                return left >= right;
            default:
                throw new IllegalArgumentException("unimplemented comparator");
        }

    }

}