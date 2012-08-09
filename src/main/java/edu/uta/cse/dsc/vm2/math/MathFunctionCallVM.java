package edu.uta.cse.dsc.vm2.math;

import static edu.uta.cse.dsc.util.Assertions.check;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.objectweb.asm.Type;

import edu.uta.cse.dsc.AbstractVM;
import edu.uta.cse.dsc.vm2.Bv32Operand;
import edu.uta.cse.dsc.vm2.Bv64Operand;
import edu.uta.cse.dsc.vm2.Fp32Operand;
import edu.uta.cse.dsc.vm2.Fp64Operand;
import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

/**
 * This listener deals with trapping function calls to symbolic functions from
 * java.lang.Math.
 * 
 * This listener is expected to be executed after the CallVM listener.
 * 
 * @author galeotti
 * 
 */
public final class MathFunctionCallVM extends AbstractVM {

	public static final String JAVA_LANG_MATH = Math.class.getName().replace(
			".", "/");

	private Stack<Expression<?>> math_params = new Stack<Expression<?>>();

	private SymbolicEnvironment env;

	public MathFunctionCallVM(SymbolicEnvironment env) {
		this.env = env;
		fillMathFunctionTable();
	}

	private void fillMathFunctionTable() {
		mathFunctions.clear();

		addMathFunction(new ABS.ABS_I());
		addMathFunction(new ABS.ABS_L());
		addMathFunction(new ABS.ABS_F());
		addMathFunction(new ABS.ABS_D());
		addMathFunction(new ACOS());
		addMathFunction(new ASIN());
		addMathFunction(new ATAN());
		addMathFunction(new ATAN2());
		addMathFunction(new CBRT());
		addMathFunction(new CEIL());
		addMathFunction(new CopySign.CopySign_F());
		addMathFunction(new CopySign.CopySign_D());
		addMathFunction(new COS());
		addMathFunction(new COSH());
		addMathFunction(new EXP());
		addMathFunction(new EXPM1());
		addMathFunction(new FLOOR());
		addMathFunction(new GetExponent.GetExponent_F());
		addMathFunction(new GetExponent.GetExponent_D());
		addMathFunction(new HYPOT());
		addMathFunction(new IEEEremainder());
		addMathFunction(new LOG());
		addMathFunction(new LOG10());
		addMathFunction(new LOG1P());
		addMathFunction(new MIN.MIN_I());
		addMathFunction(new MIN.MIN_L());
		addMathFunction(new MIN.MIN_F());
		addMathFunction(new MIN.MIN_D());
		addMathFunction(new NextAfter.NextAfter_F());
		addMathFunction(new NextAfter.NextAfter_D());
		addMathFunction(new NextUp.NextUp_F());
		addMathFunction(new NextUp.NextUp_D());
		addMathFunction(new POW());
		addMathFunction(new RINT());
		addMathFunction(new Round.Round_F());
		addMathFunction(new Round.Round_D());
		addMathFunction(new SCALB.SCALB_F());
		addMathFunction(new SCALB.SCALB_D());
		addMathFunction(new SIGNUM.SIGNUM_F());
		addMathFunction(new SIGNUM.SIGNUM_D());
		addMathFunction(new SIN());
		addMathFunction(new SINH());
		addMathFunction(new SQRT());
		addMathFunction(new TAN());
		addMathFunction(new TANH());
		addMathFunction(new ToDegrees());
		addMathFunction(new ToRadians());
		addMathFunction(new ULP.ULP_F());
		addMathFunction(new ULP.ULP_D());
	}

	private void addMathFunction(MathFunction v) {
		FunctionKey k = new FunctionKey(v.getOwner(), v.getName(), v.getDesc());
		mathFunctions.put(k, v);
	}

