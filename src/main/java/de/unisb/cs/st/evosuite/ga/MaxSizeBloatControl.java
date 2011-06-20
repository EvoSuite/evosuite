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

import de.unisb.cs.st.evosuite.Properties;

/**
 * Reject individuals when they exceed a certain size
 * 
 * @author Gordon Fraser
 * 
 */
public class MaxSizeBloatControl implements BloatControlFunction {

	private static final long serialVersionUID = -8241127914702360972L;

	/**
	 * Check whether the chromosome is bigger than the max length constant
	 */
	@Override
	public boolean isTooLong(Chromosome chromosome) {
		return chromosome.size() > Properties.MAX_SIZE;
	}

}
