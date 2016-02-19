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
package org.evosuite.ga.bloatcontrol;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;

/**
 * Reject individuals when they exceed a certain size
 *
 * @author Gordon Fraser
 */
public class MaxSizeBloatControl implements BloatControlFunction {

	private static final long serialVersionUID = -8241127914702360972L;

	/**
	 * {@inheritDoc}
	 *
	 * Check whether the chromosome is bigger than the max length constant
	 */
	@Override
	public boolean isTooLong(Chromosome chromosome) {
		return chromosome.size() > Properties.MAX_SIZE;
	}

}
