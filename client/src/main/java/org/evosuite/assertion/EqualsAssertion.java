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
/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.assertion;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
public class EqualsAssertion extends Assertion {

	private static final long serialVersionUID = 1427358542327670617L;

	protected VariableReference dest;

	/**
	 * <p>Getter for the field <code>dest</code>.</p>
	 *
	 * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
	 */
	public VariableReference getDest() {
		return dest;
	}

	/**
	 * <p>Setter for the field <code>dest</code>.</p>
	 *
	 * @param dest a {@link org.evosuite.testcase.variable.VariableReference} object.
	 */
	public void setDest(VariableReference dest) {
		this.dest = dest;
	}

	/** {@inheritDoc} */
	@Override
	public Assertion copy(TestCase newTestCase, int offset) {
		EqualsAssertion s = new EqualsAssertion();
		s.source = source.copy(newTestCase, offset);
		s.dest = dest.copy(newTestCase, offset);
		s.value = value;
		s.killedMutants.addAll(killedMutants);
		return s;
	}

	/** {@inheritDoc} */
	@Override
	public String getCode() {
		if (source.isPrimitive() && dest.isPrimitive()) {
			if (((Boolean) value).booleanValue())
				return "assertTrue(" + source.getName() + " == " + dest.getName() + ");";
			else
				return "assertFalse(" + source.getName() + " == " + dest.getName() + ");";
		} else {
			if (((Boolean) value).booleanValue())
				return "assertTrue(" + source.getName() + ".equals((java.lang.Object)" + dest.getName()
				        + "));";
			else
				return "assertFalse(" + source.getName() + ".equals((java.lang.Object)" + dest.getName()
				        + "));";
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean evaluate(Scope scope) {
		try {
			if (((Boolean) value).booleanValue()) {
				if (source.getObject(scope) == null)
					return dest.getObject(scope) == null;
				else
					return ComparisonTraceEntry.equals(source.getObject(scope), dest.getObject(scope));
			} else {
				if (source.getObject(scope) == null)
					return dest.getObject(scope) != null;
				else
					return !ComparisonTraceEntry.equals(source.getObject(scope), dest.getObject(scope));
			}
		} catch (CodeUnderTestException e) {
			throw new UnsupportedOperationException();
		}
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dest == null) ? 0 : dest.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EqualsAssertion other = (EqualsAssertion) obj;
		if (dest == null) {
			if (other.dest != null)
				return false;
		} else if (!dest.equals(other.dest))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.assertion.Assertion#getReferencedVariables()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<VariableReference> getReferencedVariables() {
		Set<VariableReference> vars = new HashSet<VariableReference>();
		vars.add(source);
		vars.add(dest);
		return vars;
	}
}
