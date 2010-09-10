package de.unisb.cs.st.evosuite.testcase;

public class InspectorAssertion extends Assertion {

	//VariableReference value;
	Inspector inspector;
	int num_inspector;
	Object result;
	
	@Override
	public Assertion clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCode() {
		/*
		if(result.getClass().equals(Boolean.class)) {
			if(result)
				return "assertTrue(var"+value.statement+"."+inspector.getMethodCall()+"())";
			else
				return "assertFalse(var"+value.statement+"."+inspector.getMethodCall()+"())";
		} else {
		*/
		return "assertEquals(var"+source.statement+"."+inspector.getMethodCall()+"(), "+result+")";			
		
	}

	@Override
	public boolean evaluate(Scope scope) {
		return inspector.getValue(scope.get(source)).equals(result);
	}
}
