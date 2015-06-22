/**
 * 
 */
package org.evosuite.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

/**
 * <p>
 * JUnitRunner class
 * </p>
 * 
 * @author José Campos
 */
public class JUnitRunner {

	/**
	 * 
	 */
	private List<JUnitResult> testResults;

	/**
	 * 
	 */
	public JUnitRunner() {
		this.testResults = new ArrayList<JUnitResult>();
	}

	public void run(Class<?> junitClass) {
		Request request = Request.aClass(junitClass);

		JUnitCore junit = new JUnitCore();
		junit.addListener(new JUnitRunListener(this));
		junit.run(request);
	}

	/**
	 * 
	 * @param testResult
	 */
	public void addResult(JUnitResult testResult) {
		this.testResults.add(testResult);
	}

	/**
	 * 
	 * @return
	 */
	public List<JUnitResult> getTestResults() {
		return this.testResults;
	}
}
