/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.mutation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.coverage.ControlFlowDistance;
import de.unisb.cs.st.evosuite.coverage.TestCoverageGoal;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * @author Gordon Fraser
 * 
 */
public class MutationGoal extends TestCoverageGoal {

	private final Mutation mutation;

	private final ControlFlowGraph cfg;

	private final String className;

	private final String methodName;

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	private AccessibleObject targetMethod = null;

	private Class<?> methodSource = null;

	private Class<?>[] parameters = null;

	public MutationGoal(Mutation mutation) {
		this.mutation = mutation;
		this.className = mutation.getClassName();
		this.methodName = mutation.getMethodName();
		//		this.cfg = ExecutionTracer.getExecutionTracer().getCFG(className, methodName);
		this.cfg = CFGMethodAdapter.getMinimizedCFG(className, methodName);
		if (this.cfg == null) {
			logger.warn("Found no CFG for " + className + "." + methodName);
		}
		try {
			Class<?> clazz = Class.forName(className);
			if (methodName.startsWith("<init>")) {
				for (Constructor<?> constructor : TestCluster.getConstructors(clazz)) {
					String name = "<init>"
					        + org.objectweb.asm.Type.getConstructorDescriptor(constructor);
					if (name.equals(methodName)) {
						this.targetMethod = constructor;
						this.parameters = constructor.getParameterTypes();
						break;
					}
				}
			} else {
				for (Method method : TestCluster.getMethods(clazz)) {
					String name = method.getName()
					        + org.objectweb.asm.Type.getMethodDescriptor(method);
					if (name.equals(methodName)) {
						this.targetMethod = method;
						this.parameters = method.getParameterTypes();
						if (!Modifier.isStatic(method.getModifiers()))
							this.methodSource = method.getDeclaringClass();
						break;
					}
				}
			}
			if (this.targetMethod == null)
				logger.error("Could not find method " + methodName);
		} catch (ClassNotFoundException e) {
			logger.error("Could not find mutated method");
		}
	}

	public Mutation getMutation() {
		return mutation;
	}

	private int getMethodDistance(ExecutionResult result) {
		int max = parameters.length;
		boolean have_callee = false;
		if (methodSource != null) {
			max++;
		} else
			have_callee = true;

		int num_satisfied = 0;
		List<Boolean> satisfied = new ArrayList<Boolean>();
		for (@SuppressWarnings("unused")
		Class<?> c : parameters) {
			satisfied.add(false);
		}

		int num = 0;
		for (StatementInterface statement : result.test.getStatements()) {
			for (int i = 0; i < parameters.length; i++) {
				if (!satisfied.get(i)) {
					if (parameters[i].isAssignableFrom(statement.getReturnValue().getVariableClass())) {
						if (result.exceptions.containsKey(num)) {
							satisfied.set(i, true);
							num_satisfied++;
						}
					}
				}
			}
			if (!have_callee) {
				if (methodSource.isAssignableFrom(statement.getReturnValue().getVariableClass())) {
					if (result.exceptions.containsKey(num)) {
						have_callee = true;
						num_satisfied++;
					}
				}
			}
			num++;
		}
		logger.debug("Satisfied " + num_satisfied + " out of " + max + " parameters");
		return max - num_satisfied; // + result.exceptions.size();
	}

