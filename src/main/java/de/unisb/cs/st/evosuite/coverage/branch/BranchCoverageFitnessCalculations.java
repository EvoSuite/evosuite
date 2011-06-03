package de.unisb.cs.st.evosuite.coverage.branch;

import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.ActualControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.ControlFlowDistance;
import de.unisb.cs.st.evosuite.coverage.TestCoverageGoal;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;

/**
 * 
 * This class holds static methods used to determine the BranchCoverage fitness
 * or in other words methods to determine, how far a given ExecutionResult was
 * away from reaching a given instruction or evaluating a certain Branch in a
 * certain way - depending on your point of view.
 * 
 * The distance to a certain Branch evaluating in a certain way is calculated as
 * follows:
 * 
 * If the given result had a Timeout, the worst possible ControlFlowDistance is
 * returned
 * 
 * Otherwise, if the given branch was null, meaning the distance to the root
 * branch of a method should be calculated, either the 0-distance is returned,
 * should the method at hand be called in the given ExecutionResult, otherwise
 * the 1-distance is returned
 * 
 * Otherwise, the distance from the given ExecutionResult to evaluating the
 * given Branch to either jump (given value being true) or not jump (given value
 * being false) is calculated as follows:
 * 
 * If the given Branch was passed in the given ExecutionResult, the respective
 * true- or false-distance - depending on the given value- is taken as the
 * returned distance's branch distance with an approach level of 0. Otherwise
 * the minimum over all distances for evaluating one of the Branches that the
 * given Branch is control dependent on is returned, after adding one to that
 * distance's approach level.
 * 
 * TODO make method that just takes a BytecodeInstruction and returns the
 * minimum over all distances to it's control dependent branches
 * 
 * 
 * @author Andre Mis
 */
public class BranchCoverageFitnessCalculations {

	private static Logger logger = Logger
			.getLogger(BranchCoverageFitnessCalculations.class);

	/**
	 * Calculates the BranchCoverage distance for how far away the given ExecutionResult was from executing the given Branch in a certain way, depending on the given value.
	 * 
	 * For more information look at this class's class comment
	 */
	public static ControlFlowDistance getDistance(ExecutionResult result,
			Branch branch, boolean value, String className, String methodName) {
		if (result == null || className == null || methodName == null)
			throw new IllegalArgumentException("null given");
		if (branch == null && !value)
			throw new IllegalArgumentException(
					"expect distance for a root branch to always have value set to true");
		if (branch != null) {
			if (!branch.getMethodName().equals(methodName)
					|| !branch.getClassName().equals(className))
				throw new IllegalArgumentException(
						"expect explicitly given information about a branch to coincide with the information given by that branch");
		}

		// handle timeout in ExecutionResult
		if (TestCoverageGoal.hasTimeout(result))
			return getTimeoutDistance(result, branch);

		// if branch is null, we will just try to call the method at hand
		if (branch == null)
			return getRootDistance(result, className, methodName);

		return getNonRootDistance(result, branch, value);
	}

	private static ControlFlowDistance getTimeoutDistance(
			ExecutionResult result, Branch branch) {

		if (!TestCoverageGoal.hasTimeout(result))
			throw new IllegalArgumentException(
					"expect given result to have a timeout");

		ControlFlowDistance d = new ControlFlowDistance();
		logger.debug("Has timeout!");
		if (branch == null) {
			d.approach = 20;
		} else {
			d.approach = branch.getActualCFG().getDiameter() + 2;
		}
		return d;
	}

	private static ControlFlowDistance getRootDistance(ExecutionResult result,
			String className, String methodName) {

		ControlFlowDistance d = new ControlFlowDistance();

		logger.debug("Looking for method without branches " + methodName);
		for (MethodCall call : result.getTrace().finished_calls) {
			if (call.class_name.equals(""))
				continue;
			if ((call.class_name + "." + call.method_name).equals(className
					+ "." + methodName)) {
				return d;
			}
		}
		d.approach = 1;
		return d;
	}

	private static ControlFlowDistance getNonRootDistance(
			ExecutionResult result, Branch branch, boolean value) {
		if (branch == null)
			throw new IllegalStateException(
					"expect this method only to be called if this goal does not try to cover the root branch");

		String className = branch.getClassName();
		String methodName = branch.getMethodName();

		ControlFlowDistance d = new ControlFlowDistance();
		d.approach = branch.getActualCFG().getDiameter() + 1;
		logger.debug("Looking for method with branches " + methodName);

		// Minimal distance between target node and path
		for (MethodCall call : result.getTrace().finished_calls) {
			if (call.class_name.equals(className)
					&& call.method_name.equals(methodName)) {
				ControlFlowDistance d2;
				d2 = getNonRootDistance(call, branch, value);
				if (d2.compareTo(d) < 0) {
					d = d2;
				}
			}
		}

		return d;
	}

	private static ControlFlowDistance getNonRootDistance(MethodCall call,
			Branch branch, boolean value) {
		if (branch == null)
			throw new IllegalStateException(
					"expect getNonRootDistance() to only be called if this goal's branch is not a root branch");
		if (call == null)
			throw new IllegalArgumentException("null given");

		List<Integer> path = call.branch_trace;
		List<Double> true_distances = call.true_distance_trace;
		List<Double> false_distances = call.false_distance_trace;

		// IDEA:
		// if this goal's branch is traced in the given path, return the
		// true_/false_distance, depending on this.value
		// otherwise, look at all Branches this.branch is control dependent on
		// and return 1 + minimum of the branch coverage goal distance over all
		// such branches taking as value the branchExpressionValue

		ActualControlFlowGraph cfg = branch.getActualCFG();
		ControlFlowDistance d = new ControlFlowDistance();
		int min_approach = cfg.getDiameter() + 1;

		double min_dist = 0.0;
		for (int i = 0; i < path.size(); i++) {
			BytecodeInstruction v = cfg.getInstruction(path.get(i));
			if (v != null) {

				int approach = cfg.getDistance(v, branch);
				// logger.debug("B: Path vertex "+i+" has approach: "+approach+" and branch distance "+distances.get(i));

				if (approach <= min_approach && approach >= 0) {
					double branch_distance = 0.0;

					if (approach > 0)
						branch_distance = true_distances.get(i)
								+ false_distances.get(i);
					else if (value)
						branch_distance = true_distances.get(i);
					else
						branch_distance = false_distances.get(i);

					if (approach == min_approach)
						min_dist = Math.min(min_dist, branch_distance);
					else {
						min_approach = approach;
						min_dist = branch_distance;
					}

				}
			} else {
				logger.info("Path vertex does not exist in graph");
			}
		}

		d.approach = min_approach;
		d.branch = min_dist;

		return d;
	}

}
