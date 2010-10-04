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

import java.util.List;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.SearchListener;


/**
 * @author Gordon Fraser
 *
 */
public class BestChromosomeTracker implements SearchListener {

	/** The actual value of the best chromosome */
	private TestSuiteChromosome best = null;
	
	/** Singleton instance */
	private static BestChromosomeTracker instance = null;
	
	/**
	 * Private constructor because this is a singleton
	 */
	private BestChromosomeTracker() {
		
	}
	
	/**
	 * Singleton accessor
	 * @return
	 */
	public static BestChromosomeTracker getInstance() {
		if(instance == null)
			instance = new BestChromosomeTracker();
		
		return instance;
	}
	
	/**
	 * The current best individual
	 * @return
	 */
	public TestSuiteChromosome getBest() {
		return best;
	}

	/** 
	 * Simply remember the current best chromosome
	 */
	public void iteration(List<Chromosome> population) {
		this.best = (TestSuiteChromosome) population.get(0);
	}

	public void searchFinished(List<Chromosome> best) { }

	public void searchStarted(FitnessFunction objective) { }

	public void fitnessEvaluation(Chromosome result) { }

}
