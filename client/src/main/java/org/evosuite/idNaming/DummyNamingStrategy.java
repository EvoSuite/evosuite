/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.idNaming;

import org.evosuite.testcase.ImportsTestCodeVisitor;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;

/**
 * @author Jose Rojas
 */
public class DummyNamingStrategy extends AbstractVariableNamingStrategy {

	private int index = 0;

	private final static String PREFIX = "var";

	public DummyNamingStrategy(ImportsTestCodeVisitor itv) {
		super(itv);
	}

	@Override
	public String getArrayReferenceName(TestCase testCase, ArrayReference var) {
		return getVariableName(testCase, var);
	}

	@Override
	public String getVariableName(TestCase testCase, VariableReference var) {
		VariableNamePair namePair = variableNames.get(var);
		String name;
		if (namePair == null) {
			name = getNextVariableName();
			put(var, name);
		} else
			name = namePair.name;
		return name;
	}

	private String getNextVariableName() {
		return PREFIX + index++;
	}

	@Override
	public void reset() {
		super.reset();
		index = 0;
	}
}
