/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.coverage.mutation;

/**
 * <p>MutationExecutionResult class.</p>
 *
 * @author fraser
 */
public class MutationExecutionResult {

	private int numAssertions = 0;

	private double impact = 0.0;

	boolean hasTimeout = false;

	boolean hasException = false;

	/**
	 * <p>Getter for the field <code>numAssertions</code>.</p>
	 *
	 * @return the numAssertions
	 */
	public int getNumAssertions() {
		return numAssertions;
	}

	/**
	 * <p>Setter for the field <code>numAssertions</code>.</p>
	 *
	 * @param numAssertions
	 *            the numAssertions to set
	 */
	public void setNumAssertions(int numAssertions) {
		this.numAssertions = numAssertions;
	}

	/**
	 * <p>Getter for the field <code>impact</code>.</p>
	 *
	 * @return the impact
	 */
	public double getImpact() {
		return impact;
	}

	/**
	 * <p>Setter for the field <code>impact</code>.</p>
	 *
	 * @param impact
	 *            the impact to set
	 */
	public void setImpact(double impact) {
		this.impact = impact;
	}

	/**
	 * <p>hasTimeout</p>
	 *
	 * @return the hasTimeout
	 */
	public boolean hasTimeout() {
		return hasTimeout;
	}

	/**
	 * <p>Setter for the field <code>hasTimeout</code>.</p>
	 *
	 * @param hasTimeout
	 *            the hasTimeout to set
	 */
	public void setHasTimeout(boolean hasTimeout) {
		this.hasTimeout = hasTimeout;
	}

	/**
	 * <p>hasException</p>
	 *
	 * @return the hasException
	 */
	public boolean hasException() {
		return hasException;
	}

	/**
	 * <p>Setter for the field <code>hasException</code>.</p>
	 *
	 * @param hasException a boolean.
	 */
	public void setHasException(boolean hasException) {
		this.hasException = hasException;
	}
}