	/**
	 * Pops the stack according to the method signature.
	 * http;//cs.au.dk/~mis/dOvs/jvmspec/ref--35.html
	 */
	@Override
	public void INVOKESTATIC(String owner, String name, String desc) {

		MathFunction f = getMathFunction(owner, name, desc);
		if (f == null) {
			return; // do nothing
		}

		if (desc.equals(MathFunctionCallVM.D2D_DESCRIPTOR)
				|| desc.equals(MathFunctionCallVM.D2L_DESCRIPTOR)
				|| desc.equals(MathFunctionCallVM.D2I_DESCRIPTOR)) {

			RealExpression param = env.topFrame().operandStack.peekFp64();
			math_params.push(param);

		} else if (desc.equals(MathFunctionCallVM.I2I_DESCRIPTOR)) {

			IntegerExpression param = env.topFrame().operandStack.peekBv32();
			math_params.push(param);

		} else if (desc.equals(MathFunctionCallVM.L2L_DESCRIPTOR)) {

			IntegerExpression param = env.topFrame().operandStack.peekBv64();
			math_params.push(param);

		} else if (desc.equals(MathFunctionCallVM.F2F_DESCRIPTOR)
				|| desc.equals(MathFunctionCallVM.F2I_DESCRIPTOR)) {

			RealExpression param = env.topFrame().operandStack.peekFp32();
			math_params.push(param);

		} else if (desc.equals(MathFunctionCallVM.DD2D_DESCRIPTOR)) {

			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			RealExpression left = fp64(it.next());
			RealExpression right = fp64(it.next());

			math_params.push(left);
			math_params.push(right);

		} else if (desc.equals(MathFunctionCallVM.II2I_DESCRIPTOR)) {

			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			IntegerExpression left = bv32(it.next());
			IntegerExpression right = bv32(it.next());

			math_params.push(left);
			math_params.push(right);

		} else if (desc.equals(MathFunctionCallVM.LL2L_DESCRIPTOR)) {

			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			IntegerExpression left = bv64(it.next());
			IntegerExpression right = bv64(it.next());

			math_params.push(left);
			math_params.push(right);

		} else if (desc.equals(MathFunctionCallVM.FF2F_DESCRIPTOR)) {

			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			RealExpression left = fp32(it.next());
			RealExpression right = fp32(it.next());

			math_params.push(left);
			math_params.push(right);

		} else if (desc.equals(MathFunctionCallVM.FI2F_DESCRIPTOR)) {

			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			RealExpression left = fp32(it.next());
			IntegerExpression right = bv32(it.next());

			math_params.push(left);
			math_params.push(right);

		} else if (desc.equals(MathFunctionCallVM.FD2F_DESCRIPTOR)) {

			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			RealExpression left = fp32(it.next());
			RealExpression right = fp64(it.next());

			math_params.push(left);
			math_params.push(right);

		} else if (desc.equals(MathFunctionCallVM.DI2D_DESCRIPTOR)) {

			Iterator<Operand> it = env.topFrame().operandStack.iterator();
			RealExpression left = fp64(it.next());
			IntegerExpression right = bv32(it.next());

			math_params.push(left);
			math_params.push(right);

		} else {
			check(false); // unreachable code
		}

	}

	private static RealExpression fp64(Operand operand) {
		Fp64Operand fp64 = (Fp64Operand) operand;
		return fp64.getRealExpression();
	}

	private static RealExpression fp32(Operand operand) {
		Fp32Operand fp32 = (Fp32Operand) operand;
		return fp32.getRealExpression();
	}

	private static IntegerExpression bv32(Operand operand) {
		Bv32Operand fp32 = (Bv32Operand) operand;
		return fp32.getIntegerExpression();
	}

	private static IntegerExpression bv64(Operand operand) {
		Bv64Operand bv64 = (Bv64Operand) operand;
		return bv64.getIntegerExpression();
	}

	private static class FunctionKey {
		public FunctionKey(String owner, String name, String desc) {
			super();
			this.owner = owner;
			this.name = name;
			this.desc = desc;
		}

		public String owner;
		public String name;
		public String desc;

