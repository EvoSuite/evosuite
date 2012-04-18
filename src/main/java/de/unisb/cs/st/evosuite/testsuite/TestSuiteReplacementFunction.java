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

package de.unisb.cs.st.evosuite.testsuite;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ReplacementFunction;
import de.unisb.cs.st.evosuite.ga.SelectionFunction;
import de.unisb.cs.st.evosuite.testcase.ExecutableChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class TestSuiteReplacementFunction extends ReplacementFunction {

	private static final long serialVersionUID = -8472469271120247395L;

	public TestSuiteReplacementFunction(boolean maximize) {
		super(maximize);
	}

	public TestSuiteReplacementFunction() {
		super(false);
	}

	public int getLengthSum(TestSuiteChromosome chromosome1,
	        TestSuiteChromosome chromosome2) {
		return chromosome1.totalLengthOfTestCases()
		        + chromosome2.totalLengthOfTestCases();
	}


	@Override
	public boolean keepOffspring(Chromosome parent1, Chromosome parent2,
	        Chromosome offspring1, Chromosome offspring2) {

		int cmp = compareBestOffspringToBestParent(parent1,parent2,offspring1,offspring2);
		
		if (Properties.CHECK_PARENTS_LENGTH) {
			
			int offspringLength = getLengthSum((TestSuiteChromosome) offspring1, (TestSuiteChromosome) offspring2);
			int parentLength = getLengthSum((TestSuiteChromosome) parent1,	 (TestSuiteChromosome) parent2);
			
			//if equivalent, only accept if it does not increase the length
			if (cmp==0 &&  offspringLength <= parentLength) {
				return true;
			} else {
				return cmp > 0;
			}
		}  else {
			//default check
			return cmp >= 0;
		}
	}
}
