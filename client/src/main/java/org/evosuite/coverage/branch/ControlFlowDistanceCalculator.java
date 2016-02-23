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
package org.evosuite.coverage.branch;

import java.util.*;

import org.evosuite.coverage.ControlFlowDistance;
import org.evosuite.coverage.TestCoverageGoal;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.MethodCall;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class holds static methods used to calculate ControlFlowDistances or in
 * other words methods to determine, how far a given ExecutionResult was away
 * from reaching a given instruction or evaluating a certain Branch in a certain
 * way - depending on your point of view.
 * 
 * The distance to a certain Branch evaluating in a certain way is calculated as
 * follows:
 * 
 * If the given result had a Timeout, the worst possible ControlFlowDistance for
 * the method at hand is returned
 * 
 * Otherwise, if the given branch was null, meaning the distance to the root
 * branch of a method should be calculated, either the 0-distance is returned,
 * should the method at hand be called in the given ExecutionResult, or
 * otherwise the 1-distance is returned
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
 * @author Andre Mis
 */
public class ControlFlowDistanceCalculator {

	private static Logger logger = LoggerFactory.getLogger(ControlFlowDistanceCalculator.class);

	// DONE hold intermediately calculated ControlFlowDistances in
	// ExecutionResult during computation in order to speed up things -
	// experiment at least 
	// ... did that, but no real speed up observed 

	/**
	 * Calculates the ControlFlowDistance indicating how far away the given
	 * ExecutionResult was from executing the given Branch in a certain way,
	 * depending on the given value.
	 * 
	 * For more information look at this class's class comment
	 * 
	 * @param result
	 *            a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 * @param branch
	 *            a {@link org.evosuite.coverage.branch.Branch} object.
	 * @param value
	 *            a boolean.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.coverage.ControlFlowDistance} object.
	 */
	public static ControlFlowDistance getDistance(ExecutionResult result, Branch branch,
	        boolean value, String className, String methodName) {
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

		if(value) {
			if (result.getTrace().getCoveredTrueBranches().contains(branch.getActualBranchId()))
				return new ControlFlowDistance(0, 0.0);
		}
		else {
			if (result.getTrace().getCoveredFalseBranches().contains(branch.getActualBranchId()))
                return new ControlFlowDistance(0, 0.0);
		}

		ControlFlowDistance nonRootDistance = getNonRootDistance(result, branch, value);

		if (nonRootDistance == null)
			throw new IllegalStateException(
			        "expect getNonRootDistance to never return null");

		return nonRootDistance;
	}

	private static ControlFlowDistance getTimeoutDistance(ExecutionResult result,
	        Branch branch) {

		if (!TestCoverageGoal.hasTimeout(result))
			throw new IllegalArgumentException("expect given result to have a timeout");
		logger.debug("Has timeout!");
		return worstPossibleDistanceForMethod(branch);
	}

	private static ControlFlowDistance worstPossibleDistanceForMethod(Branch branch) {
		ControlFlowDistance d = new ControlFlowDistance();
		if (branch == null) {
			d.setApproachLevel(20);
		} else {
			d.setApproachLevel(branch.getInstruction().getActualCFG().getDiameter() + 2);
		}
		return d;
	}

	/**
	 * If there is an exception in a superconstructor, then the corresponding
	 * constructor might not be included in the execution trace
	 * 
	 * @param results
	 * @param callCount
	 */
	private static boolean hasConstructorException(ExecutionResult result,
			String className, String methodName) {

		if (result.hasTimeout() || result.hasTestException()
				|| result.noThrownExceptions())
			return false;

		Integer exceptionPosition = result.getFirstPositionOfThrownException();
		if(!result.test.hasStatement(exceptionPosition)){
			return false;
		}
		Statement statement = result.test.getStatement(exceptionPosition);
		if (statement instanceof ConstructorStatement) {
			ConstructorStatement c = (ConstructorStatement) statement;
			String constructorClassName = c.getConstructor().getName();
			String constructorMethodName = "<init>"
					+ Type.getConstructorDescriptor(c.getConstructor().getConstructor());
			if(constructorClassName.equals(className) && constructorMethodName.equals(methodName)) {
				return true;
			}
	
		}
		return false;
	}
	
	private static ControlFlowDistance getRootDistance(ExecutionResult result,
	        String className, String methodName) {

		ControlFlowDistance d = new ControlFlowDistance();

		if(result.getTrace().getCoveredMethods().contains(className + "." + methodName)) {
			return d;
		}
		if(hasConstructorException(result, className, methodName)) {
			return d;
		}

		d.increaseApproachLevel();
		return d;
	}

	private static ControlFlowDistance getNonRootDistance(ExecutionResult result,
	        Branch branch, boolean value) {

		if (branch == null)
			throw new IllegalStateException(
			        "expect this method only to be called if this goal does not try to cover the root branch");

		String className = branch.getClassName();
		String methodName = branch.getMethodName();

		ControlFlowDistance r = new ControlFlowDistance();
		r.setApproachLevel(branch.getInstruction().getActualCFG().getDiameter() + 1);

		// Minimal distance between target node and path
		for (MethodCall call : result.getTrace().getMethodCalls()) {
			if (call.className.equals(className) && call.methodName.equals(methodName)) {
				ControlFlowDistance d2;
				Set<Branch> handled = new HashSet<Branch>();
				//				result.intermediateDistances = new HashMap<Branch,ControlFlowDistance>();
				d2 = getNonRootDistance(result, call, branch, value, className,
				                        methodName, handled);
				if (d2.compareTo(r) < 0) {
					r = d2;
				}
			}
		}

		return r;
	}

