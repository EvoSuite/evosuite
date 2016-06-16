/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerComparison;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.bv.RealComparison;
import org.evosuite.symbolic.expr.bv.RealToIntegerCast;
import org.evosuite.symbolic.expr.bv.RealUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringMultipleComparison;
import org.evosuite.symbolic.expr.bv.StringMultipleToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringToIntegerCast;
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.fp.IntegerToRealCast;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealConstant;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.reader.StringReaderExpr;
import org.evosuite.symbolic.expr.ref.GetFieldExpression;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceVariable;
import org.evosuite.symbolic.expr.str.IntegerToStringCast;
import org.evosuite.symbolic.expr.str.RealToStringCast;
import org.evosuite.symbolic.expr.str.StringBinaryExpression;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringMultipleExpression;
import org.evosuite.symbolic.expr.str.StringUnaryExpression;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.expr.token.HasMoreTokensExpr;
import org.evosuite.symbolic.expr.token.NewTokenizerExpr;
import org.evosuite.symbolic.expr.token.NextTokenizerExpr;
import org.evosuite.symbolic.expr.token.StringNextTokenExpr;
import org.evosuite.symbolic.solver.SmtExprBuilder;
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtIntConstant;
import org.evosuite.symbolic.solver.smt.SmtIntVariable;
import org.evosuite.symbolic.solver.smt.SmtRealConstant;
import org.evosuite.symbolic.solver.smt.SmtRealVariable;
import org.evosuite.symbolic.solver.smt.SmtStringConstant;
import org.evosuite.utils.RegexDistanceUtils;

import dk.brics.automaton.RegExp;

final class ExprToCVC4Visitor implements ExpressionVisitor<SmtExpr, Void> {

	private final boolean rewriteNonLinearExpressions;

	public ExprToCVC4Visitor() {
		this(false);
	}

	public ExprToCVC4Visitor(boolean rewriteNonLinearExpressions) {
		this.rewriteNonLinearExpressions = rewriteNonLinearExpressions;
	}

	@Override
	public SmtExpr visit(IntegerBinaryExpression e, Void v) {
		SmtExpr left = e.getLeftOperand().accept(this, null);
		SmtExpr right = e.getRightOperand().accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		if (!left.isSymbolic() && !right.isSymbolic()) {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}

		switch (e.getOperator()) {

		case DIV: {

			if (rewriteNonLinearExpressions) {
				if (left.isSymbolic() && right.isSymbolic()) {
					long rightValue = e.getRightOperand().getConcreteValue();
					SmtExpr conc_right = SmtExprBuilder.mkIntConstant(rightValue);
					SmtExpr rewrite_expr = SmtExprBuilder.mkIntDiv(left, conc_right);
					return rewrite_expr;
				}
			}

			SmtExpr expr = SmtExprBuilder.mkIntDiv(left, right);
			return expr;
		}
		case MUL: {

			if (rewriteNonLinearExpressions) {
				if (left.isSymbolic() && right.isSymbolic()) {
					long rightValue = e.getRightOperand().getConcreteValue();
					SmtExpr conc_right = SmtExprBuilder.mkIntConstant(rightValue);
					SmtExpr rewrite_expr = SmtExprBuilder.mkMul(left, conc_right);
					return rewrite_expr;
				}
			}

			SmtExpr expr = SmtExprBuilder.mkMul(left, right);
			return expr;
		}
		case MINUS: {
			SmtExpr expr = SmtExprBuilder.mkSub(left, right);
			return expr;
		}
		case PLUS: {
			SmtExpr expr = SmtExprBuilder.mkAdd(left, right);
			return expr;
		}
		case REM: {

			if (rewriteNonLinearExpressions) {
				if (left.isSymbolic() && right.isSymbolic()) {
					long rightValue = e.getRightOperand().getConcreteValue();
					SmtExpr conc_right = SmtExprBuilder.mkIntConstant(rightValue);
					SmtExpr rewrite_expr = SmtExprBuilder.mkMod(left, conc_right);
					return rewrite_expr;
				}
			}

			SmtExpr mod = SmtExprBuilder.mkMod(left, right);
			return mod;
		}
		case IOR: {
			SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
			SmtExpr bvor = SmtExprBuilder.mkBVOR(bv_left, bv_right);
			SmtExpr ret_val = mkBV2Int(bvor);
			return ret_val;
		}
		case IAND: {
			SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_and = SmtExprBuilder.mkBVAND(bv_left, bv_right);
			SmtExpr ret_val = mkBV2Int(bv_and);
			return ret_val;
		}
		case IXOR: {
			SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_xor = SmtExprBuilder.mkBVXOR(bv_left, bv_right);
			SmtExpr ret_val = mkBV2Int(bv_xor);
			return ret_val;
		}

		case SHL: {
			SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_shl = SmtExprBuilder.mkBVSHL(bv_left, bv_right);
			SmtExpr ret_val = mkBV2Int(bv_shl);
			return ret_val;
		}

		case SHR: {
			SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_shr = SmtExprBuilder.mkBVASHR(bv_left, bv_right);
			SmtExpr ret_val = mkBV2Int(bv_shr);
			return ret_val;
		}
		case USHR: {
			SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_shr = SmtExprBuilder.mkBVLSHR(bv_left, bv_right);
			SmtExpr ret_val = mkBV2Int(bv_shr);
			return ret_val;
		}

		case MAX: {
			SmtExpr left_gt_right = SmtExprBuilder.mkGt(left, right);
			SmtExpr ite_expr = SmtExprBuilder.mkITE(left_gt_right, left, right);
			return ite_expr;

		}

		case MIN: {
			SmtExpr left_gt_right = SmtExprBuilder.mkLt(left, right);
			SmtExpr ite_expr = SmtExprBuilder.mkITE(left_gt_right, left, right);
			return ite_expr;

		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! " + e.getOperator());
		}
		}
	}