		@Override
		public int hashCode() {
			return owner.hashCode() + name.hashCode() + desc.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !o.getClass().equals(FunctionKey.class)) {
				return false;
			} else {
				FunctionKey that = (FunctionKey) o;
				return this.owner.equals(that.owner)
						&& this.name.equals(that.name)
						&& this.desc.equals(that.desc);
			}
		}
	}

	private HashMap<FunctionKey, MathFunction> mathFunctions = new HashMap<FunctionKey, MathFunction>();

	// homogeneuos unary descriptors
	public static final String I2I_DESCRIPTOR = Type.getMethodDescriptor(
			Type.INT_TYPE, Type.INT_TYPE); // "(I)I";

	public static final String L2L_DESCRIPTOR = Type.getMethodDescriptor(
			Type.LONG_TYPE, Type.LONG_TYPE);// "(J)J";

	public static final String F2F_DESCRIPTOR = Type.getMethodDescriptor(
			Type.FLOAT_TYPE, Type.FLOAT_TYPE);// "(F)F";

	// heterogeneous unary descriptors
	public static final String F2I_DESCRIPTOR = Type.getMethodDescriptor(
			Type.INT_TYPE, Type.FLOAT_TYPE);// "(F)I";

	// homogeneuos binary descriptors
	public static final String II2I_DESCRIPTOR = Type.getMethodDescriptor(
			Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE);// "(II)I";

	public static final String LL2L_DESCRIPTOR = Type.getMethodDescriptor(
			Type.LONG_TYPE, Type.LONG_TYPE, Type.LONG_TYPE);// "(JJ)J";

	public static final String FF2F_DESCRIPTOR = Type.getMethodDescriptor(
			Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE);// "(FF)F";

	// heterogeneous binary descriptors
	public static final String FI2F_DESCRIPTOR = Type.getMethodDescriptor(
			Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.INT_TYPE);// "(FI)F";

	public static final String FD2F_DESCRIPTOR = Type.getMethodDescriptor(
			Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.DOUBLE_TYPE);// "(FD)F";

	public static final String DI2D_DESCRIPTOR = Type.getMethodDescriptor(
			Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.INT_TYPE);// "(DI)D";

	public static final String D2I_DESCRIPTOR = Type.getMethodDescriptor(
			Type.INT_TYPE, Type.DOUBLE_TYPE);// "(D)I";

	public static final String D2L_DESCRIPTOR = Type.getMethodDescriptor(
			Type.LONG_TYPE, Type.DOUBLE_TYPE);// "(D)J";

	public static final String DD2D_DESCRIPTOR = Type.getMethodDescriptor(
			Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE);// "(DD)D";

	public static final String D2D_DESCRIPTOR = Type.getMethodDescriptor(
			Type.DOUBLE_TYPE, Type.DOUBLE_TYPE);// "(D)D";

	@Override
	public void CALL_RESULT(double res, String owner, String name, String desc) {
		MathFunction f = getMathFunction(owner, name, desc);
		if (f == null) {
			return; // do nothing
		}
		RealExpression realExpr = f.execute(math_params, res);
		if (realExpr != null) {
			// replace res
			env.topFrame().operandStack.popFp64();
			env.topFrame().operandStack.pushFp64(realExpr);
		}
	}

	private MathFunction getMathFunction(String owner, String name, String desc) {
		MathFunction f;
		FunctionKey k = new FunctionKey(owner, name, desc);
		f = mathFunctions.get(k);
		return f;
	}

	@Override
	public void CALL_RESULT(int res, String owner, String name, String desc) {
		MathFunction f = getMathFunction(owner, name, desc);
		if (f == null) {
			return; // do nothing
		}
		IntegerExpression intExpr = f.execute(math_params, res);
		if (intExpr != null) {
			// replace res
			env.topFrame().operandStack.popBv32();
			env.topFrame().operandStack.pushBv32(intExpr);
		}
	}

	@Override
	public void CALL_RESULT(long res, String owner, String name, String desc) {
		MathFunction f = getMathFunction(owner, name, desc);
		if (f == null) {
			return; // do nothing
		}
		IntegerExpression intExpr = f.execute(math_params, res);
		if (intExpr != null) {
			// replace res
			env.topFrame().operandStack.popBv64();
			env.topFrame().operandStack.pushBv64(intExpr);
		}
	}

	@Override
	public void CALL_RESULT(float res, String owner, String name, String desc) {
		MathFunction f = getMathFunction(owner, name, desc);
		if (f == null) {
			return; // do nothing
		}
		RealExpression realExpr = f.execute(math_params, res);
		if (realExpr != null) {
			// replace res
			env.topFrame().operandStack.popFp32();
			env.topFrame().operandStack.pushFp32(realExpr);
		}

	}

}
