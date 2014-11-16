package org.evosuite.symbolic.solver.z3str;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

class ExprToZ3StrVisitor implements ExpressionVisitor<String, Void> {

	@Override
	public String visit(IntegerBinaryExpression e, Void v) {
		String left = e.getLeftOperand().accept(this, null);
		String right = e.getRightOperand().accept(this, null);
		if (left == null || right == null) {
			return null;
		}

		switch (e.getOperator()) {

		case DIV: {
			return Z3StrExprBuilder.mkDiv(left, right);
		}
		case MUL: {
			return Z3StrExprBuilder.mkMul(left, right);
		}
		case MINUS: {
			return Z3StrExprBuilder.mkSub(left, right);

		}
		case PLUS: {
			return Z3StrExprBuilder.mkAdd(left, right);

		}
		case REM: {
			return Z3StrExprBuilder.mkRem(left, right);

		}
		case IOR: {
			String bv_left = Z3StrExprBuilder.mkInt2BV(32, left);
			String bv_right = Z3StrExprBuilder.mkInt2BV(32, right);
			String bvor = Z3StrExprBuilder.mkBVOR(bv_left, bv_right);
			String ret_val = Z3StrExprBuilder.mkBV2Int(bvor, true);
			return ret_val;
		}
		case IAND: {
			String bv_left = Z3StrExprBuilder.mkInt2BV(32, left);
			String bv_right = Z3StrExprBuilder.mkInt2BV(32, right);
			String bvand = Z3StrExprBuilder.mkBVAND(bv_left, bv_right);
			String ret_val = Z3StrExprBuilder.mkBV2Int(bvand, true);
			return ret_val;
		}
		case IXOR: {
			String bv_left = Z3StrExprBuilder.mkInt2BV(32, left);
			String bv_right = Z3StrExprBuilder.mkInt2BV(32, right);
			String bvxor = Z3StrExprBuilder.mkBVXOR(bv_left, bv_right);
			String ret_val = Z3StrExprBuilder.mkBV2Int(bvxor, true);
			return ret_val;
		}

		case SHL: {
			String bv_left = Z3StrExprBuilder.mkInt2BV(32, left);
			String bv_right = Z3StrExprBuilder.mkInt2BV(32, right);
			String bvshl = Z3StrExprBuilder.mkBVSHL(bv_left, bv_right);
			String ret_val = Z3StrExprBuilder.mkBV2Int(bvshl, true);
			return ret_val;
		}
		case USHR: {
			String bv_left = Z3StrExprBuilder.mkInt2BV(32, left);
			String bv_right = Z3StrExprBuilder.mkInt2BV(32, right);
			String bvlshr = Z3StrExprBuilder.mkBVLSHR(bv_left, bv_right);
			String ret_val = Z3StrExprBuilder.mkBV2Int(bvlshr, true);
			return ret_val;
		}
		case SHR: {
			String bv_left = Z3StrExprBuilder.mkInt2BV(32, left);
			String bv_right = Z3StrExprBuilder.mkInt2BV(32, right);
			String bvashr = Z3StrExprBuilder.mkBVASHR(bv_left, bv_right);
			String ret_val = Z3StrExprBuilder.mkBV2Int(bvashr, true);
			return ret_val;
		}
		case MAX: {
			String left_gt_right = "(> " + left + " " + right + ")";
			String ite_expr = Z3StrExprBuilder.mkITE(left_gt_right, left, right);
			return ite_expr;

		}
		case MIN: {
			String left_gt_right = Z3StrExprBuilder.mkLt(left, right);
			String ite_expr = Z3StrExprBuilder.mkITE(left_gt_right, left, right);
			return ite_expr;

		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! "
					+ e.getOperator());
		}
		}

	}

	@Override
	public String visit(IntegerComparison e, Void v) {
		throw new IllegalStateException(
				"IntegerComparison should be removed during normalization");
	}

	@Override
	public String visit(IntegerConstant e, Void v) {
		long concreteValue = e.getConcreteValue();
		return Z3StrExprBuilder.mkIntegerConstant(concreteValue);
	}

	@Override
	public String visit(IntegerToRealCast e, Void v) {
		String operand = e.getArgument().accept(this, null);
		if (operand == null) {
			return null;
		}
		return Z3StrExprBuilder.mkToReal(operand);
	}

	@Override
	public String visit(IntegerToStringCast e, Void v) {
		return Z3StrExprBuilder.mkStringLiteral(e.getConcreteValue());
	}

	@Override
	public String visit(IntegerUnaryExpression e, Void v) {
		String intExpr = e.getOperand().accept(this, null);
		if (intExpr == null) {
			return null;
		}

		switch (e.getOperator()) {
		case ABS:
			String zero = Z3StrExprBuilder.mkIntegerConstant(0);
			String gte_than_zero = Z3StrExprBuilder.mkGe(intExpr, zero);
			String minus_expr = Z3StrExprBuilder.mkNeg(intExpr);

			String ite_expr = Z3StrExprBuilder.mkITE(gte_than_zero, intExpr,
					minus_expr);
			return ite_expr;
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	@Override
	public String visit(IntegerVariable e, Void v) {
		return e.getName();
	}

	@Override
	public String visit(RealToIntegerCast n, Void arg) {
		String operandStr = n.getArgument().accept(this, null);
		if (operandStr == null) {
			return null;
		}
		String realToIntStr = Z3StrExprBuilder.mkReal2Int(operandStr);
		return realToIntStr;
	}

	@Override
	public String visit(RealUnaryToIntegerExpression e, Void arg) {
		String realExpr = e.getOperand().accept(this, null);
		if (realExpr == null) {
			return null;
		}

		switch (e.getOperator()) {
		case GETEXPONENT:
		case ROUND: {
			long longObject = e.getConcreteValue();
			String intConst = Z3StrExprBuilder.mkIntegerConstant(longObject);
			return intConst;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}

	}

	@Override
	public String visit(RealBinaryExpression e, Void arg) {
		String left = e.getLeftOperand().accept(this, null);
		String right = e.getRightOperand().accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		switch (e.getOperator()) {

		case DIV: {
			String z3_div = Z3StrExprBuilder.mkRealDiv(left, right);
			return z3_div;
		}
		case MUL: {
			String z3_mul = Z3StrExprBuilder.mkMul(left, right);
			return z3_mul;

		}
		case MINUS: {
			String z3_sub = Z3StrExprBuilder.mkSub(left, right);
			return z3_sub;
		}
		case PLUS: {
			String z3_add = Z3StrExprBuilder.mkAdd(left, right);
			return z3_add;
		}
		case MAX: {
			String left_gt_right = Z3StrExprBuilder.mkGt(left, right);
			String ite_expr = Z3StrExprBuilder.mkITE(left_gt_right, left, right);
			return ite_expr;

		}
		case MIN: {
			String left_gt_right = Z3StrExprBuilder.mkLt(left, right);
			String ite_expr = Z3StrExprBuilder.mkITE(left_gt_right, left, right);
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
			Double concreteValue = e.getConcreteValue();
			if (!isRepresentable(concreteValue)) {
				return null;
			} else {
				return Z3StrExprBuilder.mkRealConstant(concreteValue);
			}
		}

		default: {
			throw new UnsupportedOperationException("Not implemented yet! "
					+ e.getOperator());
		}
		}

	}

	private static boolean isRepresentable(Double doubleVal) {
		return !doubleVal.isNaN() && !doubleVal.isInfinite();
	}

	@Override
	public String visit(RealConstant n, Void arg) {
		double doubleVal = n.getConcreteValue();
		if (!isRepresentable(doubleVal)) {
			return null;
		} else {
			return Z3StrExprBuilder.mkRealConstant(doubleVal);
		}
	}

	@Override
	public String visit(RealUnaryExpression e, Void arg) {
		String intExpr = e.getOperand().accept(this, null);

		if (intExpr == null) {
			return null;
		}

		switch (e.getOperator()) {
		case ABS: {
			String zero_rational = Z3StrExprBuilder.mkRealConstant(0.0);
			String gte_than_zero = Z3StrExprBuilder.mkGe(intExpr, zero_rational);
			String minus_expr = Z3StrExprBuilder.mkNeg(intExpr);

			String ite_expr = Z3StrExprBuilder.mkITE(gte_than_zero, intExpr,
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
			if (!isRepresentable(doubleVal)) {
				return null;
			} else {
				concreteRatNum = Z3StrExprBuilder.mkRealConstant(doubleVal);
			}
			return concreteRatNum;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}

	}

	@Override
	public String visit(RealVariable n, Void arg) {
		return n.getName();
	}

	@Override
	public String visit(StringReaderExpr e, Void arg) {
		Long longObject = e.getConcreteValue();
		String intConst = Z3StrExprBuilder.mkIntegerConstant(longObject);
		return intConst;
	}

	@Override
	public String visit(StringBinaryExpression e, Void arg) {
		String left = e.getLeftOperand().accept(this, null);
		String right = e.getRightOperand().accept(this, null);
		Operator op = e.getOperator();

		if (left == null || right == null) {
			return null;
		}

		switch (op) {
		case APPEND_BOOLEAN:
		case APPEND_CHAR:
		case APPEND_INTEGER: {
			Long longValue = (Long) e.getRightOperand().getConcreteValue();
			String concreteRight = String.valueOf(longValue);
			stringConstants.add(concreteRight);
			String concreteRightConstant = Z3StrExprBuilder
					.mkStringConstant(concreteRight);
			return Z3StrExprBuilder.mkStringConcat(left, concreteRightConstant);
		}
		case APPEND_REAL: {
			Double doubleValue = (Double) e.getRightOperand()
					.getConcreteValue();
			String concreteRight = String.valueOf(doubleValue);
			stringConstants.add(concreteRight);
			String concreteRightConstant = Z3StrExprBuilder
					.mkStringConstant(concreteRight);
			return Z3StrExprBuilder.mkStringConcat(left, concreteRightConstant);
		}
		case APPEND_STRING:
		case CONCAT: {
			return Z3StrExprBuilder.mkStringConcat(left, right);
		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
		}

	}

	@Override
	public String visit(StringConstant n, Void arg) {
		String str = n.getConcreteValue();
		stringConstants.add(str);
		return Z3StrExprBuilder.mkStringConstant(str);

	}

	@Override
	public String visit(StringMultipleExpression e, Void arg) {
		Expression<String> leftOperand = e.getLeftOperand();
		Expression<?> rightOperand = e.getRightOperand();
		Operator op = e.getOperator();
		ArrayList<Expression<?>> othersOperands = e.getOther();

		String left = leftOperand.accept(this, null);
		String right = rightOperand.accept(this, null);
		List<String> others = new LinkedList<String>();
		for (Expression<?> otherOperand : othersOperands) {
			String other = otherOperand.accept(this, null);
			others.add(other);
		}

		if (left == null || right == null) {
			return null;
		}
		for (String expr : others) {
			if (expr == null) {
				return null;
			}
		}

		switch (op) {
		case REPLACECS: {
			String substringExpr = Z3StrExprBuilder.mkStringReplace(left, right,
					others.get(0));
			return substringExpr;
		}

		case SUBSTRING: {
			String substringExpr = Z3StrExprBuilder.mkStringSubstring(left, right,
					others.get(0));
			return substringExpr;
		}
		case REPLACEC:
		case REPLACEALL:
		case REPLACEFIRST: {
			String concreteValue = e.getConcreteValue();
			stringConstants.add(concreteValue);
			return Z3StrExprBuilder.mkStringConstant(concreteValue);
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}

	}

	@Override
	public String visit(StringUnaryExpression e, Void arg) {
		Operator op = e.getOperator();
		switch (op) {
		case TRIM:
		case TOLOWERCASE:
		case TOUPPERCASE: {
			String concreteValue = e.getConcreteValue();
			stringConstants.add(concreteValue);
			return Z3StrExprBuilder.mkStringConstant(concreteValue);
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
	}

	@Override
	public String visit(StringVariable e, Void arg) {
		return e.getName();
	}

	@Override
	public String visit(HasMoreTokensExpr e, Void arg) {
		Long longObject = e.getConcreteValue();
		String intConst = Z3StrExprBuilder.mkIntegerConstant(longObject);
		return intConst;
	}

	@Override
	public String visit(StringNextTokenExpr n, Void arg) {
		String concreteVal = n.getConcreteValue();
		stringConstants.add(concreteVal);
		return Z3StrExprBuilder.mkStringConstant(concreteVal);
	}

	@Override
	public String visit(RealComparison n, Void arg) {
		throw new IllegalStateException(
				"RealComparison should be removed during normalization");

	}

	@Override
	public String visit(StringBinaryComparison e, Void arg) {
		Expression<String> leftOperand = e.getLeftOperand();
		Expression<?> rightOperand = e.getRightOperand();
		Operator op = e.getOperator();

		String left = leftOperand.accept(this, null);
		String right = rightOperand.accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		switch (op) {
		case EQUALS: {
			String equalsFormula = Z3StrExprBuilder.mkEq(left, right);
			String ifThenElseExpr = Z3StrExprBuilder.mkITE(equalsFormula,
					Z3StrExprBuilder.mkIntegerConstant(1),
					Z3StrExprBuilder.mkIntegerConstant(0));
			return ifThenElseExpr;
		}
		case ENDSWITH: {
			String endsWithExpr = Z3StrExprBuilder.mkStringEndsWith(left, right);
			String ifThenElseExpr = Z3StrExprBuilder.mkITE(endsWithExpr,
					Z3StrExprBuilder.mkIntegerConstant(1),
					Z3StrExprBuilder.mkIntegerConstant(0));
			return ifThenElseExpr;
		}
		case CONTAINS: {
			String containsExpr = Z3StrExprBuilder.mkStringContains(left, right);
			String ifThenElseExpr = Z3StrExprBuilder.mkITE(containsExpr,
					Z3StrExprBuilder.mkIntegerConstant(1),
					Z3StrExprBuilder.mkIntegerConstant(0));
			return ifThenElseExpr;
		}
		case STARTSWITH: {
			String startsWithExpr = Z3StrExprBuilder.mkStringStartsWith(left,
					right);
			String ifThenElseExpr = Z3StrExprBuilder.mkITE(startsWithExpr,
					Z3StrExprBuilder.mkIntegerConstant(1),
					Z3StrExprBuilder.mkIntegerConstant(0));
			return ifThenElseExpr;
		}
		case EQUALSIGNORECASE:
		case REGIONMATCHES:
		case PATTERNMATCHES:
		case APACHE_ORO_PATTERN_MATCHES: {
			Long longValue = e.getConcreteValue();
			String intConst = Z3StrExprBuilder.mkIntegerConstant(longValue);
			return intConst;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}

	}

	@Override
	public String visit(StringBinaryToIntegerExpression e, Void arg) {
		Expression<String> leftOperand = e.getLeftOperand();
		Operator op = e.getOperator();
		Expression<?> rightOperand = e.getRightOperand();

		String left = leftOperand.accept(this, null);
		String right = rightOperand.accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		switch (op) {
		case INDEXOFS: {
			String charAtExpr = Z3StrExprBuilder.mkStringIndexOf(left, right);
			return charAtExpr;
		}
		case CHARAT:
		case INDEXOFC:
		case LASTINDEXOFC:
		case LASTINDEXOFS:
		case COMPARETO:
		case COMPARETOIGNORECASE: {
			long concreteValue = e.getConcreteValue();
			return Z3StrExprBuilder.mkIntegerConstant(concreteValue);
		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet!"
					+ e.getOperator());
		}
		}

	}

	@Override
	public String visit(StringMultipleComparison e, Void arg) {
		Expression<String> leftOperand = e.getLeftOperand();
		Expression<?> rightOperand = e.getRightOperand();
		Operator op = e.getOperator();
		ArrayList<Expression<?>> othersOperands = e.getOther();

		String left = leftOperand.accept(this, null);
		String right = rightOperand.accept(this, null);
		List<String> others = new LinkedList<String>();
		for (Expression<?> otherOperand : othersOperands) {
			String other = otherOperand.accept(this, null);
			others.add(other);
		}

		if (left == null || right == null) {
			return null;
		}
		for (String expr : others) {
			if (expr == null) {
				return null;
			}
		}

		switch (op) {
		case STARTSWITH: {
			// discard index (over-approximate solution)
			String startsWithExpr = Z3StrExprBuilder.mkStringStartsWith(left,
					right);
			String ifThenElseExpr = Z3StrExprBuilder.mkITE(startsWithExpr,
					Z3StrExprBuilder.mkIntegerConstant(1),
					Z3StrExprBuilder.mkIntegerConstant(0));
			return ifThenElseExpr;
		}
		case EQUALS:
		case EQUALSIGNORECASE:
		case ENDSWITH:
		case CONTAINS: {
			throw new IllegalArgumentException(
					"Illegal StringMultipleComparison operator " + op);
		}
		case REGIONMATCHES:
		case PATTERNMATCHES:
		case APACHE_ORO_PATTERN_MATCHES: {
			Long longValue = e.getConcreteValue();
			String intConst = Z3StrExprBuilder.mkIntegerConstant(longValue);
			return intConst;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
	}

	@Override
	public String visit(StringMultipleToIntegerExpression e, Void arg) {

		Operator op = e.getOperator();
		switch (op) {
		case INDEXOFCI:
		case INDEXOFSI: {
			//over-approximate using INDEXOF
			String left = e.getLeftOperand().accept(this, null);
			String right = e.getRightOperand().accept(this, null);
			String charAtExpr = Z3StrExprBuilder.mkStringIndexOf(left, right);
			return charAtExpr;
		}
		case LASTINDEXOFCI:
		case LASTINDEXOFSI: {
			Long concreteValue = e.getConcreteValue();
			return Z3StrExprBuilder.mkIntegerConstant(concreteValue);
		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
		}
	}

	@Override
	public String visit(StringUnaryToIntegerExpression e, Void arg) {
		String innerString = e.getOperand().accept(this, null);
		Operator op = e.getOperator();
		switch (op) {
		case LENGTH: {
			return Z3StrExprBuilder.mkStringLength(innerString);
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	private final Set<String> stringConstants = new HashSet<String>();

	@Override
	public String visit(RealToStringCast n, Void arg) {
		String str = n.getConcreteValue();
		stringConstants.add(str);
		return Z3StrExprBuilder.mkStringConstant(str);
	}

	@Override
	public String visit(StringToIntegerCast n, Void arg) {
		long longValue = n.getConcreteValue();
		return Z3StrExprBuilder.mkIntegerConstant(longValue);
	}

	@Override
	public String visit(NewTokenizerExpr n, Void arg) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Implement this method");
	}

	@Override
	public String visit(NextTokenizerExpr n, Void arg) {
		// TODO
		throw new IllegalStateException(
				"NextTokenizerExpr is not implemented yet");
	}

	public Set<String> getStringConstants() {
		return this.stringConstants;
	}

}
