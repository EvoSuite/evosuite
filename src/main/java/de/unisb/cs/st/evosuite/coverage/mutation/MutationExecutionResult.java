/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

/**
 * @author fraser
 * 
 */
public class MutationExecutionResult {

	private int numAssertions = 0;

	private double impact = 0.0;

	boolean hasTimeout = false;

	boolean hasException = false;

	/**
	 * @return the numAssertions
	 */
	public int getNumAssertions() {
		return numAssertions;
	}

	/**
	 * @param numAssertions
	 *            the numAssertions to set
	 */
	public void setNumAssertions(int numAssertions) {
		this.numAssertions = numAssertions;
	}

	/**
	 * @return the impact
	 */
	public double getImpact() {
		return impact;
	}

	/**
	 * @param impact
	 *            the impact to set
	 */
	public void setImpact(double impact) {
		this.impact = impact;
	}

	/**
	 * @return the hasTimeout
	 */
	public boolean hasTimeout() {
		return hasTimeout;
	}

	/**
	 * @param hasTimeout
	 *            the hasTimeout to set
	 */
	public void setHasTimeout(boolean hasTimeout) {
		this.hasTimeout = hasTimeout;
	}

	/**
	 * @return the hasException
	 */
	public boolean hasException() {
		return hasException;
	}

	/**
	 * @param hasTimeout
	 *            the hasTimeout to set
	 */
	public void setHasException(boolean hasException) {
		this.hasException = hasException;
	}
}
