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
package org.evosuite.assertion;

import java.util.Set;

/**
 * <p>OutputTraceEntry interface.</p>
 *
 * @author fraser
 */
public interface OutputTraceEntry {

	/**
	 * <p>differs</p>
	 *
	 * @param other a {@link org.evosuite.assertion.OutputTraceEntry} object.
	 * @return a boolean.
	 */
	public boolean differs(OutputTraceEntry other);

	/**
	 * <p>getAssertions</p>
	 *
	 * @param other a {@link org.evosuite.assertion.OutputTraceEntry} object.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Assertion> getAssertions(OutputTraceEntry other);

	/**
	 * <p>getAssertions</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Assertion> getAssertions();

	/**
	 * <p>isDetectedBy</p>
	 *
	 * @param assertion a {@link org.evosuite.assertion.Assertion} object.
	 * @return a boolean.
	 */
	public boolean isDetectedBy(Assertion assertion);

	/**
	 * <p>cloneEntry</p>
	 *
	 * @return a {@link org.evosuite.assertion.OutputTraceEntry} object.
	 */
	public OutputTraceEntry cloneEntry();

}
