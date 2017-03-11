package org.evosuite.ga.metaheuristics.mosa.structural;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.evosuite.coverage.ControlFlowDistance;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.mutation.MutationExecutionResult;
import org.evosuite.coverage.mutation.MutationTestFitness;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.coverage.mutation.StrongMutationSuiteFitness;
import org.evosuite.coverage.mutation.StrongMutationTestFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class manages the goals to consider during the search according to their structural 
 * dependencies
 * @author Annibale Panichella, Fitsum Meshesha Kifetew
 * 
 */
public class StrongMutationsManager<T extends Chromosome> extends StructuralGoalManager<T>{

	private static final Logger logger = LoggerFactory.getLogger(StrongMutationsManager.class);

	protected StrongMutationGraph<T, FitnessFunction<T>> graph;

	public final Map<Integer, FitnessFunction<T>> mutantMap = new HashMap<Integer, FitnessFunction<T>>();

	/**
	 * Constructor used to initialize the set of uncovered goals, and the initial set
	 * of goals to consider as initial contrasting objectives
	 * @param mutationTargets List of all FitnessFunction<T>
	 */
	public StrongMutationsManager(List<FitnessFunction<T>> mutationTargets){
		super(mutationTargets);
		// initialize uncovered goals
		uncoveredGoals.addAll(mutationTargets);
		if (mutationTargets.size() != uncoveredGoals.size()){
			logger.error("THERE IS A PROBLEM IN StrongMutationTestFitness.equals()");
		}
		graph = new StrongMutationGraph<T, FitnessFunction<T>>(uncoveredGoals);
		this.currentGoals.addAll(graph.rootMutants);

		for(FitnessFunction<T> goal : mutationTargets) {
			StrongMutationTestFitness mutant = (StrongMutationTestFitness) goal;
			mutantMap.put(mutant.getMutation().getId(), goal);
		}
	}


	/**
	 * Update the set of covered goals and the set of current goals (actual objectives)
	 * @param population list of TestChromosome
	 * @return covered goals along with the corresponding test case
	 */
	public void calculateFitness(T c){
		// run test
		TestCase test = ((TestChromosome) c).getTestCase();
		ExecutionResult result = StrongMutationTestFitness.runTest(test, null);
		((TestChromosome) c).setLastExecutionResult(result);
		c.setChanged(false);


		// calculate current targets
		TestChromosome tch = (TestChromosome) c;
		Set<FitnessFunction<T>> visitedStatements = new HashSet<FitnessFunction<T>>(uncoveredGoals.size()*2);
		LinkedList<FitnessFunction<T>> targets = new LinkedList<FitnessFunction<T>>();
		targets.addAll(currentGoals);
		
		while (targets.size()>0){
			FitnessFunction<T> fitnessFunction = targets.poll();
			StrongMutationTestFitness testMutation = (StrongMutationTestFitness) fitnessFunction;

			int past_size = visitedStatements.size();
			visitedStatements.add(fitnessFunction);
			if (past_size == visitedStatements.size())
				continue;

			visitedStatements.add(fitnessFunction);
			
			double finalFitness = testMutation.getFitness(tch, result);
			if (result.getTrace().getTouchedMutants().contains(testMutation.getMutation().getId())){				
				for (FitnessFunction<T> child : graph.getStructuralChildren(fitnessFunction)){
					if(!isAlreadyCovered(child))
						targets.addLast(child);
				}
			}
			
			if (finalFitness!=0.0 && tch.getLastExecutionResult(testMutation.getMutation()) != null) {
				MutationExecutionResult mutantResult = tch.getLastExecutionResult(testMutation.getMutation());
				if (mutantResult.hasTimeout())
					finalFitness = 0.0;
				else if (mutantResult.hasException() && result.noThrownExceptions())
					finalFitness = 0.0;
			}

			if (finalFitness == 0.0) 
				updateCoveredGoals(fitnessFunction, c);
			else{
				if(!isAlreadyCovered(fitnessFunction))
					this.currentGoals.add(fitnessFunction);
			}
		} // for
		//logger.error("Number of touched mutants = {}", count);
		currentGoals.removeAll(coveredGoals.keySet());
		//debugStructuralDependencies(c);
	}

	protected void debugStructuralDependencies(T c){
		for (FitnessFunction<T> fitnessFunction : this.uncoveredGoals) {
			StrongMutationTestFitness strong = (StrongMutationTestFitness) fitnessFunction;
			double value = fitnessFunction.getFitness(c);
			if (value <2 && !currentGoals.contains(fitnessFunction) && !this.coveredGoals.keySet().contains(fitnessFunction)) {
				logger.error("Mutant {} has fitness {} but is not in the current goals", fitnessFunction.toString(), value);
				for (FitnessFunction<T> fparent : graph.getStructuralParents(fitnessFunction)){
					if (fparent.getFitness(c) == 0.0)
						logger.error(">> Parent {} has fitness {} \n 1) is evaluated? {} \n 2) is covered? {}", fparent.toString(), fparent.getFitness(c), c.getFitnessValues().containsKey(fparent), this.coveredGoals.containsKey(fparent) );
				}
				if (strong.getMutation().getControlDependencies().size() == 0)
					logger.error(">> NO BRANCHES");
			}
			for (BranchCoverageGoal branchGoal : strong.getMutation().getControlDependencies()){
				BranchCoverageTestFitness branchFitness = new BranchCoverageTestFitness(branchGoal);
				double branchValue = branchFitness.getFitness((TestChromosome) c);
				if (branchValue == 0 && !currentGoals.contains(fitnessFunction) && !this.coveredGoals.keySet().contains(fitnessFunction)){
					logger.error("Mutant {} has branchfitness = 0, but is not in the current goals", fitnessFunction.toString(), value);
				}
			}
		}
	}
}
