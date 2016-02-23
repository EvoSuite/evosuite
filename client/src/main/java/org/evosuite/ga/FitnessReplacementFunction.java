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
package org.evosuite.ga;

/**
 * Replacement function that only looks at the fitness and secondary objectives,
 * without checking any further constraint on the parents
 *
 * @author Gordon Fraser
 */
public class FitnessReplacementFunction extends ReplacementFunction {

	private static final long serialVersionUID = 8492857847521917540L;

	
	/**
	 * <p>Constructor for FitnessReplacementFunction.</p>
	 *
	 * @param maximize a boolean.
	 */
	public FitnessReplacementFunction(boolean maximize) {
		super(maximize);
	}
	
	/**
	 * <p>Constructor for FitnessReplacementFunction.</p>
	 */
	public FitnessReplacementFunction(){
		this(false);
	}
}
