package org.evosuite.runtime.mock;

/**
 * This interface is used to specify that this class is a "override" mock, ie
 * a class that extends the mocked one, and does mocking by @Override the parent's methods.
 * This type of mocking might not be possible when mocked class is final, no accessable 
 * constructor, etc.
 * 
 * @author arcuri
 *
 */
public interface OverrideMock extends EvoSuiteMock{
}
