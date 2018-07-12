package org.evosuite.coverage.epa;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private final List<EPAExceptionCoverageTestFitness> goals;

	public EPAExceptionCoverageSuiteFitness(String epaXMLFilename) {

		try {
			EPA target_epa = EPAFactory.buildEPA(epaXMLFilename);
			EPASuiteFitness.checkEPAStates(target_epa, Properties.TARGET_CLASS);
			EPASuiteFitness.checkEPAActionNames(target_epa, Properties.TARGET_CLASS);

			this.epa = target_epa;
			EPAExceptionCoverageFactory goalFactory = new EPAExceptionCoverageFactory(epaXMLFilename, this.epa);
			this.goals = goalFactory.getCoverageGoals();

		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new EvosuiteError(e);
		}

	}

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suiteChromosome) {
		final List<ExecutionResult> executionResults = runTestSuite(suiteChromosome);
		long coveredGoalsCount = getEPAExceptionTransitionsExecuted(executionResults).size();
		
		final double fitness = goals.size() - coveredGoalsCount;
		updateIndividual(this, suiteChromosome, fitness);
		final double coverage = (goals.size() > 0) ? (coveredGoalsCount / (double) goals.size()) : 0;
		suiteChromosome.setCoverage(this, coverage);
		suiteChromosome.setNumOfCoveredGoals(this, (int) coveredGoalsCount);
		suiteChromosome.setNumOfNotCoveredGoals(this, (int) (goals.size() - coveredGoalsCount));
		return fitness;
	}
	
	private Set<EPATransition> getEPAExceptionTransitionsExecuted(List<ExecutionResult> executionResults) {
		Set<EPATransition> exceptionTransitions = new HashSet<>();
		for(ExecutionResult executionResult : executionResults) {
			for (EPATrace epaTrace : executionResult.getTrace().getEPATraces()) {
				for (EPATransition epaTransition : epaTrace.getEpaTransitions()) {
					if (epaTransition.getDestinationState().equals(EPAState.INVALID_OBJECT_STATE)) {
						// discard the rest of the trace if an invalid object state is reached
						break;
					}
					if (epaTransition instanceof EPAExceptionalTransition)
						exceptionTransitions.add(epaTransition);
				}
			}
		}
		return exceptionTransitions;
	}
	
}
