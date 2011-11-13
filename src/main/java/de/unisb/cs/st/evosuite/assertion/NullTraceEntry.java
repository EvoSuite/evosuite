/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author fraser
 * 
 */
public class NullTraceEntry implements OutputTraceEntry {

	private final boolean isNull;

	private final VariableReference var;

	public NullTraceEntry(VariableReference var, boolean result) {
		this.var = var;
		this.isNull = result;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#differs(de.unisb.cs.st.evosuite.assertion.OutputTraceEntry)
	 */
	@Override
	public boolean differs(OutputTraceEntry other) {
		if (other instanceof NullTraceEntry) {
			NullTraceEntry otherEntry = (NullTraceEntry) other;
			if (var.equals(otherEntry.var) && otherEntry.isNull != isNull)
				return true;

		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#getAssertions(de.unisb.cs.st.evosuite.assertion.OutputTraceEntry)
	 */
	@Override
	public Set<Assertion> getAssertions(OutputTraceEntry other) {
		Set<Assertion> assertions = new HashSet<Assertion>();

		if (other instanceof NullTraceEntry) {
			NullTraceEntry otherEntry = (NullTraceEntry) other;
			if (var.equals(otherEntry.var) && otherEntry.isNull != isNull) {
				NullAssertion assertion = new NullAssertion();
				assertion.value = isNull;
				assertion.source = var;
				assertions.add(assertion);
				assert (assertion.isValid());
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
		NullAssertion assertion = new NullAssertion();
		assertion.value = isNull;
		assertion.source = var;
		assertions.add(assertion);
		assert (assertion.value != null);
		assert (assertion.isValid());
		return assertions;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#isDetectedBy(de.unisb.cs.st.evosuite.assertion.Assertion)
	 */
	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if (assertion instanceof NullAssertion) {
			NullAssertion ass = (NullAssertion) assertion;
			if (var.equals(ass.source))
				return ((Boolean) ass.value).booleanValue() != isNull;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#cloneEntry()
	 */
	@Override
	public OutputTraceEntry cloneEntry() {
		return new NullTraceEntry(var, isNull);
	}

}
