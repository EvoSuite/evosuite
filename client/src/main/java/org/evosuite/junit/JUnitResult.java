package org.evosuite.junit;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.testcase.ExecutionTrace;

/**
 * The information from executing a JUnit test case
 * 
 * @author galeotti
 * @author José Campos
 */
public class JUnitResult {

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private boolean successful;

    /**
     * 
     */
    private long runtime;

    /**
     * 
     */
    private String trace;

    /**
     * 
     */
    private ExecutionTrace executionTrace;

    /**
     * 
     */
    private int failureCount;

    /**
     * 
     */
    private int runCount;

    /**
     * 
     */
    private ArrayList<JUnitFailure> junitFailures = new ArrayList<JUnitFailure>();

    /**
     * 
     */
    public JUnitResult(String name) {
        this.successful = true;
        this.name = name;
        this.failureCount = 0;
        this.runCount = 0;
    }

    /**
     * 
     * @param wasSuccessful
     * @param failureCount
     * @param runCount
     */
	public JUnitResult(boolean wasSuccessful, int failureCount, int runCount) {
	    this.successful = wasSuccessful;
	    this.failureCount = failureCount;
	    this.runCount = runCount;
	}

	/**
     * 
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * 
     * @param n
     */
    public void setName(String n) {
        this.name = n;
    }

    /**
     * 
     * @return
     */
    public boolean wasSuccessful() {
        return this.successful;
    }

    /**
     * 
     * @param s
     */
    public void setSuccessful(boolean s) {
        this.successful = s;
    }

    /**
     * 
     * @return
     */
    public long getRuntime() {
        return this.runtime;
    }

    /**
     * 
     * @param r
     */
    public void setRuntime(long r) {
        this.runtime = r;
    }

    /**
     * 
     * @return
     */
    public String getTrace() {
        return this.trace;
    }

    /**
     * 
     * @param t
     */
    public void setTrace(String t) {
        this.trace = t;
    }

    /**
     * 
     * @return
     */
    public ExecutionTrace getExecutionTrace() {
        return this.executionTrace;
    }

    /**
     * 
     * @param et
     */
    public void setExecutionTrace(ExecutionTrace et) {
        this.executionTrace = et;
    }

    /**
     * 
     * @return
     */
    public int getFailureCount() {
        return this.failureCount;
    }

    public void incrementFailureCount() {
        this.failureCount++;
    }

    /**
     * 
     * @return
     */
    public int getRunCount() {
        return runCount;
    }

    public void incrementRunCount() {
        this.runCount++;
    }

    /**
     * 
     * @return
     */
    public List<JUnitFailure> getFailures() {
        return junitFailures;
    }

    /**
     * 
     * @param junitFailure
     */
    public void addFailure(JUnitFailure junitFailure) {
        junitFailures.add(junitFailure);
    }

    /**
     * 
     */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.failureCount;
		result = prime * result
				+ ((this.junitFailures == null) ? 0 : this.junitFailures.hashCode());
		result = prime * result + this.runCount;
		result = prime * result + (this.successful ? 1231 : 1237);
		return result;
	}

	/**
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JUnitResult other = (JUnitResult) obj;
		if (this.failureCount != other.failureCount)
			return false;
		if (this.junitFailures == null) {
			if (other.junitFailures != null)
				return false;
		} else if (!this.junitFailures.equals(other.junitFailures))
			return false;
		if (this.runCount != other.runCount)
			return false;
		if (this.successful != other.successful)
			return false;
		return true;
	}
}
