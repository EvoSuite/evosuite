package org.evosuite.coverage.epa;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.xml.sax.SAXException;

public class EPAExceptionCoverageSuiteFitness extends TestSuiteFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3588790421141471290L;
	private final EPA epa;

	public EPAExceptionCoverageSuiteFitness(String epaXMLFilename) {

		try {
			EPA target_epa = EPAFactory.buildEPA(epaXMLFilename);
			EPASuiteFitness.checkEPAStates(target_epa, Properties.TARGET_CLASS);
			EPASuiteFitness.checkEPAActionNames(target_epa, Properties.TARGET_CLASS);

			this.epa = target_epa;
			EPAExceptionCoverageFactory goalFactory = new EPAExceptionCoverageFactory(epaXMLFilename, this.epa);
			this.coverageGoalMap = buildCoverageGoalMap(goalFactory);

		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new EvosuiteError(e);
		}

	}

	private static Map<String, EPAExceptionCoverageTestFitness> buildCoverageGoalMap(EPAExceptionCoverageFactory goalFactory) {
		Map<String, EPAExceptionCoverageTestFitness> coverageGoalMap = new HashMap<>();
		for (EPAExceptionCoverageTestFitness goal : goalFactory.getCoverageGoals()) {
			coverageGoalMap.put(goal.getGoalName(), goal);
		}
		return coverageGoalMap;
	}

	private final Map<String, EPAExceptionCoverageTestFitness> coverageGoalMap;

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suiteChromosome) {

		long coveredGoalsCount = 0;
		final List<ExecutionResult> executionResults = runTestSuite(suiteChromosome);
		final Collection<EPAExceptionCoverageTestFitness> goals = this.coverageGoalMap.values();
		for (EPAExceptionCoverageTestFitness goal : goals) {
			if (goal.isCoveredByResults(executionResults)) {
				coveredGoalsCount++;
			}
		}
		final double fitness = goals.size() - coveredGoalsCount;
		updateIndividual(this, suiteChromosome, fitness);
		final double coverage = (goals.size() > 0) ? (coveredGoalsCount / (double) goals.size()) : 0;
		suiteChromosome.setCoverage(this, coverage);
		suiteChromosome.setNumOfCoveredGoals(this, (int) coveredGoalsCount);
		suiteChromosome.setNumOfNotCoveredGoals(this, (int) (goals.size() - coveredGoalsCount));
		return fitness;

	}

}
