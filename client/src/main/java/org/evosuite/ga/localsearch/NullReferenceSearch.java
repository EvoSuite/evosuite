/**
 * 
 */
package org.evosuite.ga.localsearch;

import java.util.Map;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.NullStatement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;

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
		Map<FitnessFunction<?>, Double> oldFitnesses = test.getFitnesses();
		Map<FitnessFunction<?>, Double> oldLastFitnesses = test.getLastFitnesses();

		try {
			TestFactory.getInstance().attemptGeneration(newTest,
			                                            nullStatement.getReturnType(),
			                                            statement);
			if (!objective.hasImproved(test)) {
				test.setTestCase(oldTest);
				test.setLastExecutionResult(oldResult);
				//test.setFitness(oldFitness);
				test.setFitnesses(oldFitnesses);
				test.setLastFitnesses(oldLastFitnesses);
			} else {
				return true;
			}
		} catch (ConstructionFailedException e) {
			// If we can't construct it, then ignore
		}

		return false;
	}

}
