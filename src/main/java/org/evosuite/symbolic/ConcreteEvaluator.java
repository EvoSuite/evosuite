package org.evosuite.symbolic;

import edu.uta.cse.dsc.ast.BitVector32Visitor;
import edu.uta.cse.dsc.ast.BitVector64Visitor;
import edu.uta.cse.dsc.ast.DoubleExpressionVisitor;
import edu.uta.cse.dsc.ast.FloatExpressionVisitor;
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
import edu.uta.cse.dsc.ast.reference.LiteralReference;
import edu.uta.cse.dsc.ast.reference.ReferenceVariable;
import edu.uta.cse.dsc.ast.reference.ReferenceVisitor;

import edu.uta.cse.dsc.ast.ufunction.Bv32ValuedInstanceMethod;
import edu.uta.cse.dsc.ast.ufunction.Bv64InstanceMethod;
import edu.uta.cse.dsc.ast.z3array.JavaArraySelect.ArraySelectBv32;
import edu.uta.cse.dsc.ast.z3array.JavaArraySelect.ArraySelectBv64;
import edu.uta.cse.dsc.ast.z3array.JavaArraySelect.ArraySelectFp32;
import edu.uta.cse.dsc.ast.z3array.JavaArraySelect.ArraySelectFp64;
import edu.uta.cse.dsc.ast.z3array.JavaFieldSelect.FieldSelectBv32;
import edu.uta.cse.dsc.ast.z3array.JavaFieldSelect.FieldSelectBv64;
import edu.uta.cse.dsc.ast.z3array.JavaFieldSelect.FieldSelectFp32;
import edu.uta.cse.dsc.ast.z3array.JavaFieldSelect.FieldSelectFp64;

/**
 * Translates a JvmExpression into its corresponding Literal result.
 * 
 * @author galeotti
 * 
 */
