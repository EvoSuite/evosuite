/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.idNaming;

import org.evosuite.testcase.ImportsTestCodeVisitor;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericField;

import java.util.List;

/**
 * @author Jose Rojas
 */
public abstract class AbstractVariableNamingStrategy implements VariableNamingStrategy {

	protected final ImportsTestCodeVisitor itv;

	public AbstractVariableNamingStrategy(ImportsTestCodeVisitor itv) {
		this.itv = itv;
	}

	String getConstantName(ConstantValue var) {
		ConstantValue cval = var;
		if(cval.getValue() != null && cval.getVariableClass().equals(Class.class)) {
			return this.itv.getClassNames().get((Class<?>)cval.getValue())+".class";
		}
		return var.getName();
	}

	String getFieldReferenceName(TestCase testCase, FieldReference var) {
		VariableReference source = var.getSource();
		GenericField field = var.getField();
		if (source != null)
			return getVariableName(testCase, source) + "." + field.getName();
		else
			return this.itv.getClassNames().get(field.getField().getDeclaringClass()) + "."
					+ field.getName();
	}

	String getArrayIndexName(TestCase testCase, ArrayIndex var) {
		VariableReference array = var.getArray();
		List<Integer> indices = var.getArrayIndices();
		String result = getVariableName(testCase, array);
		for (Integer index : indices) {
			result += "[" + index + "]";
		}
		return result;
	}
}