	public ControlFlowDistance getDistance(ExecutionResult result) {
		ControlFlowDistance d = new ControlFlowDistance();
		logger.debug("Getting distance");

		if (hasTimeout(result)) {
			logger.debug("Has timeout!");
			if (cfg == null) {
				d.approach = 20;
			} else {
				d.approach = cfg.getDiameter() + 2;
			}
			return d;
		}

		if (result.touched.contains(mutation.getId())) {
			logger.debug("Mutation was touched");
			return d;
		}
		
		if (cfg == null) {
			logger.warn("Have no cfg for method " + className + "." + methodName);
			for (MethodCall call : result.getTrace().finished_calls) {
				if (call.class_name.equals(""))
					continue;
				if ((call.class_name + "." + call.method_name).equals(methodName)) {
					return d;
				}
			}
			d.approach = 1;
			return d;
		}

		d.approach = cfg.getDiameter() + 1;

		// Minimal distance between target node and path
		boolean method_executed = false;
		for (MethodCall call : result.getTrace().finished_calls) {
			if (call.class_name.equals(className) && call.method_name.equals(methodName)) {
				logger.debug("Found target call for mutant " + mutation.getId()
				        + " in method " + className + "." + methodName);
				//logger.info(cfg.toString());
				//for(Integer i : call.branch_trace) {
				//	logger.info(" -> "+i);
				//}
				method_executed = true;
				ControlFlowDistance d2 = getDistance(call.branch_trace,
				                                     call.true_distance_trace,
				                                     call.false_distance_trace,
				                                     call.line_trace);
				if (d2.compareTo(d) < 0) {
					d = d2;
				}
			}
		}
		if (!method_executed) {
			logger.debug("Method " + methodName + "not executed by test");
			boolean found = false;
			for (StatementInterface s : result.test.getStatements()) {
				if (s instanceof MethodStatement) {
					MethodStatement ms = (MethodStatement) s;
					Method method = ms.getMethod();
					//logger.info("Comparing with "+className+" . "+methodName);
					//logger.info(method.getDeclaringClass().getName()+" . "+method.getName());
					if (methodName.startsWith(method.getName())
					        && method.getDeclaringClass().getName().equals(className)) {
						found = true;
						break;
					}
				}
			}
			if (!found)
				d.approach++;
			d.approach += getMethodDistance(result);
		}
		//else
		//	cfg.toDot(className+"."+methodName.replace(";","").replace("(","").replace(")", "").replace("/",".")+".dot");
		return d;
	}

	private ControlFlowDistance getDistance(List<Integer> path,
	        List<Double> true_distances, List<Double> false_distances,
	        List<Integer> line_trace) {
		//CFGVertex m = cfg.getVertex(branch_id);
		BytecodeInstruction m = cfg.getMutation(mutation.getId());
		ControlFlowDistance d = new ControlFlowDistance();
		if (m == null) {
			logger.error("Could not find mutant node " + mutation.getId());
			for (Long mi : cfg.getMutations())
				logger.error("Have mutation: " + mi);
			return d;
		}

		int min_approach = cfg.getDiameter();
		//int min_approach = cfg.getInitialDistance(m);
		logger.debug("Initial distance: " + min_approach);
		double min_dist = 0.0;
		for (int i = 0; i < path.size(); i++) {
			BytecodeInstruction v = cfg.getVertex(path.get(i));
			if (v != null) {
				int distance = cfg.getDistance(v, m);
				if (cfg.isDirectSuccessor(m, v))
					distance = 0;

				logger.debug("B: Path vertex " + i + "(" + v.toString()
				        + ") has distance: " + distance + " and branch distance "
				        + Math.max(true_distances.get(i), false_distances.get(i)));

				if (distance <= min_approach && distance >= 0) {
					double branch_distance = true_distances.get(i)
					        + false_distances.get(i);
					if (distance == 0) {
						min_approach = 0;
						min_dist = 0;
					}/*
					 else if(distance == 1) {
					 min_approach = 1;
					 min_dist = 0;
					 }*/
					else if (distance == min_approach)
						min_dist = Math.min(min_dist, branch_distance);
					else {
						min_dist = branch_distance;
						min_approach = distance;
					}
				}
			} else {
				logger.info("Path vertex does not exist in graph");
			}
		}

		d.approach = min_approach;
		d.branch = min_dist;
		/*
		if(line_trace.contains(mutation.getLineNumber()) && d.approach <= 1) {
			logger.info("Mutant line was executed");
			logger.info("But approach/branch is "+d.approach+"/"+d.branch);
			return new ControlFlowDistance();
		}
		*/

		return d;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.TestCoverageGoal#isCovered(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public boolean isCovered(TestCase test) {

		return false;
	}

	/**
	 * Determines whether this goals is connected to the given goal
	 * 
	 * This is the case when this goals target branch is control dependent on
	 * the target branch of the given goal or visa versa
	 * 
	 * This is used in the ChromosomeRecycler to determine if tests produced to
	 * cover one goal should be used initially when trying to cover the other
	 * goal
	 */
	public boolean isConnectedTo(MutationGoal goal) {
		return goal.getMethodName().equals(methodName)
		        && goal.className.equals(className);
	}

}
