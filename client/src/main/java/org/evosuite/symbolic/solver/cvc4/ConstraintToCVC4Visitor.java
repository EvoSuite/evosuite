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
package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.constraint.ConstraintVisitor;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.constraint.RealConstraint;
import org.evosuite.symbolic.expr.constraint.StringConstraint;
import org.evosuite.symbolic.solver.SmtExprBuilder;
import org.evosuite.symbolic.solver.smt.SmtExpr;

final class ConstraintToCVC4Visitor implements ConstraintVisitor<SmtExpr, Void> {

    private final ExprToCVC4Visitor exprVisitor;

    public ConstraintToCVC4Visitor() {
        this(false);
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

        ExprToCVC4Visitor v = new ExprToCVC4Visitor();
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


    public ConstraintToCVC4Visitor(boolean rewriteNonLinearConstraints) {
        this.exprVisitor = new ExprToCVC4Visitor(rewriteNonLinearConstraints);
    }

    @Override
    public SmtExpr visit(IntegerConstraint c, Void arg) {
        Expression<?> leftOperand = c.getLeftOperand();
        Expression<?> rightOperand = c.getRightOperand();
        Comparator cmp = c.getComparator();

        SmtExpr expr = translateCompareTo(leftOperand, cmp, rightOperand);
        if (expr != null) {
            return expr;
        } else {
            return visit(leftOperand, cmp, rightOperand);
        }
    }

    private SmtExpr visit(Expression<?> leftOperand, Comparator cmp, Expression<?> rightOperand) {
        SmtExpr left = leftOperand.accept(exprVisitor, null);
        SmtExpr right = rightOperand.accept(exprVisitor, null);

        if (left == null || right == null) {
            return null;
        }

        return mkComparison(left, cmp, right);
    }

    @Override
    public SmtExpr visit(RealConstraint c, Void arg) {
        Expression<?> leftOperand = c.getLeftOperand();
        Expression<?> rightOperand = c.getRightOperand();
        Comparator cmp = c.getComparator();
        return visit(leftOperand, cmp, rightOperand);
    }

    @Override
    public SmtExpr visit(StringConstraint c, Void arg) {
        Expression<?> leftOperand = c.getLeftOperand();
        Expression<?> rightOperand = c.getRightOperand();
        Comparator cmp = c.getComparator();

        SmtExpr equalsExpr = translateCompareTo(leftOperand, cmp, rightOperand);
        if (equalsExpr != null) {
            return equalsExpr;
        } else {
            return visit(leftOperand, cmp, rightOperand);
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
}