public final class ConcreteEvaluator implements BitVector32Visitor,
		BitVector64Visitor, FloatExpressionVisitor, DoubleExpressionVisitor,
		ReferenceVisitor {

	private final ConcolicState concolicState;

	public ConcreteEvaluator(ConcolicState concolicState) {
		this.concolicState = concolicState;
	}

	


	@Override
	public Object visit(BitVector32Variable b) {
		LiteralBitVector32 concrete_value = (LiteralBitVector32) concolicState
				.getConcreteValue(b);
		return concrete_value;
	}

	@Override
	public Object visit(ReferenceVariable r) {
		LiteralReference concrete_value = (LiteralReference) concolicState
				.getConcreteValue(r);
		return concrete_value;
	}

	@Override
	public Object visit(StringReferenceVariable r) {
		ReferenceVariable v = r.getReferenceVariable();
		LiteralNonNullReference concrete_value = (LiteralNonNullReference) concolicState
				.getConcreteValue(v);
		return buildStringReferenceNonNullLiteral(concrete_value
				.getStringConstant());
	}

	@Override
	public Object visit(FloatVariable f) {
		FloatLiteral concrete_value = (FloatLiteral) concolicState
				.getConcreteValue(f);
		return concrete_value;
	}

	@Override
	public Object visit(BitVector64Variable b) {
		LiteralBitVector64 concrete_value = (LiteralBitVector64) concolicState
				.getConcreteValue(b);
		return concrete_value;
	}

	@Override
	public Object visit(DoubleVariable f) {
		DoubleLiteral concrete_value = (DoubleLiteral) concolicState
				.getConcreteValue(f);
		return concrete_value;
	}

	@Override
	public Object accept(Bv32ExtendingAnBv64 b) {
		LiteralBitVector64 e = (LiteralBitVector64) b.getParam().accept(this);
		LiteralBitVector32 ret_val = new LiteralBitVector32((int) e.getValue());
		return ret_val;
	}

	@Override
	public Object visit(Bv32ExtendingAnFp32 b) {
		FloatLiteral e = (FloatLiteral) b.getParam().accept(this);
		LiteralBitVector32 ret_val = new LiteralBitVector32((int) e.getValue());
		return ret_val;
	}

	@Override
	public Object visit(Bv32ExtendingAnFp64 b) {
		DoubleLiteral e = (DoubleLiteral) b.getParam().accept(this);
		LiteralBitVector32 ret_val = new LiteralBitVector32((int) e.getValue());
		return ret_val;
	}

	@Override
	public Object visit(Fp32ExtendingAnBv32 f) {
		LiteralBitVector32 e = (LiteralBitVector32) f.getParam().accept(this);
		FloatLiteral ret_val = new FloatLiteral((float) e.getValue());
		return ret_val;
	}

	@Override
	public Object visit(LiteralBitVector32 b) {
		return b;
	}

	@Override
	public Object visit(FpSLt.Fp32 b) {
		FloatLiteral left = (FloatLiteral) b.getLeft().accept(this);
		FloatLiteral right = (FloatLiteral) b.getRight().accept(this);

		float left_concrete_value = left.getValue();
		float right_concrete_value = right.getValue();

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

		return new LiteralBitVector32(concrete_value);
	}

	@Override
	public Object visit(BvAdd.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);
		return new LiteralBitVector32(left.getValue() + right.getValue());
	}

	@Override
	public Object visit(BvAnd.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);
		return new LiteralBitVector32(left.getValue() & right.getValue());

	}

	@Override
	public Object visit(BvMul.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);
		return new LiteralBitVector32(left.getValue() * right.getValue());

	}

	@Override
	public Object visit(BvNeg.Bv32 b) {

		LiteralBitVector32 param = (LiteralBitVector32) ((LiteralBitVector32) b
				.getParam()).accept(this);

		return new LiteralBitVector32(-param.getValue());
	}

	@Override
	public Object visit(BvOr.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);
		return new LiteralBitVector32(left.getValue() | right.getValue());
	}

	@Override
	public Object visit(BvSDiv.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);
		return new LiteralBitVector32(left.getValue() / right.getValue());
	}

	@Override
	public Object visit(BvSRem.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);
		return new LiteralBitVector32(left.getValue() % right.getValue());
	}

	@Override
	public Object visit(BvSub.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);
		return new LiteralBitVector32(left.getValue() - right.getValue());
	}

	@Override
	public Object visit(BvXOr.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);
		return new LiteralBitVector32(left.getValue() ^ right.getValue());
	}

	@Override
	public Object visit(FloatLiteral f) {
		return f;
	}

	@Override
	public Object visit(FpSub.Fp32 f) {
		FloatLiteral left = (FloatLiteral) f.getLeft().accept(this);
		FloatLiteral right = (FloatLiteral) f.getRight().accept(this);

		return new FloatLiteral(left.getValue() - right.getValue());
	}

	@Override
	public Object visit(FpAdd.Fp32 f) {
		FloatLiteral left = (FloatLiteral) f.getLeft().accept(this);
		FloatLiteral right = (FloatLiteral) f.getRight().accept(this);

		return new FloatLiteral(left.getValue() + right.getValue());

	}

	@Override
	public Object visit(FpSRem.Fp32 f) {
		FloatLiteral left = (FloatLiteral) f.getLeft().accept(this);
		FloatLiteral right = (FloatLiteral) f.getRight().accept(this);

		return new FloatLiteral(left.getValue() % right.getValue());

	}

	@Override
	public Object visit(FpSDiv.Fp32 f) {
		FloatLiteral left = (FloatLiteral) f.getLeft().accept(this);
		FloatLiteral right = (FloatLiteral) f.getRight().accept(this);

		return new FloatLiteral(left.getValue() / right.getValue());

	}

	@Override
	public Object visit(FpMul.Fp32 f) {
		FloatLiteral left = (FloatLiteral) f.getLeft().accept(this);
		FloatLiteral right = (FloatLiteral) f.getRight().accept(this);

		return new FloatLiteral(left.getValue() * right.getValue());

	}

	@Override
	public Object visit(FpNeg.Fp32 f) {
		FloatLiteral param = (FloatLiteral) ((FloatLiteral) f.getParam())
				.accept(this);

		return new FloatLiteral(-param.getValue());
	}

	@Override
	public Object visit(Fp32ExtendingAnBv64 f) {
		LiteralBitVector64 param = (LiteralBitVector64) f.getParam().accept(
				this);

		return new FloatLiteral((float) param.getValue());
	}

	@Override
	public Object visit(Fp32ExtendingAnFp64 f) {
		DoubleLiteral param = (DoubleLiteral) f.getParam().accept(this);

		return new FloatLiteral((float) param.getValue());
	}

	@Override
	public Object visit(Bv64ExtendingAnBv32 b) {
		LiteralBitVector32 param = (LiteralBitVector32) b.getParam().accept(
				this);

		return new LiteralBitVector64((long) param.getValue());
	}

	@Override
	public Object accept(Bv64ExtendingAnFp32 b) {
		FloatLiteral param = (FloatLiteral) b.getParam().accept(this);

		return new LiteralBitVector64((long) param.getValue());
	}

	@Override
	public Object visit(Bv64ExtendingAnFp64 b) {
		DoubleLiteral param = (DoubleLiteral) b.getParam().accept(this);

		return new LiteralBitVector64((long) param.getValue());
	}

	@Override
	public Object visit(LiteralBitVector64 b) {
		return b;
	}

	@Override
	public Object visit(BvAdd.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);
		return new LiteralBitVector64(left.getValue() + right.getValue());
	}

	@Override
	public Object visit(BvSub.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);
		return new LiteralBitVector64(left.getValue() - right.getValue());

	}

	@Override
	public Object visit(BvMul.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);
		return new LiteralBitVector64(left.getValue() * right.getValue());

	}

	@Override
	public Object visit(BvSDiv.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);
		return new LiteralBitVector64(left.getValue() / right.getValue());
	}

	@Override
	public Object visit(BvSRem.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);
		return new LiteralBitVector64(left.getValue() % right.getValue());

	}

	@Override
	public Object visit(BvAnd.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);
		return new LiteralBitVector64(left.getValue() & right.getValue());

	}

	@Override
	public Object visit(BvOr.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);
		return new LiteralBitVector64(left.getValue() | right.getValue());

	}

	@Override
	public Object visit(BvXOr.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);
		return new LiteralBitVector64(left.getValue() ^ right.getValue());

	}

	@Override
	public Object visit(BvNeg.Bv64 b) {
		LiteralBitVector64 param = (LiteralBitVector64) ((LiteralBitVector64) b
				.getParam()).accept(this);
		return new LiteralBitVector64(-param.getValue());

	}

	@Override
	public Object visit(Fp64ExtendingAnBv32 f) {
		LiteralBitVector32 p = (LiteralBitVector32) f.getParam().accept(this);
		return new DoubleLiteral((double) p.getValue());
	}

	@Override
	public Object visit(Fp64ExtendingAnBv64 f) {
		LiteralBitVector64 p = (LiteralBitVector64) f.getParam().accept(this);
		return new DoubleLiteral((double) p.getValue());
	}

	@Override
	public Object visit(Fp64ExtendingAnFp32 f) {
		FloatLiteral p = (FloatLiteral) f.getParam().accept(this);
		return new DoubleLiteral((double) p.getValue());
	}

	@Override
	public Object visit(FpAdd.Fp64 f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(left.getValue() + right.getValue());
	}

	@Override
	public Object visit(FpSub.Fp64 f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(left.getValue() - right.getValue());

	}

	@Override
	public Object visit(FpMul.Fp64 f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(left.getValue() * right.getValue());

	}

	@Override
	public Object visit(FpSDiv.Fp64 f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(left.getValue() / right.getValue());

	}

	@Override
	public Object visit(FpSRem.Fp64 f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(left.getValue() % right.getValue());

	}

	@Override
	public Object visit(FpNeg.Fp64 f) {
		DoubleLiteral p = (DoubleLiteral) ((DoubleLiteral) f.getParam())
				.accept(this);

		return new DoubleLiteral(-p.getValue());
	}

	@Override
	public Object visit(DoubleLiteral f) {
		return f;
	}

	@Override
	public Object visit(FpSLt.Fp64 f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		double left_concrete_value = left.getValue();
		double right_concrete_value = right.getValue();

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
		return new LiteralBitVector32(concrete_value);

	}

	@Override
	public Object visit(BvSHR.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);

		return new LiteralBitVector32(left.getValue() >> right.getValue());

	}

	@Override
	public Object visit(BvSHL.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);

		return new LiteralBitVector32(left.getValue() << right.getValue());
	}

	@Override
	public Object visit(BvUSHR.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);

		return new LiteralBitVector32(left.getValue() >>> right.getValue());

	}

	@Override
	public Object visit(BvSHL.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);

		return new LiteralBitVector64(left.getValue() << right.getValue());
	}

	@Override
	public Object visit(BvSHR.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);

		return new LiteralBitVector64(left.getValue() >> right.getValue());
	}

	@Override
	public Object visit(BvUSHR.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);

		return new LiteralBitVector64(left.getValue() >>> right.getValue());
	}

	@Override
	public Object visit(COS f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.cos(p.getValue()));
	}

	@Override
	public Object visit(ACOS f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.acos(p.getValue()));
	}

	@Override
	public Object visit(LOG10 f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.log10(p.getValue()));

	}

	@Override
	public Object visit(LOG1P f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.log1p(p.getValue()));
	}

	@Override
	public Object visit(RINT f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.rint(p.getValue()));
	}

	@Override
	public Object visit(SIN f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.sin(p.getValue()));
	}

	@Override
	public Object visit(SINH f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.sinh(p.getValue()));
	}

	@Override
	public Object visit(SQRT f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.sqrt(p.getValue()));
	}

	@Override
	public Object visit(TAN f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.tan(p.getValue()));
	}

	@Override
	public Object visit(TANH f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.tanh(p.getValue()));
	}

	@Override
	public Object visit(ToDegrees f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.toDegrees(p.getValue()));
	}

	@Override
	public Object visit(ToRadians f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.toRadians(p.getValue()));
	}

	@Override
	public Object visit(ASIN f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.asin(p.getValue()));
	}

	@Override
	public Object visit(ATAN f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.atan(p.getValue()));
	}

	@Override
	public Object visit(CBRT f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.cbrt(p.getValue()));
	}

	@Override
	public Object visit(CEIL f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.ceil(p.getValue()));
	}

	@Override
	public Object visit(COSH f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.cosh(p.getValue()));
	}

	@Override
	public Object visit(EXP f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.exp(p.getValue()));
	}

	@Override
	public Object visit(EXPM1 f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.expm1(p.getValue()));
	}

	@Override
	public Object visit(FLOOR f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.floor(p.getValue()));
	}

	@Override
	public Object visit(LOG f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.log(p.getValue()));
	}

	@Override
	public Object visit(ATAN2 f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(Math.atan2(left.getValue(), right.getValue()));
	}

	@Override
	public Object visit(HYPOT f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(Math.hypot(left.getValue(), right.getValue()));
	}

	@Override
	public Object visit(IEEEremainder f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(Math.IEEEremainder(left.getValue(),
				right.getValue()));
	}

	@Override
	public Object visit(POW f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(Math.pow(left.getValue(), right.getValue()));
	}

	@Override
	public Object visit(NextUp.Fp64 f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.nextUp(p.getValue()));
	}

	@Override
	public Object visit(GetExponent.Fp64 f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.getExponent(p.getValue()));
	}

	@Override
	public Object visit(ULP.Fp64 f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.ulp(p.getValue()));
	}

	@Override
	public Object visit(ABS.Fp64 f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.abs(p.getValue()));
	}

	@Override
	public Object visit(SCALB.Fp32 f) {
		FloatLiteral left = (FloatLiteral) f.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) f.getRight().accept(
				this);

		return new DoubleLiteral(Math.scalb(left.getValue(), right.getValue()));
	}

	@Override
	public Object visit(SCALB.Fp64 f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) f.getRight().accept(
				this);

		return new DoubleLiteral(Math.scalb(left.getValue(), right.getValue()));
	}

	@Override
	public Object visit(CopySign.Fp64 f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(Math.copySign(left.getValue(),
				right.getValue()));
	}

	@Override
	public Object visit(NextAfter.Fp32 f) {
		FloatLiteral left = (FloatLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(Math.nextAfter(left.getValue(),
				right.getValue()));
	}

	@Override
	public Object visit(NextAfter.Fp64 f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(Math.nextAfter(left.getValue(),
				right.getValue()));
	}

	@Override
	public Object visit(MAX.Fp64 f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(Math.max(left.getValue(), right.getValue()));
	}

	@Override
	public Object visit(MIN.Fp64 f) {
		DoubleLiteral left = (DoubleLiteral) f.getLeft().accept(this);
		DoubleLiteral right = (DoubleLiteral) f.getRight().accept(this);

		return new DoubleLiteral(Math.min(left.getValue(), right.getValue()));
	}

	@Override
	public Object visit(SIGNUM.Fp64 f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);

		return new DoubleLiteral(Math.signum(p.getValue()));

	}

	@Override
	public Object visit(NextUp.Fp32 f) {
		FloatLiteral p = (FloatLiteral) f.getParam().accept(this);
		return new FloatLiteral(Math.nextUp(p.getValue()));
	}

	@Override
	public Object visit(GetExponent.Fp32 f) {
		FloatLiteral p = (FloatLiteral) f.getParam().accept(this);
		return new FloatLiteral(Math.nextUp(p.getValue()));

	}

	@Override
	public Object visit(ULP.Fp32 f) {
		FloatLiteral p = (FloatLiteral) f.getParam().accept(this);
		return new FloatLiteral(Math.ulp(p.getValue()));
	}

	@Override
	public Object visit(ABS.Fp32 f) {
		FloatLiteral p = (FloatLiteral) f.getParam().accept(this);
		return new FloatLiteral(Math.abs(p.getValue()));
	}

	@Override
	public Object visit(CopySign.Fp32 f) {
		FloatLiteral left = (FloatLiteral) f.getLeft().accept(this);
		FloatLiteral right = (FloatLiteral) f.getRight().accept(this);

		return new FloatLiteral(
				Math.copySign(left.getValue(), right.getValue()));
	}

	@Override
	public Object visit(MAX.Fp32 f) {
		FloatLiteral left = (FloatLiteral) f.getLeft().accept(this);
		FloatLiteral right = (FloatLiteral) f.getRight().accept(this);

		return new FloatLiteral(Math.max(left.getValue(), right.getValue()));
	}

	@Override
	public Object visit(MIN.Fp32 f) {
		FloatLiteral left = (FloatLiteral) f.getLeft().accept(this);
		FloatLiteral right = (FloatLiteral) f.getRight().accept(this);

		return new FloatLiteral(Math.min(left.getValue(), right.getValue()));
	}

	@Override
	public Object visit(SIGNUM.Fp32 f) {
		FloatLiteral p = (FloatLiteral) f.getParam().accept(this);
		return new FloatLiteral(Math.signum(p.getValue()));
	}

	@Override
	public Object visit(ABS.Bv64 b) {
		LiteralBitVector64 p = (LiteralBitVector64) b.getParam().accept(this);
		return new LiteralBitVector64(Math.abs(p.getValue()));
	}

	@Override
	public Object visit(MAX.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);

		return new LiteralBitVector64(Math.max(left.getValue(),
				right.getValue()));
	}

	@Override
	public Object visit(MIN.Bv64 b) {
		LiteralBitVector64 left = (LiteralBitVector64) b.getLeft().accept(this);
		LiteralBitVector64 right = (LiteralBitVector64) b.getRight().accept(
				this);

		return new LiteralBitVector64(Math.min(left.getValue(),
				right.getValue()));
	}

	@Override
	public Object visit(ROUND.Fp64 f) {
		DoubleLiteral p = (DoubleLiteral) f.getParam().accept(this);
		return new DoubleLiteral(Math.round(p.getValue()));
	}

	@Override
	public Object visit(ROUND.Fp32 f) {
		FloatLiteral p = (FloatLiteral) f.getParam().accept(this);
		return new FloatLiteral(Math.round(p.getValue()));
	}

	@Override
	public Object visit(ABS.Bv32 b) {
		LiteralBitVector32 p = (LiteralBitVector32) b.getParam().accept(this);
		return new LiteralBitVector32(Math.abs(p.getValue()));
	}

	@Override
	public Object visit(MAX.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);

		return new LiteralBitVector32(Math.max(left.getValue(),
				right.getValue()));
	}

	@Override
	public Object visit(MIN.Bv32 b) {
		LiteralBitVector32 left = (LiteralBitVector32) b.getLeft().accept(this);
		LiteralBitVector32 right = (LiteralBitVector32) b.getRight().accept(
				this);

		return new LiteralBitVector32(Math.min(left.getValue(),
				right.getValue()));
	}

	@Override
	public Object visit(StringLength str) {
		StringReference p = (StringReference) str.getStringParam().accept(this);
		return new StringLength(p);

	}

	@Override
	public Object visit(LiteralNonNullReference r) {
		return r;
	}

	@Override
	public Object visit(LiteralNullReference r) {
		return r;
	}

	@Override
	public Object visit(StringTrim str) {
		LiteralNonNullReference p = (LiteralNonNullReference) str
				.getStringParam().accept(this);
		String string = p.getStringConstant();
		return new LiteralBitVector32(string.length());
	}

	@Override
	public Object visit(StringToUpperCase str) {
		StringReferenceNonNullLiteral p = (StringReferenceNonNullLiteral) str
				.getStringParam().accept(this);
		String string = getString(p);
		return buildStringReferenceNonNullLiteral(string.toUpperCase());
	}

	private Object buildStringReferenceNonNullLiteral(String string) {
		LiteralNonNullReference literalRef = new LiteralNonNullReference(string);
		return new StringReferenceNonNullLiteral(literalRef);
	}

	private String getString(StringReferenceNonNullLiteral p) {
		return p.getReferenceNonNullLiteral().getStringConstant();
	}

	@Override
	public Object visit(StringToLowerCase str) {
		StringReferenceNonNullLiteral p = (StringReferenceNonNullLiteral) str
				.getStringParam().accept(this);
		String string = getString(p);
		return buildStringReferenceNonNullLiteral(string.toLowerCase());

	}

	@Override
	public Object visit(StringCompareTo str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam().accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);

		return new LiteralBitVector32(string1.compareTo(string2));
	}

	@Override
	public Object visit(StringCompareToIgnoreCase str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam().accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);

		return new LiteralBitVector32(string1.compareToIgnoreCase(string2));
	}

	@Override
	public Object visit(StringIndexOfS str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam().accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);

		return new LiteralBitVector32(string1.indexOf(string2));
	}

	@Override
	public Object visit(StringLastIndexOfS str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam().accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);

		return new LiteralBitVector32(string1.lastIndexOf(string2));

	}

	@Override
	public Object visit(StringIndexOfC str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		LiteralBitVector32 p2 = (LiteralBitVector32) str.getParam()
				.accept(this);

		String string1 = getString(p1);

		return new LiteralBitVector32(string1.indexOf(p2.getValue()));
	}

	@Override
	public Object visit(StringLastIndexOfC str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		LiteralBitVector32 p2 = (LiteralBitVector32) str.getParam()
				.accept(this);

		String string1 = getString(p1);

		return new LiteralBitVector32(string1.lastIndexOf(p2.getValue()));
	}

	@Override
	public Object visit(StringCharAt str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		LiteralBitVector32 p2 = (LiteralBitVector32) str.getParam()
				.accept(this);

		String string1 = getString(p1);

		return new LiteralBitVector32(string1.charAt(p2.getValue()));
	}

	@Override
	public Object visit(StringConcat str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam().accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);

		return buildStringReferenceNonNullLiteral(string1.concat(string2));
	}

	@Override
	public Object visit(StringReplaceAll str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam1().accept(this);
		StringReferenceNonNullLiteral p3 = (StringReferenceNonNullLiteral) str
				.getParam1().accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);
		String string3 = getString(p3);

		return buildStringReferenceNonNullLiteral(string1.replaceAll(string2,
				string3));
	}

	@Override
	public Object visit(StringReplaceFirst str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam1().accept(this);
		StringReferenceNonNullLiteral p3 = (StringReferenceNonNullLiteral) str
				.getParam1().accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);
		String string3 = getString(p3);

		return buildStringReferenceNonNullLiteral(string1.replaceFirst(string2,
				string3));
	}

	@Override
	public Object visit(StringReplaceC str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		LiteralBitVector32 p2 = (LiteralBitVector32) str.getParam1().accept(
				this);
		LiteralBitVector32 p3 = (LiteralBitVector32) str.getParam2().accept(
				this);

		String string1 = getString(p1);

		return buildStringReferenceNonNullLiteral(string1.replace(
				(char) p2.getValue(), (char) p3.getValue()));
	}

	@Override
	public Object visit(StringSubstring str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		LiteralBitVector32 p2 = (LiteralBitVector32) str.getParam1().accept(
				this);
		LiteralBitVector32 p3 = (LiteralBitVector32) str.getParam2().accept(
				this);

		String string1 = getString(p1);

		return buildStringReferenceNonNullLiteral(string1.substring(
				(char) p2.getValue(), (char) p3.getValue()));
	}

	@Override
	public Object visit(StringLastIndexOfCI str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		LiteralBitVector32 p2 = (LiteralBitVector32) str.getParam1().accept(
				this);
		LiteralBitVector32 p3 = (LiteralBitVector32) str.getParam2().accept(
				this);

		String string1 = getString(p1);

		return new LiteralBitVector32(string1.lastIndexOf(p2.getValue(),
				p3.getValue()));
	}

	@Override
	public Object visit(StringIndexOfCI str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		LiteralBitVector32 p2 = (LiteralBitVector32) str.getParam1().accept(
				this);
		LiteralBitVector32 p3 = (LiteralBitVector32) str.getParam2().accept(
				this);

		String string1 = getString(p1);

		return new LiteralBitVector32(string1.indexOf(p2.getValue(),
				p3.getValue()));
	}

	@Override
	public Object visit(StringIndexOfSI str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam1().accept(this);
		LiteralBitVector32 p3 = (LiteralBitVector32) str.getParam2().accept(
				this);

		String string1 = getString(p1);
		String string2 = getString(p2);

		return new LiteralBitVector32(string1.indexOf(string2, p3.getValue()));
	}

	@Override
	public Object visit(StringLastIndexOfSI str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam1().accept(this);
		LiteralBitVector32 p3 = (LiteralBitVector32) str.getParam2().accept(
				this);

		String string1 = getString(p1);
		String string2 = getString(p2);

		return new LiteralBitVector32(string1.lastIndexOf(string2,
				p3.getValue()));
	}

	@Override
	public Object visit(StringEquals str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam().accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);

		return new LiteralBitVector32(string1.equalsIgnoreCase(string2) ? 1 : 0);
	}

	@Override
	public Object visit(StringEqualsIgnoreCase str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam().accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);

		return new LiteralBitVector32(string1.equalsIgnoreCase(string2) ? 1 : 0);
	}

	@Override
	public Object visit(StringEndsWith str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam().accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);

		return new LiteralBitVector32(string1.endsWith(string2) ? 1 : 0);

	}

	@Override
	public Object visit(StringStartsWith str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam1().accept(this);
		LiteralBitVector32 param3 = (LiteralBitVector32) str.getParam2()
				.accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);

		return new LiteralBitVector32(string1.startsWith(string2,
				param3.getValue()) ? 1 : 0);
	}

	@Override
	public Object visit(StringRegionMatches str) {
		StringReferenceNonNullLiteral str_param_1 = (StringReferenceNonNullLiteral) str
				.getStrReceiver().accept(this);
		LiteralBitVector32 int_param_2 = (LiteralBitVector32) str
				.getIgnoreCase().accept(this);
		LiteralBitVector32 int_param_3 = (LiteralBitVector32) str.getToffset()
				.accept(this);
		StringReferenceNonNullLiteral str_param_4 = (StringReferenceNonNullLiteral) str
				.getOtherString().accept(this);
		LiteralBitVector32 int_param_5 = (LiteralBitVector32) str.getOoffset()
				.accept(this);
		LiteralBitVector32 int_param_6 = (LiteralBitVector32) str.getLen()
				.accept(this);

		String string1 = getString(str_param_1);
		String string4 = getString(str_param_4);

		return new LiteralBitVector32(string1.regionMatches(
				int_param_2.getValue() == 1 ? true : false,
				int_param_3.getValue(), string4, int_param_5.getValue(),
				int_param_6.getValue()) ? 1 : 0);

	}

	@Override
	public Object visit(StringContains str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam().accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);

		return new LiteralBitVector32(string1.contains(string2) ? 1 : 0);
	}

	@Override
	public Object visit(StringReplaceCS str) {
		StringReferenceNonNullLiteral p1 = (StringReferenceNonNullLiteral) str
				.getStringReceiver().accept(this);
		StringReferenceNonNullLiteral p2 = (StringReferenceNonNullLiteral) str
				.getParam1().accept(this);
		StringReferenceNonNullLiteral p3 = (StringReferenceNonNullLiteral) str
				.getParam1().accept(this);

		String string1 = getString(p1);
		String string2 = getString(p2);
		String string3 = getString(p3);

		return buildStringReferenceNonNullLiteral(string1.replace(string2,
				string3));

	}

	@Override
	public Object visit(StringReferenceNonNullLiteral r) {
		Object ret_val = r.getReferenceNonNullLiteral().accept(this);
		if (ret_val instanceof LiteralNonNullReference) {
			LiteralNonNullReference l = (LiteralNonNullReference) ret_val;
			return new StringReferenceNonNullLiteral(l);
		} else {
			return ret_val;
		}
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
	public Object visit(FieldSelectBv64 b) {
		throw new IllegalStateException(
				"FieldSelectBv64 is not a valid AST instance");
	}

	@Override
	public Object visit(ArraySelectFp64 f) {
		throw new IllegalStateException(
				"ArraySelectFp64 is not a valid AST instance");
	}

	@Override
	public Object visit(FieldSelectFp64 f) {
		throw new IllegalStateException(
				"FieldSelectFp64 is not a valid AST instance");
	}

	@Override
	public Object visit(Bv32ValuedInstanceMethod b) {
		throw new IllegalStateException(
				"Bv32ValuedInstanceMethod is not a valid AST instance");

	}

	@Override
	public Object visit(FieldSelectBv32 b) {
		throw new IllegalStateException(
				"FieldSelectBv32 is not a valid AST instance");
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
	public Object visit(FieldSelectFp32 f) {
		throw new IllegalStateException(
				"FieldSelectFp32 is not a valid AST instance");
	}

	@Override
	public Object visit(ArraySelectFp32 f) {
		throw new IllegalStateException(
				"ArraySelectFp32 is not a valid AST instance");
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

}
