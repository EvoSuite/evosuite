package org.evosuite.coverage.rho;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * 
 * @author José Campos
 */
public class RhoCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 5460600509431741746L;

	private int previous_number_of_ones = 0;
	private int previous_number_of_test_cases = 0;

	private Set<Set<Integer>> coverage_matrix_generated_so_far = new LinkedHashSet<Set<Integer>>();

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {

		Set<Set<Integer>> tmp_coverage_matrix = new LinkedHashSet<Set<Integer>>(this.coverage_matrix_generated_so_far);

		double fitness = 1.0;

		int number_of_goals = RhoCoverageFactory.getNumberGoals();
		int number_of_ones = RhoCoverageFactory.getNumber_of_Ones() + this.previous_number_of_ones;
		int number_of_test_cases = RhoCoverageFactory.getNumber_of_Test_Cases() + this.previous_number_of_test_cases;

		List<ExecutionResult> results = runTestSuite(suite);
		for (int i = 0; i < results.size(); i++) {

			// Execute test cases and collect the covered lines
			ExecutionResult result = results.get(i);
			Set<Integer> coveredLines = result.getTrace().getCoveredLines();

			if (Properties.STRATEGY == Properties.Strategy.ENTBUG) {
				// order set
				List<Integer> l_coveredLines = new ArrayList<Integer>(coveredLines);
				Collections.sort(l_coveredLines);
				Set<Integer> coveredLinesOrdered = new LinkedHashSet<Integer>();
				for (Integer coveredLine : l_coveredLines) {
					coveredLinesOrdered.add(coveredLine);
				}

				// no coverage
				if (coveredLinesOrdered.size() == 0) {
					continue ;
				}
				// already exists locally
				else if (tmp_coverage_matrix.add(coveredLinesOrdered) == false) {
					continue ;
				}
				// already exists on the original test suite
				else if (RhoCoverageFactory.exists(l_coveredLines)) {
					continue ;
				}
				// good
				else {
					number_of_ones += coveredLinesOrdered.size();
					number_of_test_cases++;
				}
			} else {
				number_of_ones += coveredLines.size();
				number_of_test_cases++;
			}
		}

		// was not possible to generate new test cases
		if (number_of_test_cases == 0.0) {
			fitness = 1.0; // penalise this suite
		} else {
			fitness = ((double) number_of_ones) / ((double) number_of_test_cases) / ((double) number_of_goals);
			fitness = Math.abs(0.5 - fitness);
		}

		updateIndividual(this, suite, fitness);
		return fitness;
	}

	public void incrementNumber_of_Ones(int n) {
		this.previous_number_of_ones += n;
	}
	public int getNumber_of_Ones() {
		return this.previous_number_of_ones;
	}

	public void incrementNumber_of_Test_Cases() {
		this.previous_number_of_test_cases++;
	}
	public int getNumber_of_Test_Cases() {
		return this.previous_number_of_test_cases;
	}

	public void addTestCoverage(Set<Integer> test_coverage) {
		this.coverage_matrix_generated_so_far.add(test_coverage);
	}
}
