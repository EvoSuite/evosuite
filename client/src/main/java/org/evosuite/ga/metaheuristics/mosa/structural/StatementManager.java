package org.evosuite.ga.metaheuristics.mosa.structural;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.coverage.statement.StatementCoverageTestFitness;
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
 * @author Annibale Panichella, Fitsum Meshesha Kifetew
 * 
 */
public class StatementManager<T extends Chromosome> extends StructuralGoalManager<T>{

	protected StatementFitnessGraph<T, FitnessFunction<T>> graph;

	private static final Logger logger = LoggerFactory.getLogger(StatementManager.class);

	protected Map<Integer, List<StatementCoverageTestFitness>> line2Statement;

	/**
	 * Constructor used to initialize the set of uncovered goals, and the initial set
	 * of goals to consider as initial contrasting objectives
	 * @param fitnessFunctions List of all FitnessFunction<T>
	 */
	public StatementManager(List<FitnessFunction<T>> fitnessFunctions){
		super(fitnessFunctions);
		// initialize uncovered goals
		uncoveredGoals.addAll(fitnessFunctions);
		if (fitnessFunctions.size() != uncoveredGoals.size()){
			logger.error("THERE IS A PROBLEM IN StatementCoverageTestFitness.equals()");
		}

		graph = new StatementFitnessGraph<T, FitnessFunction<T>>(uncoveredGoals);
		this.currentGoals.addAll(graph.rootStatements);

		line2Statement = new HashMap<Integer, List<StatementCoverageTestFitness>>();
		for (FitnessFunction<T> f : uncoveredGoals){
			StatementCoverageTestFitness stmt = (StatementCoverageTestFitness) f;
			int line = stmt.getGoalInstruction().getLineNumber();
			if (line2Statement.containsKey(line)){
				line2Statement.get(line).add(stmt);
			} else {
				List<StatementCoverageTestFitness> list = new ArrayList<StatementCoverageTestFitness>();
				list.add(stmt);
				line2Statement.put(line, list);
			}
		}
	}


	@SuppressWarnings("unchecked")
	public void calculateFitness(T c){
		// run the test
		TestCase test = ((TestChromosome) c).getTestCase();
		ExecutionResult result = TestCaseExecutor.runTest(test);
		((TestChromosome) c).setLastExecutionResult(result);
		c.setChanged(false);

		if (result.hasTimeout() || result.hasTestException()){
			for (FitnessFunction<T> f : uncoveredGoals)
				c.setFitness(f, Double.MAX_VALUE);
			return;
		}

		updateStatementWithNegativeLines(c);
		
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
					if (!visitedStatements.contains(child))
						targets.addLast(child);
				}
			} else {
				currentGoals.add(fitnessFunction);
			}	
		}
		// 2) we update the archive
		for (Integer line : result.getTrace().getCoveredLines()){
			List<StatementCoverageTestFitness> list = this.line2Statement.get(line);
			if (list == null)
				continue;
			for (StatementCoverageTestFitness stmt : list){
				if (c.getFitness(stmt) == 0.0) {
					if (!isAlreadyCovered((FitnessFunction<T>) stmt)){
						for (FitnessFunction<T> child : graph.getStructuralChildren((FitnessFunction<T>) stmt)){
							if (!isAlreadyCovered(child))
								this.currentGoals.add(child);
						}
					}
					updateCoveredGoals((FitnessFunction<T>) stmt, c);
				} else {
					if (!isAlreadyCovered((FitnessFunction<T>) stmt))
						this.currentGoals.add((FitnessFunction<T>) stmt);
				}
			}
		}
		//debugStructuralDependencies(c);
	}


	@SuppressWarnings("unchecked")
	protected void updateStatementWithNegativeLines(T c){
		List<StatementCoverageTestFitness> list = this.line2Statement.get(-1);
		if (list == null)
			return;
		for (StatementCoverageTestFitness stmt : list){
			double value = c.getFitness(stmt);
			if (value == 0.0){
				updateCoveredGoals((FitnessFunction<T>) stmt, c);
				for (FitnessFunction<T> child : graph.getStructuralChildren((FitnessFunction<T>) stmt)){
					this.currentGoals.add(child);
				}
			} else {
				this.currentGoals.add((FitnessFunction<T>) stmt);
			}
		}
	}

	protected void debugStructuralDependencies(T c){
		for (FitnessFunction<T> fitnessFunction : this.uncoveredGoals) {
			double value = fitnessFunction.getFitness(c);
			if (value <1 && !currentGoals.contains(fitnessFunction) && !this.coveredGoals.keySet().contains(fitnessFunction)) {
				StatementCoverageTestFitness stmt = (StatementCoverageTestFitness) fitnessFunction;
				int line = stmt.getGoalInstruction().getLineNumber();
				logger.error("Branch {}, line {},  has fitness {} but is not in the current goals", fitnessFunction.toString(), line, value);
			}
		}
	}
}
