package de.unisb.cs.st.evosuite.testcase;

public class StringAssertion extends Assertion {

	@Override
	public String getCode() {		
		if(source.isPrimitive() || source.isWrapperType())
			return "assertEquals(\""+value+"\", String.valueOf(var"+source.statement+"))";
		else {
			String escape = ((String)value).replace("\n", "\\n").replace("\"", "\\\"");
			return "assertEquals(\""+escape+"\", var"+source.statement+".toString())";
		}
	}

	@Override
	public Assertion clone() {
		StringAssertion s = new StringAssertion();
		s.source = source.clone();
		s.value  = value;
		return s;
	}
	@Override
	public boolean evaluate(Scope scope) {
		if(source.isPrimitive() || source.isWrapperType())
			return value.toString().equals(String.valueOf(scope.get(source)));
		else
			return value.toString().equals(scope.get(source).toString());
	}

}
