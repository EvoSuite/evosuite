/**
 * 
 */
package de.unisb.cs.st.evosuite.junit;

import java.util.List;

/**
 * @author fraser
 * 
 */
public interface UnitTestAdapter {

	/**
	 * Get all the framework dependent imports
	 * 
	 * @return
	 */
	public String getImports();

	/**
	 * Get the framework specific definition of the test class
	 * 
	 * @param testName
	 * @return
	 */
	public String getClassDefinition(String testName);

	/**
	 * Get the framework specific definition of a test method
	 * 
	 * @param testName
	 * @return
	 */
	public String getMethodDefinition(String testName);

	/**
	 * Get the class definition of a test suite
	 * 
	 * @param tests
	 * @return
	 */
	public String getSuite(List<String> tests);
}
