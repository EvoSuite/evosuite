/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
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
/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author fraser
 * 
 */
public class InspectorTraceEntry implements OutputTraceEntry {

	private final Map<Inspector, Object> inspectorMap = new HashMap<Inspector, Object>();

	private final VariableReference var;

	public InspectorTraceEntry(VariableReference var) {
		this.var = var;
	}

	public void addValue(Inspector inspector, Object value) {
		inspectorMap.put(inspector, value);
	}

	public int size() {
		return inspectorMap.size();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#differs(de.unisb.cs.st.evosuite.assertion.OutputTraceEntry)
	 */
	@Override
	public boolean differs(OutputTraceEntry other) {
		if (other instanceof InspectorTraceEntry) {
			if (!((InspectorTraceEntry) other).var.equals(var))
				return false;

			InspectorTraceEntry otherEntry = (InspectorTraceEntry) other;
			for (Inspector inspector : inspectorMap.keySet()) {
				if (!otherEntry.inspectorMap.containsKey(inspector)
				        || otherEntry.inspectorMap.get(inspector) == null
				        || inspectorMap.get(inspector) == null)
					continue;

				if (!otherEntry.inspectorMap.get(inspector).equals(inspectorMap.get(inspector)))
					return true;
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

		if (other instanceof InspectorTraceEntry) {
			InspectorTraceEntry otherEntry = (InspectorTraceEntry) other;
			for (Inspector inspector : inspectorMap.keySet()) {
				if (!otherEntry.inspectorMap.containsKey(inspector)
				        || otherEntry.inspectorMap.get(inspector) == null
				        || inspectorMap.get(inspector) == null)
					continue;

				if (!otherEntry.inspectorMap.get(inspector).equals(inspectorMap.get(inspector))) {
					InspectorAssertion assertion = new InspectorAssertion();
					assertion.value = inspectorMap.get(inspector);
					assertion.inspector = inspector;
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

		for (Inspector inspector : inspectorMap.keySet()) {
			InspectorAssertion assertion = new InspectorAssertion();
			assertion.value = inspectorMap.get(inspector);
			assertion.inspector = inspector;
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
		if (assertion instanceof InspectorAssertion) {
			InspectorAssertion ass = (InspectorAssertion) assertion;
			if (ass.source.equals(var) && inspectorMap.containsKey(ass.inspector)
			        && inspectorMap.get(ass.inspector) != null && ass.value != null)
				return !inspectorMap.get(ass.inspector).equals(ass.value);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#cloneEntry()
	 */
	@Override
	public OutputTraceEntry cloneEntry() {
		InspectorTraceEntry copy = new InspectorTraceEntry(var);
		copy.inspectorMap.putAll(inspectorMap);
		return copy;
	}

}
