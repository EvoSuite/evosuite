package de.unisb.cs.st.evosuite.testcase;

public class NullAssertion extends Assertion {

	@Override
	public Assertion clone() {
		NullAssertion s = new NullAssertion();
		s.source = source.clone();
		s.value  = value;
		return s;
	}

	@Override
	public boolean evaluate(Scope scope) {
		if(((Boolean)value).booleanValue()) {
			return scope.get(source) == null;
		}
		else
			return scope.get(source) != null;
	}

	@Override
	public String getCode() {
		if(((Boolean)value).booleanValue()) {
			return "assertNull(var"+source.statement+")";
		}
		else
			return "assertNotNull(var"+source.statement+")";
	}

}
