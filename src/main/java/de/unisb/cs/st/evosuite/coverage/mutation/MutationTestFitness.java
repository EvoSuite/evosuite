/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.evosuite.coverage.ControlFlowDistance;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.graphs.GraphPool;
import de.unisb.cs.st.evosuite.graphs.cfg.ActualControlFlowGraph;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class MutationTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 596930765039928708L;

	protected final Mutation mutation;

	protected final Set<BranchCoverageGoal> controlDependencies = new HashSet<BranchCoverageGoal>();

	protected final int diameter;

	public MutationTestFitness(Mutation mutation) {
		this.mutation = mutation;
		controlDependencies.addAll(mutation.getControlDependencies());
		ActualControlFlowGraph cfg = GraphPool.getActualCFG(mutation.getClassName(),
		                                                  mutation.getMethodName());
		diameter = cfg.getDiameter();
	}

	public Mutation getMutation() {
		return mutation;
	}

	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	public static ExecutionResult runTest(TestCase test, Mutation mutant) {

		ExecutionResult result = new ExecutionResult(test, mutant);

		try {
			if (mutant != null)
				logger.debug("Executing test for mutant " + mutant.getId() + ": \n"
				        + test.toCode());
			else
				logger.debug("Executing test witout mutant");

			if (mutant != null)
				MutationObserver.activateMutation(mutant);
			result = executor.execute(test);
			if (mutant != null)
				MutationObserver.deactivateMutation(mutant);

			int num = test.size();
			if (!result.exceptions.isEmpty()) {
				num = result.exceptions.keySet().iterator().next();
			}

			//if (mutant == null)
			MaxStatementsStoppingCondition.statementsExecuted(num);

		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		return result;
	}

	protected double getExecutionDistance(ExecutionResult result) {
		double fitness = 0.0;
		if (!result.getTrace().touchedMutants.contains(mutation.getId()))
			fitness += diameter;

		// Get control flow distance
		if (controlDependencies.isEmpty()) {
			// If mutant was not executed, this can be either because of an exception, or because the method was not executed

			String key = mutation.getClassName() + "." + mutation.getMethodName();
			if (result.getTrace().coveredMethods.containsKey(key)) {
				logger.debug("Target method " + key + " was executed");
			} else {
				logger.debug("Target method " + key + " was not executed");
				fitness += diameter;
			}
		} else {
			ControlFlowDistance cfgDistance = null;
			for (BranchCoverageGoal dependency : controlDependencies) {
				logger.debug("Checking dependency...");
				ControlFlowDistance distance = dependency.getDistance(result);
				if (cfgDistance == null)
					cfgDistance = distance;
				else {
					if (distance.compareTo(cfgDistance) < 0)
						cfgDistance = distance;
				}
			}
			if (cfgDistance != null) {
				logger.debug("Found control dependency");
				fitness += cfgDistance.getResultingBranchFitness();
			}
		}

		return fitness;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestFitnessFunction#getFitness(de.unisb.cs.st.evosuite.testcase.TestChromosome, de.unisb.cs.st.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public abstract double getFitness(TestChromosome individual, ExecutionResult result);

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return mutation.toString();
	}
}
