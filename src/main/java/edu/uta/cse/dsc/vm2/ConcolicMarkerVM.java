package edu.uta.cse.dsc.vm2;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.IntegerVariable;
import org.evosuite.symbolic.expr.IntegerVariableFactory;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealVariable;
import org.evosuite.symbolic.expr.RealVariableFactory;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringVariable;
import org.evosuite.symbolic.expr.StringVariableFactory;
import org.objectweb.asm.Type;

import edu.uta.cse.dsc.AbstractVM;

/**
 * This listeners: (i) collects the branch conditions (ii) collects the variable
 * name for each symbolic marked variable
 * 
 * @author galeotti
 * 
 */
public final class ConcolicMarkerVM extends AbstractVM {

	private static final String CONCOLIC_MARKER_CLASS_NAME = ConcolicMarker.class
			.getCanonicalName().replace(".", "/");

	private final SymbolicEnvironment env;

	public ConcolicMarkerVM(SymbolicEnvironment env) {
		this.env = env;
	}

	@Override
	public void GETFIELD(Object receiver, String owner, String name, String desc) {

		if (owner.startsWith(CONCOLIC_MARKER_CLASS_NAME)) {

			if (desc.equals(Type.BOOLEAN_TYPE.getDescriptor())) {
				addSymbolicBoolean();

			} else if (desc.equals(Type.BYTE_TYPE.getDescriptor())) {
				addSymbolicByte();

			} else if (desc.equals(Type.SHORT_TYPE.getDescriptor())) {
				addSymbolicShort();

			} else if (desc.equals(Type.INT_TYPE.getDescriptor())) {
				addSymbolicInt();

			} else if (desc.equals(Type.LONG_TYPE.getDescriptor())) {
				addSymbolicLong();

			} else if (desc.equals(Type.FLOAT_TYPE.getDescriptor())) {
				addSymbolicFloat();

			} else if (desc.equals(Type.DOUBLE_TYPE.getDescriptor())) {
				addSymbolicDouble();

			} else if (desc.equals(Type.CHAR_TYPE.getDescriptor())) {
				addSymbolicChar();

			} else if (desc.equals(Type.getDescriptor(String.class))) {
				addSymbolicString();

			} else
				throw new IllegalArgumentException(
						"Unsupported Concolic marking for class " + desc);

		}
	}

	private void addSymbolicFloat() {
		final float MIN_VALUE = Float.MIN_VALUE;
		final float MAX_VALUE = Float.MAX_VALUE;
		RealExpression realExpr = env.topFrame().operandStack.popFp32();

		StringExpression strExpr = getStringExpressionFromPreviousFrame();
		String java_variable_name = (String) strExpr.getConcreteValue();

		float conV = ((Double) realExpr.getConcreteValue()).floatValue();

		RealVariable v = RealVariableFactory.buildRealVariable(
				java_variable_name, conV, MIN_VALUE, MAX_VALUE);

		env.topFrame().operandStack.pushFp32(v);
	}

	private void addSymbolicDouble() {
		final double MIN_VALUE = Double.MIN_VALUE;
		final double MAX_VALUE = Double.MAX_VALUE;
		RealExpression realExpr = env.topFrame().operandStack.popFp64();

		StringExpression strExpr = getStringExpressionFromPreviousFrame();
		String java_variable_name = (String) strExpr.getConcreteValue();

		double conV = ((Double) realExpr.getConcreteValue()).doubleValue();

		RealVariable v = RealVariableFactory.buildRealVariable(
				java_variable_name, conV, MIN_VALUE, MAX_VALUE);

		env.topFrame().operandStack.pushFp64(v);
	}

	private void addSymbolicLong() {
		final long MIN_VALUE = Long.MIN_VALUE;
		final long MAX_VALUE = Long.MAX_VALUE;
		IntegerExpression intExpr = env.topFrame().operandStack.popBv64();

		StringExpression strExpr = getStringExpressionFromPreviousFrame();
		String java_variable_name = (String) strExpr.getConcreteValue();

		long conV = ((Long) intExpr.getConcreteValue()).longValue();

		IntegerVariable v = IntegerVariableFactory.buildIntegerVariable(
				java_variable_name, conV, MIN_VALUE, MAX_VALUE);

		env.topFrame().operandStack.pushBv64(v);

	}

