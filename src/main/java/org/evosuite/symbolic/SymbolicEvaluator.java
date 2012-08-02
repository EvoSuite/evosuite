package org.evosuite.symbolic;

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
 * Translates a <code>edu.uta.cse.dsc.ast.BitVector32</code> expression into a
 * <code>org.evosuite.symbolic.expr.IntegerExpression</code>.
 * 
 * If a given <code>BitVector32Variable</code> is not marked as <b>symbolic</b>,
 * then it returns its concrete value.
 * 
 * @author galeotti
 * 
 */
public final class SymbolicEvaluator implements BitVector32Visitor,
		BitVector64Visitor, FloatExpressionVisitor, DoubleExpressionVisitor,
		ReferenceVisitor {

	private final ConcolicState concolicState;

	public SymbolicEvaluator(ConcolicState concolicState) {
		this.concolicState = concolicState;
	}

	@Override
	public Object visit(BitVector32Variable b) {
		BitVector32 symbolic_value = (BitVector32) concolicState
				.getSymbolicValue(b);

		if (concolicState.isMarked(b)) {
			return b;
		} else {
			return symbolic_value;
		}
	}

	@Override
	public Object visit(ReferenceVariable r) {
		Reference symbolic_value = (Reference) concolicState
				.getSymbolicValue(r);

		if (concolicState.isMarked(r)) {
			return r;
		} else {
			return symbolic_value;
		}
	}

	@Override
	public Object visit(FloatVariable f) {
		FloatExpression symbolic_value = (FloatExpression) concolicState
				.getSymbolicValue(f);
		if (concolicState.isMarked(f)) {
			return f;
		} else {
			return symbolic_value;
		}
	}

	@Override
	public Object visit(BitVector64Variable b) {
		BitVector64 symbolic_value = (BitVector64) concolicState
				.getSymbolicValue(b);
		if (concolicState.isMarked(b)) {
			return b;
		} else {
			return symbolic_value;
		}

	}

	@Override
	public Object visit(DoubleVariable f) {
		DoubleExpression symbolic_value = (DoubleExpression) concolicState
				.getSymbolicValue(f);
		if (concolicState.isMarked(f)) {
			return f;
		} else {
			return symbolic_value;
		}

	}

	@Override
	public Object accept(Bv32ExtendingAnBv64 b) {
		BitVector64 e = (BitVector64) b.getParam().accept(this);
		return new Bv32ExtendingAnBv64(e);
	}

	@Override
	public Object visit(Bv32ExtendingAnFp32 b) {
		FloatExpression e = (FloatExpression) b.getParam().accept(this);
		return new Bv32ExtendingAnFp32(e);
	}

	@Override
	public Object visit(Bv32ExtendingAnFp64 b) {
		DoubleExpression e = (DoubleExpression) b.getParam().accept(this);
		return new Bv32ExtendingAnFp64(e);
	}

	@Override
	public Object visit(Fp32ExtendingAnBv32 f) {
		BitVector32 e = (BitVector32) f.getParam().accept(this);
		return new Fp32ExtendingAnBv32(e);
	}

	@Override
	public Object visit(LiteralBitVector32 b) {
		return b;
	}

	@Override
	public Object visit(FpSLt.Fp32 b) {
		FloatExpression left = (FloatExpression) b.getLeft().accept(this);
		FloatExpression right = (FloatExpression) b.getRight().accept(this);

		return new FpSLt.Fp32(left, right);
	}

	@Override
	public Object visit(BvAdd.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvAdd.Bv32(left, right);
	}

	@Override
	public Object visit(BvAnd.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvAnd.Bv32(left, right);

	}

	@Override
	public Object visit(BvMul.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvMul.Bv32(left, right);
	}

	@Override
	public Object visit(BvNeg.Bv32 b) {

		BitVector32 param = (BitVector32) ((BitVector32) b.getParam())
				.accept(this);

		return new BvNeg.Bv32(param);
	}

	@Override
	public Object visit(BvOr.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvOr.Bv32(left, right);

	}

	@Override
	public Object visit(BvSDiv.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvSDiv.Bv32(left, right);
	}

	@Override
	public Object visit(BvSRem.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvSRem.Bv32(left, right);
	}

	@Override
	public Object visit(BvSub.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvSub.Bv32(left, right);
	}

	@Override
	public Object visit(BvXOr.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvXOr.Bv32(left, right);
	}

	@Override
	public Object visit(FloatLiteral f) {
		return f;
	}

	@Override
	public Object visit(FpSub.Fp32 f) {
		FloatExpression left = (FloatExpression) f.getLeft().accept(this);
		FloatExpression right = (FloatExpression) f.getRight().accept(this);

		return new FpSub.Fp32(left, right);
	}

	@Override
	public Object visit(FpAdd.Fp32 f) {
		FloatExpression left = (FloatExpression) f.getLeft().accept(this);
		FloatExpression right = (FloatExpression) f.getRight().accept(this);

		return new FpAdd.Fp32(left, right);
	}

	@Override
	public Object visit(FpSRem.Fp32 f) {
		FloatExpression left = (FloatExpression) f.getLeft().accept(this);
		FloatExpression right = (FloatExpression) f.getRight().accept(this);

		return new FpSRem.Fp32(left, right);
	}

	@Override
	public Object visit(FpSDiv.Fp32 f) {
		FloatExpression left = (FloatExpression) f.getLeft().accept(this);
		FloatExpression right = (FloatExpression) f.getRight().accept(this);

		return new FpSDiv.Fp32(left, right);
	}

	@Override
	public Object visit(FpMul.Fp32 f) {
		FloatExpression left = (FloatExpression) f.getLeft().accept(this);
		FloatExpression right = (FloatExpression) f.getRight().accept(this);

		return new FpMul.Fp32(left, right);
	}

	@Override
	public Object visit(FpNeg.Fp32 f) {
		FloatExpression param = (FloatExpression) ((FloatExpression) f
				.getParam()).accept(this);

		return new FpNeg.Fp32(param);
	}

	@Override
	public Object visit(Fp32ExtendingAnBv64 f) {
		BitVector64 param = (BitVector64) f.getParam().accept(this);

		return new Fp32ExtendingAnBv64(param);
	}

	@Override
	public Object visit(Fp32ExtendingAnFp64 f) {
		DoubleExpression param = (DoubleExpression) f.getParam().accept(this);

		return new Fp32ExtendingAnFp64(param);
	}

	@Override
	public Object visit(Bv64ExtendingAnBv32 b) {
		BitVector32 param = (BitVector32) b.getParam().accept(this);
		return new Bv64ExtendingAnBv32(param);
	}

	@Override
	public Object accept(Bv64ExtendingAnFp32 b) {
		FloatExpression param = (FloatExpression) b.getParam().accept(this);
		return new Bv64ExtendingAnFp32(param);
	}

	@Override
	public Object visit(Bv64ExtendingAnFp64 b) {
		DoubleExpression param = (DoubleExpression) b.getParam().accept(this);
		return new Bv64ExtendingAnFp64(param);
	}

	@Override
	public Object visit(LiteralBitVector64 b) {
		return b;
	}

	@Override
	public Object visit(BvAdd.Bv64 b) {
		BitVector64 left = (BitVector64) b.getLeft().accept(this);
		BitVector64 right = (BitVector64) b.getRight().accept(this);

		return new BvAdd.Bv64(left, right);
	}

	@Override
	public Object visit(BvSub.Bv64 b) {
		BitVector64 left = (BitVector64) b.getLeft().accept(this);
		BitVector64 right = (BitVector64) b.getRight().accept(this);

		return new BvSub.Bv64(left, right);

	}

	@Override
	public Object visit(BvMul.Bv64 b) {
		BitVector64 left = (BitVector64) b.getLeft().accept(this);
		BitVector64 right = (BitVector64) b.getRight().accept(this);

		return new BvMul.Bv64(left, right);

	}

	@Override
	public Object visit(BvSDiv.Bv64 b) {
		BitVector64 left = (BitVector64) b.getLeft().accept(this);
		BitVector64 right = (BitVector64) b.getRight().accept(this);

		return new BvSDiv.Bv64(left, right);
	}

	@Override
	public Object visit(BvSRem.Bv64 b) {
		BitVector64 left = (BitVector64) b.getLeft().accept(this);
		BitVector64 right = (BitVector64) b.getRight().accept(this);

		return new BvSRem.Bv64(left, right);

	}

	@Override
	public Object visit(BvAnd.Bv64 b) {
		BitVector64 left = (BitVector64) b.getLeft().accept(this);
		BitVector64 right = (BitVector64) b.getRight().accept(this);

		return new BvAnd.Bv64(left, right);

	}

	@Override
	public Object visit(BvOr.Bv64 b) {
		BitVector64 left = (BitVector64) b.getLeft().accept(this);
		BitVector64 right = (BitVector64) b.getRight().accept(this);

		return new BvOr.Bv64(left, right);

	}

	@Override
	public Object visit(BvXOr.Bv64 b) {
		BitVector64 left = (BitVector64) b.getLeft().accept(this);
		BitVector64 right = (BitVector64) b.getRight().accept(this);

		return new BvXOr.Bv64(left, right);

	}

	@Override
	public Object visit(BvNeg.Bv64 b) {
		BitVector64 param = (BitVector64) ((BitVector64) b.getParam())
				.accept(this);
		return new BvNeg.Bv64(param);

	}

	@Override
	public Object visit(Fp64ExtendingAnBv32 f) {
		BitVector32 p = (BitVector32) f.getParam().accept(this);
		return new Fp64ExtendingAnBv32(p);
	}

	@Override
	public Object visit(Fp64ExtendingAnBv64 f) {
		BitVector64 p = (BitVector64) f.getParam().accept(this);
		return new Fp64ExtendingAnBv64(p);
	}

	@Override
	public Object visit(Fp64ExtendingAnFp32 f) {
		FloatExpression p = (FloatExpression) f.getParam().accept(this);
		return new Fp64ExtendingAnFp32(p);
	}

	@Override
	public Object visit(FpAdd.Fp64 f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new FpAdd.Fp64(left, right);
	}

	@Override
	public Object visit(FpSub.Fp64 f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new FpSub.Fp64(left, right);
	}

	@Override
	public Object visit(FpMul.Fp64 f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new FpMul.Fp64(left, right);
	}

	@Override
	public Object visit(FpSDiv.Fp64 f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new FpSDiv.Fp64(left, right);
	}

	@Override
	public Object visit(FpSRem.Fp64 f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new FpSRem.Fp64(left, right);

	}

	@Override
	public Object visit(FpNeg.Fp64 f) {
		DoubleExpression p = (DoubleExpression) ((DoubleExpression) f
				.getParam()).accept(this);

		return new FpNeg.Fp64(p);
	}

	@Override
	public Object visit(DoubleLiteral f) {
		return f;
	}

	@Override
	public Object visit(FpSLt.Fp64 f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new FpSLt.Fp64(left, right);
	}

	@Override
	public Object visit(BvSHR.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvSHR.Bv32(left, right);

	}

	@Override
	public Object visit(BvSHL.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvSHL.Bv32(left, right);
	}

	@Override
	public Object visit(BvUSHR.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvUSHR.Bv32(left, right);
	}

	@Override
	public Object visit(BvSHL.Bv64 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvSHL.Bv32(left, right);
	}

	@Override
	public Object visit(BvSHR.Bv64 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvSHR.Bv32(left, right);
	}

	@Override
	public Object visit(BvUSHR.Bv64 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new BvUSHR.Bv32(left, right);
	}

	@Override
	public Object visit(COS f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new COS(p);
	}

	@Override
	public Object visit(ACOS f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new ACOS(p);
	}

	@Override
	public Object visit(LOG10 f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new LOG10(p);

	}

	@Override
	public Object visit(LOG1P f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new LOG1P(p);
	}

	@Override
	public Object visit(RINT f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new RINT(p);
	}

	@Override
	public Object visit(SIN f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new SIN(p);

	}

	@Override
	public Object visit(SINH f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new SINH(p);
	}

	@Override
	public Object visit(SQRT f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new SQRT(p);
	}

	@Override
	public Object visit(TAN f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new TAN(p);
	}

	@Override
	public Object visit(TANH f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new TANH(p);
	}

	@Override
	public Object visit(ToDegrees f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new ToDegrees(p);
	}

	@Override
	public Object visit(ToRadians f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new ToRadians(p);
	}

	@Override
	public Object visit(ASIN f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new ASIN(p);
	}

	@Override
	public Object visit(ATAN f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new ATAN(p);
	}

	@Override
	public Object visit(CBRT f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new CBRT(p);
	}

	@Override
	public Object visit(CEIL f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new CEIL(p);
	}

	@Override
	public Object visit(COSH f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new COSH(p);
	}

	@Override
	public Object visit(EXP f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new EXP(p);
	}

	@Override
	public Object visit(EXPM1 f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new EXPM1(p);
	}

	@Override
	public Object visit(FLOOR f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new FLOOR(p);
	}

	@Override
	public Object visit(LOG f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new LOG(p);
	}

	@Override
	public Object visit(ATAN2 f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new ATAN2(left, right);
	}

	@Override
	public Object visit(HYPOT f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new HYPOT(left, right);
	}

	@Override
	public Object visit(IEEEremainder f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new IEEEremainder(left, right);
	}

	@Override
	public Object visit(POW f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new POW(left, right);
	}

	@Override
	public Object visit(NextUp.Fp64 f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new NextUp.Fp64(p);
	}

	@Override
	public Object visit(GetExponent.Fp64 f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new GetExponent.Fp64(p);
	}

	@Override
	public Object visit(ULP.Fp64 f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new ULP.Fp64(p);
	}

	@Override
	public Object visit(ABS.Fp64 f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new ABS.Fp64(p);
	}

	@Override
	public Object visit(SCALB.Fp32 f) {
		FloatExpression left = (FloatExpression) f.getLeft().accept(this);
		BitVector32 right = (BitVector32) f.getRight().accept(this);

		return new SCALB.Fp32(left, right);
	}

	@Override
	public Object visit(SCALB.Fp64 f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		BitVector32 right = (BitVector32) f.getRight().accept(this);

		return new SCALB.Fp64(left, right);
	}

	@Override
	public Object visit(CopySign.Fp64 f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new CopySign.Fp64(left, right);
	}

	@Override
	public Object visit(NextAfter.Fp32 f) {
		FloatExpression left = (FloatExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new NextAfter.Fp32(left, right);
	}

	@Override
	public Object visit(NextAfter.Fp64 f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new NextAfter.Fp64(left, right);
	}

	@Override
	public Object visit(MAX.Fp64 f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new MAX.Fp64(left, right);
	}

	@Override
	public Object visit(MIN.Fp64 f) {
		DoubleExpression left = (DoubleExpression) f.getLeft().accept(this);
		DoubleExpression right = (DoubleExpression) f.getRight().accept(this);

		return new MIN.Fp64(left, right);
	}

	@Override
	public Object visit(SIGNUM.Fp64 f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);

		return new SIGNUM.Fp64(p);

	}

	@Override
	public Object visit(NextUp.Fp32 f) {
		FloatExpression p = (FloatExpression) f.getParam().accept(this);
		return new NextUp.Fp32(p);
	}

	@Override
	public Object visit(GetExponent.Fp32 f) {
		FloatExpression p = (FloatExpression) f.getParam().accept(this);
		return new GetExponent.Fp32(p);

	}

	@Override
	public Object visit(ULP.Fp32 f) {
		FloatExpression p = (FloatExpression) f.getParam().accept(this);
		return new ULP.Fp32(p);
	}

	@Override
	public Object visit(ABS.Fp32 f) {
		FloatExpression p = (FloatExpression) f.getParam().accept(this);
		return new ABS.Fp32(p);
	}

	@Override
	public Object visit(CopySign.Fp32 f) {
		FloatExpression left = (FloatExpression) f.getLeft().accept(this);
		FloatExpression right = (FloatExpression) f.getRight().accept(this);

		return new CopySign.Fp32(left, right);
	}

	@Override
	public Object visit(MAX.Fp32 f) {
		FloatExpression left = (FloatExpression) f.getLeft().accept(this);
		FloatExpression right = (FloatExpression) f.getRight().accept(this);

		return new MAX.Fp32(left, right);
	}

	@Override
	public Object visit(MIN.Fp32 f) {
		FloatExpression left = (FloatExpression) f.getLeft().accept(this);
		FloatExpression right = (FloatExpression) f.getRight().accept(this);

		return new MIN.Fp32(left, right);
	}

	@Override
	public Object visit(SIGNUM.Fp32 f) {
		FloatExpression p = (FloatExpression) f.getParam().accept(this);
		return new SIGNUM.Fp32(p);
	}

	@Override
	public Object visit(ABS.Bv64 b) {
		BitVector64 p = (BitVector64) b.getParam().accept(this);
		return new ABS.Bv64(p);
	}

	@Override
	public Object visit(MAX.Bv64 b) {
		BitVector64 left = (BitVector64) b.getLeft().accept(this);
		BitVector64 right = (BitVector64) b.getRight().accept(this);

		return new MAX.Bv64(left, right);
	}

	@Override
	public Object visit(MIN.Bv64 b) {
		BitVector64 left = (BitVector64) b.getLeft().accept(this);
		BitVector64 right = (BitVector64) b.getRight().accept(this);

		return new MIN.Bv64(left, right);
	}

	@Override
	public Object visit(ROUND.Fp64 f) {
		DoubleExpression p = (DoubleExpression) f.getParam().accept(this);
		return new ROUND.Fp64(p);
	}

	@Override
	public Object visit(ROUND.Fp32 f) {
		FloatExpression p = (FloatExpression) f.getParam().accept(this);
		return new ROUND.Fp32(p);
	}

	@Override
	public Object visit(ABS.Bv32 b) {
		BitVector32 p = (BitVector32) b.getParam().accept(this);
		return new ABS.Bv32(p);
	}

	@Override
	public Object visit(MAX.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new MAX.Bv32(left, right);
	}

	@Override
	public Object visit(MIN.Bv32 b) {
		BitVector32 left = (BitVector32) b.getLeft().accept(this);
		BitVector32 right = (BitVector32) b.getRight().accept(this);

		return new MIN.Bv32(left, right);
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
		StringReference p = (StringReference) str.getStringParam().accept(this);
		return new StringTrim(p);
	}

	@Override
	public Object visit(StringToUpperCase str) {
		StringReference p = (StringReference) str.getStringParam().accept(this);
		return new StringToUpperCase(p);
	}

	@Override
	public Object visit(StringToLowerCase str) {
		StringReference p = (StringReference) str.getStringParam().accept(this);
		return new StringToLowerCase(p);

	}

	@Override
	public Object visit(StringCompareTo str) {
		StringReference left = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference right = (StringReference) str.getParam().accept(this);
		return new StringCompareTo(left, right);
	}

	@Override
	public Object visit(StringCompareToIgnoreCase str) {
		StringReference left = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference right = (StringReference) str.getParam().accept(this);
		return new StringCompareToIgnoreCase(left, right);
	}

	@Override
	public Object visit(StringIndexOfS str) {
		StringReference left = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference right = (StringReference) str.getParam().accept(this);
		return new StringIndexOfS(left, right);
	}

	@Override
	public Object visit(StringLastIndexOfS str) {
		StringReference left = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference right = (StringReference) str.getParam().accept(this);
		return new StringLastIndexOfS(left, right);
	}

	@Override
	public Object visit(StringIndexOfC str) {
		StringReference left = (StringReference) str.getStringReceiver()
				.accept(this);
		BitVector32 right = (BitVector32) str.getParam().accept(this);
		return new StringIndexOfC(left, right);
	}

	@Override
	public Object visit(StringLastIndexOfC str) {
		StringReference left = (StringReference) str.getStringReceiver()
				.accept(this);
		BitVector32 right = (BitVector32) str.getParam().accept(this);
		return new StringLastIndexOfC(left, right);
	}

	@Override
	public Object visit(StringCharAt str) {
		StringReference left = (StringReference) str.getStringReceiver()
				.accept(this);
		BitVector32 right = (BitVector32) str.getParam().accept(this);
		return new StringCharAt(left, right);
	}

	@Override
	public Object visit(StringConcat str) {
		StringReference left = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference right = (StringReference) str.getParam().accept(this);
		return new StringConcat(left, right);
	}

	@Override
	public Object visit(StringReplaceAll str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference param2 = (StringReference) str.getParam1().accept(this);
		StringReference param3 = (StringReference) str.getParam2().accept(this);

		return new StringReplaceAll(param1, param2, param3);
	}

	@Override
	public Object visit(StringReplaceFirst str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference param2 = (StringReference) str.getParam1().accept(this);
		StringReference param3 = (StringReference) str.getParam2().accept(this);

		return new StringReplaceFirst(param1, param2, param3);
	}

	@Override
	public Object visit(StringReplaceC str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		BitVector32 param2 = (BitVector32) str.getParam1().accept(this);
		BitVector32 param3 = (BitVector32) str.getParam2().accept(this);

		return new StringReplaceC(param1, param2, param3);
	}

	@Override
	public Object visit(StringSubstring str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		BitVector32 param2 = (BitVector32) str.getParam1().accept(this);
		BitVector32 param3 = (BitVector32) str.getParam2().accept(this);

		return new StringSubstring(param1, param2, param3);
	}

	@Override
	public Object visit(StringLastIndexOfCI str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		BitVector32 param2 = (BitVector32) str.getParam1().accept(this);
		BitVector32 param3 = (BitVector32) str.getParam2().accept(this);

		return new StringLastIndexOfCI(param1, param2, param3);
	}

	@Override
	public Object visit(StringIndexOfCI str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		BitVector32 param2 = (BitVector32) str.getParam1().accept(this);
		BitVector32 param3 = (BitVector32) str.getParam2().accept(this);

		return new StringIndexOfCI(param1, param2, param3);
	}

	@Override
	public Object visit(StringIndexOfSI str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference param2 = (StringReference) str.getParam1().accept(this);
		BitVector32 param3 = (BitVector32) str.getParam2().accept(this);

		return new StringIndexOfSI(param1, param2, param3);
	}

	@Override
	public Object visit(StringLastIndexOfSI str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference param2 = (StringReference) str.getParam1().accept(this);
		BitVector32 param3 = (BitVector32) str.getParam2().accept(this);

		return new StringLastIndexOfSI(param1, param2, param3);
	}

	@Override
	public Object visit(StringEquals str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		Reference param2 = (Reference) str.getParam().accept(this);

		return new StringEquals(param1, param2);

	}

	@Override
	public Object visit(StringEqualsIgnoreCase str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference param2 = (StringReference) str.getParam().accept(this);

		return new StringEqualsIgnoreCase(param1, param2);
	}

	@Override
	public Object visit(StringEndsWith str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference param2 = (StringReference) str.getParam().accept(this);

		return new StringEndsWith(param1, param2);
	}

	@Override
	public Object visit(StringStartsWith str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference param2 = (StringReference) str.getParam1().accept(this);
		BitVector32 param3 = (BitVector32) str.getParam2().accept(this);

		return new StringStartsWith(param1, param2, param3);
	}

	@Override
	public Object visit(StringRegionMatches str) {
		StringReference str_param_1 = (StringReference) str.getStrReceiver()
				.accept(this);
		BitVector32 int_param_2 = (BitVector32) str.getIgnoreCase()
				.accept(this);
		BitVector32 int_param_3 = (BitVector32) str.getToffset().accept(this);
		StringReference str_param_4 = (StringReference) str.getOtherString()
				.accept(this);
		BitVector32 int_param_5 = (BitVector32) str.getOoffset().accept(this);
		BitVector32 int_param_6 = (BitVector32) str.getLen().accept(this);

		return new StringRegionMatches(str_param_1, int_param_2, int_param_3,
				str_param_4, int_param_5, int_param_6);

	}

	@Override
	public Object visit(StringContains str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference param2 = (StringReference) str.getParam().accept(this);

		return new StringContains(param1, param2);
	}

	@Override
	public Object visit(StringReplaceCS str) {
		StringReference param1 = (StringReference) str.getStringReceiver()
				.accept(this);
		StringReference param2 = (StringReference) str.getParam1().accept(this);
		StringReference param3 = (StringReference) str.getParam2().accept(this);

		return new StringReplaceCS(param1, param2, param3);

	}

	@Override
	public Object visit(StringReferenceVariable r) {
		Object ret_val = r.getReferenceVariable().accept(this);
		if (ret_val instanceof ReferenceVariable) {
			ReferenceVariable v = (ReferenceVariable) ret_val;
			return new StringReferenceVariable(v);
		} else if (ret_val instanceof LiteralNonNullReference) {
			LiteralNonNullReference l = (LiteralNonNullReference) ret_val;
			return new StringReferenceNonNullLiteral(l);
		} else {
			return ret_val;
		}
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

	/**
	 * LiteralArray.LABv32 represents the expression new int[length].
	 * 
	 * Since this is a mutable reference, we have to return the same reference.
	 */
	@Override
	public Object visit(LABv32 r) {
		return r;
	}

	/**
	 * LiteralArray.LABv64 represents the expression new long[length].
	 * 
	 * Since this is a mutable reference, we have to return the same reference.
	 */
	@Override
	public Object visit(LABv64 r) {
		return r;
	}

	/**
	 * LiteralArray.LAFp32 represents the expression new float[length].
	 * 
	 * Since this is a mutable reference, we have to return the same reference.
	 */
	@Override
	public Object visit(LAFp32 r) {
		return r;
	}

	/**
	 * LiteralArray.LAFp64 represents the expression new double[length].
	 * 
	 * Since this is a mutable reference, we have to return the same reference.
	 */
	@Override
	public Object visit(LAFp64 r) {
		return r;
	}

	/**
	 * LiteralArray.LARef represents the expression new Object[length].
	 * 
	 * Since this is a mutable reference, we have to return the same reference.
	 */
	@Override
	public Object visit(LARef r) {
		return r;
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
