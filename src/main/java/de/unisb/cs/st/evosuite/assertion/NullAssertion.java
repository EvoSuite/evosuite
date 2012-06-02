/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.assertion;

import de.unisb.cs.st.evosuite.testcase.CodeUnderTestException;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.TestCase;

public class NullAssertion extends Assertion {

	private static final long serialVersionUID = 8486987896764253928L;

	@Override
	public Assertion copy(TestCase newTestCase, int offset) {
		NullAssertion s = new NullAssertion();
		s.source = newTestCase.getStatement(source.getStPosition() + offset).getReturnValue();
		s.value = value;
		assert (s.isValid());
		return s;
	}

	@Override
	public boolean evaluate(Scope scope) {
		try {
			if (((Boolean) value).booleanValue()) {
				return source.getObject(scope) == null;
			} else {
				return source.getObject(scope) != null;
			}
		} catch (CodeUnderTestException e) {
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public String getCode() {
		if (((Boolean) value).booleanValue()) {
			return "assertNull(" + source.getName() + ");";
		} else
			return "assertNotNull(" + source.getName() + ");";
	}

}
