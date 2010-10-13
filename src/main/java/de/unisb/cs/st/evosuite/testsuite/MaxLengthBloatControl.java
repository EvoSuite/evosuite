/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testsuite;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.BloatControlFunction;
import de.unisb.cs.st.evosuite.ga.Chromosome;

/**
 * @author Gordon Fraser
 *
 */
public class MaxLengthBloatControl implements BloatControlFunction {
	
	/**
	 * Maximum number of attempts in generating/adding/mutating things
	 */
	protected int max_length = Integer.parseInt(Properties.getPropertyOrDefault("GA.max_length", "100"));
	
	
	/**
	 * Check whether the chromosome is bigger than the max length constant 
	 */
	public boolean isTooLong(Chromosome chromosome) {
		return ((TestSuiteChromosome)chromosome).length() > max_length;
	}
}
