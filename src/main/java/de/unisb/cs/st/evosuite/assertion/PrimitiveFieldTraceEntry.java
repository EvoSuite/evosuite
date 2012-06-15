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
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author fraser
 * 
 */
public class PrimitiveFieldTraceEntry implements OutputTraceEntry {

	private final Map<Field, Object> fieldMap = new HashMap<Field, Object>();

	private final VariableReference var;

	public PrimitiveFieldTraceEntry(VariableReference var) {
		this.var = var;
	}

	/**
	 * Insert a new value into the map
	 * 
	 * @param field
	 * @param value
	 */
	public void addValue(Field field, Object value) {
		fieldMap.put(field, value);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#differs(de.unisb.cs.st.evosuite.assertion.OutputTraceEntry)
	 */
	@Override
	public boolean differs(OutputTraceEntry other) {
		if (other instanceof PrimitiveFieldTraceEntry) {
			PrimitiveFieldTraceEntry otherEntry = (PrimitiveFieldTraceEntry) other;
			for (Field field : fieldMap.keySet()) {
				if (otherEntry.fieldMap.containsKey(field)) {
					Object o1 = fieldMap.get(field);
					Object o2 = otherEntry.fieldMap.get(field);
					if (o1 == null)
						return o2 != null;
					else
						return !o1.equals(o2);
				}
			}

		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#getAssertions(de.unisb.cs.st.evosuite.assertion.OutputTraceEntry)
	 */
	@Override
	public Set<Assertion> getAssertions(OutputTraceEntry other) {
		Set<Assertion> assertions = new HashSet<Assertion>();

		if (other instanceof PrimitiveFieldTraceEntry) {
			PrimitiveFieldTraceEntry otherEntry = (PrimitiveFieldTraceEntry) other;
			for (Field field : fieldMap.keySet()) {
				if (!otherEntry.fieldMap.get(field).equals(fieldMap.get(field))) {
					PrimitiveFieldAssertion assertion = new PrimitiveFieldAssertion();
					assertion.value = fieldMap.get(field);
					assertion.field = field;
					assertion.source = var;
					assertions.add(assertion);
					assert (assertion.isValid());

				}
			}

		}
		return assertions;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#getAssertions()
	 */
	@Override
	public Set<Assertion> getAssertions() {
		Set<Assertion> assertions = new HashSet<Assertion>();
		for (Field field : fieldMap.keySet()) {
			PrimitiveFieldAssertion assertion = new PrimitiveFieldAssertion();
			assertion.value = fieldMap.get(field);
			assertion.field = field;
			assertion.source = var;
			assertions.add(assertion);
			assert (assertion.isValid());
		}
		return assertions;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#isDetectedBy(de.unisb.cs.st.evosuite.assertion.Assertion)
	 */
	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if (assertion instanceof PrimitiveFieldAssertion) {
			PrimitiveFieldAssertion ass = (PrimitiveFieldAssertion) assertion;
			if (ass.source.equals(var))
				return !fieldMap.get(ass.field).equals(ass.value);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#cloneEntry()
	 */
	@Override
	public OutputTraceEntry cloneEntry() {
		PrimitiveFieldTraceEntry copy = new PrimitiveFieldTraceEntry(var);
		copy.fieldMap.putAll(fieldMap);
		return copy;
	}

}
