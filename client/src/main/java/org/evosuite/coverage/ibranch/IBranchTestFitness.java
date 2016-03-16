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
/**
 * 
 */
package org.evosuite.coverage.ibranch;

import java.util.Map;
import java.util.Map.Entry;

import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.setup.CallContext;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

/**
 * 
 * IBranch test fitness function.
 * @author mattia, Gordon Fraser
 * 
 */
public class IBranchTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = -1399396770125054561L;

	private final BranchCoverageGoal branchGoal;

	private final CallContext context;

	public IBranchTestFitness(BranchCoverageGoal branch, CallContext context) {
		this.branchGoal = branch;
		this.context = context;
	}

	public Branch getBranch() {
		return branchGoal.getBranch();
	}

	public boolean getValue() {
		return branchGoal.getValue();
	}

	public CallContext getContext() {
		return context;
	}

	public BranchCoverageGoal getBranchGoal() {
		return branchGoal;
	}
	
	private double getMethodCallDistance(ExecutionResult result) {
		String key = branchGoal.getClassName() + "." + branchGoal.getMethodName();
		if (!result.getTrace().getMethodContextCount().containsKey(key)) {
			return Double.MAX_VALUE;
		}
		for (Entry<CallContext, Integer> value : result.getTrace().getMethodContextCount().get(key).entrySet()) {

			if (context.matches(value.getKey())) {
				return value.getValue() > 0 ? 0.0 : 1.0;
			}
		}
		return Double.MAX_VALUE;
	}
	
	public int getGenericContextBranchIdentifier(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (branchGoal == null ? 0 : branchGoal.hashCodeWithoutValue());
		result = prime * result + (context == null ? 0 : context.hashCode());
		return result;
	}
	

	private double getPredicateDistance(Map<Integer, Map<CallContext, Double>> distanceMap) {

		if (!distanceMap.containsKey(branchGoal.getBranch().getActualBranchId())) {
			return Double.MAX_VALUE;
		}

		Map<CallContext, Double> distances = distanceMap.get(branchGoal.getBranch().getActualBranchId());

		for (Entry<CallContext, Double> value : distances.entrySet()) {
			if (context.matches(value.getKey())) {
				return value.getValue();
			}
		}

		return Double.MAX_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getFitness(org.evosuite.testcase.TestChromosome, org.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = 0.0;

		if (branchGoal.getBranch() == null) {
			fitness = getMethodCallDistance(result);
		} else if (branchGoal.getValue()) {
			fitness = getPredicateDistance(result.getTrace().getTrueDistancesContext());
		} else {
			fitness = getPredicateDistance(result.getTrace().getFalseDistancesContext());
		}

		updateIndividual(this, individual, fitness);
		return fitness;
	}

	
	
	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof IBranchTestFitness) {
			IBranchTestFitness otherBranchFitness = (IBranchTestFitness) other;
			return branchGoal.compareTo(otherBranchFitness.branchGoal);
		} 
		else if (other instanceof BranchCoverageTestFitness) {
			BranchCoverageTestFitness otherBranchFitness = (BranchCoverageTestFitness) other;
			return branchGoal.compareTo(otherBranchFitness.getBranchGoal());
		}
		return compareClassName(other);
//		return -1;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
	 */
	@Override
	public String getTargetClass() {
		return branchGoal.getClassName();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
	 */
	@Override
	public String getTargetMethod() {
		return branchGoal.getMethodName();
	}

	@Override
	public String toString() {
		return "Branch " + branchGoal + " in context: " + context.toString();
	}

	public String toStringContext() {
		return context.toString() + ":" + branchGoal;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((branchGoal == null) ? 0 : branchGoal.hashCode());
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IBranchTestFitness other = (IBranchTestFitness) obj;
		if (branchGoal == null) {
			if (other.branchGoal != null)
				return false;
		} else if (!branchGoal.equals(other.branchGoal))
			return false;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		return true;
	}

}
