/*
 * Copyright (C) 2010 Saarland University
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
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.assertion;

import org.apache.commons.lang.StringEscapeUtils;

import de.unisb.cs.st.evosuite.testcase.CodeUnderTestException;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.TestCase;

public class PrimitiveAssertion extends Assertion {

	@Override
	public String getCode() {
		if (value == null) {
			return "assertNull(" + source.getName() + ");";
		} else if (value.getClass().equals(Long.class)) {
			String val = value.toString();
			return "assertEquals(" + source.getName() + ", " + val + "L);";
		} else if (value.getClass().equals(Float.class)) {
			String val = value.toString();
			return "assertEquals(" + source.getName() + ", " + val + "F, 0.01F);";
		} else if (value.getClass().equals(Double.class)) {
			String val = value.toString();
			return "assertEquals(" + source.getName() + ", " + val + "D, 0.01D);";
		} else if (value.getClass().equals(Character.class)) {
			String val = StringEscapeUtils.escapeJava(((Character) value).toString());
			return "assertEquals(" + source.getName() + ", '" + val + "');";
		} else if (value.getClass().equals(String.class)) {
			return "assertEquals(" + source.getName() + ", \""
			        + StringEscapeUtils.escapeJava((String) value) + "\");";
		} else if (value.getClass().isEnum()) {
			return "assertEquals(" + source.getName() + ", "
			        + this.source.getSimpleClassName() + "." + value + ");";
		} else
			return "assertEquals(" + source.getName() + ", " + value + ");";
	}

	@Override
	public Assertion copy(TestCase newTestCase, int offset) {
		PrimitiveAssertion s = new PrimitiveAssertion();
		s.source = source.copy(newTestCase, offset);
		s.value = value;
		return s;
	}

	@Override
	public boolean evaluate(Scope scope) {
		try {
			if (value != null)
				return value.equals(source.getObject(scope));
			else
				return source.getObject(scope) == null;
		} catch (CodeUnderTestException e) {
			throw new UnsupportedOperationException();
		}
	}

}
