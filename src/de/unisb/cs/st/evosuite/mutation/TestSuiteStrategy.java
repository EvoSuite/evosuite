/**
 * 
 */
package de.unisb.cs.st.evosuite.mutation;

import java.util.List;

import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteGenerator;

/**
 * @author Gordon Fraser
 *
 */
public class TestSuiteStrategy extends TestGenerationStrategy {

	private TestSuiteGenerator generator = new TestSuiteGenerator();
	
	@Override
	public void generateTests() {
		generator.generateTestSuite();
	}

	@Override
	public List<TestCase> getFailedTests() {
		return generator.getFailedTests();
	}

	@Override
	public List<TestCase> getTests() {
		return generator.getTests();
	}

	@Override
	public void writeTestSuite(String filename, String directory) {
		// TODO Auto-generated method stub

	}

}
