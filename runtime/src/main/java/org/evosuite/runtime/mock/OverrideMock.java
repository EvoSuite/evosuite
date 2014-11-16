package org.evosuite.runtime.mock;

/**
 * This interface is used to specify that this class is a "override" mock, ie
 * a class that extends the mocked one, and does mocking by @Override the parent's methods.
 * This type of mocking might not be possible when mocked class is final, no accessible 
 * constructor, etc.
 * 
 * <p>
 * <b>IMPORTANT</b>: each OverrideMock implementation should handle rollback to non-mocked functionality.
 * Such check can be based on {@link MockFramework#isEnabled()}.
 * Automated rollback in the instrumentation itself cannot be done, as we cannot change the signature
 * of an instrumented class based on flag. Eg: "class Foo extends File" will be replaced by "class Foo extends MockFile",
 * and we cannot change it back afterwards.  
 * 
 * @author arcuri
 *
 */
public interface OverrideMock extends EvoSuiteMock{
}
