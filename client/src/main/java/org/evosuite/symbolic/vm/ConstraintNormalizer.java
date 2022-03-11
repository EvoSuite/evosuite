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
package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.bv.IntegerComparison;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.RealComparison;
import org.evosuite.symbolic.expr.bv.StringComparison;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.constraint.RealConstraint;
import org.evosuite.symbolic.expr.constraint.StringConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms an IntegerConstraint into its corresponding StringConstriant,
 * RealConstraint or IntegerConstraint
 *
 * @author galeotti
 */
public final class ConstraintNormalizer {

    static Logger log = LoggerFactory.getLogger(ConstraintNormalizer.class);

    /**
     * Transforms an IntegerConstraint into a corresponding StringConstraint,
     * RealConstraint or IntegerConstraint.
     *
     * @param c a constraint to be normalized
     * @return
     */
    public static Constraint<?> normalize(IntegerConstraint c) {

        Expression<?> left = c.getLeftOperand();
        Expression<?> right = c.getRightOperand();
        if (left instanceof StringComparison
                || right instanceof StringComparison) {
            return createStringConstraint(c);
        } else if (left instanceof RealComparison
                || right instanceof RealComparison) {
            return createRealConstraint(c);
        } else if (left instanceof IntegerComparison
                || right instanceof IntegerComparison) {
            return normalizeIntegerConstriant(c);
        }
        // return without normalization
        log.debug("Un-normalized constraint: " + c);
        return c;
    }

    private static Constraint<?> createStringConstraint(IntegerConstraint c) {

        if (c.getLeftOperand() instanceof StringComparison) {
            StringComparison string_comparison = (StringComparison) c
                    .getLeftOperand();
            @SuppressWarnings("unchecked")
            Expression<Long> number_expr = (Expression<Long>) c
                    .getRightOperand();
            IntegerConstant constant = new IntegerConstant(
                    number_expr.getConcreteValue());
            return new StringConstraint(string_comparison, c.getComparator(),
                    constant);
        } else {
            assert c.getRightOperand() instanceof StringComparison;
            StringComparison string_comparison = (StringComparison) c
                    .getRightOperand();
            @SuppressWarnings("unchecked")
            Expression<Long> number_expr = (Expression<Long>) c
                    .getLeftOperand();
            IntegerConstant constant = new IntegerConstant(
                    number_expr.getConcreteValue());
            return new StringConstraint(string_comparison, c.getComparator(),
                    constant);
        }
    }

    private static Constraint<?> createRealConstraint(IntegerConstraint c) {

        if (c.getLeftOperand() instanceof RealComparison) {

            RealComparison cmp = (RealComparison) c.getLeftOperand();
            int value = ((Number) c.getRightOperand().getConcreteValue())
                    .intValue();
            Comparator op = c.getComparator();

            Expression<Double> cmp_left = cmp.getLeftOperant();
            Expression<Double> cmp_right = cmp.getRightOperant();
            return createRealConstraint(cmp_left, op, cmp_right, value);

        } else {

            assert (c.getRightOperand() instanceof RealComparison);
            RealComparison cmp = (RealComparison) c.getRightOperand();

            Comparator op = c.getComparator();
            Comparator swap_op = op.swap();
            int value = ((Number) c.getLeftOperand().getConcreteValue())
                    .intValue();
            int swap_value = -value;
            Expression<Double> cmp_left = cmp.getLeftOperant();
            Expression<Double> cmp_right = cmp.getRightOperant();

            return createRealConstraint(cmp_left, swap_op, cmp_right,
                    swap_value);

        }

    }

