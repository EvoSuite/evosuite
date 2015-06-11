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

class ExprToZ3Visitor implements ExpressionVisitor<String, Void> {

	@Override
	public String visit(IntegerBinaryExpression e, Void v) {
		String left = e.getLeftOperand().accept(this, null);
		String right = e.getRightOperand().accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		switch (e.getOperator()) {

		case DIV: {
			String z3_div = Z3ExprBuilder.mkDiv(left, right);
			return z3_div;
		}
		case MUL: {
			String z3_mul = Z3ExprBuilder.mkMul(left, right);
			return z3_mul;

		}
		case MINUS: {
			String z3_sub = Z3ExprBuilder.mkSub(left, right);
			return z3_sub;
		}
		case PLUS: {
			String z3_add = Z3ExprBuilder.mkAdd(left, right);
			return z3_add;
		}
		case REM: {
			String z3_mod = Z3ExprBuilder.mkMod(left, right);
			return z3_mod;
		}
		case IOR: {
			String bv_left = Z3ExprBuilder.mkInt2BV(32, left);
			String bv_right = Z3ExprBuilder.mkInt2BV(32, right);
			String bvor = Z3ExprBuilder.mkBVOR(bv_left, bv_right);
			String ret_val = Z3ExprBuilder.mkBV2Int(bvor);
			return ret_val;
		}
		case IAND: {
			String bv_left = Z3ExprBuilder.mkInt2BV(32, left);
			String bv_right = Z3ExprBuilder.mkInt2BV(32, right);
			String bv_and = Z3ExprBuilder.mkBVAND(bv_left, bv_right);
			String ret_val = Z3ExprBuilder.mkBV2Int(bv_and);
			return ret_val;
		}
		case IXOR: {
			String bv_left = Z3ExprBuilder.mkInt2BV(32, left);
			String bv_right = Z3ExprBuilder.mkInt2BV(32, right);
			String bv_xor = Z3ExprBuilder.mkBVXOR(bv_left, bv_right);
			String ret_val = Z3ExprBuilder.mkBV2Int(bv_xor);
			return ret_val;
		}

		case SHL: {
			String bv_left = Z3ExprBuilder.mkInt2BV(32, left);
			String bv_right = Z3ExprBuilder.mkInt2BV(32, right);
			String bv_shl = Z3ExprBuilder.mkBVSHL(bv_left, bv_right);
			String ret_val = Z3ExprBuilder.mkBV2Int(bv_shl);
			return ret_val;
		}

		case SHR: {
			String bv_left = Z3ExprBuilder.mkInt2BV(32, left);
			String bv_right = Z3ExprBuilder.mkInt2BV(32, right);
			String bv_shr = Z3ExprBuilder.mkBVASHR(bv_left, bv_right);
			String ret_val = Z3ExprBuilder.mkBV2Int(bv_shr);
			return ret_val;
		}
		case USHR: {
			String bv_left = Z3ExprBuilder.mkInt2BV(32, left);
			String bv_right = Z3ExprBuilder.mkInt2BV(32, right);
			String bv_shr = Z3ExprBuilder.mkBVLSHR(bv_left, bv_right);
			String ret_val = Z3ExprBuilder.mkBV2Int(bv_shr);
			return ret_val;
		}

		case MAX: {
			String left_gt_right = Z3ExprBuilder.mkGt(left, right);
			String ite_expr = Z3ExprBuilder.mkITE(left_gt_right, left, right);
			return ite_expr;

		}

		case MIN: {
			String left_gt_right = Z3ExprBuilder.mkLt(left, right);
			String ite_expr = Z3ExprBuilder.mkITE(left_gt_right, left, right);
			return ite_expr;

		}

		default: {
			throw new UnsupportedOperationException("Not implemented yet! "
					+ e.getOperator());
		}
		}
	}

	@Override
	public String visit(IntegerConstant e, Void v) {
		Long longObject = e.getConcreteValue();
		String intConst = createIntegerConstant(longObject);
		return intConst;
	}

	private static String createIntegerConstant(Long longObject) {
		String int_num;
		if (longObject.longValue() >= Integer.MIN_VALUE
				&& longObject.longValue() <= Integer.MAX_VALUE) {
			int intValue = longObject.intValue();
			int_num = Z3ExprBuilder.mkIntegerConstant(intValue);
		} else {
			long longValue = longObject.longValue();
			int_num = Z3ExprBuilder.mkIntegerConstant(longValue);
		}
		return int_num;
	}

