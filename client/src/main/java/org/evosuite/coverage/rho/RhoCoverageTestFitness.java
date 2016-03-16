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
		return compareClassName(other);
	}

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((coverage_matrix_generated_so_far == null) ? 0 : coverage_matrix_generated_so_far.hashCode());
		result = prime * result + previous_number_of_ones;
		result = prime * result + previous_number_of_test_cases;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RhoCoverageTestFitness other = (RhoCoverageTestFitness) obj;
		if (coverage_matrix_generated_so_far == null) {
			if (other.coverage_matrix_generated_so_far != null)
				return false;
		} else if (!coverage_matrix_generated_so_far.equals(other.coverage_matrix_generated_so_far))
			return false;
		if (previous_number_of_ones != other.previous_number_of_ones)
			return false;
		if (previous_number_of_test_cases != other.previous_number_of_test_cases)
			return false;
		return true;
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
