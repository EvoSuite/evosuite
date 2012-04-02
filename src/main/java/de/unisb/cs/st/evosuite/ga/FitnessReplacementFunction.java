/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * GA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * GA. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.ga;

/**
 * Replacement function that only looks at the fitness and secondary objectives,
 * without checking any further constraint on the parents
 * 
 * @author Gordon Fraser
 * 
 */
public class FitnessReplacementFunction extends ReplacementFunction {

	private static final long serialVersionUID = 8492857847521917540L;

	
	public FitnessReplacementFunction(boolean maximize) {
		super(maximize);
	}
	
	public FitnessReplacementFunction(){
		this(false);
	}
}
