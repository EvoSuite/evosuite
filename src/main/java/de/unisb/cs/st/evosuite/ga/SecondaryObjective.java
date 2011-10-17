/*
 * Copyright (C) 2010 Saarland University
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
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.ga;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class SecondaryObjective implements Serializable {

	private static final long serialVersionUID = -4117187516650844086L;

	protected static Logger logger = LoggerFactory.getLogger(SecondaryObjective.class);

	/**
	 * Compare two chromosomes with each other with respect to this objective.
	 * This comparison is mainly used during ranking.
	 * 
	 * @param chromosome1
	 * @param chromosome2
	 * @return
	 */
	public abstract int compareChromosomes(Chromosome chromosome1, Chromosome chromosome2);

	/**
	 * Compare two parents to decide whether the children can replace the
	 * parents.
	 * 
	 * @param parent1
	 * @param parent2
	 * @param child1
	 * @param child2
	 * @return
	 */
	public abstract int compareGenerations(Chromosome parent1, Chromosome parent2,
	        Chromosome child1, Chromosome child2);

}
