package org.evosuite.symbolic;

import java.util.ArrayList;
import java.util.Collections;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.IntegerToRealCast;
import org.evosuite.symbolic.expr.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.IntegerVariable;
import org.evosuite.symbolic.expr.IntegerVariableFactory;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealComparison;
import org.evosuite.symbolic.expr.RealConstant;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealToIntegerCast;
import org.evosuite.symbolic.expr.RealUnaryExpression;
import org.evosuite.symbolic.expr.RealVariable;
import org.evosuite.symbolic.expr.RealVariableFactory;
import org.evosuite.symbolic.expr.StringBinaryExpression;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringConstant;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringMultipleComparison;
import org.evosuite.symbolic.expr.StringMultipleExpression;
import org.evosuite.symbolic.expr.StringToIntCast;
import org.evosuite.symbolic.expr.StringUnaryExpression;
import org.evosuite.symbolic.expr.StringVariable;
import org.evosuite.symbolic.expr.StringVariableFactory;

import edu.uta.cse.dsc.ast.BitVector32;
import edu.uta.cse.dsc.ast.BitVector32Visitor;
import edu.uta.cse.dsc.ast.BitVector64;
import edu.uta.cse.dsc.ast.BitVector64Visitor;
import edu.uta.cse.dsc.ast.DoubleExpression;
import edu.uta.cse.dsc.ast.DoubleExpressionVisitor;
import edu.uta.cse.dsc.ast.FloatExpression;
import edu.uta.cse.dsc.ast.FloatExpressionVisitor;
import edu.uta.cse.dsc.ast.Reference;
import edu.uta.cse.dsc.ast.bitvector.BitVector32Variable;
import edu.uta.cse.dsc.ast.bitvector.BitVector64Variable;
import edu.uta.cse.dsc.ast.bitvector.Bv32ExtendingAnBv64;
import edu.uta.cse.dsc.ast.bitvector.Bv32ExtendingAnFp32;
import edu.uta.cse.dsc.ast.bitvector.Bv32ExtendingAnFp64;
import edu.uta.cse.dsc.ast.bitvector.Bv64ExtendingAnBv32;
import edu.uta.cse.dsc.ast.bitvector.Bv64ExtendingAnFp32;
import edu.uta.cse.dsc.ast.bitvector.Bv64ExtendingAnFp64;
import edu.uta.cse.dsc.ast.bitvector.BvAdd;
import edu.uta.cse.dsc.ast.bitvector.BvAnd;
import edu.uta.cse.dsc.ast.bitvector.BvMul;
import edu.uta.cse.dsc.ast.bitvector.BvNeg;
import edu.uta.cse.dsc.ast.bitvector.BvOr;
import edu.uta.cse.dsc.ast.bitvector.BvSDiv;
import edu.uta.cse.dsc.ast.bitvector.BvSHL;
import edu.uta.cse.dsc.ast.bitvector.BvSHR;
import edu.uta.cse.dsc.ast.bitvector.BvSRem;
import edu.uta.cse.dsc.ast.bitvector.BvSub;
import edu.uta.cse.dsc.ast.bitvector.BvUSHR;
import edu.uta.cse.dsc.ast.bitvector.BvXOr;
import edu.uta.cse.dsc.ast.bitvector.LiteralBitVector32;
import edu.uta.cse.dsc.ast.bitvector.LiteralBitVector64;
import edu.uta.cse.dsc.ast.constraint.ITE;
import edu.uta.cse.dsc.ast.fp.DoubleLiteral;
import edu.uta.cse.dsc.ast.fp.DoubleVariable;
import edu.uta.cse.dsc.ast.fp.FloatLiteral;
import edu.uta.cse.dsc.ast.fp.FloatVariable;
import edu.uta.cse.dsc.ast.fp.Fp32ExtendingAnBv32;
import edu.uta.cse.dsc.ast.fp.Fp32ExtendingAnBv64;
import edu.uta.cse.dsc.ast.fp.Fp32ExtendingAnFp64;
import edu.uta.cse.dsc.ast.fp.Fp64ExtendingAnBv32;
import edu.uta.cse.dsc.ast.fp.Fp64ExtendingAnBv64;
import edu.uta.cse.dsc.ast.fp.Fp64ExtendingAnFp32;
import edu.uta.cse.dsc.ast.fp.FpAdd;
import edu.uta.cse.dsc.ast.fp.FpMul;
import edu.uta.cse.dsc.ast.fp.FpNeg;
import edu.uta.cse.dsc.ast.fp.FpSDiv;
import edu.uta.cse.dsc.ast.fp.FpSLt;
import edu.uta.cse.dsc.ast.fp.FpSRem;
import edu.uta.cse.dsc.ast.fp.FpSub;
import edu.uta.cse.dsc.ast.functions.math.ABS;
import edu.uta.cse.dsc.ast.functions.math.ACOS;
import edu.uta.cse.dsc.ast.functions.math.ASIN;
import edu.uta.cse.dsc.ast.functions.math.ATAN;
import edu.uta.cse.dsc.ast.functions.math.ATAN2;
import edu.uta.cse.dsc.ast.functions.math.CBRT;
import edu.uta.cse.dsc.ast.functions.math.CEIL;
import edu.uta.cse.dsc.ast.functions.math.COS;
import edu.uta.cse.dsc.ast.functions.math.COSH;
import edu.uta.cse.dsc.ast.functions.math.CopySign;
import edu.uta.cse.dsc.ast.functions.math.EXP;
import edu.uta.cse.dsc.ast.functions.math.EXPM1;
import edu.uta.cse.dsc.ast.functions.math.FLOOR;
import edu.uta.cse.dsc.ast.functions.math.GetExponent;
import edu.uta.cse.dsc.ast.functions.math.HYPOT;
import edu.uta.cse.dsc.ast.functions.math.IEEEremainder;
import edu.uta.cse.dsc.ast.functions.math.LOG;
import edu.uta.cse.dsc.ast.functions.math.LOG10;
import edu.uta.cse.dsc.ast.functions.math.LOG1P;
import edu.uta.cse.dsc.ast.functions.math.MAX;
import edu.uta.cse.dsc.ast.functions.math.MIN;
import edu.uta.cse.dsc.ast.functions.math.NextAfter;
import edu.uta.cse.dsc.ast.functions.math.NextUp;
import edu.uta.cse.dsc.ast.functions.math.POW;
import edu.uta.cse.dsc.ast.functions.math.RINT;
import edu.uta.cse.dsc.ast.functions.math.ROUND;
import edu.uta.cse.dsc.ast.functions.math.SCALB;
import edu.uta.cse.dsc.ast.functions.math.SIGNUM;
import edu.uta.cse.dsc.ast.functions.math.SIN;
import edu.uta.cse.dsc.ast.functions.math.SINH;
import edu.uta.cse.dsc.ast.functions.math.SQRT;
import edu.uta.cse.dsc.ast.functions.math.TAN;
import edu.uta.cse.dsc.ast.functions.math.TANH;
import edu.uta.cse.dsc.ast.functions.math.ToDegrees;
import edu.uta.cse.dsc.ast.functions.math.ToRadians;
import edu.uta.cse.dsc.ast.functions.math.ULP;
import edu.uta.cse.dsc.ast.functions.string.StringCharAt;
import edu.uta.cse.dsc.ast.functions.string.StringCompareTo;
import edu.uta.cse.dsc.ast.functions.string.StringCompareToIgnoreCase;
import edu.uta.cse.dsc.ast.functions.string.StringConcat;
import edu.uta.cse.dsc.ast.functions.string.StringContains;
import edu.uta.cse.dsc.ast.functions.string.StringEndsWith;
import edu.uta.cse.dsc.ast.functions.string.StringEquals;
import edu.uta.cse.dsc.ast.functions.string.StringEqualsIgnoreCase;
import edu.uta.cse.dsc.ast.functions.string.StringIndexOfC;
import edu.uta.cse.dsc.ast.functions.string.StringIndexOfCI;
import edu.uta.cse.dsc.ast.functions.string.StringIndexOfS;
import edu.uta.cse.dsc.ast.functions.string.StringIndexOfSI;
import edu.uta.cse.dsc.ast.functions.string.StringLastIndexOfC;
import edu.uta.cse.dsc.ast.functions.string.StringLastIndexOfCI;
import edu.uta.cse.dsc.ast.functions.string.StringLastIndexOfS;
import edu.uta.cse.dsc.ast.functions.string.StringLastIndexOfSI;
import edu.uta.cse.dsc.ast.functions.string.StringLength;
import edu.uta.cse.dsc.ast.functions.string.StringReference;
import edu.uta.cse.dsc.ast.functions.string.StringReferenceNonNullLiteral;
import edu.uta.cse.dsc.ast.functions.string.StringReferenceVariable;
import edu.uta.cse.dsc.ast.functions.string.StringRegionMatches;
import edu.uta.cse.dsc.ast.functions.string.StringReplaceAll;
import edu.uta.cse.dsc.ast.functions.string.StringReplaceC;
import edu.uta.cse.dsc.ast.functions.string.StringReplaceCS;
import edu.uta.cse.dsc.ast.functions.string.StringReplaceFirst;
import edu.uta.cse.dsc.ast.functions.string.StringStartsWith;
import edu.uta.cse.dsc.ast.functions.string.StringSubstring;
import edu.uta.cse.dsc.ast.functions.string.StringToLowerCase;
import edu.uta.cse.dsc.ast.functions.string.StringToUpperCase;
import edu.uta.cse.dsc.ast.functions.string.StringTrim;
import edu.uta.cse.dsc.ast.reference.LiteralArray.LABv32;
import edu.uta.cse.dsc.ast.reference.LiteralArray.LABv64;
import edu.uta.cse.dsc.ast.reference.LiteralArray.LAFp32;
import edu.uta.cse.dsc.ast.reference.LiteralArray.LAFp64;
import edu.uta.cse.dsc.ast.reference.LiteralArray.LARef;
import edu.uta.cse.dsc.ast.reference.LiteralNonNullReference;
import edu.uta.cse.dsc.ast.reference.LiteralNullReference;
import edu.uta.cse.dsc.ast.reference.ReferenceVariable;
import edu.uta.cse.dsc.ast.reference.ReferenceVisitor;
import edu.uta.cse.dsc.ast.ufunction.Bv32ValuedInstanceMethod;
import edu.uta.cse.dsc.ast.ufunction.Bv64InstanceMethod;
import edu.uta.cse.dsc.ast.z3array.JavaArraySelect.ArraySelectBv32;
import edu.uta.cse.dsc.ast.z3array.JavaArraySelect.ArraySelectBv64;
import edu.uta.cse.dsc.ast.z3array.JavaArraySelect.ArraySelectFp32;
import edu.uta.cse.dsc.ast.z3array.JavaArraySelect.ArraySelectFp64;

/**
 * Translates a <code>edu.uta.cse.dsc.ast.JvmExpression</code> expression into a
 * <code>org.evosuite.symbolic.expr.Expression</code>.
 * 
 * If a given <code>JvmVariable</code> is not marked as <b>symbolic</b>, then it
 * returns its concrete value.
 * 
 * @author galeotti
 * 
 */
