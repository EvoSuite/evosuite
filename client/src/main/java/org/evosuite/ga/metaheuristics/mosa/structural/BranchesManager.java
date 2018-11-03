/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.ga.metaheuristics.mosa.structural;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class manages the goals to consider during the search according to their structural 
 * dependencies
 * 
 * @author Annibale Panichella, Fitsum Meshesha Kifetew
 */
public class BranchesManager<T extends Chromosome> extends StructuralGoalManager<T>{

	private static final Logger logger = LoggerFactory.getLogger(BranchesManager.class);

	protected BranchFitnessGraph<T, FitnessFunction<T>> graph;

	protected final Map<Integer, FitnessFunction<T>> branchCoverageTrueMap = new HashMap<Integer, FitnessFunction<T>>();
	protected final Map<Integer, FitnessFunction<T>> branchCoverageFalseMap = new HashMap<Integer, FitnessFunction<T>>();
	private final Map<String, FitnessFunction<T>> branchlessMethodCoverageMap = new HashMap<String, FitnessFunction<T>>();

	/**
	 * Constructor used to initialize the set of uncovered goals, and the initial set
	 * of goals to consider as initial contrasting objectives
	 * @param fitnessFunctions List of all FitnessFunction<T>
	 */
	public BranchesManager(List<FitnessFunction<T>> fitnessFunctions){
		super(fitnessFunctions);
		// initialize uncovered goals
		uncoveredGoals.addAll(fitnessFunctions);
		Set<FitnessFunction<T>> set = new HashSet<FitnessFunction<T>>();
		set.addAll(fitnessFunctions);
		graph = new BranchFitnessGraph<T, FitnessFunction<T>>(set);
		// initialize current goals
		this.currentGoals.addAll(graph.getRootBranches());

		// initialize the maps
		for (FitnessFunction<T> ff : fitnessFunctions) {
			BranchCoverageTestFitness goal = (BranchCoverageTestFitness) ff;
			// Skip instrumented branches - we only want real branches
			if(goal.getBranch() != null) {
				if(goal.getBranch().isInstrumented()) {
					continue;
				}
			}

			if (goal.getBranch() == null) {
				branchlessMethodCoverageMap.put(goal.getClassName() + "."
						+ goal.getMethod(), ff);
			} else {
				if (goal.getBranchExpressionValue())
					branchCoverageTrueMap.put(goal.getBranch().getActualBranchId(), ff);
				else
					branchCoverageFalseMap.put(goal.getBranch().getActualBranchId(), ff);
			}
		}
	}

	public void calculateFitness(T c){
		// run the test
		TestCase test = ((TestChromosome) c).getTestCase();
		ExecutionResult result = TestCaseExecutor.runTest(test);
		((TestChromosome) c).setLastExecutionResult(result);
		c.setChanged(false);
		
		if (result.hasTimeout() || result.hasTestException()){
			for (FitnessFunction<T> f : currentGoals)
					c.setFitness(f, Double.MAX_VALUE);
			return;
		}

		// 1) we update the set of currents goals
		Set<FitnessFunction<T>> visitedStatements = new HashSet<FitnessFunction<T>>(uncoveredGoals.size()*2);
		LinkedList<FitnessFunction<T>> targets = new LinkedList<FitnessFunction<T>>();
		targets.addAll(this.currentGoals);

		while (targets.size()>0){
			FitnessFunction<T> fitnessFunction = targets.poll();
			
			int past_size = visitedStatements.size();
			visitedStatements.add(fitnessFunction);
			if (past_size == visitedStatements.size())
				continue;
			
			double value = fitnessFunction.getFitness(c);
			if (value == 0.0) {
				updateCoveredGoals(fitnessFunction, c);
				for (FitnessFunction<T> child : graph.getStructuralChildren(fitnessFunction)){
					targets.addLast(child);
				}
			} else {
				currentGoals.add(fitnessFunction);
			}	
		}
		currentGoals.removeAll(coveredGoals.keySet());
		// 2) we update the archive
		for (Integer branchid : result.getTrace().getCoveredFalseBranches()){
			FitnessFunction<T> branch = this.branchCoverageFalseMap.get(branchid);
			if (branch == null)
				continue;
			updateCoveredGoals((FitnessFunction<T>) branch, c);
		}
		for (Integer branchid : result.getTrace().getCoveredTrueBranches()){
			FitnessFunction<T> branch = this.branchCoverageTrueMap.get(branchid);
			if (branch == null)
				continue;
			updateCoveredGoals((FitnessFunction<T>) branch, c);
		}
		for (String method : result.getTrace().getCoveredBranchlessMethods()){
			FitnessFunction<T> branch = this.branchlessMethodCoverageMap.get(method);
			if (branch == null)
				continue;
			updateCoveredGoals((FitnessFunction<T>) branch, c);
		}
		//debugStructuralDependencies(c);
	}

	protected void debugStructuralDependencies(T c){
		for (FitnessFunction<T> fitnessFunction : this.uncoveredGoals) {
			double value = fitnessFunction.getFitness(c);
			if (value <1 && !currentGoals.contains(fitnessFunction) && !this.coveredGoals.keySet().contains(fitnessFunction)) {
				logger.error("Branch {} has fitness {} but is not in the current goals", fitnessFunction.toString(), value);
			}
		}
	}

	public BranchFitnessGraph<T, FitnessFunction<T>> getGraph() {
		return graph;
	}

}
