/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with GA.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.ga;

/**
 * Abstract base class of replacement function for 1+1EA
 * 
 * @author Gordon Fraser
 *
 */
public abstract class OnePlusOneEAReplacementFunction {
	
	protected boolean maximize = false;
	
	public OnePlusOneEAReplacementFunction(SelectionFunction selection_function) {
		this.maximize = selection_function.maximize;
	}
	
	protected boolean isBetter(Chromosome chromosome1, Chromosome chromosome2) {
		if(maximize) {
			return chromosome1.compareTo(chromosome2) > 0;
		} else {
			return chromosome1.compareTo(chromosome2) < 0;			
		}
		
	}
	
	public abstract boolean keepOffspring(Chromosome parent, Chromosome offspring);
}
