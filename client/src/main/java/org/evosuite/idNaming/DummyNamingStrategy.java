/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p/>
 * This file is part of EvoSuite.
 * <p/>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 * <p/>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.idNaming;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jose Rojas
 */
public class DummyNamingStrategy implements VariableNamingStrategy {

	private static DummyNamingStrategy instance = null;

	private Map<TestCase, Map<VariableReference,String>> varNames = new HashMap<>();
	private Map<TestCase, Integer> varIndexes = new HashMap<>();

	/**
	 * Getter for the field {@code instance}
	 *
	 * @return a {@link org.evosuite.idNaming.ExplanatoryNamingStrategy}
	 *         object.
	 */
	public static synchronized DummyNamingStrategy getInstance() {
		if (instance == null)
			instance = new DummyNamingStrategy();
		return instance;
	}


	@Override
	public String getVariableName(TestCase testCase, VariableReference variableReference) {
		Map<VariableReference,String> variableNames = varNames.get(testCase);
		if (variableNames == null) {
			variableNames = new HashMap<>();
			varNames.put(testCase, variableNames);
			varIndexes.put(testCase, 0);
		}
		String name = variableNames.get(variableReference);
		if (name == null) {
			name = getNextVariableName(testCase);
			variableNames.put(variableReference, name);
			varNames.put(testCase, variableNames);
		}
		return name;
	}

	private String getNextVariableName(TestCase testCase) {
		int index = varIndexes.get(testCase);
		varIndexes.put(testCase, index + 1);
		return "var" + index;

	}
}
