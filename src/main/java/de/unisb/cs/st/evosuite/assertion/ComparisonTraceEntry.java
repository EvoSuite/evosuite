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
public class ComparisonTraceEntry implements OutputTraceEntry {

	private final VariableReference var;

	private final Map<VariableReference, Boolean> equalityMap = new HashMap<VariableReference, Boolean>();

	public ComparisonTraceEntry(VariableReference var) {
		this.var = var;
	}

	public void addEntry(VariableReference other, boolean value) {
		equalityMap.put(other, value);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#differs(de.unisb.cs.st.evosuite.assertion.OutputTraceEntry)
	 */
	@Override
	public boolean differs(OutputTraceEntry other) {
		if (other instanceof ComparisonTraceEntry) {
			if (!((ComparisonTraceEntry) other).var.equals(var))
				return false;

			ComparisonTraceEntry otherEntry = (ComparisonTraceEntry) other;
			for (VariableReference otherVar : equalityMap.keySet()) {
				if (!otherEntry.equalityMap.containsKey(otherVar))
					continue;

				if (!otherEntry.equalityMap.get(otherVar).equals(equalityMap.get(otherVar)))
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

		if (other instanceof ComparisonTraceEntry) {
			ComparisonTraceEntry otherEntry = (ComparisonTraceEntry) other;
			for (VariableReference otherVar : equalityMap.keySet()) {
				if (!otherEntry.equalityMap.containsKey(otherVar))
					continue;

				if (otherVar == null)
					continue;

				if (!otherEntry.equalityMap.get(otherVar).equals(equalityMap.get(otherVar))) {
					EqualsAssertion assertion = new EqualsAssertion();
					assertion.source = var;
					assertion.dest = otherVar;
					assertion.value = equalityMap.get(otherVar);
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

		for (VariableReference otherVar : equalityMap.keySet()) {
			if (otherVar == null)
				continue;

			EqualsAssertion assertion = new EqualsAssertion();
			assertion.source = var;
			assertion.dest = otherVar;
			assertion.value = equalityMap.get(otherVar);
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
		if (assertion instanceof EqualsAssertion) {
			EqualsAssertion ass = (EqualsAssertion) assertion;
			if (ass.source.equals(var) && equalityMap.containsKey(ass.dest))
				return !equalityMap.get(ass.dest).equals(ass.value);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#cloneEntry()
	 */
	@Override
	public OutputTraceEntry cloneEntry() {
		ComparisonTraceEntry copy = new ComparisonTraceEntry(var);
		copy.equalityMap.putAll(equalityMap);
		return copy;
	}

}
