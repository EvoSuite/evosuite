package org.evosuite.junit;

import java.util.ArrayList;
import java.util.List;

/**
 * The information from executing a JUnit test case needed
 * by the JUnitAnalyzer
 * 
 * @author galeotti
 *
 */
public class JUnitResult {

	public JUnitResult(boolean wasSuccessful, int failureCount, int runCount) {
		super();
		this.wasSuccessful = wasSuccessful;
		this.failureCount = failureCount;
		this.runCount = runCount;
	}

	private final boolean wasSuccessful;
	private final int failureCount;
	private final int runCount;
	private final ArrayList<JUnitFailure> junitFailures = new ArrayList<JUnitFailure>();
	
	public void addFailure(JUnitFailure junitFailure) {
		junitFailures.add(junitFailure);
	}

	public List<JUnitFailure> getFailures() {
		return junitFailures;
	}

	public boolean wasSuccessful() {
		return this.wasSuccessful;
	}

	public int getFailureCount() {
		return this.failureCount;
	}

	public int getRunCount() {
		return runCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + failureCount;
		result = prime * result
				+ ((junitFailures == null) ? 0 : junitFailures.hashCode());
		result = prime * result + runCount;
		result = prime * result + (wasSuccessful ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JUnitResult other = (JUnitResult) obj;
		if (failureCount != other.failureCount)
			return false;
		if (junitFailures == null) {
			if (other.junitFailures != null)
				return false;
		} else if (!junitFailures.equals(other.junitFailures))
			return false;
		if (runCount != other.runCount)
			return false;
		if (wasSuccessful != other.wasSuccessful)
			return false;
		return true;
	}

}
