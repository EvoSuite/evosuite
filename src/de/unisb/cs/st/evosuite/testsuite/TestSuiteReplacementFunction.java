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
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.SelectionFunction;
import de.unisb.cs.st.evosuite.ga.ReplacementFunction;


/**
 * @author Gordon Fraser
 *
 */
public class TestSuiteReplacementFunction extends
		ReplacementFunction {

	private final static boolean BEST_LENGTH = Properties.getPropertyOrDefault("check_best_length", true);  

	/**
	 * @param selectionFunction
	 */
	public TestSuiteReplacementFunction(SelectionFunction selectionFunction) {
		super(selectionFunction);
	}
	
	public TestSuiteReplacementFunction(boolean maximize) {
		super(maximize);
	}
	
	public TestSuiteReplacementFunction() {
		super(false);
	}

	public int getLengthSum(TestSuiteChromosome chromosome1, TestSuiteChromosome chromosome2) {
		return chromosome1.length() + chromosome2.length();
	}
	
	protected double getBestFitness(TestSuiteChromosome chromosome1, TestSuiteChromosome chromosome2) {
		if(maximize) {
			return Math.max(chromosome1.getFitness(), chromosome2.getFitness());
		} else {
			return Math.min(chromosome1.getFitness(), chromosome2.getFitness());
		}		
	}
	
	/**
	 * min(d(O1),d(O2)) < min(d(P1),d(P2))Ê ||
     * ( min(d(O1),d(O2)) == min(d(P1),d(P2))ÊÊ &&ÊÊ Z= (l(O1)+l(O2) <= l(P1)+l(P2)) ) 
	 */
	public boolean keepOffspring(Chromosome parent1, Chromosome parent2,
			Chromosome offspring1, Chromosome offspring2) {

		double fitness_offspring = getBestFitness((TestSuiteChromosome)offspring1, (TestSuiteChromosome)offspring2);
		double fitness_parents   = getBestFitness((TestSuiteChromosome)parent1, (TestSuiteChromosome)parent2);
		
		if(BEST_LENGTH && (fitness_offspring == fitness_parents && 
				getLengthSum((TestSuiteChromosome)offspring1, (TestSuiteChromosome)offspring2) <= getLengthSum((TestSuiteChromosome)parent1, (TestSuiteChromosome)parent2)))
			return true;
		if(fitness_offspring < fitness_parents) {
			return true;
		} else {
			return false;
		}
		
	}

	/* (non-Javadoc)
     * @see de.unisb.cs.st.evosuite.ga.ReplacementFunction#keepOffspring(de.unisb.cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome)
     */
    @Override
    public boolean keepOffspring(Chromosome parent, Chromosome offspring) {
    	return isBetter(offspring, parent);
    }

}
