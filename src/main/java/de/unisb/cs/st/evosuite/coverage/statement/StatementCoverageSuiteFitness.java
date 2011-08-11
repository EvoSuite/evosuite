package de.unisb.cs.st.evosuite.coverage.statement;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

public class StatementCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -4479582777935260157L;

	@Override
	public double getFitness(Chromosome individual) {
		TestSuiteChromosome suite = (TestSuiteChromosome)individual;
		List<ExecutionResult> results = runTestSuite(suite);
		double fitness = 0.0;

		// first simple and naive idea: 
		//  just take each goal, calculate the minimal fitness over all results in the suite
		//  once a goal is covered don't check for it again
		//  in the end sum up all those fitness and it's the resulting suite-fitness
		
		// guess this is horribly inefficient but it's a start
		List<TestFitnessFunction> totalGoals = StatementCoverageFactory.retrieveCoverageGoals();
		List<TestFitnessFunction> coveredGoals = new ArrayList<TestFitnessFunction>(); 

		for(TestFitnessFunction goal : totalGoals) {
			double goalFitness = Double.MAX_VALUE;
			for(ExecutionResult result : results) {
				TestChromosome tc = new TestChromosome();
				tc.setTestCase(result.test);
				double resultFitness = goal.getFitness(tc,result);
				if(resultFitness<goalFitness)
					goalFitness=resultFitness;
				if(goalFitness == 0.0) {
					result.test.addCoveredGoal(goal);
					coveredGoals.add(goal);
					break;
				}
			}
			fitness += goalFitness;
		}
		
		suite.setCoverage(coveredGoals.size()/(double)totalGoals.size());
		updateIndividual(individual, fitness);
		return fitness;
	}

}