	@Override
	public String visit(IntegerUnaryExpression e, Void v) {
		String intExpr = e.getOperand().accept(this, null);

		if (intExpr == null) {
			return null;
		}

		switch (e.getOperator()) {
		case ABS:
			String zero = Z3ExprBuilder.mkIntegerConstant(0);
			String gte_than_zero = Z3ExprBuilder.mkGe(intExpr, zero);
			String minus_expr = Z3ExprBuilder.mkNeg(intExpr);

			String ite_expr = Z3ExprBuilder.mkITE(gte_than_zero, intExpr,
					minus_expr);
			return ite_expr;
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	@Override
	public String visit(RealToIntegerCast e, Void v) {
		String realExpr = e.getArgument().accept(this, null);
		if (realExpr == null) {
			return null;
		}
		String intExpr = Z3ExprBuilder.mkReal2Int(realExpr);
		return intExpr;
	}

	@Override
	public String visit(RealUnaryToIntegerExpression e, Void v) {
		String realExpr = e.getOperand().accept(this, null);
		if (realExpr == null) {
			return null;
		}

		switch (e.getOperator()) {
		case GETEXPONENT:
		case ROUND: {
			Long longObject = e.getConcreteValue();
			String intConst = createIntegerConstant(longObject);
			return intConst;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	@Override
	public String visit(IntegerToRealCast e, Void v) {
		String integerExpr = e.getArgument().accept(this, null);
		if (integerExpr == null) {
			return null;
		}
		String realExpr = Z3ExprBuilder.mkInt2Real(integerExpr);
		return realExpr;

	}

	@Override
	public String visit(RealBinaryExpression e, Void v) {
		String left = e.getLeftOperand().accept(this, null);
		String right = e.getRightOperand().accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		switch (e.getOperator()) {

		case DIV: {
			String z3_div = Z3ExprBuilder.mkDiv(left, right);
			return z3_div;
		}
		case MUL: {
			String z3_mul = Z3ExprBuilder.mkMul(left, right);
			return z3_mul;

		}
		case MINUS: {
			String z3_sub = Z3ExprBuilder.mkSub(left, right);
			return z3_sub;
		}
		case PLUS: {
			String z3_add = Z3ExprBuilder.mkAdd(left, right);
			return z3_add;
		}
		case MAX: {
			String left_gt_right = Z3ExprBuilder.mkGt(left, right);
			String ite_expr = Z3ExprBuilder.mkITE(left_gt_right, left, right);
			return ite_expr;

		}

		case MIN: {
			String left_gt_right = Z3ExprBuilder.mkLt(left, right);
			String ite_expr = Z3ExprBuilder.mkITE(left_gt_right, left, right);
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
			String realConstant = Z3ExprBuilder.mkRealConstant(concreteValue);
			return realConstant;
		}

		default: {
			throw new UnsupportedOperationException("Not implemented yet! "
					+ e.getOperator());
		}
		}

	}

	@Override
	public String visit(RealConstant e, Void v) {
		double doubleVal = e.getConcreteValue();
		String realExpr = Z3ExprBuilder.mkRealConstant(doubleVal);
		return realExpr;
	}

	@Override
	public String visit(RealUnaryExpression e, Void v) {
		String intExpr = e.getOperand().accept(this, null);

		if (intExpr == null) {
			return null;
		}

		switch (e.getOperator()) {
		case ABS: {
			String zero_rational = Z3ExprBuilder.mkRealConstant(0);
			String gte_than_zero = Z3ExprBuilder.mkGe(intExpr, zero_rational);
			String minus_expr = Z3ExprBuilder.mkNeg(intExpr);

			String ite_expr = Z3ExprBuilder.mkITE(gte_than_zero, intExpr,
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
			Double doubleVal = e.getConcreteValue();
			String concreteRatNum;
			if (doubleVal.isNaN() || doubleVal.isInfinite()) {
				return null;
			} else {
				concreteRatNum = Z3ExprBuilder.mkRealConstant(doubleVal);
			}
			return concreteRatNum;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	@Override
	public String visit(RealVariable e, Void v) {
		return e.getName();
	}

	@Override
	public String visit(IntegerVariable e, Void v) {
		return e.getName();
	}

	@Override
	public String visit(StringConstant e, Void v) {
		throw new IllegalStateException(
				"This function should not be invoked by the translation!");
	}

	@Override
	public String visit(StringMultipleExpression e, Void v) {
		Operator op = e.getOperator();
		switch (op) {
		case REPLACEC:
		case REPLACECS:
		case REPLACEALL:
		case REPLACEFIRST:
		case SUBSTRING: {
			throw new IllegalStateException(
					"This function should not be invoked by the translation!");

		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}

	}

	@Override
	public String visit(StringUnaryExpression e, Void v) {
		Operator op = e.getOperator();
		switch (op) {
		case TRIM:
		case TOLOWERCASE:
		case TOUPPERCASE: {
			throw new IllegalStateException(
					"This function should not be invoked by the translation!");
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
	}

	@Override
	public String visit(StringVariable e, Void v) {
		return e.getName();
	}

	@Override
	public String visit(StringBinaryExpression e, Void v) {
		Operator op = e.getOperator();
		switch (op) {
		case APPEND_BOOLEAN:
		case APPEND_CHAR:
		case APPEND_INTEGER:
		case APPEND_REAL:
		case APPEND_STRING:
		case CONCAT: {
			throw new IllegalStateException(
					"This function should not be invoked by the translation!");
		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
		}
	}

	@Override
	public String visit(StringBinaryComparison e, Void v) {
		Operator op = e.getOperator();

		switch (op) {
		case EQUALSIGNORECASE: {
			throw new UnsupportedOperationException(
					"Must implement equalsIgnoreCase()!");
		}
		case STARTSWITH: {
			throw new IllegalArgumentException(
					"Illegal StringBinaryComparison operator " + op);
		}
		case ENDSWITH:
		case EQUALS:
		case CONTAINS:
		case REGIONMATCHES:
		case PATTERNMATCHES:
		case APACHE_ORO_PATTERN_MATCHES: {
			Long longValue = e.getConcreteValue();
			String intConst = createIntegerConstant(longValue);
			return intConst;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
	}

	@Override
	public String visit(StringBinaryToIntegerExpression e, Void v) {
		Operator op = e.getOperator();
		switch (op) {
		case CHARAT:
		case INDEXOFC:
		case INDEXOFS:
		case LASTINDEXOFC:
		case LASTINDEXOFS:
		case COMPARETO:
		case COMPARETOIGNORECASE: {
			long concreteValue = e.getConcreteValue();
			return createIntegerConstant(concreteValue);
		}

		default: {
			throw new UnsupportedOperationException("Not implemented yet!"
					+ e.getOperator());
		}

		}
	}

	@Override
	public String visit(StringMultipleComparison e, Void v) {
		Operator op = e.getOperator();

		switch (op) {
		case EQUALS:
		case EQUALSIGNORECASE:
		case ENDSWITH:
		case CONTAINS: {
			throw new IllegalArgumentException(
					"Illegal StringMultipleComparison operator " + op);
		}
		case STARTSWITH:
		case REGIONMATCHES:
		case PATTERNMATCHES:
		case APACHE_ORO_PATTERN_MATCHES: {
			Long longValue = e.getConcreteValue();
			String intConst = createIntegerConstant(longValue);
			return intConst;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
	}

	@Override
	public String visit(StringMultipleToIntegerExpression e, Void v) {
		Operator op = e.getOperator();
		switch (op) {
		case INDEXOFCI:
		case INDEXOFSI:
		case LASTINDEXOFCI:
		case LASTINDEXOFSI: {
			Long concreteValue = e.getConcreteValue();
			String intNum = createIntegerConstant(concreteValue);
			return intNum;
		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
		}
	}

	@Override
	public String visit(StringToIntegerCast e, Void v) {
		long concreteValue = e.getConcreteValue();
		return createIntegerConstant(concreteValue);
	}

	@Override
	public String visit(StringUnaryToIntegerExpression e, Void v) {
		Operator op = e.getOperator();
		switch (op) {
		case LENGTH: {
			Long concreteValue = e.getConcreteValue();
			String intNum = createIntegerConstant(concreteValue);
			return intNum;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	@Override
	public String visit(RealComparison e, Void v) {
		throw new IllegalStateException(
				"RealComparison should be removed during normalization");
	}

	@Override
	public String visit(IntegerComparison e, Void v) {
		throw new IllegalStateException(
				"IntegerComparison should be removed during normalization");
	}

	@Override
	public String visit(IntegerToStringCast e, Void v) {
		throw new IllegalStateException(
				"This function should not be invoked by the translation!");
	}

	@Override
	public String visit(RealToStringCast e, Void v) {
		throw new IllegalStateException(
				"This function should not be invoked by the translation!");
	}

	@Override
	public String visit(StringNextTokenExpr e, Void v) {
		throw new IllegalStateException(
				"This function should not be invoked by the translation!");
	}

	@Override
	public String visit(HasMoreTokensExpr e, Void v) {
		Long longObject = e.getConcreteValue();
		String intConst = createIntegerConstant(longObject);
		return intConst;
	}

	@Override
	public String visit(StringReaderExpr e, Void v) {
		Long longObject = e.getConcreteValue();
		String intConst = createIntegerConstant(longObject);
		return intConst;
	}

	@Override
	public String visit(NewTokenizerExpr e, Void v) {
		// TODO
		throw new IllegalStateException(
				"NewTokenizerExpr is not implemented yet");

	}

	@Override
	public String visit(NextTokenizerExpr e, Void v) {
		// TODO
		throw new IllegalStateException(
				"NextTokenizerExpr is not implemented yet");
	}

}
