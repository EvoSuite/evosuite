/**
 * 
 */
package org.evosuite.assertion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.evosuite.testcase.VariableReference;

/**
 * @author Gordon Fraser
 * 
 */
public class ArrayTraceEntry implements OutputTraceEntry {

	protected VariableReference var;

	protected Object[] value;

	public ArrayTraceEntry(VariableReference var, Object[] value) {
		this.var = var;
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.OutputTraceEntry#differs(org.evosuite.assertion.OutputTraceEntry)
	 */
	@Override
	public boolean differs(OutputTraceEntry other) {
		if (other instanceof ArrayTraceEntry) {
			ArrayTraceEntry otherEntry = (ArrayTraceEntry) other;
			if (!Arrays.equals(value, otherEntry.value))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.OutputTraceEntry#getAssertions(org.evosuite.assertion.OutputTraceEntry)
	 */
	@Override
	public Set<Assertion> getAssertions(OutputTraceEntry other) {
		Set<Assertion> assertions = new HashSet<Assertion>();
		if (other instanceof ArrayTraceEntry) {
			ArrayTraceEntry otherEntry = (ArrayTraceEntry) other;
			if (!Arrays.equals(value, otherEntry.value)) {
				ArrayEqualsAssertion assertion = new ArrayEqualsAssertion();
				assertion.value = value;
				assertion.source = var;
				assertions.add(assertion);
				assert (assertion.isValid());
			}
		}
		return assertions;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.OutputTraceEntry#getAssertions()
	 */
	@Override
	public Set<Assertion> getAssertions() {
		Set<Assertion> assertions = new HashSet<Assertion>();
		ArrayEqualsAssertion assertion = new ArrayEqualsAssertion();
		assertion.source = var;
		assertion.value = value;
		assertions.add(assertion);
		assert (assertion.isValid());

		return assertions;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.OutputTraceEntry#isDetectedBy(org.evosuite.assertion.Assertion)
	 */
	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if (assertion instanceof ArrayEqualsAssertion) {
			ArrayEqualsAssertion ass = (ArrayEqualsAssertion) assertion;
			if (var.equals(ass.source)) {
				if (!Arrays.equals(value, (Object[]) ass.value)) {
					return true;
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.OutputTraceEntry#cloneEntry()
	 */
	@Override
	public OutputTraceEntry cloneEntry() {
		return new ArrayTraceEntry(var, Arrays.copyOf(value, value.length));
	}

}
