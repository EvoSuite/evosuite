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
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtIntConstant;
import org.evosuite.symbolic.solver.smt.SmtIntVariable;
import org.evosuite.symbolic.solver.smt.SmtRealConstant;
import org.evosuite.symbolic.solver.smt.SmtRealVariable;
import org.evosuite.symbolic.solver.smt.SmtStringConstant;

class ExprToCVC4Visitor implements ExpressionVisitor<SmtExpr, Void> {

	@Override
	public SmtExpr visit(IntegerBinaryExpression e, Void v) {
		SmtExpr left = e.getLeftOperand().accept(this, null);
		SmtExpr right = e.getRightOperand().accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		if (!left.hasSymbolicValue() && !right.hasSymbolicValue()) {
			long longValue = e.getConcreteValue();
			return CVC4ExprBuilder.mkIntConstant(longValue);
		}

		switch (e.getOperator()) {

		case DIV: {
			SmtExpr expr = CVC4ExprBuilder.mkDiv(left, right);
			return expr;
		}
		case MUL: {
			SmtExpr expr = CVC4ExprBuilder.mkMul(left, right);
			return expr;

		}
		case MINUS: {
			SmtExpr expr = CVC4ExprBuilder.mkSub(left, right);
			return expr;
		}
		case PLUS: {
			SmtExpr expr = CVC4ExprBuilder.mkAdd(left, right);
			return expr;
		}
		case REM: {
			SmtExpr mod = CVC4ExprBuilder.mkMod(left, right);
			return mod;
		}
		case IOR: {
			SmtExpr bv_left = CVC4ExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = CVC4ExprBuilder.mkInt2BV(32, right);
			SmtExpr bvor = CVC4ExprBuilder.mkBVOR(bv_left, bv_right);
			SmtExpr ret_val = CVC4ExprBuilder.mkBV2Nat(bvor);
			return ret_val;
		}
		case IAND: {
			SmtExpr bv_left = CVC4ExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = CVC4ExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_and = CVC4ExprBuilder.mkBVAND(bv_left, bv_right);
			SmtExpr ret_val = CVC4ExprBuilder.mkBV2Nat(bv_and);
			return ret_val;
		}
		case IXOR: {
			SmtExpr bv_left = CVC4ExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = CVC4ExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_xor = CVC4ExprBuilder.mkBVXOR(bv_left, bv_right);
			SmtExpr ret_val = CVC4ExprBuilder.mkBV2Int(bv_xor);
			return ret_val;
		}

		case SHL: {
			SmtExpr bv_left = CVC4ExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = CVC4ExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_shl = CVC4ExprBuilder.mkBVSHL(bv_left, bv_right);
			SmtExpr ret_val = CVC4ExprBuilder.mkBV2Nat(bv_shl);
			return ret_val;
		}

		case SHR: {
			SmtExpr bv_left = CVC4ExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = CVC4ExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_shr = CVC4ExprBuilder.mkBVASHR(bv_left, bv_right);
			SmtExpr ret_val = CVC4ExprBuilder.mkBV2Nat(bv_shr);
			return ret_val;
		}
		case USHR: {
			SmtExpr bv_left = CVC4ExprBuilder.mkInt2BV(32, left);
			SmtExpr bv_right = CVC4ExprBuilder.mkInt2BV(32, right);
			SmtExpr bv_shr = CVC4ExprBuilder.mkBVLSHR(bv_left, bv_right);
			SmtExpr ret_val = CVC4ExprBuilder.mkBV2Nat(bv_shr);
			return ret_val;
		}

		case MAX: {
			SmtExpr left_gt_right = CVC4ExprBuilder.mkGt(left, right);
			SmtExpr ite_expr = CVC4ExprBuilder
					.mkITE(left_gt_right, left, right);
			return ite_expr;

		}

		case MIN: {
			SmtExpr left_gt_right = CVC4ExprBuilder.mkLt(left, right);
			SmtExpr ite_expr = CVC4ExprBuilder
					.mkITE(left_gt_right, left, right);
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
		long longValue = e.getConcreteValue();
		SmtExpr intConst = CVC4ExprBuilder.mkIntConstant(longValue);
		return intConst;
	}

	@Override
	public SmtExpr visit(IntegerUnaryExpression e, Void v) {
		SmtExpr intExpr = e.getOperand().accept(this, null);

		if (intExpr == null) {
			return null;
		}

		if (!intExpr.hasSymbolicValue()) {
			long longValue = e.getConcreteValue();
			return CVC4ExprBuilder.mkIntConstant(longValue);
		}

		switch (e.getOperator()) {
		case ABS:
			SmtIntConstant zero = CVC4ExprBuilder.ZERO_INT;
			SmtExpr gte_than_zero = CVC4ExprBuilder.mkGe(intExpr, zero);
			SmtExpr minus_expr = CVC4ExprBuilder.mkNeg(intExpr);
			SmtExpr ite_expr = CVC4ExprBuilder.mkITE(gte_than_zero, intExpr,
					minus_expr);
			return ite_expr;
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	@Override
	public SmtExpr visit(RealToIntegerCast e, Void v) {
		SmtExpr realExpr = e.getArgument().accept(this, null);
		if (realExpr == null) {
			return null;
		}
		if (!realExpr.hasSymbolicValue()) {
			long longValue = e.getConcreteValue();
			return CVC4ExprBuilder.mkIntConstant(longValue);
		}

		SmtExpr intExpr = CVC4ExprBuilder.mkReal2Int(realExpr);
		return intExpr;
	}

	@Override
	public SmtExpr visit(RealUnaryToIntegerExpression e, Void v) {
		SmtExpr realExpr = e.getOperand().accept(this, null);
		if (realExpr == null) {
			return null;
		}
		if (!realExpr.hasSymbolicValue()) {
			long longValue = e.getConcreteValue();
			return CVC4ExprBuilder.mkIntConstant(longValue);
		}

		switch (e.getOperator()) {
		case ROUND: {
			SmtExpr toIntExpr = CVC4ExprBuilder.mkReal2Int(realExpr);
			return toIntExpr;
		}
		case GETEXPONENT: {
			long longValue = e.getConcreteValue();
			SmtExpr intConst = CVC4ExprBuilder.mkIntConstant(longValue);
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
		if (!integerExpr.hasSymbolicValue()) {
			double doubleValue = e.getConcreteValue();
			return CVC4ExprBuilder.mkRealConstant(doubleValue);
		}

		SmtExpr realExpr = CVC4ExprBuilder.mkInt2Real(integerExpr);
		return realExpr;
	}

	@Override
	public SmtExpr visit(RealBinaryExpression e, Void v) {
		SmtExpr left = e.getLeftOperand().accept(this, null);
		SmtExpr right = e.getRightOperand().accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		if (!left.hasSymbolicValue() && !right.hasSymbolicValue()) {
			double doubleValue = e.getConcreteValue();
			return CVC4ExprBuilder.mkRealConstant(doubleValue);
		}

		switch (e.getOperator()) {

		case DIV: {
			SmtExpr expr = CVC4ExprBuilder.mkDiv(left, right);
			return expr;
		}
		case MUL: {
			SmtExpr expr = CVC4ExprBuilder.mkMul(left, right);
			return expr;
		}
		case MINUS: {
			SmtExpr expr = CVC4ExprBuilder.mkSub(left, right);
			return expr;
		}
		case PLUS: {
			SmtExpr expr = CVC4ExprBuilder.mkAdd(left, right);
			return expr;
		}
		case MAX: {
			SmtExpr left_gt_right = CVC4ExprBuilder.mkGt(left, right);
			SmtExpr ite_expr = CVC4ExprBuilder
					.mkITE(left_gt_right, left, right);
			return ite_expr;

		}
		case MIN: {
			SmtExpr left_gt_right = CVC4ExprBuilder.mkLt(left, right);
			SmtExpr ite_expr = CVC4ExprBuilder
					.mkITE(left_gt_right, left, right);
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
			SmtExpr realConstant = CVC4ExprBuilder
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
	public SmtExpr visit(RealConstant e, Void v) {
		double doubleVal = e.getConcreteValue();
		SmtExpr realExpr = CVC4ExprBuilder.mkRealConstant(doubleVal);
		return realExpr;
	}

	@Override
	public SmtExpr visit(RealUnaryExpression e, Void v) {
		SmtExpr intExpr = e.getOperand().accept(this, null);

		if (intExpr == null) {
			return null;
		}

		switch (e.getOperator()) {
		case ABS: {
			SmtRealConstant zero_rational = CVC4ExprBuilder.ZERO_REAL;
			SmtExpr gte_than_zero = CVC4ExprBuilder
					.mkGe(intExpr, zero_rational);
			SmtExpr minus_expr = CVC4ExprBuilder.mkNeg(intExpr);

			SmtExpr ite_expr = CVC4ExprBuilder.mkITE(gte_than_zero, intExpr,
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
			Double doubleVal = e.getConcreteValue();
			SmtExpr concreteRatNum;
			if (doubleVal.isNaN() || doubleVal.isInfinite()) {
				return null;
			} else {
				concreteRatNum = CVC4ExprBuilder.mkRealConstant(doubleVal);
			}
			return concreteRatNum;
		}
		case GETEXPONENT:
		case ROUND: {
			throw new IllegalArgumentException("The Operation "
					+ e.getOperator() + " does not return a real expression!");
		}

		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	@Override
	public SmtExpr visit(RealVariable e, Void v) {
		String varName = e.getName();
		SmtRealVariable var = CVC4ExprBuilder.mkRealVariable(varName);
		return var;
	}

	@Override
	public SmtExpr visit(IntegerVariable e, Void v) {
		String varName = e.getName();
		SmtIntVariable var = CVC4ExprBuilder.mkIntVariable(varName);
		return var;
	}

	@Override
	public SmtExpr visit(StringConstant e, Void v) {
		String stringValue = e.getConcreteValue();
		return CVC4ExprBuilder.mkStringConstant(stringValue);
	}

	@Override
	public SmtExpr visit(StringMultipleExpression e, Void v) {

		Expression<String> leftOperand = e.getLeftOperand();
		Expression<?> rightOperand = e.getRightOperand();
		ArrayList<Expression<?>> othersOperands = e.getOther();

		SmtExpr left = leftOperand.accept(this, null);
		SmtExpr right = rightOperand.accept(this, null);

		if (othersOperands.size() != 1) {
			throw new IllegalStateException("Substring should be ternary!");
		}

		Expression<?> otherOperand = othersOperands.get(0);
		SmtExpr other = otherOperand.accept(this, null);

		if (left == null || right == null || other == null) {
			return null;
		}

		Operator op = e.getOperator();
		switch (op) {
		case SUBSTRING: {
			SmtExpr startIndex = right;
			SmtExpr endIndex = other;
			SmtExpr offset = CVC4ExprBuilder.mkSub(endIndex, startIndex);
			SmtExpr substring = CVC4ExprBuilder.mkStrSubstring(left,
					startIndex, offset);
			return substring;
		}
		case REPLACEC: {
			Long concreteTarget = (Long) rightOperand.getConcreteValue();
			Long concreteReplacement = (Long) otherOperand.getConcreteValue();

			String targetString = String.valueOf((char) concreteTarget
					.intValue());
			String replacementString = String
					.valueOf((char) concreteReplacement.intValue());

			SmtExpr target = CVC4ExprBuilder.mkStringConstant(targetString);
			SmtExpr replacement = CVC4ExprBuilder
					.mkStringConstant(replacementString);

			SmtExpr replace = CVC4ExprBuilder.mkStrReplace(left, target,
					replacement);
			return replace;
		}
		case REPLACECS: {
			SmtExpr target = right;
			SmtExpr replacement = other;

			SmtExpr replace = CVC4ExprBuilder.mkStrReplace(left, target,
					replacement);
			return replace;

		}
		case REPLACEALL:
		case REPLACEFIRST: {
			String concreteValue = e.getConcreteValue();
			SmtExpr strConstant = CVC4ExprBuilder
					.mkStringConstant(concreteValue);
			return strConstant;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}

	}

	@Override
	public SmtExpr visit(StringUnaryExpression e, Void v) {
		Operator op = e.getOperator();
		switch (op) {
		case TRIM:
		case TOLOWERCASE:
		case TOUPPERCASE: {
			String concreteValue = e.getConcreteValue();
			return CVC4ExprBuilder.mkStringConstant(concreteValue);
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
	}

	@Override
	public SmtExpr visit(StringVariable e, Void v) {
		String varName = e.getName();
		return CVC4ExprBuilder.mkStringVariable(varName);
	}

	@Override
	public SmtExpr visit(StringBinaryExpression e, Void v) {
		SmtExpr left = e.getLeftOperand().accept(this, null);
		SmtExpr right = e.getRightOperand().accept(this, null);

		if (left == null || right == null) {
			return null;
		}

		Operator op = e.getOperator();

		switch (op) {
		case CONCAT: {
			SmtExpr concatExpr = CVC4ExprBuilder.mkStrConcat(left, right);

			return concatExpr;
		}
		case APPEND_STRING: {
			SmtExpr concatExpr = CVC4ExprBuilder.mkStrConcat(left, right);

			return concatExpr;

		}
		case APPEND_INTEGER: {
			SmtExpr rigthStr = CVC4ExprBuilder.mkIntToStr(right);
			SmtExpr concatExpr = CVC4ExprBuilder.mkStrConcat(left, rigthStr);

			return concatExpr;
		}
		case APPEND_BOOLEAN: {
			SmtIntConstant zero = CVC4ExprBuilder.ZERO_INT;
			SmtExpr eqZero = CVC4ExprBuilder.mkEq(right, zero);
			SmtStringConstant falseConstantExpr = CVC4ExprBuilder
					.mkStringConstant(String.valueOf(Boolean.FALSE));
			SmtStringConstant trueConstantExpr = CVC4ExprBuilder
					.mkStringConstant(String.valueOf(Boolean.TRUE));
			SmtExpr ite = CVC4ExprBuilder.mkITE(eqZero, falseConstantExpr,
					trueConstantExpr);
			SmtExpr concatExpr = CVC4ExprBuilder.mkStrConcat(left, ite);
			return concatExpr;

		}
		case APPEND_CHAR: {

		}
		case APPEND_REAL: {
			String concreteValue = e.getConcreteValue();
			SmtExpr strConstant = CVC4ExprBuilder
					.mkStringConstant(concreteValue);
			return strConstant;
		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
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

		SmtIntConstant oneConstant = CVC4ExprBuilder.ONE_INT;
		SmtIntConstant zeroConstant = CVC4ExprBuilder.ZERO_INT;
		switch (op) {
		case EQUALS: {
			SmtExpr equalsFormula = CVC4ExprBuilder.mkEq(left, right);
			SmtExpr ifThenElseFormula = CVC4ExprBuilder.mkITE(equalsFormula,
					oneConstant, zeroConstant);
			return ifThenElseFormula;
		}
		case EQUALSIGNORECASE: {
			throw new UnsupportedOperationException(
					"Must implement equalsIgnoreCase()!");
		}
		case ENDSWITH: {
			SmtExpr endsWithExpr = CVC4ExprBuilder.mkStrSuffixOf(right, left);
			SmtExpr ifThenElseFormula = CVC4ExprBuilder.mkITE(endsWithExpr,
					oneConstant, zeroConstant);
			return ifThenElseFormula;
		}
		case CONTAINS: {
			SmtExpr equalsFormula = CVC4ExprBuilder.mkStrContains(left, right);
			SmtExpr ifThenElseFormula = CVC4ExprBuilder.mkITE(equalsFormula,
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
			long longValue = e.getConcreteValue();
			SmtExpr intConst = CVC4ExprBuilder.mkIntConstant(longValue);
			return intConst;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
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

		switch (op) {
		case CHARAT: {
			SmtExpr charAtExpr = CVC4ExprBuilder.mkStrAt(left, right);
			SmtExpr strToInt = CVC4ExprBuilder.mkCharToInt(charAtExpr);
			return strToInt;
		}
		case INDEXOFS: {
			SmtExpr zeroIndex = CVC4ExprBuilder.mkIntConstant(0);
			SmtExpr indexOf = CVC4ExprBuilder.mkStrIndexOf(left, right,
					zeroIndex);
			return indexOf;
		}
		case INDEXOFC: {
			SmtExpr zeroIndex = CVC4ExprBuilder.mkIntConstant(0);
			SmtExpr charExpr = CVC4ExprBuilder.mkIntToChar(right);
			SmtExpr indexOf = CVC4ExprBuilder.mkStrIndexOf(left, charExpr,
					zeroIndex);
			return indexOf;
		}
		case LASTINDEXOFC:
		case LASTINDEXOFS:
		case COMPARETO:
		case COMPARETOIGNORECASE: {
			long concreteValue = e.getConcreteValue();
			return CVC4ExprBuilder.mkIntConstant(concreteValue);
		}

		default: {
			throw new UnsupportedOperationException("Not implemented yet!"
					+ e.getOperator());
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

		if (left == null || right == null) {
			return null;
		}
		for (SmtExpr expr : others) {
			if (expr == null) {
				return null;
			}
		}

		switch (op) {
		case STARTSWITH: {

			SmtExpr indexExpr = others.get(0);
			if (indexExpr.equals(CVC4ExprBuilder.ZERO_INT)) {
				SmtIntConstant oneExpr = CVC4ExprBuilder.ONE_INT;
				SmtIntConstant zeroExpr = CVC4ExprBuilder.ZERO_INT;
				SmtExpr startsWithFormula = CVC4ExprBuilder.mkStrPrefixOf(
						right, left);
				SmtExpr ifThenElseFormula = CVC4ExprBuilder.mkITE(
						startsWithFormula, oneExpr, zeroExpr);
				return ifThenElseFormula;

			} else {
				long longValue = e.getConcreteValue();
				SmtExpr intConst = CVC4ExprBuilder.mkIntConstant(longValue);
				return intConst;
			}
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
			int intValue = longValue.intValue();
			SmtExpr intConst = CVC4ExprBuilder.mkIntConstant(intValue);
			return intConst;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
	}

	@Override
	public SmtExpr visit(StringMultipleToIntegerExpression e, Void v) {
		Operator op = e.getOperator();
		switch (op) {
		case INDEXOFSI: {
			SmtExpr left = e.getLeftOperand().accept(this, null);
			SmtExpr right = e.getRightOperand().accept(this, null);
			SmtExpr other = e.getOther().get(0).accept(this, null);
			SmtExpr indexOf = CVC4ExprBuilder.mkStrIndexOf(left, right, other);
			return indexOf;

		}
		case INDEXOFCI: {
			SmtExpr right = e.getRightOperand().accept(this, null);

			SmtExpr int2Str = CVC4ExprBuilder.mkIntToChar(right);
			SmtExpr left = e.getLeftOperand().accept(this, null);
			SmtExpr other = e.getOther().get(0).accept(this, null);
			SmtExpr indexOf = CVC4ExprBuilder
					.mkStrIndexOf(left, int2Str, other);

			return indexOf;
		}
		case LASTINDEXOFCI:
		case LASTINDEXOFSI: {
			long longValue = e.getConcreteValue();
			SmtExpr intNum = CVC4ExprBuilder.mkIntConstant(longValue);
			return intNum;
		}
		default: {
			throw new UnsupportedOperationException("Not implemented yet! "
					+ op);
		}
		}
	}

	@Override
	public SmtExpr visit(StringToIntegerCast e, Void v) {
		long concreteValue = e.getConcreteValue();
		return CVC4ExprBuilder.mkIntConstant(concreteValue);
	}

	@Override
	public SmtExpr visit(StringUnaryToIntegerExpression e, Void v) {
		SmtExpr innerString = e.getOperand().accept(this, null);
		Operator op = e.getOperator();
		switch (op) {
		case LENGTH: {
			SmtExpr app = CVC4ExprBuilder.mkStrLen(innerString);
			return app;
		}
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
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
		String concreteValue = e.getConcreteValue();
		return CVC4ExprBuilder.mkStringConstant(concreteValue);
	}

	@Override
	public SmtExpr visit(RealToStringCast e, Void v) {
		String concreteValue = e.getConcreteValue();
		return CVC4ExprBuilder.mkStringConstant(concreteValue);
	}

	@Override
	public SmtExpr visit(StringNextTokenExpr e, Void v) {
		String concreteValue = e.getConcreteValue();
		return CVC4ExprBuilder.mkStringConstant(concreteValue);
	}

	@Override
	public SmtExpr visit(HasMoreTokensExpr e, Void v) {
		long longValue = e.getConcreteValue();
		SmtExpr intConst = CVC4ExprBuilder.mkIntConstant(longValue);
		return intConst;
	}

	@Override
	public SmtExpr visit(StringReaderExpr e, Void v) {
		Long longObject = e.getConcreteValue();
		SmtExpr intConst = CVC4ExprBuilder.mkIntConstant(longObject);
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

}
