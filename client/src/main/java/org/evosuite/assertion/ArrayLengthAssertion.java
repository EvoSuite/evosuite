package org.evosuite.assertion;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;

import java.lang.reflect.Array;

public class ArrayLengthAssertion extends Assertion {

    private static final long serialVersionUID = -3524688649310294677L;

    public int length = 0;

    /** {@inheritDoc} */
    @Override
    public Assertion copy(TestCase newTestCase, int offset) {
        ArrayLengthAssertion s = new ArrayLengthAssertion();
        s.source = source.copy(newTestCase, offset);
        s.value = value;
        s.length = length;
        s.comment = comment;
        return s;
    }

    /** {@inheritDoc} */
    @Override
    public String getCode() {
        return "assertEquals(" + value + ", " + source.getName() + ".length);";
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.Assertion#evaluate(org.evosuite.testcase.Scope)
     */
    @Override
    public boolean evaluate(Scope scope) {
        try {
            if (source.getObject(scope) == null)
                return value == null;
            else
                return Array.getLength(source.getObject(scope)) == length;
        } catch (CodeUnderTestException e) {
            throw new UnsupportedOperationException();
        }
    }
}
