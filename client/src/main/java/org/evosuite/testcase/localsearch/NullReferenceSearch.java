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
/**
 * 
 */
package org.evosuite.testcase.localsearch;

import java.util.Map;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.NullStatement;

/**
 * Try to replace a null reference with a non-null reference
 * 
 * @author Gordon Fraser
 * 
 */
public class NullReferenceSearch extends StatementLocalSearch {

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.LocalSearch#doSearch(org.evosuite.testcase.TestChromosome, int, org.evosuite.ga.LocalSearchObjective)
	 */
	@Override
	public boolean doSearch(TestChromosome test, int statement,
	        LocalSearchObjective<TestChromosome> objective) {
		NullStatement nullStatement = (NullStatement) test.getTestCase().getStatement(statement);
		TestCase newTest = test.getTestCase();
		TestCase oldTest = newTest.clone();
		ExecutionResult oldResult = test.getLastExecutionResult();
		//double oldFitness = test.getFitness();
		Map<FitnessFunction<?>, Double> oldFitnesses = test.getFitnessValues();
		Map<FitnessFunction<?>, Double> oldLastFitnesses = test.getPreviousFitnessValues();

		try {
			TestFactory.getInstance().attemptGeneration(newTest,
			                                            nullStatement.getReturnType(),
			                                            statement);
			if (!objective.hasImproved(test)) {
				test.setTestCase(oldTest);
				test.setLastExecutionResult(oldResult);
				//test.setFitness(oldFitness);
				test.setFitnessValues(oldFitnesses);
				test.setPreviousFitnessValues(oldLastFitnesses);
			} else {
				return true;
			}
		} catch (ConstructionFailedException e) {
			// If we can't construct it, then ignore
		}

		return false;
	}

}
