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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cross over two individuals
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class CrossOverFunction implements Serializable {

	private static final long serialVersionUID = -4765602400132319324L;

	protected static Logger logger = LoggerFactory.getLogger(CrossOverFunction.class);

	/**
	 * Replace parents with crossed over individuals
	 * 
	 * @param parent1
	 * @param parent2
	 * @throws ConstructionFailedException
	 */
	public abstract void crossOver(Chromosome parent1, Chromosome parent2)
	        throws ConstructionFailedException;

}
