package org.evosuite.symbolic.expr;

import java.util.HashMap;

public abstract class StringVariableFactory {

	private static HashMap<String, StringVariable> stringVariables = new HashMap<String, StringVariable>();

	public static StringVariable buildStringVariable(String name,
			String concVal, String minValue, String maxValue) {

		StringVariable stringVariable;
		if (stringVariables.containsKey(name)) {
			stringVariable = stringVariables.get(name);
			stringVariable.setConcreteValue(concVal);
			stringVariable.setMinValue(minValue);
			stringVariable.setMaxValue(maxValue);
		} else {
			stringVariable = new StringVariable(name, concVal, minValue,
					maxValue);
			stringVariables.put(name, stringVariable);
		}
		return stringVariable;
	}

	public static void clearFactory() {
		stringVariables.clear();
	}

}
