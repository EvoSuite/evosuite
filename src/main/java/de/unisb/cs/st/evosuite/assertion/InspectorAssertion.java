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
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class InspectorAssertion extends Assertion {

	// VariableReference value;
	public Inspector inspector;
	public VariableReference inspectorSource;
	public int num_inspector;
	public Object result;

	@Override
	public Assertion copy(TestCase newTestCase, int offset) {
		InspectorAssertion s = new InspectorAssertion();
		s.source = newTestCase.getStatement(source.getStPosition() + offset).getReturnValue();
		s.inspectorSource = newTestCase.getStatement(inspectorSource.getStPosition()
		                                                     + offset).getReturnValue();
		s.inspector = inspector;
		s.num_inspector = num_inspector;
		s.result = result;
		s.value = value;
		return s;

	}

	@Override
	public String getCode() {
		/*
		 * if(result.getClass().equals(Boolean.class)) { if(result) return
		 * "assertTrue(var"+value.statement+"."+inspector.getMethodCall()+"())";
		 * else return
		 * "assertFalse(var"+value.statement+"."+inspector.getMethodCall
		 * ()+"())"; } else {
		 */
		if (result == null) {
			return "assertEquals(" + inspectorSource.getName() + "."
			        + inspector.getMethodCall() + "(), null);";
		} else if (result.getClass().equals(Long.class)) {
			String val = result.toString();
			return "assertEquals(" + inspectorSource.getName() + "."
			        + inspector.getMethodCall() + "(), " + val + "L);";
		} else if (result.getClass().equals(Float.class)) {
			String val = result.toString();
			return "assertEquals(" + inspectorSource.getName() + "."
			        + inspector.getMethodCall() + "(), " + val + "F);";
		} else if (result.getClass().equals(Character.class)) {
			String val = result.toString();
			return "assertEquals(" + inspectorSource.getName() + "."
			        + inspector.getMethodCall() + "(), '" + val + "');";
		} else if (result.getClass().equals(String.class)) {
			return "assertEquals(" + inspectorSource.getName() + "."
			        + inspector.getMethodCall() + "(), \""
			        + StringEscapeUtils.escapeJava((String) result) + "\");";
		} else
			return "assertEquals(" + inspectorSource.getName() + "."
			        + inspector.getMethodCall() + "(), " + result + ");";
	}

	@Override
	public boolean evaluate(Scope scope) {
		try {
			if (inspectorSource.getObject(scope) == null)
				return true; // TODO - true or false?
			else {
				try {
					Object val = inspector.getValue(inspectorSource.getObject(scope));
					if (val == null)
						return val == result;
					else
						return val.equals(result);
				} catch (Exception e) {
					logger.info("* Exception during call to inspector: " + e + ": "
					        + e.getCause());
					return true;
				}
			}
		} catch (CodeUnderTestException e) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
		        + ((inspectorSource == null) ? 0 : inspectorSource.hashCode());
		result = prime * result + ((inspector == null) ? 0 : inspector.hashCode());
		result = prime * result + num_inspector;
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		InspectorAssertion other = (InspectorAssertion) obj;
		if (inspector == null) {
			if (other.inspector != null)
				return false;
		} else if (!inspector.equals(other.inspector))
			return false;
		if (inspectorSource == null) {
			if (other.inspectorSource != null)
				return false;
		} else if (!inspectorSource.equals(other.inspectorSource))
			return false;

		if (num_inspector != other.num_inspector)
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		return true;
	}

}