	private static ControlFlowDistance getNonRootDistance(ExecutionResult result,
	        MethodCall call, Branch branch, boolean value, String className,
	        String methodName, Set<Branch> handled) {

		if (branch == null)
			throw new IllegalStateException(
			        "expect getNonRootDistance() to only be called if this goal's branch is not a root branch");
		if (call == null)
			throw new IllegalArgumentException("null given");

		//		ControlFlowDistance r = result.intermediateDistances.get(branch);

		if (handled.contains(branch)) {
			//			if(r== null)
			return worstPossibleDistanceForMethod(branch);
			//			else {
			//				return r;
			//			}
		}
		handled.add(branch);

		List<Double> trueDistances = call.trueDistanceTrace;
		List<Double> falseDistances = call.falseDistanceTrace;

		// IDEA:
		// if this goal's branch is traced in the given path, return the
		// true_/false_distance, depending on this.value
		// otherwise, look at all Branches this.branch is control dependent on
		// and return 1 + minimum of the branch coverage goal distance over all
		// such branches taking as value the branchExpressionValue

		Set<Integer> branchTracePositions = determineBranchTracePositions(call, branch);

		if (!branchTracePositions.isEmpty()) {

			// branch was traced in given path
			ControlFlowDistance r = new ControlFlowDistance(0, Double.MAX_VALUE);

			for (Integer branchTracePosition : branchTracePositions)
				if (value)
					r.setBranchDistance(Math.min(r.getBranchDistance(),
					                             trueDistances.get(branchTracePosition)));
				else
					r.setBranchDistance(Math.min(r.getBranchDistance(),
					                             falseDistances.get(branchTracePosition)));

			if (r.getBranchDistance() == Double.MAX_VALUE)
				throw new IllegalStateException("should be impossible");

			//			result.intermediateDistances.put(branch, r);
			return r;
		}

		ControlFlowDistance controlDependenceDistance = getControlDependenceDistancesFor(result,
		                                                                                 call,
		                                                                                 branch.getInstruction(),
		                                                                                 className,
		                                                                                 methodName,
		                                                                                 handled);

		controlDependenceDistance.increaseApproachLevel();

		//		result.intermediateDistances.put(branch, controlDependenceDistance);

		return controlDependenceDistance;
	}

	private static ControlFlowDistance getControlDependenceDistancesFor(
	        ExecutionResult result, MethodCall call, BytecodeInstruction instruction,
	        String className, String methodName, Set<Branch> handled) {

		Set<ControlFlowDistance> cdDistances = getDistancesForControlDependentBranchesOf(result,
		                                                                                 call,
		                                                                                 instruction,
		                                                                                 className,
		                                                                                 methodName,
		                                                                                 handled);

		if (cdDistances == null)
			throw new IllegalStateException("expect cdDistances to never be null");

		return Collections.min(cdDistances);
	}

	/**
	 * Returns a set containing the ControlFlowDistances in the given result for
	 * all branches the given instruction is control dependent on
	 * 
	 * @param handled
	 */
	private static Set<ControlFlowDistance> getDistancesForControlDependentBranchesOf(
	        ExecutionResult result, MethodCall call, BytecodeInstruction instruction,
	        String className, String methodName, Set<Branch> handled) {

		Set<ControlFlowDistance> r = new HashSet<ControlFlowDistance>();
		Set<ControlDependency> nextToLookAt = instruction.getControlDependencies();

		for (ControlDependency next : nextToLookAt) {
			if (instruction.equals(next.getBranch().getInstruction()))
				continue; // avoid loops

			boolean nextValue = next.getBranchExpressionValue();
			ControlFlowDistance nextDistance = getNonRootDistance(result, call,
			                                                      next.getBranch(),
			                                                      nextValue, className,
			                                                      methodName, handled);
			assert (nextDistance != null);
			r.add(nextDistance);
		}

		if (r.isEmpty()) {
			// instruction only dependent on root branch
			// since this method is called by getNonRootDistance(MethodCall)
			// which in turn is only called when a MethodCall for this branch's
			// method was found in the given result, i can safely assume that
			// the 0-distance is a control dependence distance for the given
			// instruction ... right?
			r.add(new ControlFlowDistance());
		}

		return r;
	}

	private static Set<Integer> determineBranchTracePositions(MethodCall call,
	        Branch branch) {

		Set<Integer> r = new HashSet<Integer>();
		List<Integer> path = call.branchTrace;
		for (int pos = 0; pos < path.size(); pos++) {
			if (path.get(pos) == branch.getActualBranchId()) { //.getActualBranchId()); {
				r.add(pos);
			}
		}
		return r;
	}

}