	private static SmtExpr mkBV2Int(SmtExpr bv) {
		SmtExpr bv2nat = SmtExprBuilder.mkBV2Nat(bv);
		SmtIntConstant maxIntValue = SmtExprBuilder.mkIntConstant(Integer.MAX_VALUE);
		SmtExpr condExpr = SmtExprBuilder.mkLe(bv2nat, maxIntValue);
		SmtExpr bvMinusOne = SmtExprBuilder.mkInt2BV(32, SmtExprBuilder.mkIntConstant(-1));
		SmtExpr xor = SmtExprBuilder.mkBVXOR(bv, bvMinusOne);
		SmtExpr bvOne = SmtExprBuilder.mkInt2BV(32, SmtExprBuilder.ONE_INT);
		SmtExpr bvAdd = SmtExprBuilder.mkBVADD(xor, bvOne);
		SmtExpr bv2natAdd = SmtExprBuilder.mkBV2Nat(bvAdd);

		SmtExpr thenExpr = bv2nat;
		SmtExpr elseExpr = SmtExprBuilder.mkNeg(bv2natAdd);

		SmtExpr ite = SmtExprBuilder.mkITE(condExpr, thenExpr, elseExpr);
		return ite;
	}

	@Override
	public SmtExpr visit(IntegerConstant e, Void v) {
		long longValue = e.getConcreteValue();
		SmtExpr intConst = SmtExprBuilder.mkIntConstant(longValue);
		return intConst;
	}

	@Override
	public SmtExpr visit(IntegerUnaryExpression e, Void v) {
		SmtExpr operand = e.getOperand().accept(this, null);

		if (operand == null) {
			return null;
		}

		if (!operand.isSymbolic()) {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}

		switch (e.getOperator()) {
		case ABS: {
			SmtExpr abs_expr = SmtExprBuilder.mkAbs(operand);
			return abs_expr;
		}
		case NEG: {
			SmtExpr minus_expr = SmtExprBuilder.mkNeg(operand);
			return minus_expr;
		}
		case GETNUMERICVALUE:
		case ISDIGIT:
		case ISLETTER: {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}
		default:
			throw new IllegalArgumentException(
					"The operator " + e.getOperator() + " is not a valid for integer unary expressions");
		}
	}

