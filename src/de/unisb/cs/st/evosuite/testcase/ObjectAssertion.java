package de.unisb.cs.st.evosuite.testcase;

public class ObjectAssertion extends Assertion {

	@Override
	public String getCode() {
		return "assert(var"+source.statement+".equals("+value+")";
	}

	@Override
	public Assertion clone() {
		ObjectAssertion s = new ObjectAssertion();
		s.source = source.clone();
		s.value  = value;
		return s;
	}

	@Override
	public boolean evaluate(Scope scope) {
		return scope.get(source).equals(value);
	}

}
