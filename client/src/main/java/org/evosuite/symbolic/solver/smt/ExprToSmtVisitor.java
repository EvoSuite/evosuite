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
package org.evosuite.symbolic.solver.smt;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.*;
import org.evosuite.symbolic.expr.fp.*;
import org.evosuite.symbolic.expr.reader.StringReaderExpr;
import org.evosuite.symbolic.expr.ref.GetFieldExpression;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceVariable;
import org.evosuite.symbolic.expr.ref.array.ArrayConstant;
import org.evosuite.symbolic.expr.ref.array.ArraySelect;
import org.evosuite.symbolic.expr.ref.array.ArrayStore;
import org.evosuite.symbolic.expr.ref.array.ArrayVariable;
import org.evosuite.symbolic.expr.reftype.LambdaSyntheticType;
import org.evosuite.symbolic.expr.reftype.LiteralClassType;
import org.evosuite.symbolic.expr.reftype.LiteralNullType;
import org.evosuite.symbolic.expr.str.*;
import org.evosuite.symbolic.expr.token.HasMoreTokensExpr;
import org.evosuite.symbolic.expr.token.NewTokenizerExpr;
import org.evosuite.symbolic.expr.token.NextTokenizerExpr;
import org.evosuite.symbolic.expr.token.StringNextTokenExpr;
import org.evosuite.symbolic.solver.SmtExprBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class ExprToSmtVisitor implements ExpressionVisitor<SmtExpr, Void> {

    protected static SmtExpr approximateToConcreteValue(Expression<?> e) {
        if (e instanceof IntegerValue) {
            return approximateToConcreteValue((IntegerValue) e);
        } else if (e instanceof RealValue) {
            return approximateToConcreteValue((RealValue) e);
        } else if (e instanceof StringValue) {
            return approximateToConcreteValue((StringValue) e);
        } else {
            throw new UnsupportedOperationException("unknown expression type:" + e.getClass().getName());
        }
    }

    private static SmtRealConstant mkRepresentableRealConstant(double doubleValue) {
        if (isRepresentable(doubleValue)) {
            return SmtExprBuilder.mkRealConstant(doubleValue);
        } else {
            return null;
        }
    }

    private static boolean isRepresentable(Double doubleVal) {
        return !doubleVal.isNaN() && !doubleVal.isInfinite();
    }

    protected static SmtIntConstant approximateToConcreteValue(IntegerValue e) {
        long longValue = e.getConcreteValue();
        return SmtExprBuilder.mkIntConstant(longValue);

    }

    protected static SmtStringConstant approximateToConcreteValue(StringValue e) {
        String stringValue = e.getConcreteValue();
        return SmtExprBuilder.mkStringConstant(stringValue);
    }

    protected static SmtRealConstant approximateToConcreteValue(RealValue e) {
        double doubleValue = e.getConcreteValue();
        return mkRepresentableRealConstant(doubleValue);
    }

    @Override
    public final SmtExpr visit(IntegerBinaryExpression e, Void v) {
        SmtExpr left = e.getLeftOperand().accept(this, null);
        SmtExpr right = e.getRightOperand().accept(this, null);

        if (left == null || right == null) {
            return null;
        }

        if (!left.isSymbolic() && !right.isSymbolic()) {
            long longValue = e.getConcreteValue();
            return SmtExprBuilder.mkIntConstant(longValue);
        }

        Operator operator = e.getOperator();
        return postVisit(e, left, operator, right);
    }

    protected SmtExpr postVisit(IntegerBinaryExpression source, SmtExpr left, Operator operator, SmtExpr right) {
        switch (operator) {

            case DIV: {
                return SmtExprBuilder.mkIntDiv(left, right);
            }
            case MUL: {
                return SmtExprBuilder.mkMul(left, right);

            }
            case MINUS: {
                return SmtExprBuilder.mkSub(left, right);
            }
            case PLUS: {
                return SmtExprBuilder.mkAdd(left, right);
            }
            case REM: {
                return SmtExprBuilder.mkMod(left, right);
            }
            case IOR: {
                SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
                SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
                SmtExpr bvor = SmtExprBuilder.mkBVOR(bv_left, bv_right);
                return SmtExprBuilder.mkBV2Int(bvor);
            }
            case IAND: {
                SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
                SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
                SmtExpr bv_and = SmtExprBuilder.mkBVAND(bv_left, bv_right);
                return SmtExprBuilder.mkBV2Int(bv_and);
            }
            case IXOR: {
                SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
                SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
                SmtExpr bv_xor = SmtExprBuilder.mkBVXOR(bv_left, bv_right);
                return SmtExprBuilder.mkBV2Int(bv_xor);
            }

            case SHL: {
                SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
                SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
                SmtExpr bv_shl = SmtExprBuilder.mkBVSHL(bv_left, bv_right);
                return SmtExprBuilder.mkBV2Int(bv_shl);
            }

            case SHR: {
                SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
                SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
                SmtExpr bv_shr = SmtExprBuilder.mkBVASHR(bv_left, bv_right);
                return SmtExprBuilder.mkBV2Int(bv_shr);
            }
            case USHR: {
                SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
                SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
                SmtExpr bv_shr = SmtExprBuilder.mkBVLSHR(bv_left, bv_right);
                return SmtExprBuilder.mkBV2Int(bv_shr);
            }

            case MAX: {
                SmtExpr left_gt_right = SmtExprBuilder.mkGt(left, right);
                return SmtExprBuilder.mkITE(left_gt_right, left, right);

            }

            case MIN: {
                SmtExpr left_gt_right = SmtExprBuilder.mkLt(left, right);
                return SmtExprBuilder.mkITE(left_gt_right, left, right);

            }

            default: {
                throw new UnsupportedOperationException("Operatior not implemented yet: " + source.getOperator());
            }
        }
    }

    @Override
    public final SmtExpr visit(IntegerConstant e, Void v) {
        long concreteValue = e.getConcreteValue();
        return SmtExprBuilder.mkIntConstant(concreteValue);
    }

    @Override
    public final SmtExpr visit(IntegerUnaryExpression e, Void v) {
        SmtExpr operand = e.getOperand().accept(this, null);

        if (operand == null) {
            return null;
        }

        if (!operand.isSymbolic()) {
            return approximateToConcreteValue(e);
        }

        Operator operator = e.getOperator();
        return postVisit(e, operator, operand);
    }

    protected SmtExpr postVisit(IntegerUnaryExpression source, Operator operator, SmtExpr operand) {
        switch (operator) {
            case NEG: {
                return SmtExprBuilder.mkNeg(operand);
            }
            case GETNUMERICVALUE:
            case ISDIGIT:
            case ISLETTER: {
                return approximateToConcreteValue(source);
            }
            case ABS:
                SmtExpr zero = SmtExprBuilder.mkIntConstant(0);
                SmtExpr gte_than_zero = SmtExprBuilder.mkGe(operand, zero);
                SmtExpr minus_expr = SmtExprBuilder.mkNeg(operand);

                return SmtExprBuilder.mkITE(gte_than_zero, operand, minus_expr);
            default:
                throw new UnsupportedOperationException("Not implemented yet!" + operator);
        }
    }

    @Override
    public final SmtExpr visit(RealToIntegerCast e, Void v) {
        SmtExpr operand = e.getArgument().accept(this, null);
        if (operand == null) {
            return null;
        }
        if (!operand.isSymbolic()) {
            return approximateToConcreteValue(e);
        }

        return postVisit(e, operand);
    }

    protected SmtExpr postVisit(RealToIntegerCast source, SmtExpr operand) {
        return SmtExprBuilder.mkToInt(operand);
    }

    @Override
    public final SmtExpr visit(RealUnaryToIntegerExpression e, Void v) {
        SmtExpr realExpr = e.getOperand().accept(this, null);
        if (realExpr == null) {
            return null;
        }
        if (!realExpr.isSymbolic()) {
            return approximateToConcreteValue(e);
        }
        Operator operator = e.getOperator();
        return postVisit(e, realExpr, operator);
    }

    protected SmtExpr postVisit(RealUnaryToIntegerExpression source, SmtExpr operand, Operator operator) {
        switch (operator) {
            case ROUND: {
                return SmtExprBuilder.mkToInt(operand);
            }
            case GETEXPONENT: {
                return approximateToConcreteValue(source);
            }
            default:
                throw new UnsupportedOperationException("Not implemented yet!");
        }
    }

    @Override
    public final SmtExpr visit(IntegerToRealCast e, Void v) {
        SmtExpr operand = e.getArgument().accept(this, null);
        if (operand == null) {
            return null;
        }
        if (!operand.isSymbolic()) {
            return approximateToConcreteValue(e);
        }
        return postVisit(e, operand);

    }

    protected SmtExpr postVisit(IntegerToRealCast source, SmtExpr operand) {
        return SmtExprBuilder.mkToReal(operand);
    }

    @Override
    public final SmtExpr visit(RealBinaryExpression e, Void v) {
        SmtExpr left = e.getLeftOperand().accept(this, null);
        Operator operator = e.getOperator();
        SmtExpr right = e.getRightOperand().accept(this, null);

        if (left == null || right == null) {
            return null;
        }

        if (!left.isSymbolic() && !right.isSymbolic()) {
            double doubleValue = e.getConcreteValue();
            return mkRepresentableRealConstant(doubleValue);
        }

        return postVisit(e, left, operator, right);

    }

    protected SmtExpr postVisit(RealBinaryExpression source, SmtExpr left, Operator operator, SmtExpr right) {
        switch (operator) {

            case DIV: {
                return SmtExprBuilder.mkRealDiv(left, right);
            }
            case MUL: {
                return SmtExprBuilder.mkMul(left, right);

            }
            case MINUS: {
                return SmtExprBuilder.mkSub(left, right);
            }
            case PLUS: {
                return SmtExprBuilder.mkAdd(left, right);
            }
            case MAX: {
                SmtExpr left_gt_right = SmtExprBuilder.mkGt(left, right);
                return SmtExprBuilder.mkITE(left_gt_right, left, right);

            }

            case MIN: {
                SmtExpr left_gt_right = SmtExprBuilder.mkLt(left, right);
                return SmtExprBuilder.mkITE(left_gt_right, left, right);
            }
            case ATAN2:
            case COPYSIGN:
            case HYPOT:
            case NEXTAFTER:
            case POW:
            case SCALB:
            case IEEEREMAINDER:
            case REM: {
                return approximateToConcreteValue(source);
            }

            default: {
                throw new UnsupportedOperationException("Not implemented yet! " + operator);
            }
        }
    }

    @Override
    public final SmtExpr visit(RealConstant e, Void v) {
        double doubleValue = e.getConcreteValue();
        return mkRepresentableRealConstant(doubleValue);
    }

    @Override
    public final SmtExpr visit(RealVariable e, Void v) {
        String varName = e.getName();
        return SmtExprBuilder.mkRealVariable(varName);
    }

    @Override
    public final SmtExpr visit(RealUnaryExpression e, Void v) {
        SmtExpr operand = e.getOperand().accept(this, null);

        if (operand == null) {
            return null;
        }

        if (!operand.isSymbolic()) {
            return approximateToConcreteValue(e);
        }

        Operator operator = e.getOperator();
        return postVisit(e, operand, operator);
    }

    protected SmtExpr postVisit(RealUnaryExpression source, SmtExpr operand, Operator operator) {
        switch (operator) {
            case ABS: {
                SmtExpr zero_rational = SmtExprBuilder.ZERO_REAL;
                SmtExpr gte_than_zero = SmtExprBuilder.mkGe(operand, zero_rational);
                SmtExpr minus_expr = SmtExprBuilder.mkNeg(operand);
                return SmtExprBuilder.mkITE(gte_than_zero, operand, minus_expr);
            }
            // trigonometric
            case ACOS:
            case ASIN:
            case ATAN:
            case COS:
            case COSH:
            case SIN:
            case SINH:
            case TAN:
            case TANH:
                // other functions
            case CBRT:
            case CEIL:
            case EXP:
            case EXPM1:
            case FLOOR:
            case LOG:
            case LOG10:
            case LOG1P:
            case NEXTUP:
            case RINT:
            case SIGNUM:
            case SQRT:
            case TODEGREES:
            case TORADIANS:
            case ULP: {
                return approximateToConcreteValue(source);
            }
            case GETEXPONENT:
            case ROUND: {
                throw new IllegalArgumentException("The Operation " + operator + " does not return a real expression!");
            }
            default:
                throw new UnsupportedOperationException("Not implemented yet!");
        }
    }

    @Override
    public final SmtExpr visit(IntegerVariable e, Void v) {
        String varName = e.getName();
        return SmtExprBuilder.mkIntVariable(varName);
    }

    @Override
    public final SmtExpr visit(RealComparison e, Void v) {
        throw new IllegalStateException("RealComparison should be removed during normalization");
    }

    @Override
    public final SmtExpr visit(IntegerComparison e, Void v) {
        throw new IllegalStateException("IntegerComparison should be removed during normalization");
    }

    @Override
    public final SmtExpr visit(IntegerToStringCast e, Void v) {
        SmtExpr operand = e.getArgument().accept(this, null);
        if (operand == null) {
            return null;
        }
        if (!operand.isSymbolic()) {
            return approximateToConcreteValue(e);
        }
        return postVisit(e, operand);
    }

    protected SmtExpr postVisit(IntegerToStringCast source, SmtExpr operand) {
        return SmtExprBuilder.mkIntToStr(operand);
    }

    @Override
    public final SmtExpr visit(RealToStringCast e, Void arg) {
        SmtExpr operand = e.getArgument().accept(this, null);
        if (operand == null) {
            return null;
        }
        if (!operand.isSymbolic()) {
            return approximateToConcreteValue(e);
        }

        return postVisit(e, operand);
    }

    protected SmtExpr postVisit(RealToStringCast source, SmtExpr operand) {
        return approximateToConcreteValue(source);
    }

    @Override
    public final SmtExpr visit(HasMoreTokensExpr e, Void v) {
        SmtExpr expr = e.getTokenizerExpr().accept(this, null);
        if (expr == null) {
            return null;
        }

        return approximateToConcreteValue(e);
    }

    @Override
    public final SmtExpr visit(NewTokenizerExpr e, Void v) {
        // TODO
        throw new IllegalStateException("NewTokenizerExpr is not implemented yet");

    }

    @Override
    public final SmtExpr visit(NextTokenizerExpr e, Void v) {
        return null;
    }

    @Override
    public final SmtExpr visit(ReferenceConstant referenceConstant, Void arg) {
        throw new UnsupportedOperationException("Translation to Z3 of ReferenceConstant is not yet implemented!");
    }

    @Override
    public final SmtExpr visit(ReferenceVariable r, Void arg) {
        throw new UnsupportedOperationException("Translation to Z3 of ReferenceVariable is not yet implemented!");

    }

    @Override
    public final SmtExpr visit(GetFieldExpression r, Void arg) {
        throw new UnsupportedOperationException("Translation to Z3 of GetFieldExpression is not yet implemented!");

    }

    @Override
    public SmtExpr visit(ArraySelect.IntegerArraySelect r, Void arg) {
        SmtExpr arrayExpr = r.getSymbolicArray().accept(this, null);
        SmtExpr indexExpr = r.getSymbolicIndex().accept(this, null);

        if (arrayExpr == null || indexExpr == null) {
            return null;
        }

        if (!arrayExpr.isSymbolic() && !indexExpr.isSymbolic()) {
            Long arrayValue = r.getConcreteValue();
            return SmtExprBuilder.mkIntConstant(arrayValue);
        }

        return SmtExprBuilder.mkArrayLoad(arrayExpr, indexExpr);
    }

    @Override
    public SmtExpr visit(ArrayStore.IntegerArrayStore r, Void arg) {
        SmtExpr arrayExpr = r.getSymbolicArray().accept(this, null);
        SmtExpr indexExpr = r.getSymbolicIndex().accept(this, null);
        SmtExpr valueExpression = r.getSymbolicValue().accept(this, null);

        if (arrayExpr == null || indexExpr == null || valueExpression == null) {
            return null;
        }

        if (!arrayExpr.isSymbolic() && !valueExpression.isSymbolic()) {
            Object arrayValue = r.getConcreteValue();
            return SmtExprBuilder.mkIntegerArrayConstant(arrayValue);
        }

        return SmtExprBuilder.mkArrayStore(arrayExpr, indexExpr, valueExpression);
    }


    @Override
    public SmtExpr visit(ArraySelect.RealArraySelect r, Void arg) {
        SmtExpr arrayExpr = r.getSymbolicArray().accept(this, null);
        SmtExpr indexExpr = r.getSymbolicIndex().accept(this, null);

        if (arrayExpr == null || indexExpr == null) {
            return null;
        }

        if (!arrayExpr.isSymbolic() && !indexExpr.isSymbolic()) {
            Double arrayValue = r.getConcreteValue();
            return SmtExprBuilder.mkRealConstant(arrayValue);
        }

        return SmtExprBuilder.mkArrayLoad(arrayExpr, indexExpr);
    }

    @Override
    public SmtExpr visit(ArraySelect.StringArraySelect r, Void arg) {
        SmtExpr arrayExpr = r.getSymbolicArray().accept(this, null);
        SmtExpr indexExpr = r.getSymbolicIndex().accept(this, null);

        if (arrayExpr == null || indexExpr == null) {
            return null;
        }

        if (!arrayExpr.isSymbolic() && !indexExpr.isSymbolic()) {
            String arrayValue = r.getConcreteValue();
            return SmtExprBuilder.mkStringConstant(arrayValue);
        }

        return SmtExprBuilder.mkArrayLoad(arrayExpr, indexExpr);
    }

    @Override
    public SmtExpr visit(ArrayStore.RealArrayStore r, Void arg) {
        SmtExpr arrayExpr = r.getSymbolicArray().accept(this, null);
        SmtExpr indexExpr = r.getSymbolicIndex().accept(this, null);
        SmtExpr valueExpression = r.getSymbolicValue().accept(this, null);

        if (arrayExpr == null || indexExpr == null || valueExpression == null) {
            return null;
        }

        if (!arrayExpr.isSymbolic() && !valueExpression.isSymbolic()) {
            Object arrayValue = r.getConcreteValue();
            return SmtExprBuilder.mkRealArrayConstant(arrayValue);
        }

        return SmtExprBuilder.mkArrayStore(arrayExpr, indexExpr, valueExpression);
    }

    @Override
    public SmtExpr visit(ArrayStore.StringArrayStore r, Void arg) {
        SmtExpr arrayExpr = r.getSymbolicArray().accept(this, null);
        SmtExpr indexExpr = r.getSymbolicIndex().accept(this, null);
        SmtExpr valueExpression = r.getSymbolicValue().accept(this, null);

        if (arrayExpr == null || indexExpr == null || valueExpression == null) {
            return null;
        }

        if (!arrayExpr.isSymbolic() && !valueExpression.isSymbolic()) {
            Object arrayValue = r.getConcreteValue();
            return SmtExprBuilder.mkRealArrayConstant(arrayValue);
        }

        return SmtExprBuilder.mkArrayStore(arrayExpr, indexExpr, valueExpression);
    }

    @Override
    public SmtExpr visit(ArrayConstant.IntegerArrayConstant r, Void arg) {
        Object concreteValue = r.getConcreteValue();
        return SmtExprBuilder.mkIntegerArrayConstant(concreteValue);
    }

    @Override
    public SmtExpr visit(ArrayConstant.RealArrayConstant r, Void arg) {
        Object concreteValue = r.getConcreteValue();
        return SmtExprBuilder.mkRealArrayConstant(concreteValue);
    }

    @Override
    public SmtExpr visit(ArrayConstant.StringArrayConstant r, Void arg) {
        Object concreteValue = r.getConcreteValue();
        return SmtExprBuilder.mkStringArrayConstant(concreteValue);
    }

    @Override
    public SmtExpr visit(ArrayConstant.ReferenceArrayConstant r, Void arg) {
        Object concreteValue = r.getConcreteValue();
        return SmtExprBuilder.mkReferenceArrayConstant(concreteValue);
    }

    @Override
    public SmtExpr visit(ArrayVariable.IntegerArrayVariable r, Void arg) {
        String varName = r.getName();
        return SmtExprBuilder.mkIntegerArrayVariable(varName);
    }

    @Override
    public SmtExpr visit(ArrayVariable.RealArrayVariable r, Void arg) {
        String varName = r.getName();
        return SmtExprBuilder.mkRealArrayVariable(varName);
    }

    @Override
    public SmtExpr visit(ArrayVariable.StringArrayVariable r, Void arg) {
        return null;
    }

    @Override
    public SmtExpr visit(ArrayVariable.ReferenceArrayVariable r, Void arg) {
        return null;
    }

    @Override
    public SmtExpr visit(LambdaSyntheticType r, Void arg) {
        return null;
    }

    @Override
    public SmtExpr visit(LiteralNullType r, Void arg) {
        return null;
    }

    @Override
    public SmtExpr visit(LiteralClassType r, Void arg) {
        return null;
    }

    @Override
    public final SmtExpr visit(StringBinaryComparison e, Void arg) {
        Expression<String> leftOperand = e.getLeftOperand();
        Expression<?> rightOperand = e.getRightOperand();
        Operator op = e.getOperator();

        SmtExpr left = leftOperand.accept(this, null);
        SmtExpr right = rightOperand.accept(this, null);

        if (left == null || right == null) {
            return null;
        }

        if (!left.isSymbolic() && !right.isSymbolic()) {
            return approximateToConcreteValue(e);
        }

        return postVisit(e, left, op, right);

    }

    protected SmtExpr postVisit(StringBinaryComparison source, SmtExpr left, Operator op, SmtExpr right) {
        switch (op) {
            case EQUALS: {
                SmtExpr equalsFormula = SmtExprBuilder.mkEq(left, right);
                return SmtExprBuilder.mkITE(equalsFormula, SmtExprBuilder.ONE_INT,
                        SmtExprBuilder.ZERO_INT);
            }
            case ENDSWITH: {
                SmtExpr endsWithExpr = SmtExprBuilder.mkStrSuffixOf(right, left);
                return SmtExprBuilder.mkITE(endsWithExpr, SmtExprBuilder.ONE_INT,
                        SmtExprBuilder.ZERO_INT);
            }
            case CONTAINS: {
                SmtExpr containsExpr = SmtExprBuilder.mkStrContains(left, right);
                return SmtExprBuilder.mkITE(containsExpr, SmtExprBuilder.ONE_INT,
                        SmtExprBuilder.ZERO_INT);
            }
            case STARTSWITH: {
                SmtExpr startsWithExpr = SmtExprBuilder.mkStrPrefixOf(right, left);
                SmtExpr eqTrue = SmtExprBuilder.mkEq(startsWithExpr, SmtExprBuilder.TRUE);
                return SmtExprBuilder.mkITE(eqTrue, SmtExprBuilder.ONE_INT, SmtExprBuilder.ZERO_INT);
            }
            case EQUALSIGNORECASE:
            case REGIONMATCHES:
            case PATTERNMATCHES:
            case APACHE_ORO_PATTERN_MATCHES: {
                return approximateToConcreteValue(source);
            }
            default:
                throw new UnsupportedOperationException("Not implemented yet! " + op);
        }
    }

    @Override
    public final SmtExpr visit(StringBinaryExpression e, Void arg) {
        SmtExpr left = e.getLeftOperand().accept(this, null);
        SmtExpr right = e.getRightOperand().accept(this, null);
        Operator operator = e.getOperator();

        if (left == null || right == null) {
            return null;
        }

        if (!left.isSymbolic() && !right.isSymbolic()) {
            return approximateToConcreteValue(e);
        }

        return postVisit(e, left, operator, right);

    }

    protected SmtExpr postVisit(StringBinaryExpression source, SmtExpr left, Operator operator, SmtExpr right) {
        switch (operator) {
            case CONCAT:
            case APPEND_STRING: {
                return SmtExprBuilder.mkStrConcat(left, right);
            }
            case APPEND_BOOLEAN: {
                SmtIntConstant zero = SmtExprBuilder.ZERO_INT;
                SmtExpr eqZero = SmtExprBuilder.mkEq(right, zero);
                SmtExpr ite = SmtExprBuilder.mkITE(eqZero, SmtExprBuilder.FALSE_STRING, SmtExprBuilder.TRUE_STRING);
                return SmtExprBuilder.mkStrConcat(left, ite);
            }
            case APPEND_CHAR: {
                SmtExpr rigthStr = SmtExprBuilder.mkIntToChar(right);
                return SmtExprBuilder.mkStrConcat(left, rigthStr);
            }
            case APPEND_INTEGER: {
                SmtExpr rigthStr = SmtExprBuilder.mkIntToStr(right);
                return SmtExprBuilder.mkStrConcat(left, rigthStr);
            }
            case APPEND_REAL: {
                double concreteRightValue = (Double) source.getRightOperand().getConcreteValue();
                String concreteRight = String.valueOf(concreteRightValue);
                SmtExpr concreteRightConstant = SmtExprBuilder.mkStringConstant(concreteRight);
                return SmtExprBuilder.mkStrConcat(left, concreteRightConstant);
            }
            default: {
                throw new UnsupportedOperationException("Not implemented yet! " + operator);
            }
        }
    }

    @Override
    public final SmtExpr visit(StringBinaryToIntegerExpression e, Void arg) {
        Expression<String> leftOperand = e.getLeftOperand();
        Operator op = e.getOperator();
        Expression<?> rightOperand = e.getRightOperand();

        SmtExpr left = leftOperand.accept(this, null);
        SmtExpr right = rightOperand.accept(this, null);

        if (left == null || right == null) {
            return null;
        }

        if (!left.isSymbolic() && !right.isSymbolic()) {
            return approximateToConcreteValue(e);
        }

        return postVisit(e, left, op, right);

    }

    protected SmtExpr postVisit(StringBinaryToIntegerExpression source, SmtExpr left, Operator op, SmtExpr right) {
        switch (op) {
            case CHARAT: {
                SmtExpr charAtExpr = SmtExprBuilder.mkStrAt(left, right);
                return SmtExprBuilder.mkCharToInt(charAtExpr);
            }
            case INDEXOFS: {
                return SmtExprBuilder.mkStrIndexOf(left, right, SmtExprBuilder.ZERO_INT);
            }

            case INDEXOFC: {
                SmtExpr string = SmtExprBuilder.mkIntToChar(right);
                return SmtExprBuilder.mkStrIndexOf(left, string, SmtExprBuilder.ZERO_INT);

            }
            case LASTINDEXOFC:
            case LASTINDEXOFS:
            case COMPARETO:
            case COMPARETOIGNORECASE: {
                return approximateToConcreteValue(source);
            }
            default: {
                throw new UnsupportedOperationException("Not implemented yet!" + source.getOperator());
            }
        }
    }

    @Override
    public final SmtExpr visit(StringConstant n, Void arg) {
        String stringValue = n.getConcreteValue();
        return SmtExprBuilder.mkStringConstant(stringValue);
    }

    @Override
    public final SmtExpr visit(StringMultipleComparison e, Void arg) {
        SmtExpr left = e.getLeftOperand().accept(this, null);
        Operator operator = e.getOperator();
        SmtExpr right = e.getRightOperand().accept(this, null);

        List<SmtExpr> others = e.getOther().stream().map(t -> t.accept(this, null)).collect(Collectors.toList());

        if (left == null || right == null || others.contains(null)) {
            return null;
        }

        if (!left.isSymbolic() && !right.isSymbolic() && !others.stream().anyMatch(other -> other.isSymbolic())) {
            return approximateToConcreteValue(e);
        }

        return postVisit(e, left, operator, right, others);
    }

    protected SmtExpr postVisit(StringMultipleComparison source, SmtExpr left, Operator operator, SmtExpr right,
                                List<SmtExpr> others) {
        switch (operator) {
            case STARTSWITH: {
                SmtExpr startIndex = others.get(0);
                SmtExpr leftLength = SmtExprBuilder.mkStrLen(left);
                SmtExpr offset = SmtExprBuilder.mkSub(leftLength, startIndex);
                SmtExpr s = SmtExprBuilder.mkStrSubstr(left, startIndex, offset);
                SmtExpr startsWith = SmtExprBuilder.mkStrPrefixOf(right, s);
                SmtExpr ifThenElse = SmtExprBuilder.mkITE(startsWith, SmtExprBuilder.ONE_INT, SmtExprBuilder.ZERO_INT);
                return ifThenElse;
            }
            case EQUALS:
            case EQUALSIGNORECASE:
            case ENDSWITH:
            case CONTAINS: {
                throw new IllegalArgumentException("Illegal StringMultipleComparison operator " + operator);
            }
            case REGIONMATCHES:
            case PATTERNMATCHES:
            case APACHE_ORO_PATTERN_MATCHES: {
                return approximateToConcreteValue(source);
            }
            default:
                throw new UnsupportedOperationException("Not implemented yet! " + operator);
        }
    }

    @Override
    public final SmtExpr visit(StringMultipleExpression e, Void arg) {
        Operator operator = e.getOperator();
        SmtExpr left = e.getLeftOperand().accept(this, null);
        SmtExpr right = e.getRightOperand().accept(this, null);
        List<SmtExpr> others = e.getOther().stream().map(t -> t.accept(this, null)).collect(Collectors.toList());

        if (left == null || right == null || others.contains(null)) {
            return null;
        }

        if (!left.isSymbolic() && !right.isSymbolic() && !others.stream().anyMatch(other -> other.isSymbolic())) {
            return approximateToConcreteValue(e);
        }

        return postVisit(e, left, operator, right, others);

    }

    protected SmtExpr postVisit(StringMultipleExpression source, SmtExpr left, Operator operator, SmtExpr right,
                                List<SmtExpr> others) {
        switch (operator) {
            case REPLACECS:
            case REPLACEFIRST: {
                SmtExpr replacement = others.get(0);
                return SmtExprBuilder.mkStrReplace(left, right, replacement);
            }

            case SUBSTRING: {
                SmtExpr endIndex = others.get(0);
                SmtExpr offset = SmtExprBuilder.mkSub(endIndex, right);
                return SmtExprBuilder.mkStrSubstr(left, right, offset);
            }
            case REPLACEC: {
                SmtExpr target = SmtExprBuilder.mkIntToChar(right);
                SmtExpr replacement = SmtExprBuilder.mkIntToChar(others.get(0));
                return SmtExprBuilder.mkStrReplace(left, target, replacement);
            }
            case REPLACEALL: {
                return approximateToConcreteValue(source);
            }
            default:
                throw new UnsupportedOperationException("Not implemented yet! " + operator);
        }
    }

    @Override
    public final SmtExpr visit(StringMultipleToIntegerExpression e, Void arg) {
        SmtExpr left = e.getLeftOperand().accept(this, null);
        SmtExpr right = e.getRightOperand().accept(this, null);
        List<SmtExpr> others = e.getOther().stream().map(t -> t.accept(this, null)).collect(Collectors.toList());

        if (left == null || right == null || others.contains(null)) {
            return null;
        }

        if (!left.isSymbolic() && !right.isSymbolic() && !others.stream().anyMatch(other -> other.isSymbolic())) {
            return approximateToConcreteValue(e);
        }

        Operator op = e.getOperator();
        return postVisit(e, left, op, right, others);
    }

    protected SmtExpr postVisit(StringMultipleToIntegerExpression source, SmtExpr left, Operator operator,
                                SmtExpr right, List<SmtExpr> others) {
        switch (operator) {
            case INDEXOFCI: {
                SmtExpr strExpr = SmtExprBuilder.mkIntToChar(right);
                SmtExpr fromIndex = others.get(0);
                return SmtExprBuilder.mkStrIndexOf(left, strExpr, fromIndex);
            }
            case INDEXOFSI: {
                SmtExpr fromIndex = others.get(0);
                return SmtExprBuilder.mkStrIndexOf(left, right, fromIndex);
            }
            case LASTINDEXOFCI:
            case LASTINDEXOFSI: {
                return approximateToConcreteValue(source);
            }
            default: {
                throw new UnsupportedOperationException("Not implemented yet! " + operator);
            }
        }
    }

    @Override
    public final SmtExpr visit(StringNextTokenExpr n, Void arg) {
        SmtExpr operand = n.getTokenizerExpr().accept(this, null);
        if (operand == null) {
            return null;
        }
        if (!operand.isSymbolic()) {
            return approximateToConcreteValue(n);
        }

        return postVisit(n, operand);
    }

    protected SmtExpr postVisit(StringNextTokenExpr source, SmtExpr operand) {
        return approximateToConcreteValue(source);
    }

    @Override
    public final SmtExpr visit(StringReaderExpr e, Void arg) {
        SmtExpr operand = e.getString().accept(this, null);
        if (operand == null) {
            return null;
        }

        if (!operand.isSymbolic()) {
            return approximateToConcreteValue(e);
        }

        return postVisit(e, operand);
    }

    protected SmtExpr postVisit(StringReaderExpr source, SmtExpr operand) {
        return approximateToConcreteValue(source);
    }

    @Override
    public final SmtExpr visit(StringUnaryExpression e, Void arg) {
        SmtExpr operand = e.getOperand().accept(this, null);

        if (operand == null) {
            return null;
        }

        if (!operand.isSymbolic()) {
            return approximateToConcreteValue(e);
        }

        Operator operator = e.getOperator();
        return postVisit(e, operator, operand);
    }

    protected SmtExpr postVisit(StringUnaryExpression source, Operator operator, SmtExpr operand) {
        switch (operator) {
            case TRIM:
            case TOLOWERCASE:
            case TOUPPERCASE: {
                return approximateToConcreteValue(source);
            }
            default:
                throw new UnsupportedOperationException("Not implemented yet! " + operator);
        }
    }

    @Override
    public final SmtExpr visit(StringUnaryToIntegerExpression e, Void arg) {
        SmtExpr operand = e.getOperand().accept(this, null);
        if (operand == null) {
            return null;
        }
        if (!operand.isSymbolic()) {
            return approximateToConcreteValue(e);
        }

        Operator operator = e.getOperator();
        return postVisit(e, operator, operand);
    }

    protected SmtExpr postVisit(StringUnaryToIntegerExpression source, Operator operator, SmtExpr operand) {
        switch (operator) {
            case LENGTH: {
                return SmtExprBuilder.mkStrLen(operand);
            }
            case IS_INTEGER: {
                SmtExpr plusRE = SmtExprBuilder.mkStrToRE(SmtExprBuilder.PLUS);
                SmtExpr minusRE = SmtExprBuilder.mkStrToRE(SmtExprBuilder.MINUS);

                SmtExpr optPlusRE = SmtExprBuilder.mkREOpt(plusRE);
                SmtExpr optMinusRE = SmtExprBuilder.mkREOpt(minusRE);

                SmtExpr symbols = SmtExprBuilder.mkREUnion(optPlusRE, optMinusRE);

                SmtExpr rangeRE = SmtExprBuilder.mkRERange(SmtExprBuilder.ZERO_STRING, SmtExprBuilder.NINE_STRING);
                SmtExpr digits = SmtExprBuilder.mkREKleeneCross(rangeRE);

                SmtExpr isIntegerPattern = SmtExprBuilder.mkREConcat(symbols, digits);

                SmtExpr matchesExpr = SmtExprBuilder.mkStrInRE(operand, isIntegerPattern);
                return SmtExprBuilder.mkITE(matchesExpr, SmtExprBuilder.ONE_INT, SmtExprBuilder.ZERO_INT);
            }
            default:
                throw new UnsupportedOperationException("Not implemented yet!");
        }
    }

    @Override
    public final SmtExpr visit(StringVariable e, Void arg) {
        return SmtExprBuilder.mkStringVariable(e.getName());
    }

    @Override
    public final SmtExpr visit(StringToIntegerCast e, Void arg) {
        SmtExpr operand = e.getArgument().accept(this, null);

        if (operand == null) {
            return null;
        }

        if (!operand.isSymbolic()) {
            return approximateToConcreteValue(e);
        }

        return postVisit(e, operand);
    }

    protected SmtExpr postVisit(StringToIntegerCast source, SmtExpr operand) {
        return SmtExprBuilder.mkStrToInt(operand);
    }
}
