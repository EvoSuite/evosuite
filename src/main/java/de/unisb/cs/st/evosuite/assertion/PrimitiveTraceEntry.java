/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author fraser
 * 
 */
public class PrimitiveTraceEntry implements OutputTraceEntry {

	private static Logger logger = LoggerFactory.getLogger(PrimitiveTraceEntry.class);

	protected VariableReference var;

	protected Object value;

	public PrimitiveTraceEntry(VariableReference var, Object value) {
		this.var = var;
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#differs(de.unisb.cs.st.evosuite.assertion.OutputTraceEntry)
	 */
	@Override
	public boolean differs(OutputTraceEntry other) {
		if (other instanceof PrimitiveTraceEntry) {
			PrimitiveTraceEntry otherEntry = (PrimitiveTraceEntry) other;
			if (!value.equals(otherEntry.value))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#getAssertion(de.unisb.cs.st.evosuite.assertion.OutputTraceEntry)
	 */
	@Override
	public Set<Assertion> getAssertions(OutputTraceEntry other) {
		Set<Assertion> assertions = new HashSet<Assertion>();
		if (other instanceof PrimitiveTraceEntry) {
			PrimitiveTraceEntry otherEntry = (PrimitiveTraceEntry) other;
			if (otherEntry != null && otherEntry.value != null && value != null
			        && var.equals(otherEntry.var))
				if (!value.equals(otherEntry.value)) {
					PrimitiveAssertion assertion = new PrimitiveAssertion();
					assertion.value = value;
					assertion.source = var;
					assertions.add(assertion);
				}
		}
		return assertions;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#getAssertion()
	 */
	@Override
	public Set<Assertion> getAssertions() {
		Set<Assertion> assertions = new HashSet<Assertion>();
		PrimitiveAssertion assertion = new PrimitiveAssertion();
		assertion.source = var;
		assertion.value = value;
		assertions.add(assertion);
		return assertions;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#isDetectedBy(de.unisb.cs.st.evosuite.assertion.Assertion)
	 */
	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if (assertion instanceof PrimitiveAssertion) {
			PrimitiveAssertion ass = (PrimitiveAssertion) assertion;
			if (var.equals(ass.source) && ass.value != null && value != null)
				return !value.equals(ass.value);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.OutputTraceEntry#cloneEntry()
	 */
	@Override
	public OutputTraceEntry cloneEntry() {
		return new PrimitiveTraceEntry(var, value);
	}

}
