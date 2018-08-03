/**
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

class ExprToZ3Visitor implements ExpressionVisitor<SmtExpr, Void> {

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
			SmtExpr z3_div = SmtExprBuilder.mkIntDiv(left, right);
			return z3_div;
		}
		case MUL: {
			SmtExpr z3_mul = SmtExprBuilder.mkMul(left, right);
			return z3_mul;

		}
		case MINUS: {
			SmtExpr z3_sub = SmtExprBuilder.mkSub(left, right);
			return z3_sub;
		}
		case PLUS: {
			SmtExpr z3_add = SmtExprBuilder.mkAdd(left, right);
			return z3_add;
		}
		case REM: {
			SmtExpr z3_mod = SmtExprBuilder.mkMod(left, right);
			return z3_mod;
		}
		case IOR: {
			SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
			SmtExpr bvor = SmtExprBuilder.mkBVOR(bv_left, bv_right);
			SmtExpr ret_val = SmtExprBuilder.mkBV2Int(bvor);
			return ret_val;
		}
		case IAND: {
			SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_and = SmtExprBuilder.mkBVAND(bv_left, bv_right);
			SmtExpr ret_val = SmtExprBuilder.mkBV2Int(bv_and);
			return ret_val;
		}
		case IXOR: {
			SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_xor = SmtExprBuilder.mkBVXOR(bv_left, bv_right);
			SmtExpr ret_val = SmtExprBuilder.mkBV2Int(bv_xor);
			return ret_val;
		}

		case SHL: {
			SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_shl = SmtExprBuilder.mkBVSHL(bv_left, bv_right);
			SmtExpr ret_val = SmtExprBuilder.mkBV2Int(bv_shl);
			return ret_val;
		}

		case SHR: {
			SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_shr = SmtExprBuilder.mkBVASHR(bv_left, bv_right);
			SmtExpr ret_val = SmtExprBuilder.mkBV2Int(bv_shr);
			return ret_val;
		}
		case USHR: {
			SmtExpr bv_left = SmtExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = SmtExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_shr = SmtExprBuilder.mkBVLSHR(bv_left, bv_right);
			SmtExpr ret_val = SmtExprBuilder.mkBV2Int(bv_shr);
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
			throw new UnsupportedOperationException("Not implemented yet! "
					+ e.getOperator());
		}
		}
	}

	@Override
	public SmtExpr visit(IntegerConstant e, Void v) {
		long concreteValue = e.getConcreteValue();
		return SmtExprBuilder.mkIntConstant(concreteValue);
	}

	private static SmtExpr createIntegerConstant(Long longObject) {
		long concreteValue = longObject.longValue();
		return SmtExprBuilder.mkIntConstant(concreteValue);
	}

	@Override
	public SmtExpr visit(IntegerUnaryExpression e, Void v) {
		SmtExpr intExpr = e.getOperand().accept(this, null);

		if (intExpr == null) {
			return null;
		}

		if (!intExpr.isSymbolic()) {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}

		switch (e.getOperator()) {
		case NEG: {
			SmtExpr minus_expr = SmtExprBuilder.mkNeg(intExpr);
			return minus_expr;
		}
		case GETNUMERICVALUE:
		case ISDIGIT:
		case ISLETTER: {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}
		case ABS:
			SmtExpr zero = SmtExprBuilder.mkIntConstant(0);
			SmtExpr gte_than_zero = SmtExprBuilder.mkGe(intExpr, zero);
			SmtExpr minus_expr = SmtExprBuilder.mkNeg(intExpr);

			SmtExpr ite_expr = SmtExprBuilder.mkITE(gte_than_zero, intExpr,
					minus_expr);
			return ite_expr;
		default:
			throw new UnsupportedOperationException("Not implemented yet!" + e.getOperator());
		}
	}

	@Override
	public SmtExpr visit(RealToIntegerCast e, Void v) {
		SmtExpr realExpr = e.getArgument().accept(this, null);
		if (realExpr == null) {
			return null;
		}
		if (!realExpr.isSymbolic()) {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}
		SmtExpr intExpr = SmtExprBuilder.mkReal2Int(realExpr);
		return intExpr;
	}

	@Override
	public SmtExpr visit(RealUnaryToIntegerExpression e, Void v) {
		SmtExpr realExpr = e.getOperand().accept(this, null);
		if (realExpr == null) {
			return null;
		}
		if (!realExpr.isSymbolic()) {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}
		switch (e.getOperator()) {
		case ROUND: {
			SmtExpr smtExpr = SmtExprBuilder.mkReal2Int(realExpr);
			return smtExpr;

		}
		case GETEXPONENT:{
			long longObject = e.getConcreteValue();
			SmtExpr intConst = SmtExprBuilder.mkIntConstant(longObject);
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
			double doubleVal = e.getConcreteValue();
			return mkRepresentableRealConstant(doubleVal);
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
			return mkRepresentableRealConstant(doubleValue);
		}

		switch (e.getOperator()) {

		case DIV: {
			SmtExpr z3_div = SmtExprBuilder.mkRealDiv(left, right);
			return z3_div;
		}
		case MUL: {
			SmtExpr z3_mul = SmtExprBuilder.mkMul(left, right);
			return z3_mul;

		}
		case MINUS: {
			SmtExpr z3_sub = SmtExprBuilder.mkSub(left, right);
			return z3_sub;
		}
		case PLUS: {
			SmtExpr z3_add = SmtExprBuilder.mkAdd(left, right);
			return z3_add;
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
			double doubleValue = e.getConcreteValue();
			return mkRepresentableRealConstant(doubleValue);
		}

		default: {
			throw new UnsupportedOperationException("Not implemented yet! "
					+ e.getOperator());
		}
		}

	}

	@Override
	public SmtExpr visit(RealConstant e, Void v) {
		double doubleValue = e.getConcreteValue();
		return mkRepresentableRealConstant(doubleValue);
	}

	@Override
	public SmtExpr visit(RealUnaryExpression e, Void v) {
		SmtExpr intExpr = e.getOperand().accept(this, null);

		if (intExpr == null) {
			return null;
		}


		if (!intExpr.isSymbolic()) {
			double doubleValue = e.getConcreteValue();
			return mkRepresentableRealConstant(doubleValue);
		}
		
		switch (e.getOperator()) {
		case ABS: {
			SmtExpr zero_rational = SmtExprBuilder.mkRealConstant(0);
			SmtExpr gte_than_zero = SmtExprBuilder.mkGe(intExpr, zero_rational);
			SmtExpr minus_expr = SmtExprBuilder.mkNeg(intExpr);
			SmtExpr ite_expr = SmtExprBuilder.mkITE(gte_than_zero, intExpr,
					minus_expr);
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
		case GETEXPONENT:
		case LOG:
		case LOG10:
		case LOG1P:
		case NEXTUP:
		case RINT:
		case ROUND:
		case SIGNUM:
		case SQRT:
		case TODEGREES:
		case TORADIANS:
		case ULP: {
			double doubleValue = e.getConcreteValue();
			return mkRepresentableRealConstant(doubleValue);
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	private static SmtExpr mkRepresentableRealConstant(double doubleValue) {
		if (isRepresentable(doubleValue)) {
			return SmtExprBuilder.mkRealConstant(doubleValue);
		} else {
			return null;
		}
	}

	private static boolean isRepresentable(Double doubleVal) {
		return !doubleVal.isNaN() && !doubleVal.isInfinite();
	}

	@Override
	public SmtExpr visit(RealVariable e, Void v) {
		String varName = e.getName();
		return SmtExprBuilder.mkRealVariable(varName);
	}

	@Override
	public SmtExpr visit(IntegerVariable e, Void v) {
		String varName = e.getName();
		return SmtExprBuilder.mkIntVariable(varName);
	}

	@Override
	public SmtExpr visit(RealComparison e, Void v) {
		throw new IllegalStateException(
				"RealComparison should be removed during normalization");
	}

	@Override
	public SmtExpr visit(IntegerComparison e, Void v) {
		throw new IllegalStateException(
				"IntegerComparison should be removed during normalization");
	}

	@Override
	public SmtExpr visit(IntegerToStringCast e, Void v) {
		String stringValue = e.getConcreteValue();
		return SmtExprBuilder.mkStringConstant(stringValue);
	}


	@Override
	public SmtExpr visit(RealToStringCast n, Void arg) {
		String stringValue = n.getConcreteValue();
		return SmtExprBuilder.mkStringConstant(stringValue);
	}

	@Override
	public SmtExpr visit(HasMoreTokensExpr e, Void v) {
		Long longObject = e.getConcreteValue();
		SmtExpr intConst = createIntegerConstant(longObject);
		return intConst;
	}

	@Override
	public SmtExpr visit(NewTokenizerExpr e, Void v) {
		// TODO
		throw new IllegalStateException(
				"NewTokenizerExpr is not implemented yet");

	}

	@Override
	public SmtExpr visit(NextTokenizerExpr e, Void v) {
		// TODO
		throw new IllegalStateException(
				"NextTokenizerExpr is not implemented yet");
	}

	@Override
	public SmtExpr visit(ReferenceConstant referenceConstant, Void arg) {
		throw new UnsupportedOperationException("Translation to Z3 of ReferenceConstant is not yet implemented!");
	}

	@Override
	public SmtExpr visit(ReferenceVariable r, Void arg) {
		throw new UnsupportedOperationException("Translation to Z3 of ReferenceVariable is not yet implemented!");

	}

	@Override
	public SmtExpr visit(GetFieldExpression r, Void arg) {
		throw new UnsupportedOperationException("Translation to Z3 of GetFieldExpression is not yet implemented!");

	}

	@Override
	public SmtExpr visit(StringBinaryComparison e, Void arg) {
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
			return SmtExprBuilder.mkIntConstant(longValue);
		}
	
		switch (op) {
		case EQUALS: {
			SmtExpr equalsFormula = SmtExprBuilder.mkEq(left, right);
			SmtExpr ifThenElseExpr = SmtExprBuilder.mkITE(equalsFormula, SmtExprBuilder.ONE_INT,
					SmtExprBuilder.ZERO_INT);
			return ifThenElseExpr;
		}
		case ENDSWITH: {
			SmtExpr endsWithExpr = SmtExprBuilder.mkStrSuffixOf(right, left);
			SmtExpr ifThenElseExpr = SmtExprBuilder.mkITE(endsWithExpr, SmtExprBuilder.ONE_INT,
					SmtExprBuilder.ZERO_INT);
			return ifThenElseExpr;
		}
		case CONTAINS: {
			SmtExpr containsExpr = SmtExprBuilder.mkStrContains(left, right);
			SmtExpr ifThenElseExpr = SmtExprBuilder.mkITE(containsExpr, SmtExprBuilder.ONE_INT,
					SmtExprBuilder.ZERO_INT);
			return ifThenElseExpr;
		}
		case STARTSWITH: {
			SmtExpr startsWithExpr = SmtExprBuilder.mkStrPrefixOf(right, left);
			SmtExpr eqTrue = SmtExprBuilder.mkEq(startsWithExpr, SmtExprBuilder.TRUE);
			SmtExpr ifThenElseExpr = SmtExprBuilder.mkITE(eqTrue, SmtExprBuilder.ONE_INT, SmtExprBuilder.ZERO_INT);
			return ifThenElseExpr;
		}
		case EQUALSIGNORECASE:
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
	public SmtExpr visit(StringBinaryExpression e, Void arg) {
		SmtExpr left = e.getLeftOperand().accept(this, null);
		SmtExpr right = e.getRightOperand().accept(this, null);
		Operator op = e.getOperator();
	
		if (left == null || right == null) {
			return null;
		}
	
		if (!left.isSymbolic() && !right.isSymbolic()) {
			String stringValue = e.getConcreteValue();
			return SmtExprBuilder.mkStringConstant(stringValue);
		}
	
		switch (op) {
		case APPEND_BOOLEAN:
		case APPEND_CHAR:
		case APPEND_INTEGER: {
			long longValue = (Long) e.getRightOperand().getConcreteValue();
			String concreteRight = String.valueOf(longValue);
			SmtExpr concreteRightConstant = SmtExprBuilder.mkStringConstant(concreteRight);
			return SmtExprBuilder.mkStrConcat(left, concreteRightConstant);
		}
		case APPEND_REAL: {
			double doubleValue = (Double) e.getRightOperand().getConcreteValue();
			String concreteRight = String.valueOf(doubleValue);
			SmtExpr concreteRightConstant = SmtExprBuilder.mkStringConstant(concreteRight);
			return SmtExprBuilder.mkStrConcat(left, concreteRightConstant);
		}
		case APPEND_STRING:
		case CONCAT: {
			return SmtExprBuilder.mkStrConcat(left, right);
		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! " + op);
		}
		}
	
	}

	@Override
	public SmtExpr visit(StringBinaryToIntegerExpression e, Void arg) {
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
		case INDEXOFS: {
			SmtExpr indexOfExpr = SmtExprBuilder.mkIndexOf(left, right);
			return indexOfExpr;
		}
		case CHARAT: {
			SmtExpr charAtExpr = SmtExprBuilder.mkStrAt(left, right);
			SmtExpr strToInt = SmtExprBuilder.mkCharToInt(charAtExpr);
			return strToInt;
		}
		case INDEXOFC:
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
	public SmtExpr visit(StringConstant n, Void arg) {
		String stringValue = n.getConcreteValue();
		return SmtExprBuilder.mkStringConstant(stringValue);
	}

	@Override
	public SmtExpr visit(StringMultipleComparison e, Void arg) {
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
	
		if (left == null || right == null) {
			return null;
		}
		for (SmtExpr expr : others) {
			if (expr == null) {
				return null;
			}
		}
	
		if (!left.isSymbolic() && !right.isSymbolic()) {
			boolean isSymbolic = false;
			for (SmtExpr smtExpr : others) {
				if (smtExpr.isSymbolic()) {
					isSymbolic = true;
					break;
				}
			}
			if (!isSymbolic) {
				long longValue = e.getConcreteValue();
				return SmtExprBuilder.mkIntConstant(longValue);
			}
		}
	
		switch (op) {
		case STARTSWITH: {
			// discard index (over-approximate solution)
			SmtExpr startsWithExpr = SmtExprBuilder.mkStrPrefixOf(right, left);
			SmtExpr ifThenElseExpr = SmtExprBuilder.mkITE(startsWithExpr, SmtExprBuilder.ONE_INT,
					SmtExprBuilder.ZERO_INT);
			return ifThenElseExpr;
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
	public SmtExpr visit(StringMultipleExpression e, Void arg) {
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
	
		if (left == null || right == null) {
			return null;
		}
		for (SmtExpr expr : others) {
			if (expr == null) {
				return null;
			}
		}
	
		if (!left.isSymbolic() && !right.isSymbolic()) {
			boolean isSymbolic = false;
			for (SmtExpr smtExpr : others) {
				if (smtExpr.isSymbolic()) {
					isSymbolic = true;
				}
			}
			if (!isSymbolic) {
				String stringValue = e.getConcreteValue();
				return SmtExprBuilder.mkStringConstant(stringValue);
			}
		}
	
		switch (op) {
		case REPLACECS: {
			SmtExpr string = left;
			SmtExpr target = right;
			SmtExpr replacement = others.get(0);
			SmtExpr substringExpr = SmtExprBuilder.mkReplace(string, target, replacement);
			return substringExpr;
		}
	
		case SUBSTRING: {
			SmtExpr string = left;
			SmtExpr fromExpr = right;
			SmtExpr toExpr = others.get(0);
			SmtExpr substringExpr = SmtExprBuilder.mkSubstring(string, fromExpr, toExpr);
			return substringExpr;
		}
		case REPLACEC:
		case REPLACEALL:
		case REPLACEFIRST: {
			String stringValue = e.getConcreteValue();
			return SmtExprBuilder.mkStringConstant(stringValue);
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! " + op);
		}
	
	}

	@Override
	public SmtExpr visit(StringMultipleToIntegerExpression e, Void arg) {
	
		Operator op = e.getOperator();
		switch (op) {
		case INDEXOFCI:
		case INDEXOFSI: {
			// over-approximate using INDEXOF
			SmtExpr left = e.getLeftOperand().accept(this, null);
			SmtExpr right = e.getRightOperand().accept(this, null);
			SmtExpr indexOfExpr = SmtExprBuilder.mkIndexOf(left, right);
			return indexOfExpr;
		}
		case LASTINDEXOFCI:
		case LASTINDEXOFSI: {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! " + op);
		}
		}
	}

	@Override
	public SmtExpr visit(StringNextTokenExpr n, Void arg) {
		String stringValue = n.getConcreteValue();
		return SmtExprBuilder.mkStringConstant(stringValue);
	}

	@Override
	public SmtExpr visit(StringReaderExpr e, Void arg) {
		long longValue = e.getConcreteValue();
		SmtExpr intConst = SmtExprBuilder.mkIntConstant(longValue);
		return intConst;
	}

	@Override
	public SmtExpr visit(StringToIntegerCast n, Void arg) {
		long longValue = n.getConcreteValue();
		return SmtExprBuilder.mkIntConstant(longValue);
	}

	@Override
	public SmtExpr visit(StringUnaryExpression e, Void arg) {
		Operator op = e.getOperator();
		switch (op) {
		case TRIM:
		case TOLOWERCASE:
		case TOUPPERCASE: {
			String stringValue = e.getConcreteValue();
			return SmtExprBuilder.mkStringConstant(stringValue);
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! " + op);
		}
	}

	@Override
	public SmtExpr visit(StringUnaryToIntegerExpression e, Void arg) {
		SmtExpr innerString = e.getOperand().accept(this, null);
		if (innerString == null) {
			return null;
		}
		if (!innerString.isSymbolic()) {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}
	
		Operator op = e.getOperator();
		switch (op) {
		case LENGTH: {
			return SmtExprBuilder.mkStringLength(innerString);
		}
		case IS_INTEGER: {
			long longValue = e.getConcreteValue();
			return SmtExprBuilder.mkIntConstant(longValue);
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	@Override
	public SmtExpr visit(StringVariable e, Void arg) {
		String varName = e.getName();
		return SmtExprBuilder.mkStringVariable(varName);
	}


}