    private static RealConstraint createRealConstraint(
            Expression<Double> cmp_left, Comparator op,
            Expression<Double> cmp_right, int value) {
        switch (op) {
            case EQ:
                if (value < 0) {
                    return (new RealConstraint(cmp_left, Comparator.LT, cmp_right));
                } else if (value == 0) {
                    return (new RealConstraint(cmp_left, Comparator.EQ, cmp_right));
                } else {
                    return (new RealConstraint(cmp_left, Comparator.GT, cmp_right));
                }
            case NE:
                if (value < 0) {
                    return (new RealConstraint(cmp_left, Comparator.GE, cmp_right));
                } else if (value == 0) {
                    return (new RealConstraint(cmp_left, Comparator.NE, cmp_right));
                } else {
                    return (new RealConstraint(cmp_left, Comparator.LE, cmp_right));
                }
            case LE:
                if (value < 0) {
                    return (new RealConstraint(cmp_left, Comparator.LT, cmp_right));
                } else if (value == 0) {
                    return (new RealConstraint(cmp_left, Comparator.LE, cmp_right));
                } else {
                    throw new RuntimeException("Unexpected Constraint");
                }
            case LT:
                if (value < 0) {
                    throw new RuntimeException("Unexpected Constraint");
                } else if (value == 0) {
                    return (new RealConstraint(cmp_left, Comparator.LT, cmp_right));
                } else {
                    return (new RealConstraint(cmp_left, Comparator.LE, cmp_right));
                }
            case GE:
                if (value < 0) {
                    throw new RuntimeException("Unexpected Constraint");
                } else if (value == 0) {
                    return (new RealConstraint(cmp_left, Comparator.GE, cmp_right));
                } else {
                    return (new RealConstraint(cmp_left, Comparator.GT, cmp_right));
                }
            case GT:
                if (value < 0) {
                    return (new RealConstraint(cmp_left, Comparator.GE, cmp_right));
                } else if (value == 0) {
                    return (new RealConstraint(cmp_left, Comparator.GT, cmp_right));
                } else {
                    throw new RuntimeException("Unexpected Constraint");
                }
            default:
                throw new IllegalArgumentException("Unknown operator : " + op);
        }
    }

    private static Constraint<?> normalizeIntegerConstriant(IntegerConstraint c) {

        if (c.getLeftOperand() instanceof IntegerComparison) {
            IntegerComparison cmp = (IntegerComparison) c.getLeftOperand();
            int value = ((Number) c.getRightOperand().getConcreteValue())
                    .intValue();
            Comparator op = c.getComparator();
            Expression<Long> cmp_left = cmp.getLeftOperant();
            Expression<Long> cmp_right = cmp.getRightOperant();
            return createIntegerConstraint(cmp_left, op, cmp_right, value);

        } else {
            assert (c.getRightOperand() instanceof IntegerComparison);

            IntegerComparison cmp = (IntegerComparison) c.getRightOperand();
            int value = ((Number) c.getLeftOperand().getConcreteValue())
                    .intValue();
            Comparator op = c.getComparator();
            Expression<Long> cmp_left = cmp.getLeftOperant();
            Expression<Long> cmp_right = cmp.getRightOperant();
            Comparator swap_op = op.swap();
            int swap_value = -value;
            return createIntegerConstraint(cmp_left, swap_op, cmp_right,
                    swap_value);

        }
    }

    private static IntegerConstraint createIntegerConstraint(
            Expression<Long> cmp_left, Comparator op,
            Expression<Long> cmp_right, int value) {
        switch (op) {
            case EQ:
                if (value < 0) {
                    return (new IntegerConstraint(cmp_left, Comparator.LT,
                            cmp_right));
                } else if (value == 0) {
                    return (new IntegerConstraint(cmp_left, Comparator.EQ,
                            cmp_right));
                } else {
                    return (new IntegerConstraint(cmp_left, Comparator.GT,
                            cmp_right));
                }
            case NE:
                if (value < 0) {
                    return (new IntegerConstraint(cmp_left, Comparator.GE,
                            cmp_right));
                } else if (value == 0) {
                    return (new IntegerConstraint(cmp_left, Comparator.NE,
                            cmp_right));
                } else {
                    return (new IntegerConstraint(cmp_left, Comparator.LE,
                            cmp_right));
                }
            case LE:
                if (value < 0) {
                    return (new IntegerConstraint(cmp_left, Comparator.LT,
                            cmp_right));
                } else if (value == 0) {
                    return (new IntegerConstraint(cmp_left, Comparator.LE,
                            cmp_right));
                } else {
                    throw new RuntimeException("Unexpected Constraint");
                }
            case LT:
                if (value < 0) {
                    throw new RuntimeException("Unexpected Constraint");
                } else if (value == 0) {
                    return (new IntegerConstraint(cmp_left, Comparator.LT,
                            cmp_right));
                } else {
                    return (new IntegerConstraint(cmp_left, Comparator.LE,
                            cmp_right));
                }
            case GE:
                if (value < 0) {
                    throw new RuntimeException("Unexpected Constraint");
                } else if (value == 0) {
                    return (new IntegerConstraint(cmp_left, Comparator.GE,
                            cmp_right));
                } else {
                    return (new IntegerConstraint(cmp_left, Comparator.GT,
                            cmp_right));
                }
            case GT:
                if (value < 0) {
                    return (new IntegerConstraint(cmp_left, Comparator.GE,
                            cmp_right));
                } else if (value == 0) {
                    return (new IntegerConstraint(cmp_left, Comparator.GT,
                            cmp_right));
                } else {
                    throw new RuntimeException("Unexpected Constraint");
                }
            default:
                throw new IllegalStateException("Unknown operator " + op);
        }
    }

}