	private void addSymbolicInt() {
		final long MIN_VALUE = Integer.MIN_VALUE;
		final long MAX_VALUE = Integer.MAX_VALUE;
		IntegerExpression intExpr = env.topFrame().operandStack.popBv32();

		StringExpression strExpr = getStringExpressionFromPreviousFrame();
		String java_variable_name = (String) strExpr.getConcreteValue();

		int conV = ((Long) intExpr.getConcreteValue()).intValue();

		IntegerVariable v = IntegerVariableFactory.buildIntegerVariable(
				java_variable_name, conV, MIN_VALUE, MAX_VALUE);

		env.topFrame().operandStack.pushBv32(v);

	}

	private void addSymbolicChar() {
		final long MIN_VALUE = Character.MIN_VALUE;
		final long MAX_VALUE = Character.MAX_VALUE;
		IntegerExpression intExpr = env.topFrame().operandStack.popBv32();

		StringExpression strExpr = getStringExpressionFromPreviousFrame();
		String java_variable_name = (String) strExpr.getConcreteValue();

		int conV = ((Long) intExpr.getConcreteValue()).intValue();

		IntegerVariable v = IntegerVariableFactory.buildIntegerVariable(
				java_variable_name, conV, MIN_VALUE, MAX_VALUE);

		env.topFrame().operandStack.pushBv32(v);

	}

	private void addSymbolicString() {
		StringExpression strExpr = env.topFrame().operandStack.popStringRef();

		StringExpression strNameExpr = getStringExpressionFromPreviousFrame();
		String java_variable_name = (String) strNameExpr.getConcreteValue();

		String conV = (String) strExpr.getConcreteValue();

		StringVariable stringVariable = StringVariableFactory
				.buildStringVariable(java_variable_name, conV, conV, conV);

		env.topFrame().operandStack.pushStringRef(stringVariable);
	}

	private void addSymbolicBoolean() {

		final long MIN_VALUE = 0;
		final long MAX_VALUE = 1;
		IntegerExpression booleanExpr = env.topFrame().operandStack.popBv32();
		
		StringExpression strNameExpr = getStringExpressionFromPreviousFrame();
		String java_variable_name = (String) strNameExpr.getConcreteValue();

		IntegerVariable v = IntegerVariableFactory.buildIntegerVariable(
				java_variable_name, (Long) booleanExpr.getConcreteValue(), MIN_VALUE, MAX_VALUE);

		env.topFrame().operandStack.pushBv32(v);
	}

	private void addSymbolicByte() {
		final long MIN_VALUE = Byte.MIN_VALUE;
		final long MAX_VALUE = Byte.MAX_VALUE;
		IntegerExpression intExpr = env.topFrame().operandStack.popBv32();

		StringExpression strExpr = getStringExpressionFromPreviousFrame();
		String java_variable_name = (String) strExpr.getConcreteValue();

		int conV = ((Long) intExpr.getConcreteValue()).intValue();

		IntegerVariable v = IntegerVariableFactory.buildIntegerVariable(
				java_variable_name, conV, MIN_VALUE, MAX_VALUE);

		env.topFrame().operandStack.pushBv32(v);
	}

	private void addSymbolicShort() {
		final long MIN_VALUE = Short.MIN_VALUE;
		final long MAX_VALUE = Short.MAX_VALUE;
		IntegerExpression intExpr = env.topFrame().operandStack.popBv32();

		StringExpression strExpr = getStringExpressionFromPreviousFrame();
		String java_variable_name = (String) strExpr.getConcreteValue();

		int conV = ((Long) intExpr.getConcreteValue()).intValue();

		IntegerVariable v = IntegerVariableFactory.buildIntegerVariable(
				java_variable_name, conV, MIN_VALUE, MAX_VALUE);

		env.topFrame().operandStack.pushBv32(v);

	}

	private StringExpression getStringExpressionFromPreviousFrame() {
		Frame f = env.popFrame();
		StringExpression strRef = env.topFrame().operandStack.peekStringRef();
		env.pushFrame(f);
		return strRef;
	}

}
