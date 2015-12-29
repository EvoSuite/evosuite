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

import org.evosuite.parameterize.InputVariable;
import org.evosuite.testcase.ImportsTestCodeVisitor;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jose Rojas
 */
public class DummyNamingStrategy extends AbstractVariableNamingStrategy {

	private Map<TestCase, Map<VariableReference,String>> varNames = new HashMap<>();
	private Map<TestCase, Integer> varIndexes = new HashMap<>();

	private final static String PREFFIX = "var";

	public DummyNamingStrategy(ImportsTestCodeVisitor itv) {
		super(itv);
	}

	@Override
	public String getVariableName(TestCase testCase, VariableReference var) {
		if (var instanceof ConstantValue) {
			return getConstantName((ConstantValue)var);
		} else if (var instanceof InputVariable) {
			return var.getName();
		} else if (var instanceof FieldReference) {
			return getFieldReferenceName(testCase, (FieldReference)var);
		} else if (var instanceof ArrayIndex) {
			return getArrayIndexName(testCase, (ArrayIndex)var);
		} else {
			Map<VariableReference, String> variableNames = varNames.get(testCase);
			if (variableNames == null) {
				variableNames = new HashMap<>();
				varNames.put(testCase, variableNames);
				varIndexes.put(testCase, 0);
			}
			String name = variableNames.get(var);
			if (name == null) {
				name = getNextVariableName(testCase);
				variableNames.put(var, name);
				varNames.put(testCase, variableNames);
			}
			return name;
		}
	}

	private String getNextVariableName(TestCase testCase) {
		int index = varIndexes.get(testCase);
		varIndexes.put(testCase, index + 1);
		return PREFFIX + index;

	}
}
