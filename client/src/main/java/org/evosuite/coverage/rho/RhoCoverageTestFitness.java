package org.evosuite.coverage.rho;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

public class RhoCoverageTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = -1483213330289592274L;

	private int previous_number_of_ones = 0;
	private int previous_number_of_test_cases = 0;

	private Set<Set<Integer>> coverage_matrix_generated_so_far = new LinkedHashSet<Set<Integer>>();

	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {

		Set<Set<Integer>> tmp_coverage_matrix = new LinkedHashSet<Set<Integer>>(this.coverage_matrix_generated_so_far);

		double fitness = 1.0;

		int number_of_goals = RhoCoverageFactory.getNumberGoals();
		int number_of_ones = RhoCoverageFactory.getNumber_of_Ones() + this.previous_number_of_ones;
		int number_of_test_cases = RhoCoverageFactory.getNumber_of_Test_Cases() + this.previous_number_of_test_cases;

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
				updateIndividual(this, individual, 1.0);
				return 1.0;
			}
			// already exists locally
			else if (tmp_coverage_matrix.add(coveredLinesOrdered) == false) {
				updateIndividual(this, individual, 1.0);
				return 1.0;
			}
			// already exists on the original test suite
			else if (RhoCoverageFactory.exists(l_coveredLines)) {
				updateIndividual(this, individual, 1.0);
				return 1.0;
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

		// was not possible to generate new test cases
		if (number_of_test_cases == 0.0) {
			fitness = 1.0; // penalise this test case
		} else {
			fitness = ((double) number_of_ones) / ((double) number_of_test_cases) / ((double) number_of_goals);
			fitness = Math.abs(0.5 - fitness);
		}

		updateIndividual(this, individual, fitness);
		return fitness;
	}

	@Override
	public int compareTo(TestFitnessFunction other) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getTargetClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTargetMethod() {
		// TODO Auto-generated method stub
		return null;
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
