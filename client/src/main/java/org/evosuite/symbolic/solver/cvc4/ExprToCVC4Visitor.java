package org.evosuite.symbolic.solver.cvc4;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.evosuite.symbolic.solver.SmtLibExprBuilder;

class ExprToCVC4Visitor implements ExpressionVisitor<String, Void> {

	private String createIntegerConstant(int i) {
		return createIntegerConstant((long) i);
	}

	@Override
	public String visit(IntegerBinaryExpression e, Void v) {
		String left = e.getLeftOperand().accept(this, null);
		String right = e.getRightOperand().accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		switch (e.getOperator()) {

		case DIV: {
			String z3_div = SmtLibExprBuilder.mkDiv(left, right);
			return z3_div;
		}
		case MUL: {
			String z3_mul = SmtLibExprBuilder.mkMul(left, right);
			return z3_mul;

		}
		case MINUS: {
			String z3_sub = SmtLibExprBuilder.mkSub(left, right);
			return z3_sub;
		}
		case PLUS: {
			String z3_add = SmtLibExprBuilder.mkAdd(left, right);
			return z3_add;
		}
		case REM: {
			String z3_mod = SmtLibExprBuilder.mkMod(left, right);
			return z3_mod;
		}
		case IOR: {
			String bv_left = SmtLibExprBuilder.mkInt2BV(32, left);
			String bv_right = SmtLibExprBuilder.mkInt2BV(32, right);
			String bvor = SmtLibExprBuilder.mkBVOR(bv_left, bv_right);
			String ret_val = SmtLibExprBuilder.mkBV2Int(bvor, true);
			return ret_val;
		}
		case IAND: {
			String bv_left = SmtLibExprBuilder.mkInt2BV(32, left);
			String bv_right = SmtLibExprBuilder.mkInt2BV(32, right);
			String bv_and = SmtLibExprBuilder.mkBVAND(bv_left, bv_right);
			String ret_val = SmtLibExprBuilder.mkBV2Int(bv_and, true);
			return ret_val;
		}
		case IXOR: {
			String bv_left = SmtLibExprBuilder.mkInt2BV(32, left);
			String bv_right = SmtLibExprBuilder.mkInt2BV(32, right);
			String bv_xor = SmtLibExprBuilder.mkBVXOR(bv_left, bv_right);
			String ret_val = SmtLibExprBuilder.mkBV2Int(bv_xor, true);
			return ret_val;
		}

		case SHL: {
			String bv_left = SmtLibExprBuilder.mkInt2BV(32, left);
			String bv_right = SmtLibExprBuilder.mkInt2BV(32, right);
			String bv_shl = SmtLibExprBuilder.mkBVSHL(bv_left, bv_right);
			String ret_val = SmtLibExprBuilder.mkBV2Int(bv_shl, true);
			return ret_val;
		}

		case SHR: {
			String bv_left = SmtLibExprBuilder.mkInt2BV(32, left);
			String bv_right = SmtLibExprBuilder.mkInt2BV(32, right);
			String bv_shr = SmtLibExprBuilder.mkBVASHR(bv_left, bv_right);
			String ret_val = SmtLibExprBuilder.mkBV2Int(bv_shr, true);
			return ret_val;
		}
		case USHR: {
			String bv_left = SmtLibExprBuilder.mkInt2BV(32, left);
			String bv_right = SmtLibExprBuilder.mkInt2BV(32, right);
			String bv_shr = SmtLibExprBuilder.mkBVLSHR(bv_left, bv_right);
			String ret_val = SmtLibExprBuilder.mkBV2Int(bv_shr, true);
			return ret_val;
		}

		case MAX: {
			String left_gt_right = SmtLibExprBuilder.mkGt(left, right);
			String ite_expr = SmtLibExprBuilder.mkITE(left_gt_right, left,
					right);
			return ite_expr;

		}

		case MIN: {
			String left_gt_right = SmtLibExprBuilder.mkLt(left, right);
			String ite_expr = SmtLibExprBuilder.mkITE(left_gt_right, left,
					right);
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

	private String createIntegerConstant(Long longObject) {
		String int_num;
		if (longObject.longValue() >= Integer.MIN_VALUE
				&& longObject.longValue() <= Integer.MAX_VALUE) {
			int intValue = longObject.intValue();
			int_num = SmtLibExprBuilder.mkIntegerConstant(intValue);
		} else {
			long longValue = longObject.longValue();
			int_num = SmtLibExprBuilder.mkIntegerConstant(longValue);
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
			String zero = SmtLibExprBuilder.mkIntegerConstant(0);
			String gte_than_zero = SmtLibExprBuilder.mkGe(intExpr, zero);
			String minus_expr = SmtLibExprBuilder.mkNeg(intExpr);

			String ite_expr = SmtLibExprBuilder.mkITE(gte_than_zero, intExpr,
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
		String intExpr = SmtLibExprBuilder.mkReal2Int(realExpr);
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
		String realExpr = SmtLibExprBuilder.mkToReal(integerExpr);
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
			String z3_div = SmtLibExprBuilder.mkDiv(left, right);
			return z3_div;
		}
		case MUL: {
			String z3_mul = SmtLibExprBuilder.mkMul(left, right);
			return z3_mul;

		}
		case MINUS: {
			String z3_sub = SmtLibExprBuilder.mkSub(left, right);
			return z3_sub;
		}
		case PLUS: {
			String z3_add = SmtLibExprBuilder.mkAdd(left, right);
			return z3_add;
		}
		case MAX: {
			String left_gt_right = SmtLibExprBuilder.mkGt(left, right);
			String ite_expr = SmtLibExprBuilder.mkITE(left_gt_right, left,
					right);
			return ite_expr;

		}

		case MIN: {
			String left_gt_right = SmtLibExprBuilder.mkLt(left, right);
			String ite_expr = SmtLibExprBuilder.mkITE(left_gt_right, left,
					right);
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
			String realConstant = SmtLibExprBuilder
					.mkRealConstant(concreteValue);
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
		String realExpr = SmtLibExprBuilder.mkRealConstant(doubleVal);
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
			String zero_rational = SmtLibExprBuilder.mkRealConstant(0);
			String gte_than_zero = SmtLibExprBuilder.mkGe(intExpr,
					zero_rational);
			String minus_expr = SmtLibExprBuilder.mkNeg(intExpr);

			String ite_expr = SmtLibExprBuilder.mkITE(gte_than_zero, intExpr,
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
				concreteRatNum = SmtLibExprBuilder.mkRealConstant(doubleVal);
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
		java.lang.String str = e.getConcreteValue();
		return createStringConstant(str);
	}

	@Override
	public String visit(StringMultipleExpression e, Void v) {
		Operator op = e.getOperator();
		switch (op) {
		case SUBSTRING: {
			Expression<String> leftOperand = e.getLeftOperand();
			Expression<?> rightOperand = e.getRightOperand();
			ArrayList<Expression<?>> othersOperands = e.getOther();

			String left = leftOperand.accept(this, null);
			String right = rightOperand.accept(this, null);

			if (othersOperands.size() != 1) {
				throw new IllegalStateException("Substring should be ternary!");
			}

			Expression<?> otherOperand = othersOperands.get(0);
			String other = otherOperand.accept(this, null);

			if (left == null || right == null || other == null) {
				return null;
			}

			SmtLibExprBuilder.mkApp(CVC4Solver.STR_SUBSTR, left, right, other);

		}
		case REPLACEC:
		case REPLACECS:
		case REPLACEALL:
		case REPLACEFIRST: {
			String concreteValue = e.getConcreteValue();
			String strConstant = createStringConstant(concreteValue);
			return strConstant;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}

	}

	private String createStringConstant(String concreteValue) {
		return "\"" + concreteValue + "\"";
	}

	@Override
	public String visit(StringUnaryExpression e, Void v) {
		Operator op = e.getOperator();
		switch (op) {
		case TRIM:
		case TOLOWERCASE:
		case TOUPPERCASE: {
			String concreteValue = e.getConcreteValue();
			return createStringConstant(concreteValue);
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
			String concreteValue = e.getConcreteValue();
			String strConstant = createStringConstant(concreteValue);
			return strConstant;
		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
		}
	}

	@Override
	public String visit(StringBinaryComparison e, Void v) {
		Expression<String> leftOperand = e.getLeftOperand();
		Expression<?> rightOperand = e.getRightOperand();
		Operator op = e.getOperator();

		String left = leftOperand.accept(this, null);
		String right = rightOperand.accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		String oneConstant = createIntegerConstant(1);
		String zeroConstant = createIntegerConstant(0);
		switch (op) {
		case EQUALS: {
			String equalsFormula = SmtLibExprBuilder.mkEq(left, right);
			return equalsFormula;
		}
		case EQUALSIGNORECASE: {
			throw new UnsupportedOperationException(
					"Must implement equalsIgnoreCase()!");
		}
		case ENDSWITH: {
			String equalsFormula = buildEndsWithFormula(left, right);
			String ifThenElseFormula = SmtLibExprBuilder.mkITE(equalsFormula,
					oneConstant, zeroConstant);
			return ifThenElseFormula;
		}
		case CONTAINS: {
			String equalsFormula = buildContainsFormula(left, right);
			String ifThenElseFormula = SmtLibExprBuilder.mkITE(equalsFormula,
					oneConstant, zeroConstant);
			return ifThenElseFormula;
		}
		case STARTSWITH: {
			throw new IllegalArgumentException(
					"Illegal StringBinaryComparison operator " + op);
		}
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

	private String buildContainsFormula(String left, String right) {
		// TODO Auto-generated method stub
		return null;
	}

	private String buildEndsWithFormula(String left, String right) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(StringBinaryToIntegerExpression e, Void v) {
		Expression<String> leftOperand = e.getLeftOperand();
		Operator op = e.getOperator();
		Expression<?> rightOperand = e.getRightOperand();

		String left = leftOperand.accept(this, null);
		String right = rightOperand.accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		switch (op) {
		case CHARAT: {
			String charAtExpr = SmtLibExprBuilder.mkApp(CVC4Solver.STR_AT,
					left, right);
			return charAtExpr;
		}
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
			String indexExpr = others.get(0);
			String oneExpr = createIntegerConstant(1);
			String zeroExpr = createIntegerConstant(0);

			String startsWithFormula = buildStartsWithFormula(left, right,
					indexExpr);
			String ifThenElseFormula = SmtLibExprBuilder.mkITE(
					startsWithFormula, oneExpr, zeroExpr);
			return ifThenElseFormula;
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
			String intConst = createIntegerConstant(longValue);
			return intConst;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
	}

	private String buildStartsWithFormula(String left, String right,
			String indexExpr) {
		// TODO Auto-generated method stub
		return null;
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
		String innerString = e.getOperand().accept(this, null);
		Operator op = e.getOperator();
		switch (op) {
		case LENGTH: {
			String app = SmtLibExprBuilder.mkApp(CVC4Solver.STR_LENGTH,
					innerString);
			return app;
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
		String concreteValue = e.getConcreteValue();
		return createStringConstant(concreteValue);
	}

	@Override
	public String visit(RealToStringCast e, Void v) {
		String concreteValue = e.getConcreteValue();
		return createStringConstant(concreteValue);
	}

	@Override
	public String visit(StringNextTokenExpr e, Void v) {
		String concreteValue = e.getConcreteValue();
		return createStringConstant(concreteValue);
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
