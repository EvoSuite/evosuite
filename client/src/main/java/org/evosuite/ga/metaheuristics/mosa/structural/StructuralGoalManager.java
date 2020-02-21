/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.ga.metaheuristics.mosa.structural;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

/**
 * 
 * 
 * @author Annibale Panichella
 */
public abstract class StructuralGoalManager<T extends Chromosome> implements Serializable {

	private static final long serialVersionUID = -2577487057354286024L;

	/** Set of goals currently used as objectives **/
	protected Set<FitnessFunction<T>> currentGoals;

	/** Archive of tests and corresponding covered targets*/
	protected Archive archive;

	protected StructuralGoalManager(List<FitnessFunction<T>> fitnessFunctions){
		currentGoals = new HashSet<FitnessFunction<T>>(fitnessFunctions.size());
		archive = Archive.getArchiveInstance();

		// initialize uncovered goals
		this.archive.addTargets(fitnessFunctions);
	}

	/**
	 * Update the set of covered goals and the set of current goals (actual objectives)
	 * @param c a TestChromosome
	 * @return covered goals along with the corresponding test case
	 */
	public abstract void calculateFitness(T c, GeneticAlgorithm ga);

	public Set<FitnessFunction<T>> getUncoveredGoals() {
		return this.archive.getUncoveredTargets();
	}

	public Set<FitnessFunction<T>> getCurrentGoals() {
		return currentGoals;
	}

	public Set<FitnessFunction<T>> getCoveredGoals() {
		return this.archive.getCoveredTargets();
	}

	protected boolean isAlreadyCovered(FitnessFunction<T> target){
		return this.archive.getCoveredTargets().contains(target);
	}

	protected void updateCoveredGoals(FitnessFunction<T> f, T tc) {
		// the next two lines are needed since that coverage information are used
		// during EvoSuite post-processing
		TestChromosome tch = (TestChromosome) tc;
		tch.getTestCase().getCoveredGoals().add((TestFitnessFunction) f);

		// update covered targets
		this.archive.updateArchive((TestFitnessFunction) f, (TestChromosome) tc, tc.getFitness(f));
	}
}
