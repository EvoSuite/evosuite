package org.evosuite.symbolic.expr;

import java.util.HashMap;

public abstract class IntegerVariableFactory {

	private static HashMap<String, IntegerVariable> integerVariables = new HashMap<String, IntegerVariable>();

	public static IntegerVariable buildIntegerVariable(String name, long conV,
			long minValue, long maxValue) {

		IntegerVariable integerVariable;
		if (integerVariables.containsKey(name)) {
			integerVariable = integerVariables.get(name);
			integerVariable.setConcreteValue(conV);
			assert minValue == integerVariable.getMinValue();
			assert maxValue == integerVariable.getMaxValue();
		} else {
			integerVariable = new IntegerVariable(name, conV, minValue,
					maxValue);
			integerVariables.put(name, integerVariable);
		}
		return integerVariable;
	}

	public static void clearFactory() {
		integerVariables.clear();
	}

}
