package org.evosuite.coverage.epa;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.xml.sax.SAXException;

public class EPAAdjacentEdgesCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -8137606898642577596L;

	public EPAAdjacentEdgesCoverageSuiteFitness(String epaXMLFilename) {

		try {
			EPA target_epa = EPAFactory.buildEPA(epaXMLFilename);
			EPASuiteFitness.checkEPAStates(target_epa, Properties.TARGET_CLASS);
			EPASuiteFitness.checkEPAActionNames(target_epa, Properties.TARGET_CLASS);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new EvosuiteError(e);
		}

	}

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> individual) {
		final List<ExecutionResult> executionResults = runTestSuite(individual);

		Set<EPAAdjacentEdgesPair> adjacentPairs = new HashSet<EPAAdjacentEdgesPair>();
		for (ExecutionResult executionResult : executionResults) {
			Set<EPAAdjacentEdgesPair> adjacentPairsForResult = constructGoals(executionResult, this);
			adjacentPairs.addAll(adjacentPairsForResult);
		}

		long coveredGoalsCount = adjacentPairs.size();
		long upperBoundOfGoals = EPAAdjacentEdgesCoverageFactory.UPPER_BOUND_OF_GOALS;
		final double fitness = upperBoundOfGoals - coveredGoalsCount;
		updateIndividual(this, individual, fitness);
		final double coverage = (upperBoundOfGoals > 0) ?
				(coveredGoalsCount / (double) upperBoundOfGoals) : 0;
		individual.setCoverage(this, coverage);
		individual.setNumOfCoveredGoals(this, (int) coveredGoalsCount);
		individual.setNumOfNotCoveredGoals(this, (int) (upperBoundOfGoals - coveredGoalsCount));
		return fitness;
	}

	public static Set<EPAAdjacentEdgesPair> constructGoals(ExecutionResult executionResult, EPAAdjacentEdgesCoverageSuiteFitness contextFitness) {

		TestChromosome test = new TestChromosome();
		test.setTestCase(executionResult.test);
		test.setLastExecutionResult(executionResult);
		test.setChanged(false);

		Set<EPAAdjacentEdgesPair> adjacentPairsForResult = EPAAdjacentEdgesPair.getAdjacentEdgesPairsExecuted(executionResult);
		for (EPAAdjacentEdgesPair pair : adjacentPairsForResult) {
			EPATransition firstTransition = pair.getFirstEpaTransition();
			EPATransition secondTransition = pair.getSecondEpaTransition();
			EPAAdjacentEdgesCoverageGoal coverageGoal = new EPAAdjacentEdgesCoverageGoal(Properties.TARGET_CLASS,firstTransition, secondTransition);
			EPAAdjacentEdgesCoverageTestFitness goal = new EPAAdjacentEdgesCoverageTestFitness(coverageGoal);
			String key = goal.getKey();
			if (!EPAAdjacentEdgesCoverageFactory.getGoals().containsKey(key)) {
				EPAAdjacentEdgesCoverageFactory.getGoals().put(key, goal);
				test.getTestCase().addCoveredGoal(goal);
				if (Properties.TEST_ARCHIVE && contextFitness != null) {
					Archive.getArchiveInstance().addTarget(goal);
					Archive.getArchiveInstance().updateArchive(goal, test, 0.0);
				}
			}
		}
		return adjacentPairsForResult;
	}

}
