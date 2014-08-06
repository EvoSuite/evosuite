package org.evosuite.runtime.mock;

/**
 * This type of mock uses only static methods.
 * On one hand, static methods of the mocked class will be replaced directly.
 * On the other hand, instance methods will be replaced by static ones that take
 * as input the given instance. How to handle change of state in those instances 
 * is up to mock class (eg, reflection or accessors).
 * 
 * <p>
 * This type of mock is particularly useful for singleton classes that cannot be
 * extended (ie no OverrideMock)
 * 
 * 
 * @author arcuri
 *
 */
public interface StaticReplacementMock extends EvoSuiteMock{

	/**
	 * Determine which class this mock is mocking
	 * 
	 * @return a fully qualifying String
	 */
	public String getMockedClassName();
	
}
