package org.evosuite.coverage.epa;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.xml.sax.SAXException;

public class EPAAdjacentEdgesCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -8137606898642577596L;
	private final EPA epa;

	public EPAAdjacentEdgesCoverageSuiteFitness(String epaXMLFilename) {

		try {
			EPA target_epa = EPAFactory.buildEPA(epaXMLFilename);
			EPASuiteFitness.checkEPAStates(target_epa, Properties.TARGET_CLASS);
			EPASuiteFitness.checkEPAActionNames(target_epa, Properties.TARGET_CLASS);

			this.epa = target_epa;

		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new EvosuiteError(e);
		}

	}

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> individual) {
		final List<ExecutionResult> executionResults = runTestSuite(individual);

		Set<EPAAdjacentEdgesPair> adjacentPairs = new HashSet<EPAAdjacentEdgesPair>();
		for (ExecutionResult executionResult : executionResults) {
			Set<EPAAdjacentEdgesPair> adjacentPairsForResult = EPAAdjacentEdgesPair
					.getAdjacentEdgesPairsExecuted(executionResult);
			for (EPAAdjacentEdgesPair pair : adjacentPairsForResult) {
				EPATransition firstTransition = pair.getFirstEpaTransition();
				EPATransition secondTransition = pair.getSecondEpaTransition();
				EPAAdjacentEdgesCoverageGoal coverageGoal = new EPAAdjacentEdgesCoverageGoal(Properties.TARGET_CLASS,
						firstTransition, secondTransition);
				EPAAdjacentEdgesCoverageTestFitness goal = new EPAAdjacentEdgesCoverageTestFitness(coverageGoal);
				String key = goal.getKey();
				if (!EPAAdjacentEdgesCoverageFactory.getGoals().containsKey(key)) {
					EPAAdjacentEdgesCoverageFactory.getGoals().put(key, goal);
					if (Properties.TEST_ARCHIVE) {
						TestsArchive.instance.addGoalToCover(this, goal);
						TestsArchive.instance.putTest(this, goal, executionResult);
					}
				}
			}
			adjacentPairs.addAll(adjacentPairsForResult);
		}

		long coveredGoalsCount = adjacentPairs.size();

		long numOfObservedGoals = EPAAdjacentEdgesCoverageFactory.getGoals().size();
		long upperBoundOfGoals = this.epa.getStates().size() * this.epa.getActions().size()
				* this.epa.getStates().size() * 2;

		final double fitness = numOfObservedGoals - coveredGoalsCount;
		updateIndividual(this, individual, fitness);
		final double coverage = (upperBoundOfGoals > 0) ? (coveredGoalsCount / (double) upperBoundOfGoals) : 0;
		individual.setCoverage(this, coverage);
		individual.setNumOfCoveredGoals(this, (int) coveredGoalsCount);
		individual.setNumOfNotCoveredGoals(this, (int) (upperBoundOfGoals - coveredGoalsCount));
		return fitness;
	}

}
