package org.evosuite.assertion;

import org.evosuite.testcase.variable.VariableReference;

import java.util.HashSet;
import java.util.Set;

public class ArrayLengthTraceEntry implements OutputTraceEntry {

    protected VariableReference var;

    protected int length;

    public ArrayLengthTraceEntry(VariableReference var, Object[] value) {
        this.var = var;
        this.length = value.length;
    }

    public ArrayLengthTraceEntry(VariableReference var, int length) {
        this.var = var;
        this.length = length;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#differs(org.evosuite.assertion.OutputTraceEntry)
     */
    @Override
    public boolean differs(OutputTraceEntry other) {
        if (other instanceof ArrayLengthTraceEntry) {
            ArrayLengthTraceEntry otherEntry = (ArrayLengthTraceEntry) other;
            if (length !=  otherEntry.length)
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
        if (other instanceof ArrayLengthTraceEntry) {
            ArrayLengthTraceEntry otherEntry = (ArrayLengthTraceEntry) other;
            if (length != otherEntry.length) {
                ArrayLengthAssertion assertion = new ArrayLengthAssertion();
                assertion.length = length;
                assertion.source = var;
                assertion.value = length;
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
        ArrayLengthAssertion assertion = new ArrayLengthAssertion();
        assertion.source = var;
        assertion.length = length;
        assertion.value = length;
        assertions.add(assertion);
        assert (assertion.isValid());

        return assertions;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#isDetectedBy(org.evosuite.assertion.Assertion)
     */
    @Override
    public boolean isDetectedBy(Assertion assertion) {
        if (assertion instanceof ArrayLengthAssertion) {
            ArrayLengthAssertion ass = (ArrayLengthAssertion) assertion;
            if (var.equals(ass.source)) {
                if (length != ass.length) {
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
        return new ArrayLengthTraceEntry(var, length);
    }

}