public final class JvmExpressionTranslator implements BitVector32Visitor,
		BitVector64Visitor, FloatExpressionVisitor, DoubleExpressionVisitor,
		ReferenceVisitor {

	private final ConcolicState concolicState;

	public JvmExpressionTranslator(ConcolicState concolicState) {
		this.concolicState = concolicState;
	}

	/**
	 * Translates a BitVector32Variable into:
	 * 
	 * (i) an IntegerVariable if the variable is symbolically marked.
	 * 
	 * (ii) its value if the variable is not symbolically marked.
	 */
	@Override
	public Object visit(BitVector32Variable b) {
		BitVector32 symbolic_value = (BitVector32) concolicState
				.getSymbolicValue(b);
		IntegerExpression integer_expr = (IntegerExpression) symbolic_value
				.accept(this);
		if (concolicState.isMarked(b)) {
			long concreteValue = (Long) integer_expr.getConcreteValue();
			String symbolic_name = concolicState.getSymbolicName(b);
			IntegerVariable integerVariable = IntegerVariableFactory
					.buildIntegerVariable(symbolic_name, concreteValue,
							(long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE);
			return integerVariable;
		} else {

			return integer_expr;
		}
	}

	/**
	 * Translates a (int)long expression into an IntegerExpression.
	 * 
	 * No constraints on casting are added by the moment.
	 */
	@Override
	public Object accept(Bv32ExtendingAnBv64 b) {
		IntegerExpression e = (IntegerExpression) b.getParam().accept(this);
		// TODO: Add constraints about casts among cardinal primitives
		// (char,short,...)
		return e;
	}

	/**
	 * Translates a int literal into a IntegerConstant.
	 */
	@Override
	public Object visit(LiteralBitVector32 b) {
		int value = b.getValue();
		return new IntegerConstant(value);
	}

	/**
	 * Translates a single-precision float comparison (-1 on NaN)
	 * 
	 * <ul>
	 * <li>If left==right ==> returns 0</li>
	 * <li>If left>right ==> returns 1</li>
	 * <li>If left<right ==> returns -1</li>
	 * <li>If left is NaN or right is NaN ==> returns 1</li>
	 * </ul>
	 */
	@Override
	public Object visit(FpSLt.Fp32 b) {
		RealExpression left_real_expression = (RealExpression) b.getLeft()
				.accept(this);
		RealExpression right_real_expression = (RealExpression) b.getRight()
				.accept(this);

		float left_concrete_value = ((Double) left_real_expression
				.getConcreteValue()).floatValue();
		float right_concrete_value = ((Double) right_real_expression
				.getConcreteValue()).floatValue();

		int concrete_value;
		if (new Float(left_concrete_value).isNaN()
				|| new Float(right_concrete_value).isNaN()) {
			concrete_value = 1;
		} else if (left_concrete_value == right_concrete_value) {
			concrete_value = 0;
		} else if (left_concrete_value > right_concrete_value) {
			concrete_value = 1;
		} else {
			assert left_concrete_value < right_concrete_value;
			concrete_value = -1;
		}

		return new RealComparison(left_real_expression, right_real_expression,
				(long) concrete_value);
	}

	/**
	 * Translates a (int)+(int) into a IntegerBinaryExpression(PLUS)
	 * 
	 * It uses 32 bit arithmetic to compute the concrete value.
	 */
	@Override
	public Object visit(BvAdd.Bv32 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		int concrete_value = left_concrete_value + right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.PLUS, right_expr, (long) concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (int)&(int) into a IntegerBinaryExpression(IAND).
	 * 
	 * It Uses 32-bit arithmetics to compute the concrete value.
	 */
	@Override
	public Object visit(BvAnd.Bv32 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		int concrete_value = left_concrete_value & right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.IAND, right_expr, (long) concrete_value);

		return ret_val;

	}

	/**
	 * Translates a (int)*(int) expression into a IntegerBinaryExpression(MUL).
	 * 
	 * It Uses 32-bit arithmetics to compute the concrete value.
	 */
	@Override
	public Object visit(BvMul.Bv32 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		int concrete_value = left_concrete_value * right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.MUL, right_expr, (long) concrete_value);

		return ret_val;
	}

	/**
	 * Translates a -(int) expression into a IntegerUnaryExpression(NEG)
	 * 
	 * It uses 32-bit arithmetics to compute the concrete value.
	 */
	@Override
	public Object visit(BvNeg.Bv32 b) {
		BitVector32 param = (BitVector32) b.getParam();
		IntegerExpression param_expr = (IntegerExpression) param.accept(this);

		int param_concrete_value = ((Long) param_expr.getConcreteValue())
				.intValue();

		int concrete_value = -param_concrete_value;

		IntegerUnaryExpression ret_val = new IntegerUnaryExpression(param_expr,
				Operator.NEG, (long) concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (int)|(int) expression into a IntegerBinaryExpression(OR)
	 * 
	 * It uses 32-bit arithmetics to compute the concrete value.
	 */
	@Override
	public Object visit(BvOr.Bv32 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		int concrete_value = left_concrete_value | right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.IOR, right_expr, (long) concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (int)/(int) expression into a IntegerBinaryExpression(DIV)
	 * 
	 * It uses 32-bit arithmetics to conpute the concrete value.
	 */

	@Override
	public Object visit(BvSDiv.Bv32 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		int concrete_value = left_concrete_value / right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.DIV, right_expr, (long) concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (int)%(int) expression into a IntegerBinaryExpression(REM)
	 * 
	 * It uses 32-bit arithmetic to comput the concrete value.
	 */

	@Override
	public Object visit(BvSRem.Bv32 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		// use 32-bit arithmetic
		int concrete_value = left_concrete_value % right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.REM, right_expr, (long) concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (int)-(int) expression into a IntegerBinaryExpression(MINUS)
	 * 
	 * It uses 32-bit arithmetic to compute the concrete value.
	 */
	@Override
	public Object visit(BvSub.Bv32 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		// use 32-bit arithmetic
		int concrete_value = left_concrete_value - right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.MINUS, right_expr, (long) concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (int)^(int) expression into a IntegerBinaryExpression(XOR)
	 * 
	 * It uses 32-bit arithmetics.
	 */
	@Override
	public Object visit(BvXOr.Bv32 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		// use 32-bit arithmetic
		int concrete_value = left_concrete_value ^ right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.IXOR, right_expr, (long) concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (int)float expression
	 */
	@Override
	public Object visit(Bv32ExtendingAnFp32 b) {
		FloatExpression floatExpression = b.getParam();
		RealExpression realExpression = (RealExpression) floatExpression
				.accept(this);
		float param_concrete_value = ((Double) realExpression
				.getConcreteValue()).intValue();
		int concrete_value = (int) param_concrete_value;
		return new RealToIntegerCast(realExpression, (long) concrete_value);
	}

	/**
	 * Translates a (int)double expression to a RealToIntegerCast instance.
	 */
	@Override
	public Object visit(Bv32ExtendingAnFp64 b) {
		RealExpression realExpr = (RealExpression) b.getParam().accept(this);
		double realExprConcreteValue = ((Double) realExpr.getConcreteValue())
				.doubleValue();

		int concreteValue = (int) realExprConcreteValue;

		return new RealToIntegerCast(realExpr, (long) concreteValue);
	}

	/**
	 * Creates a new RealConstant for the float literal Returns a RealConstant
	 */
	@Override
	public Object visit(FloatLiteral f) {
		float value = f.getValue();
		return new RealConstant(value);
	}

	/**
	 * Translates a variable whose type is float. Returns a RealVariable
	 */
	@Override
	public Object visit(FloatVariable f) {
		FloatExpression symbolic_value = (FloatExpression) concolicState
				.getSymbolicValue(f);
		RealExpression float_expr = (RealExpression) symbolic_value
				.accept(this);
		if (concolicState.isMarked(f)) {

			double concreteValue = (Double) float_expr.getConcreteValue();
			String symbolic_name = concolicState.getSymbolicName(f);
			RealVariable realVariable = RealVariableFactory.buildRealVariable(
					symbolic_name, concreteValue, Float.MIN_VALUE,
					Float.MAX_VALUE);
			return realVariable;
		} else {

			return float_expr;
		}
	}

	/**
	 * Translates a (float)int expression. Returns a IntegerToRealCast instance.
	 */
	@Override
	public Object visit(Fp32ExtendingAnBv32 f) {
		IntegerExpression integerExpression = (IntegerExpression) f.getParam()
				.accept(this);
		double concreteValue = ((Long) integerExpression.getConcreteValue())
				.floatValue();
		return new IntegerToRealCast(integerExpression, concreteValue);
	}

	/**
	 * Translates a (float)-(float) expression. It returns a
	 * RealBinaryExpression(MINUS).
	 * 
	 * It uses 32-floating point arithmetic for computing the concrete value.
	 */
	@Override
	public Object visit(FpSub.Fp32 f) {
		RealExpression left_expr = (RealExpression) f.getLeft().accept(this);
		RealExpression right_expr = (RealExpression) f.getRight().accept(this);

		float left_concrete_float = ((Double) left_expr.getConcreteValue())
				.floatValue();
		float right_concrete_float = ((Double) right_expr.getConcreteValue())
				.floatValue();

		float concrete_value = left_concrete_float - right_concrete_float;

		return new RealBinaryExpression(left_expr, Operator.MINUS, right_expr,
				(double) concrete_value);
	}

	/**
	 * Translates a (float)+(float) expression. It returns a
	 * RealBinaryExpression(PLUS)
	 * 
	 * It uses 32-floating point arithmetic for computing the concrete value.
	 */
	@Override
	public Object visit(FpAdd.Fp32 f) {
		RealExpression left_expr = (RealExpression) f.getLeft().accept(this);
		RealExpression right_expr = (RealExpression) f.getRight().accept(this);

		float left_concrete_float = ((Double) left_expr.getConcreteValue())
				.floatValue();
		float right_concrete_float = ((Double) right_expr.getConcreteValue())
				.floatValue();

		float concrete_value = left_concrete_float + right_concrete_float;

		return new RealBinaryExpression(left_expr, Operator.PLUS, right_expr,
				(double) concrete_value);
	}

	/**
	 * Translates a (float)%(float) expression. It returns a
	 * RealBinaryExpression(REM)
	 * 
	 * It uses 32-floating point arithmetic for computing the concrete value.
	 */
	@Override
	public Object visit(FpSRem.Fp32 f) {
		RealExpression left_expr = (RealExpression) f.getLeft().accept(this);
		RealExpression right_expr = (RealExpression) f.getRight().accept(this);

		float left_concrete_float = ((Double) left_expr.getConcreteValue())
				.floatValue();
		float right_concrete_float = ((Double) right_expr.getConcreteValue())
				.floatValue();

		float concrete_value = left_concrete_float % right_concrete_float;

		return new RealBinaryExpression(left_expr, Operator.REM, right_expr,
				(double) concrete_value);
	}

	/**
	 * Translates a (float)/(float) expression. It returns a
	 * RealBinaryExpression(DIV)
	 * 
	 * It uses 32-floating point arithmetic for computing the concrete value.
	 */
	@Override
	public Object visit(FpSDiv.Fp32 f) {
		RealExpression left_expr = (RealExpression) f.getLeft().accept(this);
		RealExpression right_expr = (RealExpression) f.getRight().accept(this);

		float left_concrete_float = ((Double) left_expr.getConcreteValue())
				.floatValue();
		float right_concrete_float = ((Double) right_expr.getConcreteValue())
				.floatValue();

		float concrete_value = left_concrete_float / right_concrete_float;

		return new RealBinaryExpression(left_expr, Operator.DIV, right_expr,
				(double) concrete_value);
	}

	/**
	 * Translates a (float)*(float) expression. It returns a
	 * RealBinaryExpression(DIV)
	 * 
	 * It uses 32-floating point arithmetic for computing the concrete value.
	 */
	@Override
	public Object visit(FpMul.Fp32 f) {
		RealExpression left_expr = (RealExpression) f.getLeft().accept(this);
		RealExpression right_expr = (RealExpression) f.getRight().accept(this);

		float left_concrete_float = ((Double) left_expr.getConcreteValue())
				.floatValue();
		float right_concrete_float = ((Double) right_expr.getConcreteValue())
				.floatValue();

		float concrete_value = left_concrete_float * right_concrete_float;

		return new RealBinaryExpression(left_expr, Operator.MUL, right_expr,
				(double) concrete_value);
	}

	/**
	 * Translates a -(float) expression. It returns a RealUnaryExpression(NEG)
	 * 
	 * It uses 32-floating point arithmetic for computing the concrete value.
	 */
	@Override
	public Object visit(FpNeg.Fp32 f) {
		if (!(f.getParam() instanceof FloatExpression)) {
			throw new IllegalArgumentException(
					"Unexpected FpNeg.Fp32 with a param of type "
							+ f.getParam().getClass().getName());
		}
		FloatExpression param_expr = (FloatExpression) f.getParam();

		RealExpression real_param_expr = (RealExpression) param_expr
				.accept(this);

		float param_concrete_float = ((Double) real_param_expr
				.getConcreteValue()).floatValue();

		float concrete_value = -param_concrete_float;

		return new RealUnaryExpression(real_param_expr, Operator.NEG,
				(double) concrete_value);
	}

	/**
	 * Translates a (float)long cast expression to an IntegerToReal expression.
	 * 
	 * It uses floating point 32 bit arithmetic to compute the concrete value.
	 */
	@Override
	public Object visit(Fp32ExtendingAnBv64 f) {
		IntegerExpression integerExpression = (IntegerExpression) f.getParam()
				.accept(this);
		long integerExpressionConcreteValue = ((Long) integerExpression
				.getConcreteValue()).longValue();
		float concreteValue = (float) integerExpressionConcreteValue;
		return new IntegerToRealCast(integerExpression, (double) concreteValue);
	}

	/**
	 * Translates a (float)double cast expression.
	 * 
	 * We are not adding additional constraints for casting among floating point
	 * representations by the moment.
	 * 
	 * TODO: Handle constraints for casting float<->double
	 */
	@Override
	public Object visit(Fp32ExtendingAnFp64 f) {
		RealExpression r = (RealExpression) f.getParam().accept(this);
		return r;
	}

	/**
	 * Translates a BitVector64Variable into:
	 * 
	 * (i) an IntegerVariable if the variable is symbolically marked.
	 * 
	 * (ii) its value if the variable is not symbolically marked.
	 */
	@Override
	public Object visit(BitVector64Variable b) {
		BitVector64 symbolic_value = (BitVector64) concolicState
				.getSymbolicValue(b);
		IntegerExpression integer_expr = (IntegerExpression) symbolic_value
				.accept(this);
		if (concolicState.isMarked(b)) {
			long concreteValue = (Long) integer_expr.getConcreteValue();
			String symbolic_name = concolicState.getSymbolicName(b);
			IntegerVariable integerVariable = IntegerVariableFactory
					.buildIntegerVariable(symbolic_name, concreteValue,
							Long.MIN_VALUE, Long.MAX_VALUE);
			return integerVariable;
		} else {

			return integer_expr;
		}

	}

	/**
	 * Translates a (long)int expression to an IntegerExpression
	 */
	@Override
	public Object visit(Bv64ExtendingAnBv32 b) {
		IntegerExpression integerExpression = (IntegerExpression) b.getParam()
				.accept(this);
		// TODO: Add constraints about casts among cardinal primitives
		// (char,short,...)
		return integerExpression;
	}

	/**
	 * Translates a (long)float expression to a RealToIntegerCast expression.
	 * 
	 * It uses 64-bit arithmetic to compute the concrete value.
	 */
	@Override
	public Object accept(Bv64ExtendingAnFp32 b) {
		RealExpression paramRealExpr = (RealExpression) b.getParam().accept(
				this);
		float paramConcreteValue = ((Double) paramRealExpr.getConcreteValue())
				.floatValue();
		long concreteValue = (long) paramConcreteValue;
		return new RealToIntegerCast(paramRealExpr, concreteValue);
	}

	/**
	 * Translates a (long)double expression to RealToIntegerCast expression.
	 */
	@Override
	public Object visit(Bv64ExtendingAnFp64 b) {
		RealExpression realExpr = (RealExpression) b.getParam().accept(this);
		double realExprConcreteValue = ((Double) realExpr.getConcreteValue())
				.doubleValue();

		long concreteValue = (long) realExprConcreteValue;

		return new RealToIntegerCast(realExpr, concreteValue);
	}

	/**
	 * Translates a long literal to a IntegerConstant expression.
	 */
	@Override
	public Object visit(LiteralBitVector64 b) {
		long concreteValue = b.getValue();
		return new IntegerConstant(concreteValue);
	}

	/**
	 * Translates a (long)+(long) operation into a IntegerBinaryExpression
	 * instance.
	 * 
	 * It uses 64-bit arithmetics for computing the concrete value.
	 */
	@Override
	public Object visit(BvAdd.Bv64 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		long right_concrete_value = ((Long) right_expr.getConcreteValue())
				.longValue();

		long concrete_value = left_concrete_value + right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.PLUS, right_expr, concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (long)-(long) operation into a IntegerBinaryExpression
	 * instance.
	 * 
	 * It uses 64-bit arithmetics for computing the concrete value.
	 */
	@Override
	public Object visit(BvSub.Bv64 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		long right_concrete_value = ((Long) right_expr.getConcreteValue())
				.longValue();

		long concrete_value = left_concrete_value - right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.MINUS, right_expr, concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (long)*(long) operation into a IntegerBinaryExpression
	 * instance.
	 * 
	 * It uses 64-bit arithmetics for computing the concrete value.
	 */
	@Override
	public Object visit(BvMul.Bv64 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		long right_concrete_value = ((Long) right_expr.getConcreteValue())
				.longValue();

		long concrete_value = left_concrete_value + right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.MUL, right_expr, concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (long)/(long) operation into a IntegerBinaryExpression
	 * instance.
	 * 
	 * It uses 64-bit arithmetics for computing the concrete value.
	 */
	@Override
	public Object visit(BvSDiv.Bv64 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		long right_concrete_value = ((Long) right_expr.getConcreteValue())
				.longValue();

		long concrete_value = left_concrete_value / right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.DIV, right_expr, concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (long)%(long) operation into a IntegerBinaryExpression
	 * instance.
	 * 
	 * It uses 64-bit arithmetics for computing the concrete value.
	 */
	@Override
	public Object visit(BvSRem.Bv64 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		long right_concrete_value = ((Long) right_expr.getConcreteValue())
				.longValue();

		long concrete_value = left_concrete_value % right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.REM, right_expr, concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (long)&(long) operation into a IntegerBinaryExpression
	 * instance.
	 * 
	 * It uses 64-bit arithmetics for computing the concrete value.
	 */
	@Override
	public Object visit(BvAnd.Bv64 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		long right_concrete_value = ((Long) right_expr.getConcreteValue())
				.longValue();

		long concrete_value = left_concrete_value & right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.AND, right_expr, concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (long)|(long) operation into a IntegerBinaryExpression
	 * instance.
	 * 
	 * It uses 64-bit arithmetics for computing the concrete value.
	 */
	@Override
	public Object visit(BvOr.Bv64 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		long right_concrete_value = ((Long) right_expr.getConcreteValue())
				.longValue();

		long concrete_value = left_concrete_value | right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.OR, right_expr, concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (long)^(long) operation into a IntegerBinaryExpression
	 * instance.
	 * 
	 * It uses 64-bit arithmetics for computing the concrete value.
	 */
	@Override
	public Object visit(BvXOr.Bv64 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		long right_concrete_value = ((Long) right_expr.getConcreteValue())
				.longValue();

		long concrete_value = left_concrete_value ^ right_concrete_value;

		IntegerBinaryExpression ret_val = new IntegerBinaryExpression(
				left_expr, Operator.XOR, right_expr, concrete_value);

		return ret_val;
	}

	@Override
	public Object visit(BvNeg.Bv64 b) {
		BitVector64 param = (BitVector64) b.getParam();
		IntegerExpression param_expr = (IntegerExpression) param.accept(this);

		long param_concrete_value = ((Long) param_expr.getConcreteValue())
				.longValue();

		long concrete_value = -param_concrete_value;

		IntegerUnaryExpression ret_val = new IntegerUnaryExpression(param_expr,
				Operator.NEG, (long) concrete_value);

		return ret_val;
	}

	/**
	 * Translates a (double)int expression. Returns a IntegerToRealCast
	 * instance.
	 */
	@Override
	public Object visit(Fp64ExtendingAnBv32 f) {
		IntegerExpression integerExpression = (IntegerExpression) f.getParam()
				.accept(this);
		int integerExpressionConcreteValue = ((Long) integerExpression
				.getConcreteValue()).intValue();
		double concreteValue = (double) integerExpressionConcreteValue;
		return new IntegerToRealCast(integerExpression, concreteValue);
	}

	/**
	 * Translates a (double)long expression. Returns a IntegerToRealCast
	 * instance.
	 */
	@Override
	public Object visit(Fp64ExtendingAnBv64 f) {
		IntegerExpression integerExpression = (IntegerExpression) f.getParam()
				.accept(this);
		long integerExpressionConcreteValue = ((Long) integerExpression
				.getConcreteValue()).longValue();
		double concreteValue = (double) integerExpressionConcreteValue;
		return new IntegerToRealCast(integerExpression, concreteValue);
	}

	/**
	 * Translates a (double)float expression.
	 * 
	 * No additional constraints for modelling casts are added by the moment.
	 */
	@Override
	public Object visit(Fp64ExtendingAnFp32 f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		return realExpression;
	}

	/**
	 * Translates a (double)+(double) expression. It returns a
	 * RealBinaryExpression(PLUS)
	 * 
	 * It uses 64-floating point arithmetic for computing the concrete value.
	 */
	@Override
	public Object visit(FpAdd.Fp64 f) {
		RealExpression left_expr = (RealExpression) f.getLeft().accept(this);
		RealExpression right_expr = (RealExpression) f.getRight().accept(this);

		double left_concrete_float = ((Double) left_expr.getConcreteValue())
				.doubleValue();
		double right_concrete_float = ((Double) right_expr.getConcreteValue())
				.doubleValue();

		double concrete_value = left_concrete_float + right_concrete_float;

		return new RealBinaryExpression(left_expr, Operator.PLUS, right_expr,
				concrete_value);
	}

	/**
	 * Translates a (double)-(double) expression. It returns a
	 * RealBinaryExpression(MINUS)
	 * 
	 * It uses 64-floating point arithmetic for computing the concrete value.
	 */
	@Override
	public Object visit(FpSub.Fp64 f) {
		RealExpression left_expr = (RealExpression) f.getLeft().accept(this);
		RealExpression right_expr = (RealExpression) f.getRight().accept(this);

		double left_concrete_float = ((Double) left_expr.getConcreteValue())
				.doubleValue();
		double right_concrete_float = ((Double) right_expr.getConcreteValue())
				.doubleValue();

		double concrete_value = left_concrete_float - right_concrete_float;

		return new RealBinaryExpression(left_expr, Operator.MINUS, right_expr,
				concrete_value);
	}

	/**
	 * Translates a (double)*(double) expression. It returns a
	 * RealBinaryExpression(MUL)
	 * 
	 * It uses 64-floating point arithmetic for computing the concrete value.
	 */
	@Override
	public Object visit(FpMul.Fp64 f) {
		RealExpression left_expr = (RealExpression) f.getLeft().accept(this);
		RealExpression right_expr = (RealExpression) f.getRight().accept(this);

		double left_concrete_float = ((Double) left_expr.getConcreteValue())
				.doubleValue();
		double right_concrete_float = ((Double) right_expr.getConcreteValue())
				.doubleValue();

		double concrete_value = left_concrete_float * right_concrete_float;

		return new RealBinaryExpression(left_expr, Operator.MUL, right_expr,
				concrete_value);
	}

	/**
	 * Translates a (double)/(double) expression. It returns a
	 * RealBinaryExpression(PLUS)
	 * 
	 * It uses 64-floating point arithmetic for computing the concrete value.
	 */
	@Override
	public Object visit(FpSDiv.Fp64 f) {
		RealExpression left_expr = (RealExpression) f.getLeft().accept(this);
		RealExpression right_expr = (RealExpression) f.getRight().accept(this);

		double left_concrete_float = ((Double) left_expr.getConcreteValue())
				.doubleValue();
		double right_concrete_float = ((Double) right_expr.getConcreteValue())
				.doubleValue();

		double concrete_value = left_concrete_float / right_concrete_float;

		return new RealBinaryExpression(left_expr, Operator.DIV, right_expr,
				concrete_value);
	}

	/**
	 * Translates a (double)%(double) expression. It returns a
	 * RealBinaryExpression(PLUS)
	 * 
	 * It uses 64-floating point arithmetic for computing the concrete value.
	 */
	@Override
	public Object visit(FpSRem.Fp64 f) {
		RealExpression left_expr = (RealExpression) f.getLeft().accept(this);
		RealExpression right_expr = (RealExpression) f.getRight().accept(this);

		double left_concrete_float = ((Double) left_expr.getConcreteValue())
				.doubleValue();
		double right_concrete_float = ((Double) right_expr.getConcreteValue())
				.doubleValue();

		double concrete_value = left_concrete_float % right_concrete_float;

		return new RealBinaryExpression(left_expr, Operator.REM, right_expr,
				concrete_value);
	}

	/**
	 * Translates a -(double) expression. It returns a RealUnaryExpression(NEG)
	 * 
	 * It uses 64-floating point arithmetic for computing the concrete value.
	 */
	@Override
	public Object visit(FpNeg.Fp64 f) {
		if (!(f.getParam() instanceof DoubleExpression)) {
			throw new IllegalArgumentException(
					"Unexpected FpNeg.Fp64 with a param of type "
							+ f.getParam().getClass().getName());
		}
		DoubleExpression param_expr = (DoubleExpression) f.getParam();

		RealExpression real_param_expr = (RealExpression) param_expr
				.accept(this);

		double param_concrete_float = ((Double) real_param_expr
				.getConcreteValue()).doubleValue();

		double concrete_value = -param_concrete_float;

		return new RealUnaryExpression(real_param_expr, Operator.NEG,
				concrete_value);
	}

	/**
	 * Translates a variable whose type is double. Returns a RealVariable if the
	 * variable is symbolically marked.
	 */
	@Override
	public Object visit(DoubleVariable f) {
		DoubleExpression symbolic_value = (DoubleExpression) concolicState
				.getSymbolicValue(f);
		RealExpression float_expr = (RealExpression) symbolic_value
				.accept(this);
		if (concolicState.isMarked(f)) {

			double concreteValue = (Double) float_expr.getConcreteValue();
			String symbolic_name = concolicState.getSymbolicName(f);
			RealVariable realVariable = RealVariableFactory.buildRealVariable(
					symbolic_name, concreteValue, Double.MIN_VALUE,
					Double.MAX_VALUE);
			return realVariable;
		} else {

			return float_expr;
		}

	}

	/**
	 * Translates a double literal into a RealConstant.
	 */
	@Override
	public Object visit(DoubleLiteral f) {
		return new RealConstant(f.getValue());
	}

	/**
	 * Translates a double-precision floating point comparison (-1 on NaN)
	 * 
	 * <ul>
	 * <li>If left==right ==> returns 0</li>
	 * <li>If left>right ==> returns 1</li>
	 * <li>If left<right ==> returns -1</li>
	 * <li>If left is NaN or right is NaN ==> returns 1</li>
	 * </ul>
	 */
	@Override
	public Object visit(FpSLt.Fp64 b) {
		RealExpression left_real_expression = (RealExpression) b.getLeft()
				.accept(this);
		RealExpression right_real_expression = (RealExpression) b.getRight()
				.accept(this);

		double left_concrete_value = ((Double) left_real_expression
				.getConcreteValue()).doubleValue();
		double right_concrete_value = ((Double) right_real_expression
				.getConcreteValue()).doubleValue();

		int concrete_value;
		if (new Double(left_concrete_value).isNaN()
				|| new Double(right_concrete_value).isNaN()) {
			concrete_value = 1;
		} else if (left_concrete_value == right_concrete_value) {
			concrete_value = 0;
		} else if (left_concrete_value > right_concrete_value) {
			concrete_value = 1;
		} else {
			assert left_concrete_value < right_concrete_value;
			concrete_value = -1;
		}

		return new RealComparison(left_real_expression, right_real_expression,
				(long) concrete_value);
	}

	/**
	 * Translates a ISHR(left,right) operation into a IntegerBinaryExpression
	 * 
	 * It follows the ISHR semantics (only takes 5 low bits of right value)
	 */
	@Override
	public Object visit(BvSHR.Bv32 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		int concrete_value = left_concrete_value >> (right_concrete_value & 0x001F);

		return new IntegerBinaryExpression(left_expr, Operator.SHR, right_expr,
				(long) concrete_value);
	}

	/**
	 * Translates a ISHL(left,right) operation into a IntegerBinaryExpression
	 * 
	 * It follows the ISHL semantics (only takes 5 low bits of right value)
	 */
	@Override
	public Object visit(BvSHL.Bv32 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		int concrete_value = left_concrete_value << (right_concrete_value & 0x001F);

		return new IntegerBinaryExpression(left_expr, Operator.SHL, right_expr,
				(long) concrete_value);
	}

	/**
	 * Translates a IUSHR(left,right) operation into a IntegerBinaryExpression
	 * 
	 * It follows the IUSHR semantics (only takes 5 low bits of right value)
	 */
	@Override
	public Object visit(BvUSHR.Bv32 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		int concrete_value = left_concrete_value >>> (right_concrete_value & 0x001F);

		return new IntegerBinaryExpression(left_expr, Operator.USHR,
				right_expr, (long) concrete_value);
	}

	/**
	 * Translates a (long)<<(int) expression into a IntegerBinaryExpression.
	 * 
	 * Shifts left_expr (the long integer) left by the amount indicated in the
	 * low six bits of right_expr (an int)
	 */
	@Override
	public Object visit(BvSHL.Bv64 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		long concrete_value = left_concrete_value << (right_concrete_value & 0x003F);

		return new IntegerBinaryExpression(left_expr, Operator.SHL, right_expr,
				concrete_value);
	}

	/**
	 * Stack=value1(int)|value2(long)
	 * 
	 * Pops an int and a long integer from the stack. Shifts value2 (the long
	 * integer) right by the amount indicated in the low six bits of value1 (an
	 * int). The long integer result is then pushed back onto the stack. The
	 * value is shifted arithmetically (preserving the sign extension).
	 */
	@Override
	public Object visit(BvSHR.Bv64 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		long concrete_value = left_concrete_value >> (right_concrete_value & 0x003F);

		return new IntegerBinaryExpression(left_expr, Operator.SHR, right_expr,
				concrete_value);
	}

	/**
	 * Stack=value1(int)|value2(long)
	 * 
	 * Pops an integer and a long integer and from the stack. Shifts value2 (the
	 * long integer) right by the amount indicated in the low six bits of value1
	 * (an int). The long integer result is then pushed back onto the stack. The
	 * value is shifted logically (ignoring the sign extension - useful for
	 * unsigned values).
	 */
	@Override
	public Object visit(BvUSHR.Bv64 b) {
		IntegerExpression left_expr = (IntegerExpression) b.getLeft().accept(
				this);
		IntegerExpression right_expr = (IntegerExpression) b.getRight().accept(
				this);

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		long concrete_value = left_concrete_value >>> (right_concrete_value & 0x003F);

		return new IntegerBinaryExpression(left_expr, Operator.USHR,
				right_expr, concrete_value);
	}

	/**
	 * Translates a floating-point cosine function call to a RealUnaryExpression
	 * instance.
	 * 
	 * It uses Math.cos(double) to compute the concrete value of the expression.
	 */
	@Override
	public Object visit(COS f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.COS;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.cos(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a ACOS (Arc Cosine) function call to the corresponding
	 * RealUnaryExpression.
	 * 
	 * It uses Math.acos for computing the concrete value.
	 */
	@Override
	public Object visit(ACOS f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.ACOS;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.acos(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a LOG10 (Base 10 Logarithm) function call to the corresponding
	 * RealUnaryExpression.
	 * 
	 * It uses Math.log10 for computing the concrete value
	 */
	@Override
	public Object visit(LOG10 f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.LOG10;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.log10(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a LOG1P (Natural Logarithm Plus 1) function call to the
	 * corresponding RealUnaryExpression.
	 * 
	 * It uses Math.log1p for computing the concrete value
	 */
	@Override
	public Object visit(LOG1P f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.LOG1P;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.log1p(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a RINT (the double value that is closest in value to the
	 * argument and is equal to a mathematical integer) function call to the
	 * corresponding RealUnaryExpression.
	 * 
	 * It uses Math.rint for computing the concrete value.
	 */
	@Override
	public Object visit(RINT f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.RINT;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.rint(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a SIN (trigonometric sine of an angle) function call to the
	 * corresponding RealUnaryExpression.
	 * 
	 * It uses Math.sin for computing the concrete value.
	 */
	@Override
	public Object visit(SIN f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.SIN;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.sin(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a SINH ( hyperbolic sine of double value) function call to the
	 * corresponding RealUnaryExpression.
	 * 
	 * It uses Math.sinh for computing the concrete value.
	 */
	@Override
	public Object visit(SINH f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.SINH;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.sinh(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a SQRT (rounded positive square root ) function call to the
	 * corresponding RealUnaryExpression.
	 * 
	 * It uses Math.sqrt for computing the concrete value.
	 */
	@Override
	public Object visit(SQRT f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.SQRT;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.sqrt(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a TAN ( trigonometric tangent of an angle) function call to
	 * the corresponding RealUnaryExpression.
	 * 
	 * It uses Math.tan for computing the concrete value.
	 */
	@Override
	public Object visit(TAN f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.TAN;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.tan(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a TANH (hyperbolic tangent of a double value) function call to
	 * the corresponding RealUnaryExpression.
	 * 
	 * It uses Math.tanh for computing the concrete value.
	 */
	@Override
	public Object visit(TANH f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.TANH;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.tanh(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a ToDegrees (converts an angle measured in radians to degrees)
	 * function call to the corresponding RealUnaryExpression.
	 * 
	 * It uses Math.toDegrees for computing the concrete value.
	 */
	@Override
	public Object visit(ToDegrees f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.TODEGREES;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.toDegrees(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a ToRadians (converts an angle measured in degrees to radians)
	 * function call to the corresponding RealUnaryExpression.
	 * 
	 * It uses Math.toDegrees for computing the concrete value.
	 */
	@Override
	public Object visit(ToRadians f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.TORADIANS;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.toRadians(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a ASIN (arc sine of a value) function call to the
	 * corresponding RealUnaryExpression.
	 * 
	 * It uses Math.asin for computing the concrete value.
	 */
	@Override
	public Object visit(ASIN f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.ASIN;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.asin(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a ATAN ( arc tangent of a value) function call to the
	 * corresponding RealUnaryExpression.
	 * 
	 * It uses Math.atan for computing the concrete value.
	 */
	@Override
	public Object visit(ATAN f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.ATAN;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.atan(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a CBRT (cube root) function call to the corresponding
	 * RealUnaryExpression.
	 * 
	 * It uses Math.cbrt for computing the concrete value.
	 */
	@Override
	public Object visit(CBRT f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.CBRT;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.cbrt(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a CEIL (ceiling) function call to the corresponding
	 * RealUnaryExpression.
	 * 
	 * It uses Math.ceil for computing the concrete value.
	 */
	@Override
	public Object visit(CEIL f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.CEIL;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.ceil(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a COSH (hyperbolic cosine) function call to the corresponding
	 * RealUnaryExpression.
	 * 
	 * It uses Math.cosh for computing the concrete value.
	 */
	@Override
	public Object visit(COSH f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.COSH;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.cosh(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a EXP (Euer number exponent) function call to the
	 * corresponding RealUnaryExpression.
	 * 
	 * It uses Math.exp for computing the concrete value.
	 */
	@Override
	public Object visit(EXP f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.EXP;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.exp(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a EXPM1 (Euer number exponent minus 1) function call to the
	 * corresponding RealUnaryExpression.
	 * 
	 * It uses Math.expm1 for computing the concrete value.
	 */
	@Override
	public Object visit(EXPM1 f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.EXPM1;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.expm1(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a FLOOR (floor) function call to the corresponding
	 * RealUnaryExpression.
	 * 
	 * It uses Math.floor for computing the concrete value.
	 */
	@Override
	public Object visit(FLOOR f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.FLOOR;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.floor(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a LOG (Natural logarithm) function call to the corresponding
	 * RealUnaryExpression.
	 * 
	 * It uses Math.log for computing the concrete value.
	 */
	@Override
	public Object visit(LOG f) {
		RealExpression realExpression = (RealExpression) f.getParam().accept(
				this);
		Operator op = Operator.LOG;
		double param_concrete_value = ((Double) realExpression
				.getConcreteValue()).doubleValue();
		double concrete_value = Math.log(param_concrete_value);
		return new RealUnaryExpression(realExpression, op, concrete_value);
	}

	/**
	 * Translates a ATAN2 (s the angle theta from the conversion of rectangular
	 * coordinates (x, y) to polar coordinates (r, theta)) function call to the
	 * corresponding RealBinaryExpression.
	 * 
	 * It uses Math.atan2 for computing the concrete value.
	 */
	@Override
	public Object visit(ATAN2 f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		RealExpression right = (RealExpression) f.getRight().accept(this);
		Operator op = Operator.ATAN2;
		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		double concrete_value = Math.atan2(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right, concrete_value);
	}

	/**
	 * Translates a HYPOT (sqrt(pow(x,2) +pow(y,2))) function call to the
	 * corresponding RealBinaryExpression.
	 * 
	 * It uses Math.hypot for computing the concrete value.
	 */
	@Override
	public Object visit(HYPOT f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		RealExpression right = (RealExpression) f.getRight().accept(this);
		Operator op = Operator.HYPOT;
		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		double concrete_value = Math.hypot(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right, concrete_value);
	}

	/**
	 * Translates a IEEEremainder (Computes the remainder operation on two
	 * arguments as prescribed by the IEEE 754 standard) function call to the
	 * corresponding RealBinaryExpression.
	 * 
	 * It uses Math.IEEEremainder for computing the concrete value.
	 */
	@Override
	public Object visit(IEEEremainder f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		RealExpression right = (RealExpression) f.getRight().accept(this);
		Operator op = Operator.IEEEREMAINDER;
		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		double concrete_value = Math.IEEEremainder(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right, concrete_value);
	}

	/**
	 * Translates a POW (returns the value of the first argument raised to the
	 * power of the second argument. ) function call to the corresponding
	 * RealBinaryExpression.
	 * 
	 * It uses Math.pow for computing the concrete value.
	 */
	@Override
	public Object visit(POW f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		RealExpression right = (RealExpression) f.getRight().accept(this);
		Operator op = Operator.POW;
		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		double concrete_value = Math.pow(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right, concrete_value);
	}

	@Override
	public Object visit(NextUp.Fp64 f) {
		RealExpression param = (RealExpression) f.getParam().accept(this);
		double param_value = ((Double) param.getConcreteValue()).doubleValue();
		double concrete_value = Math.nextUp(param_value);
		return new RealUnaryExpression(param, Operator.NEXTUP, concrete_value);
	}

	@Override
	public Object visit(GetExponent.Fp64 f) {
		RealExpression param = (RealExpression) f.getParam().accept(this);
		double param_concrete_value = ((Double) param.getConcreteValue())
				.doubleValue();

		int concrete_value = Math.getExponent(param_concrete_value);

		RealUnaryExpression realUExpr = new RealUnaryExpression(param,
				Operator.GETEXPONENT, (double) concrete_value);

		RealToIntegerCast result = new RealToIntegerCast(realUExpr,
				(long) concrete_value);
		return result;
	}

	@Override
	public Object visit(ULP.Fp64 f) {
		RealExpression param = (RealExpression) f.getParam().accept(this);
		double param_value = ((Double) param.getConcreteValue()).doubleValue();
		double concrete_value = Math.ulp(param_value);
		return new RealUnaryExpression(param, Operator.ULP,
				(double) concrete_value);
	}

	@Override
	public Object visit(ABS.Fp64 f) {
		RealExpression param = (RealExpression) f.getParam().accept(this);
		double param_value = ((Double) param.getConcreteValue()).doubleValue();
		double concrete_value = Math.abs(param_value);
		return new RealUnaryExpression(param, Operator.ABS, concrete_value);
	}

	@Override
	public Object visit(SCALB.Fp32 f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		IntegerExpression right = (IntegerExpression) f.getRight().accept(this);
		Operator op = Operator.SCALB;
		float left_concrete_value = ((Double) left.getConcreteValue())
				.floatValue();
		int right_concrete_value = ((Long) right.getConcreteValue()).intValue();

		float concrete_value = Math.scalb(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right,
				(double) concrete_value);
	}

	@Override
	public Object visit(SCALB.Fp64 f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		IntegerExpression right = (IntegerExpression) f.getRight().accept(this);
		Operator op = Operator.SCALB;
		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		int right_concrete_value = ((Long) right.getConcreteValue()).intValue();

		double concrete_value = Math.scalb(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right, concrete_value);
	}

	@Override
	public Object visit(CopySign.Fp64 f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		RealExpression right = (RealExpression) f.getRight().accept(this);
		Operator op = Operator.COPYSIGN;
		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		double concrete_value = Math.copySign(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right, concrete_value);
	}

	@Override
	public Object visit(NextAfter.Fp32 f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		RealExpression right = (RealExpression) f.getRight().accept(this);
		Operator op = Operator.NEXTAFTER;
		float left_concrete_value = ((Double) left.getConcreteValue())
				.floatValue();
		float right_concrete_value = ((Double) right.getConcreteValue())
				.floatValue();

		float concrete_value = Math.nextAfter(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right,
				(double) concrete_value);
	}

	@Override
	public Object visit(NextAfter.Fp64 f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		RealExpression right = (RealExpression) f.getRight().accept(this);
		Operator op = Operator.NEXTAFTER;
		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		double concrete_value = Math.nextAfter(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right, concrete_value);
	}

	@Override
	public Object visit(MAX.Fp64 f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		RealExpression right = (RealExpression) f.getRight().accept(this);
		Operator op = Operator.MAX;
		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		double concrete_value = Math.max(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right, concrete_value);
	}

	@Override
	public Object visit(MIN.Fp64 f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		RealExpression right = (RealExpression) f.getRight().accept(this);
		Operator op = Operator.MIN;
		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		double concrete_value = Math.min(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right, concrete_value);
	}

	@Override
	public Object visit(SIGNUM.Fp64 f) {
		RealExpression param = (RealExpression) f.getParam().accept(this);
		double param_value = ((Double) param.getConcreteValue()).doubleValue();
		double concrete_value = Math.signum(param_value);
		return new RealUnaryExpression(param, Operator.SIGNUM, concrete_value);

	}

	@Override
	public Object visit(NextUp.Fp32 f) {
		RealExpression param = (RealExpression) f.getParam().accept(this);
		float param_value = ((Double) param.getConcreteValue()).floatValue();
		float concrete_value = Math.nextUp(param_value);
		return new RealUnaryExpression(param, Operator.NEXTUP,
				(double) concrete_value);
	}

	@Override
	public Object visit(GetExponent.Fp32 f) {
		RealExpression param = (RealExpression) f.getParam().accept(this);
		float param_concrete_value = ((Double) param.getConcreteValue())
				.floatValue();

		int concrete_value = Math.getExponent(param_concrete_value);

		RealUnaryExpression realUExpr = new RealUnaryExpression(param,
				Operator.GETEXPONENT, (double) concrete_value);
		RealToIntegerCast result = new RealToIntegerCast(realUExpr,
				(long) concrete_value);
		return result;

	}

	@Override
	public Object visit(ULP.Fp32 f) {
		RealExpression param = (RealExpression) f.getParam().accept(this);
		float param_value = ((Double) param.getConcreteValue()).floatValue();
		float concrete_value = Math.ulp(param_value);
		return new RealUnaryExpression(param, Operator.ULP,
				(double) concrete_value);
	}

	/**
	 * Translates a fp32 ABS instance into a RealUnaryExpression(ABS).
	 */
	@Override
	public Object visit(ABS.Fp32 f) {
		RealExpression param = (RealExpression) f.getParam().accept(this);
		float param_value = ((Double) param.getConcreteValue()).floatValue();
		float concrete_value = Math.abs(param_value);
		return new RealUnaryExpression(param, Operator.ABS,
				(double) concrete_value);
	}

	@Override
	public Object visit(CopySign.Fp32 f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		RealExpression right = (RealExpression) f.getRight().accept(this);
		Operator op = Operator.COPYSIGN;
		float left_concrete_value = ((Double) left.getConcreteValue())
				.floatValue();
		float right_concrete_value = ((Double) right.getConcreteValue())
				.floatValue();

		float concrete_value = Math.copySign(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right,
				(double) concrete_value);
	}

	@Override
	public Object visit(MAX.Fp32 f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		RealExpression right = (RealExpression) f.getRight().accept(this);
		Operator op = Operator.MAX;
		float left_concrete_value = ((Double) left.getConcreteValue())
				.floatValue();
		float right_concrete_value = ((Double) right.getConcreteValue())
				.floatValue();

		float concrete_value = Math.max(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right,
				(double) concrete_value);
	}

	@Override
	public Object visit(MIN.Fp32 f) {
		RealExpression left = (RealExpression) f.getLeft().accept(this);
		RealExpression right = (RealExpression) f.getRight().accept(this);
		Operator op = Operator.MIN;
		float left_concrete_value = ((Double) left.getConcreteValue())
				.floatValue();
		float right_concrete_value = ((Double) right.getConcreteValue())
				.floatValue();

		float concrete_value = Math.min(left_concrete_value,
				right_concrete_value);
		return new RealBinaryExpression(left, op, right,
				(double) concrete_value);
	}

	@Override
	public Object visit(SIGNUM.Fp32 f) {
		RealExpression param = (RealExpression) f.getParam().accept(this);
		float param_value = ((Double) param.getConcreteValue()).floatValue();
		float concrete_value = Math.signum(param_value);
		return new RealUnaryExpression(param, Operator.SIGNUM,
				(double) concrete_value);
	}

	@Override
	public Object visit(ABS.Bv64 b) {
		IntegerExpression param = (IntegerExpression) b.getParam().accept(this);
		int param_value = ((Long) param.getConcreteValue()).intValue();
		int concrete_value = Math.abs(param_value);
		return new IntegerUnaryExpression(param, Operator.ABS,
				(long) concrete_value);
	}

	@Override
	public Object visit(MAX.Bv64 b) {
		IntegerExpression left = (IntegerExpression) b.getLeft().accept(this);
		IntegerExpression right = (IntegerExpression) b.getRight().accept(this);
		Operator op = Operator.MAX;
		long left_concrete_value = ((Long) left.getConcreteValue()).longValue();
		long right_concrete_value = ((Long) right.getConcreteValue())
				.longValue();

		long concrete_value = Math.max(left_concrete_value,
				right_concrete_value);
		return new IntegerBinaryExpression(left, op, right, concrete_value);
	}

	@Override
	public Object visit(MIN.Bv64 b) {
		IntegerExpression left = (IntegerExpression) b.getLeft().accept(this);
		IntegerExpression right = (IntegerExpression) b.getRight().accept(this);
		Operator op = Operator.MIN;
		long left_concrete_value = ((Long) left.getConcreteValue()).longValue();
		long right_concrete_value = ((Long) right.getConcreteValue())
				.longValue();

		long concrete_value = Math.min(left_concrete_value,
				right_concrete_value);
		return new IntegerBinaryExpression(left, op, right, concrete_value);
	}

	@Override
	public Object visit(ROUND.Fp64 f) {
		RealExpression param = (RealExpression) f.getParam().accept(this);
		double param_value = ((Double) param.getConcreteValue()).doubleValue();

		long concrete_value = Math.round(param_value);
		RealUnaryExpression realExpr = new RealUnaryExpression(param,
				Operator.ROUND, (double) concrete_value);

		RealToIntegerCast ret = new RealToIntegerCast(realExpr, concrete_value);

		return ret;
	}

	@Override
	public Object visit(ROUND.Fp32 f) {
		RealExpression param = (RealExpression) f.getParam().accept(this);
		float param_value = ((Double) param.getConcreteValue()).floatValue();

		int concrete_value = Math.round(param_value);
		RealUnaryExpression realExpr = new RealUnaryExpression(param,
				Operator.ROUND, (double) concrete_value);

		RealToIntegerCast ret = new RealToIntegerCast(realExpr,
				(long) concrete_value);

		return ret;
	}

	@Override
	public Object visit(ABS.Bv32 b) {
		IntegerExpression param = (IntegerExpression) b.getParam().accept(this);
		long param_value = ((Long) param.getConcreteValue()).longValue();
		long concrete_value = Math.abs(param_value);
		return new IntegerUnaryExpression(param, Operator.ABS, concrete_value);
	}

	@Override
	public Object visit(MAX.Bv32 b) {
		IntegerExpression left = (IntegerExpression) b.getLeft().accept(this);
		IntegerExpression right = (IntegerExpression) b.getRight().accept(this);
		Operator op = Operator.MAX;
		int left_concrete_value = ((Long) left.getConcreteValue()).intValue();
		int right_concrete_value = ((Long) right.getConcreteValue()).intValue();

		int concrete_value = Math
				.max(left_concrete_value, right_concrete_value);
		return new IntegerBinaryExpression(left, op, right,
				(long) concrete_value);
	}

	@Override
	public Object visit(MIN.Bv32 b) {
		IntegerExpression left = (IntegerExpression) b.getLeft().accept(this);
		IntegerExpression right = (IntegerExpression) b.getRight().accept(this);
		Operator op = Operator.MIN;
		int left_concrete_value = ((Long) left.getConcreteValue()).intValue();
		int right_concrete_value = ((Long) right.getConcreteValue()).intValue();

		int concrete_value = Math
				.min(left_concrete_value, right_concrete_value);
		return new IntegerBinaryExpression(left, op, right,
				(long) concrete_value);
	}

	/**
	 * Translates a lenght(string) expression into a
	 * StringToIntCast(StringUnary(LENGTH)) expression.
	 * 
	 * It uses String.length() to compute the concrete value;
	 */
	@Override
	public Object visit(StringLength str) {
		StringExpression str_expr_one = (StringExpression) str.getStringParam()
				.accept(this);
		String str_concrete_value = (String) str_expr_one.getConcreteValue();
		int concrete_value = str_concrete_value.length();
		StringUnaryExpression strUnExpr = new StringUnaryExpression(
				str_expr_one, Operator.LENGTH, Integer.toString(concrete_value));
		return new StringToIntCast(strUnExpr, (long) (concrete_value));

	}

	/**
	 * If a reference variable is marked, it is supposed to be a String
	 * variable.
	 */
	@Override
	public Object visit(ReferenceVariable r) {
		Reference symbolic_value = (Reference) concolicState
				.getSymbolicValue(r);
		StringExpression string_expr = (StringExpression) symbolic_value
				.accept(this);
		if (concolicState.isMarked(r)) {

			String concreteValue = (String) string_expr.getConcreteValue();
			String symbolic_name = concolicState.getSymbolicName(r);
			StringVariable stringVariable = StringVariableFactory
					.buildStringVariable(symbolic_name, concreteValue,
							concreteValue, concreteValue);
			return stringVariable;
		} else {

			return string_expr;
		}
	}

	@Override
	public Object visit(LiteralNonNullReference r) {
		if (!r.getDynamicType().getType().equals(String.class)) {
			throw new IllegalArgumentException(
					"Cannot handle references of type "
							+ r.getDynamicType().getType());
		}

		String str_constant = r.getStringConstant();
		StringConstant c = new StringConstant(str_constant);
		return c;
	}

	@Override
	public Object visit(LiteralNullReference r) {
		if (!r.getDynamicType().getType().equals(String.class)) {
			throw new IllegalArgumentException(
					"Cannot handle references of type "
							+ r.getDynamicType().getType());
		}

		return null;
	}

	/**
	 * Translates a trim(string) expression into a StringUnaryExpression(TRIM)
	 * instance.
	 * 
	 * It uses String.trim() to compute the concrete value;
	 */
	@Override
	public Object visit(StringTrim str) {
		StringExpression str_param = (StringExpression) str.getStringParam()
				.accept(this);
		String str_param_concrete_value = (String) str_param.getConcreteValue();
		String concrete_value = str_param_concrete_value.trim();
		StringUnaryExpression strUnExpr = new StringUnaryExpression(str_param,
				Operator.TRIM, concrete_value);
		return strUnExpr;
	}

	/**
	 * Translates a toUpperCase(string) expression into a
	 * StringUnaryExpression(TOUPPERCASE) instance.
	 * 
	 * It uses String.toUpperCase() to compute the concrete value;
	 */
	@Override
	public Object visit(StringToUpperCase str) {
		StringExpression str_param = (StringExpression) str.getStringParam()
				.accept(this);
		String str_param_concrete_value = (String) str_param.getConcreteValue();
		String concrete_value = str_param_concrete_value.toUpperCase();
		StringUnaryExpression strUnExpr = new StringUnaryExpression(str_param,
				Operator.TOUPPERCASE, concrete_value);
		return strUnExpr;
	}

	/**
	 * Translates a toLowerCase(string) expression into a
	 * StringUnaryExpression(TOLOWERCASE) instance.
	 * 
	 * It uses String.toLowerCase() to compute the concrete value;
	 */
	@Override
	public Object visit(StringToLowerCase str) {
		StringExpression str_param = (StringExpression) str.getStringParam()
				.accept(this);
		String str_param_concrete_value = (String) str_param.getConcreteValue();
		String concrete_value = str_param_concrete_value.toLowerCase();
		StringUnaryExpression strUnExpr = new StringUnaryExpression(str_param,
				Operator.TOLOWERCASE, concrete_value);
		return strUnExpr;
	}

	/**
	 * Translates a compareTo(string,string) expression into a
	 * StringToIntCast(StringBinaryExpression(COMPARETO)) expression.
	 * 
	 * It uses String.compareTo() to compute the concrete value.
	 */
	@Override
	public Object visit(StringCompareTo str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);
		StringExpression str_param_2 = (StringExpression) str.getParam()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();

		int concrete_value = str_param_1_concrete_value
				.compareTo(str_param_2_concrete_value);
		StringBinaryExpression strBExpr = new StringBinaryExpression(
				str_param_1, Operator.COMPARETO, str_param_2,
				Integer.toString(concrete_value));
		return new StringToIntCast(strBExpr, (long) concrete_value);
	}

	/**
	 * Translates a compareToIgnoreCase(string,string) expression into a
	 * StringToIntCast(StringBinaryExpression(COMPARETOIGNORECASE)) expression.
	 * 
	 * It uses String.compareToIgnoreCase() to compute the concrete value.
	 */
	@Override
	public Object visit(StringCompareToIgnoreCase str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);
		StringExpression str_param_2 = (StringExpression) str.getParam()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();

		int concrete_value = str_param_1_concrete_value
				.compareToIgnoreCase(str_param_2_concrete_value);
		StringBinaryExpression strBExpr = new StringBinaryExpression(
				str_param_1, Operator.COMPARETOIGNORECASE, str_param_2,
				Integer.toString(concrete_value));
		return new StringToIntCast(strBExpr, (long) concrete_value);

	}

	/**
	 * Translates a indexOf(string,string) expression into a
	 * StringToIntCast(StringBinaryExpression(INDEXOFS)) expression.
	 * 
	 * It uses String.indexOf(string,string) to compute the concrete value.
	 */
	@Override
	public Object visit(StringIndexOfS str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);
		StringExpression str_param_2 = (StringExpression) str.getParam()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();

		int concrete_value = str_param_1_concrete_value
				.indexOf(str_param_2_concrete_value);
		StringBinaryExpression strBExpr = new StringBinaryExpression(
				str_param_1, Operator.INDEXOFS, str_param_2,
				Integer.toString(concrete_value));
		return new StringToIntCast(strBExpr, (long) concrete_value);
	}

	/**
	 * Translates a lastIndexOf(string,string) expression into a
	 * StringToIntCast(StringBinaryExpression(LASTINDEXOFS)) expression.
	 * 
	 * It uses String.lastIndexOf(string,string) to compute the concrete value.
	 */
	@Override
	public Object visit(StringLastIndexOfS str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);
		StringExpression str_param_2 = (StringExpression) str.getParam()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();

		int concrete_value = str_param_1_concrete_value
				.lastIndexOf(str_param_2_concrete_value);
		StringBinaryExpression strBExpr = new StringBinaryExpression(
				str_param_1, Operator.LASTINDEXOFS, str_param_2,
				Integer.toString(concrete_value));
		return new StringToIntCast(strBExpr, (long) concrete_value);
	}

	/**
	 * Translates a indexOf(string,int) expression into a
	 * StringToIntCast(StringBinaryExpression(INDEXOFC)) expression.
	 * 
	 * It uses String.indexOf(string,int) to compute the concrete value.
	 */
	@Override
	public Object visit(StringIndexOfC str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);
		IntegerExpression str_param_2 = (IntegerExpression) str.getParam()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		int str_param_2_concrete_value = ((Long) str_param_2.getConcreteValue())
				.intValue();

		int concrete_value = str_param_1_concrete_value
				.indexOf(str_param_2_concrete_value);
		StringBinaryExpression strBExpr = new StringBinaryExpression(
				str_param_1, Operator.INDEXOFC, str_param_2,
				Integer.toString(concrete_value));
		return new StringToIntCast(strBExpr, (long) concrete_value);
	}

	/**
	 * Translates a lastIndexOf(string,int) expression into a
	 * StringToIntCast(StringBinaryExpression(LASTINDEXOFC)) expression.
	 * 
	 * It uses String.lastIndexOf(string,int) to compute the concrete value.
	 */
	@Override
	public Object visit(StringLastIndexOfC str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);
		IntegerExpression str_param_2 = (IntegerExpression) str.getParam()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		int str_param_2_concrete_value = ((Long) str_param_2.getConcreteValue())
				.intValue();

		int concrete_value = str_param_1_concrete_value
				.lastIndexOf(str_param_2_concrete_value);
		StringBinaryExpression strBExpr = new StringBinaryExpression(
				str_param_1, Operator.LASTINDEXOFC, str_param_2,
				Integer.toString(concrete_value));
		return new StringToIntCast(strBExpr, (long) concrete_value);
	}

	/**
	 * Translates a charAt(string,int) expression into a
	 * StringToIntCast(StringBinaryExpression(CHARAT)) expression.
	 * 
	 * It uses String.charAt(string,int) to compute the concrete value.
	 */
	@Override
	public Object visit(StringCharAt str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);
		IntegerExpression str_param_2 = (IntegerExpression) str.getParam()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		int str_param_2_concrete_value = ((Long) str_param_2.getConcreteValue())
				.intValue();

		char concrete_value = str_param_1_concrete_value
				.charAt(str_param_2_concrete_value);
		StringBinaryExpression strBExpr = new StringBinaryExpression(
				str_param_1, Operator.CHARAT, str_param_2,
				Character.toString(concrete_value));
		return new StringToIntCast(strBExpr, (long) concrete_value);
	}

	/**
	 * Translates a concat(string,string) expression into a
	 * StringBinaryExpression(CONCAT) expression.
	 * 
	 * It uses String.concat(string,string) to compute the concrete value.
	 */
	@Override
	public Object visit(StringConcat str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);
		StringExpression str_param_2 = (StringExpression) str.getParam()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();

		String concrete_value = str_param_1_concrete_value
				.concat(str_param_2_concrete_value);
		StringBinaryExpression strBExpr = new StringBinaryExpression(
				str_param_1, Operator.CONCAT, str_param_2, concrete_value);
		return strBExpr;
	}

	/**
	 * Translates a replaceAll(string,string,string) expression into a
	 * StringMultipleExpression(REPLACEALL).
	 * 
	 * It uses String.replaceAll(string,string) to compute the concrete value.
	 */
	@Override
	public Object visit(StringReplaceAll r) {
		StringExpression str_param_1 = (StringExpression) r.getStringReceiver()
				.accept(this);
		StringExpression str_param_2 = (StringExpression) r.getParam1().accept(
				this);
		StringExpression str_param_3 = (StringExpression) r.getParam2().accept(
				this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();
		String str_param_3_concrete_value = (String) str_param_3
				.getConcreteValue();

		String concrete_value = str_param_1_concrete_value.replaceAll(
				str_param_2_concrete_value, str_param_3_concrete_value);

		StringMultipleExpression strTExpr = new StringMultipleExpression(
				str_param_1, Operator.REPLACEALL, str_param_2,
				new ArrayList<Expression<?>>(
						Collections.singletonList(str_param_3)), concrete_value);

		return strTExpr;
	}

	/**
	 * Translates a replaceAll(string,string,string) expression into a
	 * StringMultipleExpression(REPLACEFIRST).
	 * 
	 * It uses String.replaceFirst(string,string) to compute the concrete value.
	 */
	@Override
	public Object visit(StringReplaceFirst r) {
		StringExpression str_param_1 = (StringExpression) r.getStringReceiver()
				.accept(this);
		StringExpression str_param_2 = (StringExpression) r.getParam1().accept(
				this);
		StringExpression str_param_3 = (StringExpression) r.getParam2().accept(
				this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();
		String str_param_3_concrete_value = (String) str_param_3
				.getConcreteValue();

		String concrete_value = str_param_1_concrete_value.replaceFirst(
				str_param_2_concrete_value, str_param_3_concrete_value);

		StringMultipleExpression strTExpr = new StringMultipleExpression(
				str_param_1, Operator.REPLACEFIRST, str_param_2,
				new ArrayList<Expression<?>>(
						Collections.singletonList(str_param_3)), concrete_value);

		return strTExpr;
	}

	/**
	 * Translates a replace(string,char,char) expression into a
	 * StringMultipleExpression(REPLACEC).
	 * 
	 * It uses String.replace(char,char) to compute the concrete value.
	 */
	@Override
	public Object visit(StringReplaceC r) {
		StringExpression str_param_1 = (StringExpression) r.getStringReceiver()
				.accept(this);
		IntegerExpression int_param_2 = (IntegerExpression) r.getParam1()
				.accept(this);
		IntegerExpression int_param_3 = (IntegerExpression) r.getParam2()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		char int_param_2_concrete_value = (char) ((Long) int_param_2
				.getConcreteValue()).intValue();
		char int_param_3_concrete_value = (char) ((Long) int_param_3
				.getConcreteValue()).intValue();

		String concrete_value = str_param_1_concrete_value.replace(
				int_param_2_concrete_value, int_param_3_concrete_value);

		StringMultipleExpression strTExpr = new StringMultipleExpression(
				str_param_1, Operator.REPLACEC, int_param_2,
				new ArrayList<Expression<?>>(
						Collections.singletonList(int_param_3)), concrete_value);

		return strTExpr;
	}

	/**
	 * Translates a substring(string,int,int) expression into a
	 * StringMultipleExpression(SUBSTRING).
	 * 
	 * It uses String.substring(int,int) to compute the concrete value.
	 */
	@Override
	public Object visit(StringSubstring r) {
		StringExpression str_param_1 = (StringExpression) r.getStringReceiver()
				.accept(this);
		IntegerExpression int_param_2 = (IntegerExpression) r.getParam1()
				.accept(this);
		IntegerExpression int_param_3 = (IntegerExpression) r.getParam2()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		int int_param_2_concrete_value = ((Long) int_param_2.getConcreteValue())
				.intValue();
		int int_param_3_concrete_value = ((Long) int_param_3.getConcreteValue())
				.intValue();

		String concrete_value = str_param_1_concrete_value.substring(
				int_param_2_concrete_value, int_param_3_concrete_value);

		StringMultipleExpression strTExpr = new StringMultipleExpression(
				str_param_1, Operator.SUBSTRING, int_param_2,
				new ArrayList<Expression<?>>(
						Collections.singletonList(int_param_3)), concrete_value);

		return strTExpr;
	}

	/**
	 * Translates a lastIndexOfCI(string,int,int) expression into a
	 * StringToIntCast(StringMultipleExpression(LASTINDEXOFCI)).
	 * 
	 * It uses String.lastIndexOf(int,int) to compute the concrete value.
	 */
	@Override
	public Object visit(StringLastIndexOfCI r) {
		StringExpression str_param_1 = (StringExpression) r.getStringReceiver()
				.accept(this);
		IntegerExpression int_param_2 = (IntegerExpression) r.getParam1()
				.accept(this);
		IntegerExpression int_param_3 = (IntegerExpression) r.getParam2()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		int int_param_2_concrete_value = ((Long) int_param_2.getConcreteValue())
				.intValue();
		int int_param_3_concrete_value = ((Long) int_param_3.getConcreteValue())
				.intValue();

		int concrete_value = str_param_1_concrete_value.lastIndexOf(
				int_param_2_concrete_value, int_param_3_concrete_value);

		StringMultipleExpression strTExpr = new StringMultipleExpression(
				str_param_1, Operator.LASTINDEXOFCI, int_param_2,
				new ArrayList<Expression<?>>(Collections
						.singletonList(int_param_3)),
				Integer.toString(concrete_value));

		return new StringToIntCast(strTExpr, (long) concrete_value);
	}

	/**
	 * Translates a indexOfCI(string,int,int) expression into a
	 * StringToIntCast(StringMultipleExpression(INDEXOFCI)).
	 * 
	 * It uses String.indexOf(int,int) to compute the concrete value.
	 */
	@Override
	public Object visit(StringIndexOfCI r) {
		StringExpression str_param_1 = (StringExpression) r.getStringReceiver()
				.accept(this);
		IntegerExpression int_param_2 = (IntegerExpression) r.getParam1()
				.accept(this);
		IntegerExpression int_param_3 = (IntegerExpression) r.getParam2()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		int int_param_2_concrete_value = ((Long) int_param_2.getConcreteValue())
				.intValue();
		int int_param_3_concrete_value = ((Long) int_param_3.getConcreteValue())
				.intValue();

		int concrete_value = str_param_1_concrete_value.indexOf(
				int_param_2_concrete_value, int_param_3_concrete_value);

		StringMultipleExpression strTExpr = new StringMultipleExpression(
				str_param_1, Operator.INDEXOFCI, int_param_2,
				new ArrayList<Expression<?>>(Collections
						.singletonList(int_param_3)),
				Integer.toString(concrete_value));

		return new StringToIntCast(strTExpr, (long) concrete_value);
	}

	/**
	 * Translates a indexOfSI(string,string,int) expression into a
	 * StringToIntCast(StringMultipleExpression(INDEXOFSI)).
	 * 
	 * It uses String.indexOf(string,int) to compute the concrete value.
	 */
	@Override
	public Object visit(StringIndexOfSI r) {
		StringExpression str_param_1 = (StringExpression) r.getStringReceiver()
				.accept(this);
		StringExpression str_param_2 = (StringExpression) r.getParam1().accept(
				this);
		IntegerExpression int_param_3 = (IntegerExpression) r.getParam2()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();
		int int_param_3_concrete_value = ((Long) int_param_3.getConcreteValue())
				.intValue();

		int concrete_value = str_param_1_concrete_value.indexOf(
				str_param_2_concrete_value, int_param_3_concrete_value);

		StringMultipleExpression strTExpr = new StringMultipleExpression(
				str_param_1, Operator.INDEXOFSI, str_param_2,
				new ArrayList<Expression<?>>(Collections
						.singletonList(int_param_3)),
				Integer.toString(concrete_value));

		return new StringToIntCast(strTExpr, (long) concrete_value);
	}

	/**
	 * Translates a lastIndexOfSI(string,string,int) expression into a
	 * StringToIntCast(StringMultipleExpression(LASTINDEXOFSI)).
	 * 
	 * It uses String.lastIndexOf(string,int) to compute the concrete value.
	 */
	@Override
	public Object visit(StringLastIndexOfSI r) {
		StringExpression str_param_1 = (StringExpression) r.getStringReceiver()
				.accept(this);
		StringExpression str_param_2 = (StringExpression) r.getParam1().accept(
				this);
		IntegerExpression int_param_3 = (IntegerExpression) r.getParam2()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();
		int int_param_3_concrete_value = ((Long) int_param_3.getConcreteValue())
				.intValue();

		int concrete_value = str_param_1_concrete_value.lastIndexOf(
				str_param_2_concrete_value, int_param_3_concrete_value);

		StringMultipleExpression strTExpr = new StringMultipleExpression(
				str_param_1, Operator.LASTINDEXOFSI, str_param_2,
				new ArrayList<Expression<?>>(Collections
						.singletonList(int_param_3)),
				Integer.toString(concrete_value));

		return new StringToIntCast(strTExpr, (long) concrete_value);
	}

	/**
	 * Translates a equals(string,object) expression into a
	 * StringToIntCast(StringComparison(EQUALS)).
	 * 
	 * It uses String.equals(Object) to compute the concrete value.
	 */
	@Override
	public Object visit(StringEquals str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);

		StringExpression object_param_2 = null;

		if (str.getParam() instanceof LiteralNonNullReference) {
			LiteralNonNullReference literal = (LiteralNonNullReference) str
					.getParam();
			if (literal.getDynamicType().getType().equals(String.class)) {
				object_param_2 = (StringExpression) str.getParam().accept(this);
			}
		} else if (str.getParam() instanceof ReferenceVariable) {
			ReferenceVariable r = (ReferenceVariable) str.getParam();
			if (r.getStaticTypeClass().equals(String.class)) {
				object_param_2 = (StringExpression) str.getParam().accept(this);
			}
		} else if (str.getParam() instanceof StringReference) {
			object_param_2 = (StringExpression) str.getParam().accept(this);
		}

		if (object_param_2 == null) {
			// return false if param is null or non string
			return new IntegerConstant((long) 0);
		} else {
			// otherwise compare to other string
			String str_param_1_concrete_value = (String) str_param_1
					.getConcreteValue();
			Object object_param_2_concrete_value = object_param_2
					.getConcreteValue();

			int concrete_value = str_param_1_concrete_value
					.equals(object_param_2_concrete_value) ? 1 : 0;

			StringComparison strComp = new StringComparison(str_param_1,
					Operator.EQUALS, object_param_2, (long) concrete_value);
			return new StringToIntCast(strComp, (long) concrete_value);
		}
	}

	/**
	 * Translates a equalsIgnoreCase(string,string) expression into a
	 * StringToIntCast(StringComparison(EQUALSIGNORECASE)).
	 * 
	 * It uses String.equalsIgnoreCase(string) to compute the concrete value.
	 */
	@Override
	public Object visit(StringEqualsIgnoreCase str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);
		StringExpression str_param_2 = (StringExpression) str.getParam()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();

		int concrete_value = str_param_1_concrete_value
				.equalsIgnoreCase(str_param_2_concrete_value) ? 1 : 0;

		StringComparison strComp = new StringComparison(str_param_1,
				Operator.EQUALSIGNORECASE, str_param_2, (long) concrete_value);

		return new StringToIntCast(strComp, (long) concrete_value);
	}

	/**
	 * Translates a endsWith(string,string) expression into a
	 * StringToIntCast(StringComparison(ENDSWITH)).
	 * 
	 * It uses String.endsWith(string) to compute the concrete value.
	 */
	@Override
	public Object visit(StringEndsWith str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);
		StringExpression str_param_2 = (StringExpression) str.getParam()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();

		int concrete_value = str_param_1_concrete_value
				.endsWith(str_param_2_concrete_value) ? 1 : 0;

		StringComparison strComp = new StringComparison(str_param_1,
				Operator.ENDSWITH, str_param_2, (long) concrete_value);
		return new StringToIntCast(strComp, (long) concrete_value);
	}

	/**
	 * Translates a startsWith(string,string,int) expression into a
	 * StringToIntCast(StringMultipleComparison(STARTSWITH)).
	 * 
	 * It uses String.startsWith(string,int) to compute the concrete value.
	 */
	@Override
	public Object visit(StringStartsWith str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);
		StringExpression str_param_2 = (StringExpression) str.getParam1()
				.accept(this);
		IntegerExpression int_param_3 = (IntegerExpression) str.getParam2()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();
		int int_param_3_concrete_value = ((Long) int_param_3.getConcreteValue())
				.intValue();

		int concrete_value = str_param_1_concrete_value.startsWith(
				str_param_2_concrete_value, int_param_3_concrete_value) ? 1 : 0;

		StringMultipleComparison strComp = new StringMultipleComparison(
				str_param_1,
				Operator.STARTSWITH,
				str_param_2,
				new ArrayList<Expression<?>>(Collections.singleton(int_param_3)),
				(long) concrete_value);
		return new StringToIntCast(strComp, (long) concrete_value);
	}

	/**
	 * Translates a regionMatches(string,boolean,int,string,int,int) expression
	 * into a StringToIntCast(StringMultipleComparison(REGIONMATCHES)).
	 * 
	 * It uses String.regionMatches(...) to compute the concrete value.
	 */
	@Override
	public Object visit(StringRegionMatches str) {
		StringExpression str_param_1 = (StringExpression) str.getStrReceiver()
				.accept(this);
		IntegerExpression int_param_2 = (IntegerExpression) str.getIgnoreCase()
				.accept(this);
		IntegerExpression int_param_3 = (IntegerExpression) str.getToffset()
				.accept(this);
		StringExpression str_param_4 = (StringExpression) str.getOtherString()
				.accept(this);
		IntegerExpression int_param_5 = (IntegerExpression) str.getOoffset()
				.accept(this);
		IntegerExpression int_param_6 = (IntegerExpression) str.getLen()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		boolean int_param_2_concrete_value = ((Long) int_param_2
				.getConcreteValue()) == 1 ? true : false;
		int int_param_3_concrete_value = ((Long) int_param_2.getConcreteValue())
				.intValue();
		String str_param_4_concrete_value = (String) str_param_4
				.getConcreteValue();
		int int_param_5_concrete_value = ((Long) int_param_5.getConcreteValue())
				.intValue();
		int int_param_6_concrete_value = ((Long) int_param_6.getConcreteValue())
				.intValue();

		int concrete_value = str_param_1_concrete_value.regionMatches(
				int_param_2_concrete_value, int_param_3_concrete_value,
				str_param_4_concrete_value, int_param_5_concrete_value,
				int_param_6_concrete_value) ? 1 : 0;

		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(int_param_3);
		other.add(int_param_5);
		other.add(int_param_6);
		other.add(int_param_2);

		StringMultipleComparison strComp = new StringMultipleComparison(
				str_param_1, Operator.REGIONMATCHES, str_param_4, other,
				(long) concrete_value);
		return new StringToIntCast(strComp, (long) concrete_value);

	}

	/**
	 * This method translates a contains(string,string) expression into a
	 * StringToIntCast(StringComparison(CONTAINS)) expression.
	 * 
	 * It uses String.contains(CharSequence) to compute its concrete value.
	 */
	@Override
	public Object visit(StringContains str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);

		StringExpression str_param_2 = null;
		Reference param = str.getParam();
		str_param_2 = (StringExpression) param.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();

		int concrete_value = str_param_1_concrete_value
				.contains(str_param_2_concrete_value) ? 1 : 0;

		StringComparison strComp = new StringComparison(str_param_1,
				Operator.CONTAINS, str_param_2, (long) concrete_value);
		return new StringToIntCast(strComp, (long) concrete_value);
	}

	@Override
	public Object visit(StringReplaceCS str) {
		StringExpression str_param_1 = (StringExpression) str
				.getStringReceiver().accept(this);
		StringExpression str_param_2 = (StringExpression) str.getParam1()
				.accept(this);
		StringExpression str_param_3 = (StringExpression) str.getParam2()
				.accept(this);

		String str_param_1_concrete_value = (String) str_param_1
				.getConcreteValue();
		String str_param_2_concrete_value = (String) str_param_2
				.getConcreteValue();
		String str_param_3_concrete_value = (String) str_param_3
				.getConcreteValue();

		String concrete_value = str_param_1_concrete_value.replace(
				str_param_2_concrete_value, str_param_3_concrete_value);

		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(str_param_3);
		return new StringMultipleExpression(str_param_1, Operator.REPLACECS,
				str_param_2, other, concrete_value);

	}

	@Override
	public Object visit(StringReferenceVariable r) {
		return r.getReferenceVariable().accept(this);
	}

	@Override
	public Object visit(StringReferenceNonNullLiteral r) {
		return r.getReferenceNonNullLiteral().accept(this);

	}

	@Override
	public Object visit(LABv32 r) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LABv64 r) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LAFp32 r) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LAFp64 r) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LARef r) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * <code>
	 * ===================================================================
	 * Unsupported operations
	 * ===================================================================
	 * </code>
	 */

	@Override
	public Object visit(Bv64InstanceMethod b) {
		throw new IllegalStateException(
				"Bv64InstanceMethod is not a valid AST instance");
	}

	@Override
	public Object visit(ArraySelectBv64 b) {
		throw new IllegalStateException(
				"ArraySelectBv64 is not a valid AST instance");
	}

	@Override
	public Object visit(ArraySelectFp64 f) {
		throw new IllegalStateException(
				"ArraySelectFp64 is not a valid AST instance");
	}

	@Override
	public Object visit(Bv32ValuedInstanceMethod b) {
		throw new IllegalStateException(
				"Bv32ValuedInstanceMethod is not a valid AST instance");

	}

	@Override
	public Object visit(ArraySelectBv32 b) {
		throw new IllegalStateException(
				"FieldSelectBv32 is not a valid AST instance");
	}

	@Override
	public Object visit(ITE.Bv32 b) {
		throw new UnsupportedOperationException(
				"Constraint ? Bv32 : Bv32 still not supported!");

	}

	@Override
	public Object visit(ArraySelectFp32 f) {
		throw new IllegalStateException(
				"ArraySelectFp32 is not a valid AST instance");
	}

}
