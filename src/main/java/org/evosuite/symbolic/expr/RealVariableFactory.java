package org.evosuite.symbolic.expr;

import java.util.HashMap;

public abstract class RealVariableFactory {

	private static HashMap<String, RealVariable> realVariables = new HashMap<String, RealVariable>();

	public static RealVariable buildRealVariable(String name, double conV,
			double minValue, double maxValue) {

		RealVariable realVariable;
		if (realVariables.containsKey(name)) {
			realVariable = realVariables.get(name);
			realVariable.setConcreteValue(conV);
			assert minValue == realVariable.getMinValue();
			assert maxValue == realVariable.getMaxValue();
		} else {
			realVariable = new RealVariable(name, conV, minValue, maxValue);
			realVariables.put(name, realVariable);
		}
		return realVariable;
	}

	public static void clearFactory() {
		realVariables.clear();
	}

}
