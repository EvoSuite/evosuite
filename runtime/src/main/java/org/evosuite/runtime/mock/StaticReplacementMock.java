package org.evosuite.runtime.mock;

/**
 * This type of mock uses only static methods.
 * On one hand, static methods of the mocked class will be replaced directly.
 * On the other hand, instance methods will be replaced by static ones that take
 * as input the given instance. How to handle change of state in those instances 
 * is up to the mock classes (eg, reflection or public accessors).
 * Furthermore, in case of public constructors, the mock should provide a static
 * method with same parameters, name equal to the constructor (ie the mocked class)
 * and return a new instance of the mocked class, eg:
 *
 * <p>
 *     {@code public Foo(int x){...}}
 * <p>
 *     should be mocked into:
 * <p>
 *     {@code public static Foo Foo(int x){...}}
 *
 * <p>
 * This type of mock is particularly useful for singleton classes that cannot be
 * extended (ie no OverrideMock), final classes, no public constructor, etc.
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
