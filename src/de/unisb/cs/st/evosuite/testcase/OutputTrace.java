package de.unisb.cs.st.evosuite.testcase;

public abstract class OutputTrace {


	public abstract boolean differs(OutputTrace other);
	
	public abstract int numDiffer(OutputTrace other);
	
	public abstract int getAssertions(TestCase test, OutputTrace other);
	
	public abstract boolean isDetectedBy(Assertion assertion); 
	
}