	@Override
	public SmtExpr visit(RealToIntegerCast e, Void v) {
		SmtExpr argument = e.getArgument().accept(this, null);
		if (argument == null) {
			return null;
		}
		if (!argument.isSymbolic()) {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}

		SmtExpr intExpr = SmtExprBuilder.mkReal2Int(argument);
		return intExpr;
	}

	@Override
	public SmtExpr visit(RealUnaryToIntegerExpression e, Void v) {
		SmtExpr operand = e.getOperand().accept(this, null);
		if (operand == null) {
			return null;
		}
		if (!operand.isSymbolic()) {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}

		switch (e.getOperator()) {
		case ROUND: {
			SmtExpr toIntExpr = SmtExprBuilder.mkReal2Int(operand);
			return toIntExpr;
		}
		case GETEXPONENT: {
			long longValue = e.getConcreteValue();
			SmtExpr intConst = SmtExprBuilder.mkIntConstant(longValue);
			return intConst;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	@Override
	public SmtExpr visit(IntegerToRealCast e, Void v) {
		SmtExpr integerExpr = e.getArgument().accept(this, null);
		if (integerExpr == null) {
			return null;
		}
		if (!integerExpr.isSymbolic()) {
			double doubleValue = e.getConcreteValue();
			return SmtExprBuilder.mkRealConstant(doubleValue);
		}

		SmtExpr realExpr = SmtExprBuilder.mkInt2Real(integerExpr);
		return realExpr;
	}

	@Override
	public SmtExpr visit(RealBinaryExpression e, Void v) {
		SmtExpr left = e.getLeftOperand().accept(this, null);
		SmtExpr right = e.getRightOperand().accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		if (!left.isSymbolic() && !right.isSymbolic()) {
			double doubleValue = e.getConcreteValue();
			return SmtExprBuilder.mkRealConstant(doubleValue);
		}

		switch (e.getOperator()) {

		case DIV: {
			if (rewriteNonLinearExpressions) {
				if (left.isSymbolic() && right.isSymbolic()) {
					RealValue r = (RealValue) e.getRightOperand();
					double rightValue = r.getConcreteValue();
					SmtExpr conc_right = SmtExprBuilder.mkRealConstant(rightValue);
					SmtExpr rewrite_expr = SmtExprBuilder.mkRealDiv(left, conc_right);
					return rewrite_expr;
				}
			}

			SmtExpr expr = SmtExprBuilder.mkRealDiv(left, right);
			return expr;
		}
		case MUL: {
			if (rewriteNonLinearExpressions) {
				if (left.isSymbolic() && right.isSymbolic()) {
					RealValue r = (RealValue) e.getRightOperand();
					double rightValue = r.getConcreteValue();
					SmtExpr conc_right = SmtExprBuilder.mkRealConstant(rightValue);
					SmtExpr rewrite_expr = SmtExprBuilder.mkMul(left, conc_right);
					return rewrite_expr;
				}
			}

			SmtExpr expr = SmtExprBuilder.mkMul(left, right);
			return expr;
		}
		case MINUS: {
			SmtExpr expr = SmtExprBuilder.mkSub(left, right);
			return expr;
		}
		case PLUS: {
			SmtExpr expr = SmtExprBuilder.mkAdd(left, right);
			return expr;
		}
		case MAX: {
			SmtExpr left_gt_right = SmtExprBuilder.mkGt(left, right);
			SmtExpr ite_expr = SmtExprBuilder.mkITE(left_gt_right, left, right);
			return ite_expr;

		}
		case MIN: {
			SmtExpr left_gt_right = SmtExprBuilder.mkLt(left, right);
			SmtExpr ite_expr = SmtExprBuilder.mkITE(left_gt_right, left, right);
			return ite_expr;
		}
		case ATAN2:
		case COPYSIGN:
		case HYPOT:
		case NEXTAFTER:
		case POW:
		case SCALB:
		case IEEEREMAINDER:
		case REM: {
			double concreteValue = e.getConcreteValue();
			SmtExpr realConstant = createRatNumber(concreteValue);
			return realConstant;
		}

		default: {
			throw new UnsupportedOperationException("Not implemented yet! " + e.getOperator());
		}
		}

	}

	@Override
	public SmtExpr visit(RealConstant e, Void v) {
		double doubleVal = e.getConcreteValue();
		SmtExpr realExpr = createRatNumber(doubleVal);
		return realExpr;
	}

	@Override
	public SmtExpr visit(RealUnaryExpression e, Void v) {
		SmtExpr operand = e.getOperand().accept(this, null);

		if (operand == null) {
			return null;
		}

		if (!operand.isSymbolic()) {
			double doubleVal = e.getConcreteValue();
			return createRatNumber(doubleVal);
		}

		switch (e.getOperator()) {
		case ABS: {
			SmtRealConstant zero_rational = SmtExprBuilder.ZERO_REAL;
			SmtExpr gte_than_zero = SmtExprBuilder.mkGe(operand, zero_rational);
			SmtExpr minus_expr = SmtExprBuilder.mkNeg(operand);

			SmtExpr ite_expr = SmtExprBuilder.mkITE(gte_than_zero, operand, minus_expr);
			return ite_expr;
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
			double doubleVal = e.getConcreteValue();
			return createRatNumber(doubleVal);
		}
		case GETEXPONENT:
		case ROUND: {
			throw new IllegalArgumentException(
					"The Operation " + e.getOperator() + " does not return a real expression!");
		}

		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	private static SmtExpr createRatNumber(Double doubleVal) {
		SmtExpr concreteRatNum;
		if (doubleVal.isNaN() || doubleVal.isInfinite()) {
			return null;
		} else {
			concreteRatNum = SmtExprBuilder.mkRealConstant(doubleVal);
		}
		return concreteRatNum;
	}

	@Override
	public SmtExpr visit(RealVariable e, Void v) {
		String varName = e.getName();
		SmtRealVariable var = SmtExprBuilder.mkRealVariable(varName);
		return var;
	}

	@Override
	public SmtExpr visit(IntegerVariable e, Void v) {
		String varName = e.getName();
		SmtIntVariable var = SmtExprBuilder.mkIntVariable(varName);
		return var;
	}

	@Override
	public SmtExpr visit(StringConstant e, Void v) {
		String stringValue = e.getConcreteValue();
		return SmtExprBuilder.mkStringConstant(stringValue);
	}

	@Override
	public SmtExpr visit(StringMultipleExpression e, Void v) {

		Expression<String> leftOperand = e.getLeftOperand();
		Expression<?> rightOperand = e.getRightOperand();
		ArrayList<Expression<?>> othersOperands = e.getOther();

		SmtExpr left = leftOperand.accept(this, null);
		SmtExpr right = rightOperand.accept(this, null);
		List<SmtExpr> others = new LinkedList<SmtExpr>();
		for (Expression<?> otherExpr : othersOperands) {
			SmtExpr other = otherExpr.accept(this, null);
			others.add(other);
		}

		if (isNull(left, right, others)) {
			return null;
		}

		if (!isSymbolic(left, right, others)) {
			String stringValue = e.getConcreteValue();
			SmtExpr strConstant = SmtExprBuilder.mkStringConstant(stringValue);
			return strConstant;
		}

		Operator op = e.getOperator();
		switch (op) {
		case SUBSTRING: {
			SmtExpr startIndex = right;
			SmtExpr endIndex = others.get(0);
			SmtExpr offset = SmtExprBuilder.mkSub(endIndex, startIndex);
			SmtExpr substring = SmtExprBuilder.mkStrSubstring(left, startIndex, offset);
			return substring;
		}
		case REPLACEC: {
			long concreteTarget = (Long) rightOperand.getConcreteValue();
			long concreteReplacement = (Long) othersOperands.get(0).getConcreteValue();

			String targetString = String.valueOf((char) concreteTarget);
			String replacementString = String.valueOf((char) concreteReplacement);

			SmtExpr target = SmtExprBuilder.mkStringConstant(targetString);
			SmtExpr replacement = SmtExprBuilder.mkStringConstant(replacementString);

			SmtExpr replace = SmtExprBuilder.mkStrReplace(left, target, replacement);
			return replace;
		}
		case REPLACECS: {
			SmtExpr target = right;
			SmtExpr replacement = others.get(0);

			SmtExpr replace = SmtExprBuilder.mkStrReplace(left, target, replacement);
			return replace;

		}
		case REPLACEALL:
		case REPLACEFIRST: {
			String stringValue = e.getConcreteValue();
			SmtExpr strConstant = SmtExprBuilder.mkStringConstant(stringValue);
			return strConstant;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! " + op);
		}

	}

	/**
	 * Returns true if any of the arguments is null
	 * 
	 * @param left
	 * @param right
	 * @param others
	 * @return
	 */
	private static boolean isNull(SmtExpr left, SmtExpr right, List<SmtExpr> others) {
		if (left == null || right == null) {
			return true;
		}
		for (SmtExpr smtExpr : others) {
			if (smtExpr == null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public SmtExpr visit(StringUnaryExpression e, Void v) {
		SmtExpr operand = e.getOperand().accept(this, null);
		if (operand == null) {
			return null;
		}
		if (!operand.isSymbolic()) {
			String stringValue = e.getConcreteValue();
			return SmtExprBuilder.mkStringConstant(stringValue);
		}
		Operator op = e.getOperator();
		switch (op) {
		case TRIM:
		case TOLOWERCASE:
		case TOUPPERCASE: {
			String stringValue = e.getConcreteValue();
			return SmtExprBuilder.mkStringConstant(stringValue);
		}
		default:
			throw new IllegalArgumentException("The operation " + op + " is not a unary string operation");
		}
	}

	@Override
	public SmtExpr visit(StringVariable e, Void v) {
		String varName = e.getName();
		return SmtExprBuilder.mkStringVariable(varName);
	}

	@Override
	public SmtExpr visit(StringBinaryExpression e, Void v) {
		SmtExpr left = e.getLeftOperand().accept(this, null);
		SmtExpr right = e.getRightOperand().accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		if (!left.isSymbolic() && !right.isSymbolic()) {
			String stringValue = e.getConcreteValue();
			SmtExpr strConstant = SmtExprBuilder.mkStringConstant(stringValue);
			return strConstant;
		}

		Operator op = e.getOperator();

		switch (op) {
		case CONCAT: {
			SmtExpr concatExpr = SmtExprBuilder.mkStrConcat(left, right);
			return concatExpr;
		}
		case APPEND_STRING: {
			SmtExpr concatExpr = SmtExprBuilder.mkStrConcat(left, right);
			return concatExpr;

		}
		case APPEND_INTEGER: {
			SmtExpr rigthStr = SmtExprBuilder.mkIntToStr(right);
			SmtExpr concatExpr = SmtExprBuilder.mkStrConcat(left, rigthStr);
			return concatExpr;
		}
		case APPEND_BOOLEAN: {
			SmtIntConstant zero = SmtExprBuilder.ZERO_INT;
			SmtExpr eqZero = SmtExprBuilder.mkEq(right, zero);
			SmtStringConstant falseConstantExpr = SmtExprBuilder.mkStringConstant(String.valueOf(Boolean.FALSE));
			SmtStringConstant trueConstantExpr = SmtExprBuilder.mkStringConstant(String.valueOf(Boolean.TRUE));
			SmtExpr ite = SmtExprBuilder.mkITE(eqZero, falseConstantExpr, trueConstantExpr);
			SmtExpr concatExpr = SmtExprBuilder.mkStrConcat(left, ite);
			return concatExpr;
		}
		case APPEND_CHAR: {
			SmtExpr rigthStr = SmtExprBuilder.mkIntToChar(right);
			SmtExpr concatExpr = SmtExprBuilder.mkStrConcat(left, rigthStr);
			return concatExpr;
		}
		case APPEND_REAL: {
			String stringValue = e.getConcreteValue();
			SmtExpr strConstant = SmtExprBuilder.mkStringConstant(stringValue);
			return strConstant;
		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! " + op);
		}
		}
	}

	@Override
	public SmtExpr visit(StringBinaryComparison e, Void v) {
		Expression<String> leftOperand = e.getLeftOperand();
		Expression<?> rightOperand = e.getRightOperand();
		Operator op = e.getOperator();

		SmtExpr left = leftOperand.accept(this, null);
		SmtExpr right = rightOperand.accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		if (!left.isSymbolic() && !right.isSymbolic()) {
			long longValue = e.getConcreteValue();
			SmtExpr intConst = SmtExprBuilder.mkIntConstant(longValue);
			return intConst;
		}

		SmtIntConstant oneConstant = SmtExprBuilder.ONE_INT;
		SmtIntConstant zeroConstant = SmtExprBuilder.ZERO_INT;
		switch (op) {
		case EQUALS: {
			SmtExpr equalsFormula = SmtExprBuilder.mkEq(left, right);
			SmtExpr ifThenElseFormula = SmtExprBuilder.mkITE(equalsFormula, oneConstant, zeroConstant);
			return ifThenElseFormula;
		}
		case ENDSWITH: {
			SmtExpr endsWithExpr = SmtExprBuilder.mkStrSuffixOf(right, left);
			SmtExpr ifThenElseFormula = SmtExprBuilder.mkITE(endsWithExpr, oneConstant, zeroConstant);
			return ifThenElseFormula;
		}
		case CONTAINS: {
			SmtExpr equalsFormula = SmtExprBuilder.mkStrContains(left, right);
			SmtExpr ifThenElseFormula = SmtExprBuilder.mkITE(equalsFormula, oneConstant, zeroConstant);
			return ifThenElseFormula;
		}
		case STARTSWITH: {
			throw new IllegalArgumentException("Illegal StringBinaryComparison operator " + op);
		}
		case PATTERNMATCHES: {
			String regex = e.getLeftOperand().getConcreteValue();
			String expandedRegex = RegexDistanceUtils.expandRegex(regex);
			RegExp regexp = new RegExp(expandedRegex, RegExp.INTERSECTION);
			RegExpToCVC4Visitor visitor = new RegExpToCVC4Visitor();
			SmtExpr regExpSmtExpr = visitor.visitRegExp(regexp);

			if (regExpSmtExpr == null) {
				long longValue = e.getConcreteValue();
				SmtExpr intConst = SmtExprBuilder.mkIntConstant(longValue);
				return intConst;
			} else {
				SmtExpr strInRegExp = SmtExprBuilder.mkStrInRegExp(right, regExpSmtExpr);
				SmtExpr iteExpr = SmtExprBuilder.mkITE(strInRegExp, SmtExprBuilder.ONE_INT, SmtExprBuilder.ZERO_INT);
				return iteExpr;
			}
		}
		case EQUALSIGNORECASE:
		case REGIONMATCHES:
		case APACHE_ORO_PATTERN_MATCHES: {
			long longValue = e.getConcreteValue();
			SmtExpr intConst = SmtExprBuilder.mkIntConstant(longValue);
			return intConst;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! " + op);
		}
	}

	@Override
	public SmtExpr visit(StringBinaryToIntegerExpression e, Void v) {
		Expression<String> leftOperand = e.getLeftOperand();
		Operator op = e.getOperator();
		Expression<?> rightOperand = e.getRightOperand();

		SmtExpr left = leftOperand.accept(this, null);
		SmtExpr right = rightOperand.accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		if (!left.isSymbolic() && !right.isSymbolic()) {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}

		switch (op) {
		case CHARAT: {
			SmtExpr charAtExpr = SmtExprBuilder.mkStrAt(left, right);
			SmtExpr strToInt = SmtExprBuilder.mkCharToInt(charAtExpr);
			return strToInt;
		}
		case INDEXOFS: {
			SmtExpr zeroIndex = SmtExprBuilder.mkIntConstant(0);
			SmtExpr indexOf = SmtExprBuilder.mkStrIndexOf(left, right, zeroIndex);
			return indexOf;
		}
		case INDEXOFC: {
			SmtExpr zeroIndex = SmtExprBuilder.mkIntConstant(0);
			SmtExpr charExpr = SmtExprBuilder.mkIntToChar(right);
			SmtExpr indexOf = SmtExprBuilder.mkStrIndexOf(left, charExpr, zeroIndex);
			return indexOf;
		}
		case LASTINDEXOFC:
		case LASTINDEXOFS:
		case COMPARETO:
		case COMPARETOIGNORECASE: {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}

		default: {
			throw new UnsupportedOperationException("Not implemented yet!" + e.getOperator());
		}

		}
	}

	@Override
	public SmtExpr visit(StringMultipleComparison e, Void v) {
		Expression<String> leftOperand = e.getLeftOperand();
		Expression<?> rightOperand = e.getRightOperand();
		Operator op = e.getOperator();
		ArrayList<Expression<?>> othersOperands = e.getOther();

		SmtExpr left = leftOperand.accept(this, null);
		SmtExpr right = rightOperand.accept(this, null);
		List<SmtExpr> others = new LinkedList<SmtExpr>();
		for (Expression<?> otherOperand : othersOperands) {
			SmtExpr other = otherOperand.accept(this, null);
			others.add(other);
		}

		if (isNull(left, right, others)) {
			return null;
		}

		if (!isSymbolic(left, right, others)) {
			long longValue = e.getConcreteValue();
			SmtExpr intConst = SmtExprBuilder.mkIntConstant(longValue);
			return intConst;
		}

		switch (op) {
		case STARTSWITH: {

			SmtExpr indexExpr = others.get(0);
			if (indexExpr.equals(SmtExprBuilder.ZERO_INT)) {
				SmtIntConstant oneExpr = SmtExprBuilder.ONE_INT;
				SmtIntConstant zeroExpr = SmtExprBuilder.ZERO_INT;
				SmtExpr startsWithFormula = SmtExprBuilder.mkStrPrefixOf(right, left);
				SmtExpr ifThenElseFormula = SmtExprBuilder.mkITE(startsWithFormula, oneExpr, zeroExpr);
				return ifThenElseFormula;

			} else {
				long longValue = e.getConcreteValue();
				SmtExpr intConst = SmtExprBuilder.mkIntConstant(longValue);
				return intConst;
			}
		}
		case EQUALS:
		case EQUALSIGNORECASE:
		case ENDSWITH:
		case CONTAINS: {
			throw new IllegalArgumentException("Illegal StringMultipleComparison operator " + op);
		}
		case REGIONMATCHES:
		case PATTERNMATCHES:
		case APACHE_ORO_PATTERN_MATCHES: {
			long longValue = e.getConcreteValue();
			SmtExpr intConst = SmtExprBuilder.mkIntConstant(longValue);
			return intConst;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! " + op);
		}
	}

	@Override
	public SmtExpr visit(StringMultipleToIntegerExpression e, Void v) {
		SmtExpr left = e.getLeftOperand().accept(this, null);
		SmtExpr right = e.getRightOperand().accept(this, null);
		List<SmtExpr> others = new LinkedList<SmtExpr>();
		for (Expression<?> otherExpr : e.getOther()) {
			SmtExpr otherSmtExpr = otherExpr.accept(this, null);
			others.add(otherSmtExpr);
		}

		if (isNull(left, right, others)) {
			return null;
		}

		if (!isSymbolic(left, right, others)) {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}

		Operator op = e.getOperator();
		switch (op) {
		case INDEXOFSI: {
			SmtExpr other = e.getOther().get(0).accept(this, null);
			SmtExpr indexOf = SmtExprBuilder.mkStrIndexOf(left, right, other);
			return indexOf;

		}
		case INDEXOFCI: {
			SmtExpr int2Str = SmtExprBuilder.mkIntToChar(right);
			SmtExpr other = e.getOther().get(0).accept(this, null);
			SmtExpr indexOf = SmtExprBuilder.mkStrIndexOf(left, int2Str, other);

			return indexOf;
		}
		case LASTINDEXOFCI:
		case LASTINDEXOFSI: {
			long longValue = e.getConcreteValue();
			SmtExpr intNum = SmtExprBuilder.mkIntConstant(longValue);
			return intNum;
		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! " + op);
		}
		}
	}

	/**
	 * Returns true if any of the arguments is symbolic
	 * 
	 * @param left
	 * @param right
	 * @param others
	 * @return
	 */
	private static boolean isSymbolic(SmtExpr left, SmtExpr right, List<SmtExpr> others) {
		if (left.isSymbolic() || right.isSymbolic()) {
			return true;
		}
		for (SmtExpr smtExpr : others) {
			if (smtExpr.isSymbolic()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public SmtExpr visit(StringToIntegerCast e, Void v) {
		SmtExpr argument = e.getArgument().accept(this, null);
		if (argument == null) {
			return null;
		}
		if (!argument.isSymbolic()) {
			long concreteValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(concreteValue);
		}

		SmtExpr argumentExpr = SmtExprBuilder.mkStrToInt(argument);
		return argumentExpr;
	}

	@Override
	public SmtExpr visit(StringUnaryToIntegerExpression e, Void v) {
		SmtExpr operand = e.getOperand().accept(this, null);
		if (operand == null) {
			return null;
		}
		if (!operand.isSymbolic()) {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}

		Operator op = e.getOperator();
		switch (op) {
		case LENGTH: {
			SmtExpr app = SmtExprBuilder.mkStrLen(operand);
			return app;
		}
		case IS_INTEGER: {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}
		default:
			throw new IllegalArgumentException(
					"The operator " + e.getOperator() + " is not a string operation returning an integer");
		}
	}

	@Override
	public SmtExpr visit(RealComparison e, Void v) {
		throw new IllegalStateException("RealComparison should be removed during normalization");
	}

	@Override
	public SmtExpr visit(IntegerComparison e, Void v) {
		throw new IllegalStateException("IntegerComparison should be removed during normalization");
	}

	@Override
	public SmtExpr visit(IntegerToStringCast e, Void v) {
		SmtExpr argument = e.getArgument().accept(this, null);
		if (argument == null) {
			return null;
		}
		if (!argument.isSymbolic()) {
			String stringValue = e.getConcreteValue();
			return SmtExprBuilder.mkStringConstant(stringValue);
		}
		SmtExpr intToStr = SmtExprBuilder.mkIntToStr(argument);
		return intToStr;
	}

	@Override
	public SmtExpr visit(RealToStringCast e, Void v) {
		String stringValue = e.getConcreteValue();
		return SmtExprBuilder.mkStringConstant(stringValue);
	}

	@Override
	public SmtExpr visit(NewTokenizerExpr e, Void v) {
		throw new IllegalStateException("NewTokenizerExpr should not be visited");

	}

	@Override
	public SmtExpr visit(NextTokenizerExpr e, Void v) {
		throw new IllegalStateException("NextTokenizerExpr should not be visited");
	}

	@Override
	public SmtExpr visit(StringNextTokenExpr e, Void v) {
		String stringValue = e.getConcreteValue();
		return SmtExprBuilder.mkStringConstant(stringValue);
	}

	@Override
	public SmtExpr visit(HasMoreTokensExpr e, Void v) {
		long longValue = e.getConcreteValue();
		SmtExpr intConst = SmtExprBuilder.mkIntConstant(longValue);
		return intConst;
	}

	@Override
	public SmtExpr visit(StringReaderExpr e, Void v) {
		long longValue = e.getConcreteValue();
		SmtExpr intConst = SmtExprBuilder.mkIntConstant(longValue);
		return intConst;
	}

	@Override
	public SmtExpr visit(ReferenceConstant referenceConstant, Void arg) {
		throw new UnsupportedOperationException("Translation to CVC4 of ReferenceConstant is not yet implemented!");
	}

	@Override
	public SmtExpr visit(ReferenceVariable r, Void arg) {
		throw new UnsupportedOperationException("Translation to CVC4 of ReferenceVariable is not yet implemented!");
	}

	@Override
	public SmtExpr visit(GetFieldExpression r, Void arg) {
		throw new UnsupportedOperationException("Translation to CVC4 of GetFieldExpression is not yet implemented!");
	}

}
