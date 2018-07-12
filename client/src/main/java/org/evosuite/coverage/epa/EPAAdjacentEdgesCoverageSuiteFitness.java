package org.evosuite.coverage.epa;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.xml.sax.SAXException;

public class EPAAdjacentEdgesCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -8137606898642577596L;
	private final EPA epa;
	private final List<EPAAdjacentEdgesCoverageTestFitness> goals;

	public EPAAdjacentEdgesCoverageSuiteFitness(String epaXMLFilename) {

		try {
			EPA target_epa = EPAFactory.buildEPA(epaXMLFilename);
			EPASuiteFitness.checkEPAStates(target_epa, Properties.TARGET_CLASS);
			EPASuiteFitness.checkEPAActionNames(target_epa, Properties.TARGET_CLASS);

			this.epa = target_epa;
			EPAAdjacentEdgesCoverageFactory goalFactory = new EPAAdjacentEdgesCoverageFactory(this.epa);
			this.goals = goalFactory.getCoverageGoals();

		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new EvosuiteError(e);
		}

	}

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> individual) {
		final List<ExecutionResult> executionResults = runTestSuite(individual);
		long coveredGoalsCount = EPAAdjacentEdgesPair.getAdjacentEdgesPairsExecuted(executionResults).size();
		
		final double fitness = goals.size() - coveredGoalsCount;
		updateIndividual(this, individual, fitness);
		final double coverage = (goals.size() > 0) ? (coveredGoalsCount / (double) goals.size()) : 0;
		individual.setCoverage(this, coverage);
		individual.setNumOfCoveredGoals(this, (int) coveredGoalsCount);
		individual.setNumOfNotCoveredGoals(this, (int) (goals.size() - coveredGoalsCount));
		return fitness;
	}
	
}
