package org.evosuite.testcase;

public class ImmutableStringPrimitiveStatement extends StringPrimitiveStatement {

	private static final long serialVersionUID = 4689686677200684012L;

	public ImmutableStringPrimitiveStatement(TestCase tc, String value) {
		super(tc, value);
	}

	@Override
	public boolean mutate(TestCase test, TestFactory factory) {
		return false;
	}
	
	@Override
	public void delta() {
		return;
	}
	
	@Override
	public void increment() {
	}
	
	@Override
	public void negate() {
	}
	
	@Override
	public void randomize() {
	}

	@Override
	public void setValue(String val) {
		// Is immutable - cannot be changed
	}
	
	
}
