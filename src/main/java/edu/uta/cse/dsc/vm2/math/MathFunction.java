package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.objectweb.asm.Type;

public abstract class MathFunction {

	public MathFunction(String name, String desc) {
		super();
		this.owner = MathFunctionCallVM.JAVA_LANG_MATH;
		this.name = name;
		this.desc = desc;
	}

	private final String owner;
	private final String name;
	private final String desc;
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

	public String getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public RealExpression execute(Stack<Expression<?>> params, double res) {
		return null;
	}

	public RealExpression execute(Stack<Expression<?>> params, float res) {
		return null;
	}

	public IntegerExpression execute(Stack<Expression<?>> params, int res) {
		return null;
	}

	public IntegerExpression execute(Stack<Expression<?>> params, long res) {
		return null;
	}

}
