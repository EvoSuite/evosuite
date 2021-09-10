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
package org.evosuite.symbolic.solver.z3;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringComparison;
import org.evosuite.symbolic.expr.constraint.ConstraintVisitor;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.constraint.RealConstraint;
import org.evosuite.symbolic.expr.constraint.StringConstraint;
import org.evosuite.symbolic.solver.SmtExprBuilder;
import org.evosuite.symbolic.solver.smt.ExprToSmtVisitor;
import org.evosuite.symbolic.solver.smt.SmtExpr;

class ConstraintToZ3Visitor implements ConstraintVisitor<SmtExpr, Void> {

    public ConstraintToZ3Visitor() {
    }

    @Override
    public SmtExpr visit(IntegerConstraint c, Void arg) {
        Expression<?> left = c.getLeftOperand();
        Comparator cmp = c.getComparator();
        Expression<?> right = c.getRightOperand();

        SmtExpr equalsExpr = translateCompareTo(left, cmp, right);
        if (equalsExpr != null) {
            return equalsExpr;
        }

        ExprToSmtVisitor v = new ExprToSmtVisitor();
        SmtExpr leftExpr = left.accept(v, null);
        SmtExpr rightExpr = right.accept(v, null);

        if (leftExpr == null || rightExpr == null) {
            return null;
        }

        return mkComparison(leftExpr, cmp, rightExpr);
    }

    private static SmtExpr translateCompareTo(Expression<?> left, Comparator cmp, Expression<?> right) {

        if (!(left instanceof StringBinaryToIntegerExpression)) {
            return null;
        }
        if (!(right instanceof IntegerConstant)) {
            return null;
        }
        if (cmp != Comparator.NE && cmp != Comparator.EQ) {
            return null;
        }

        StringBinaryToIntegerExpression leftExpr = (StringBinaryToIntegerExpression) left;
        if (leftExpr.getOperator() != Operator.COMPARETO) {
            return null;
        }

        IntegerConstant rightExpr = (IntegerConstant) right;
        if (rightExpr.getConcreteValue() != 0) {
            return null;
        }

        ExprToSmtVisitor v = new ExprToSmtVisitor();
        SmtExpr leftEquals = leftExpr.getLeftOperand().accept(v, null);
        SmtExpr rightEquals = leftExpr.getRightOperand().accept(v, null);

        if (leftEquals == null || rightEquals == null) {
            return null;
        }

        SmtExpr eqExpr = SmtExprBuilder.mkEq(leftEquals, rightEquals);
        if (cmp == Comparator.EQ) {
            return eqExpr;
        } else {
            return SmtExprBuilder.mkNot(eqExpr);
        }
    }

    private static SmtExpr mkComparison(SmtExpr left, Comparator cmp, SmtExpr right) {
        switch (cmp) {
            case LT: {
                SmtExpr lt = SmtExprBuilder.mkLt(left, right);
                return lt;
            }
            case LE: {
                SmtExpr le = SmtExprBuilder.mkLe(left, right);
                return le;
            }
            case GT: {
                SmtExpr gt = SmtExprBuilder.mkGt(left, right);
                return gt;
            }
            case GE: {
                SmtExpr ge = SmtExprBuilder.mkGe(left, right);
                return ge;
            }
            case EQ: {
                SmtExpr ge = SmtExprBuilder.mkEq(left, right);
                return ge;
            }
            case NE: {
                SmtExpr ge = SmtExprBuilder.mkEq(left, right);
                SmtExpr ne = SmtExprBuilder.mkNot(ge);
                return ne;
            }
            default: {
                throw new RuntimeException("Unknown comparator for constraint " + cmp);
            }
        }
    }

    @Override
    public SmtExpr visit(RealConstraint c, Void arg) {
        ExprToSmtVisitor v = new ExprToSmtVisitor();

        SmtExpr left = c.getLeftOperand().accept(v, null);
        SmtExpr right = c.getRightOperand().accept(v, null);

        if (left == null || right == null) {
            return null;
        }

        Comparator cmp = c.getComparator();
        SmtExpr boolExpr = mkComparison(left, cmp, right);
        return boolExpr;
    }

    @Override
    public SmtExpr visit(StringConstraint c, Void arg) {
        ExprToSmtVisitor v = new ExprToSmtVisitor();

        StringComparison stringComparison = (StringComparison) c.getLeftOperand();
        Comparator cmp = c.getComparator();
        IntegerConstant integerConstant = (IntegerConstant) c.getRightOperand();

        SmtExpr left = stringComparison.accept(v, null);
        SmtExpr right = integerConstant.accept(v, null);

        if (left == null || right == null) {
            return null;
        }

        return mkComparison(left, cmp, right);
    }
}
