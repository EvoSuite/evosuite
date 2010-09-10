package de.unisb.cs.st.evosuite.testcase;

public class PrimitiveAssertion extends Assertion {

	@Override
	public String getCode() {
		if(value.getClass().equals(Long.class)) {
			String val = value.toString();
			return "assertEquals(var"+source.statement+", "+val+"L)";
		}
		else
			return "assertEquals(var"+source.statement+", "+value+")";
	}

	@Override
	public Assertion clone() {
		PrimitiveAssertion s = new PrimitiveAssertion();
		s.source = source.clone();
		s.value  = value;
		return s;
	}

	@Override
	public boolean evaluate(Scope scope) {
		return scope.get(source).equals(value);
	}

}
