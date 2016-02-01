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

import org.apache.commons.lang3.CharUtils;
import org.evosuite.testcase.ImportsTestCodeVisitor;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gordon Fraser, Jose Rojas
 */
public class DefaultNamingStrategy extends AbstractVariableNamingStrategy {

	protected final Map<String, Integer> indices = new HashMap<>();

	public DefaultNamingStrategy(ImportsTestCodeVisitor itv) {
		super(itv);
	}

	@Override
	public String getArrayReferenceName(TestCase testCase, ArrayReference var) {
		String className = var.getSimpleClassName();
		// int num = 0;
		// for (VariableReference otherVar : variableNames.keySet()) {
		// if (!otherVar.equals(var)
		// && otherVar.getVariableClass().equals(var.getVariableClass()))
		// num++;
		// }
		String variableName = className.substring(0, 1).toLowerCase()
				+ className.substring(1) + "Array";
		variableName = variableName.replace(".", "_").replace("[]", "");

		if (!variableNames.containsKey(var)) {
			if (!indices.containsKey(variableName)) {
				indices.put(variableName, 0);
			}

			int index = indices.get(variableName);
			indices.put(variableName, index + 1);

			variableName += index;

			put(var, variableName);
		}
		return variableNames.get(var).name;
	}

	@Override
	public String getVariableName(TestCase testCase, VariableReference var) {
		if (! variableNames.containsKey(var)) {
			String className = var.getSimpleClassName();

			String variableName = className.substring(0, 1).toLowerCase()
					+ className.substring(1);
			if (variableName.contains("[]")) {
				variableName = variableName.replace("[]", "Array");
			}
			variableName = variableName.replace(".", "_");

			if (CharUtils.isAsciiNumeric(variableName.charAt(variableName.length() - 1)))
				variableName += "_";

			if (!indices.containsKey(variableName)) {
				indices.put(variableName, 0);
			}

			int index = indices.get(variableName);
			indices.put(variableName, index + 1);

			variableName += index;

			put(var, variableName);
		}

		return variableNames.get(var).name;
	}

	@Override
	public void reset() {
		super.reset();
		indices.clear();
	}

}
