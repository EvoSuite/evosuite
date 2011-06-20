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

import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * Abstract base class of fitness functions
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class FitnessFunction implements Serializable {

	private static final long serialVersionUID = -8876797554111396910L;

	protected static Logger logger = Logger.getLogger(FitnessFunction.class);

	/**
	 * Make sure that the individual gets to know about its fitness
	 * 
	 * @param individual
	 * @param fitness
	 */
	protected abstract void updateIndividual(Chromosome individual, double fitness);

	/**
	 * Calculate and set fitness function #TODO the 'set fitness' part should be
	 * done by some abstract super class of all FitnessFunctions
	 * 
	 * @param individual
	 * @return new fitness
	 */
	public abstract double getFitness(Chromosome individual);

	/**
	 * Normalize a value using Andrea's normalization function
	 * 
	 * @param value
	 * @return
	 */
	public static double normalize(double value) {
		return value / (1.0 + value);
	}
}
