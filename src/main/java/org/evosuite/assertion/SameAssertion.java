/**
 * 
 */
package org.evosuite.assertion;

import org.evosuite.testcase.Scope;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.VariableReference;

/**
 * @author Gordon Fraser
 * 
 */
public class SameAssertion extends Assertion {

	private static final long serialVersionUID = -8575378209167070678L;

	protected VariableReference dest;

	public VariableReference getDest() {
		return dest;
	}

	public void setDest(VariableReference dest) {
		this.dest = dest;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.Assertion#getCode()
	 */
	@Override
	public String getCode() {
		if (((Boolean) value).booleanValue())
			return "assertSame(" + source.getName() + ", " + dest.getName() + ");";
		else
			return "assertNotSame(" + source.getName() + ", " + dest.getName() + ");";
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.Assertion#copy(org.evosuite.testcase.TestCase, int)
	 */
	@Override
	public Assertion copy(TestCase newTestCase, int offset) {
		SameAssertion s = new SameAssertion();
		s.source = source.copy(newTestCase, offset);
		s.dest = dest.copy(newTestCase, offset);
		s.value = value;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.Assertion#evaluate(org.evosuite.testcase.Scope)
	 */
	@Override
	public boolean evaluate(Scope scope) {
		// TODO Auto-generated method stub
		return false;
	}

}
