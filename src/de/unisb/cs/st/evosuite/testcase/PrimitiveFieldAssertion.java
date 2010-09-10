package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Field;

public class PrimitiveFieldAssertion extends Assertion {

	Field field;
	
	@Override
	public String getCode() {
		return "assertEquals(var"+source.statement+"."+field.getName()+", "+value+")";
	}

	@Override
	public Assertion clone() {
		PrimitiveFieldAssertion s = new PrimitiveFieldAssertion();
		s.source = source.clone();
		s.value  = value;
		s.field  = field;
		return s;
	}

	@Override
	public boolean evaluate(Scope scope) {
		return scope.get(source).equals(value);
	}

}
