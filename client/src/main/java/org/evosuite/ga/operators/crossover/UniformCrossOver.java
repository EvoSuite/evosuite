/**
 * Copyright (C) 2010-2017 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.ga.operators.crossover;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;

/**
 * Implement uniform crossover 
 *
 * @author Yan Ge
 */
public class UniformCrossOver extends CrossOverFunction {

	private static final long serialVersionUID = 2981387570766261795L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void crossOver(Chromosome parent1, Chromosome bestMutant)
			throws ConstructionFailedException {

		if (parent1.size() < 2 || bestMutant.size() < 2) {
			return;
		}

		int parentSize = parent1.size();
		int bestMutantSize = bestMutant.size();

		Chromosome tparent = parent1.clone();
		Chromosome tbestMutant = bestMutant.clone();

		if (parentSize < bestMutantSize) {
			parent1.uniformCrossOver(tbestMutant, true);
		} else {
			bestMutant.uniformCrossOver(tparent, false);
		}
	}
}

