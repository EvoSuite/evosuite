package de.unisb.cs.st.evosuite.testcase;

public class ExceptionAssertion extends Assertion {

	Throwable exception = null;
	
	int statement = 0;
	
	public ExceptionAssertion(Throwable exception, int statement) {
		this.exception = exception;
		this.statement = statement;
	}

	@Override
	public Assertion clone() {
		return new ExceptionAssertion(exception, statement);
	}

	@Override
	public boolean evaluate(Scope scope) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean evaluate(Throwable t) {
		return (exception.getMessage() != null && !exception.getMessage().equals(t.getMessage())); 
	}
	
	@Override
	public String getCode() {
		return "// Expecting exception: "+exception.getMessage();
	}

}
