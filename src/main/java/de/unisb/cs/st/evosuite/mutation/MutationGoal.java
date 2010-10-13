/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.mutation;

import java.util.List;

import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.coverage.ControlFlowDistance;
import de.unisb.cs.st.evosuite.coverage.TestCoverageGoal;
import de.unisb.cs.st.evosuite.mutation.HOM.HOMObserver;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * @author Gordon Fraser
 *
 */
public class MutationGoal extends TestCoverageGoal {

	private Mutation mutation;
	
	private ControlFlowGraph cfg;
	
	private String className;
	
	private String methodName;
	
	public MutationGoal(Mutation mutation) {
		this.mutation = mutation;
		this.className = mutation.getClassName();
		this.methodName = mutation.getMethodName();
		this.cfg = ExecutionTracer.getExecutionTracer().getCFG(className, methodName);
	}
	
	public ControlFlowDistance getDistance(ExecutionResult result) {
		ControlFlowDistance d = new ControlFlowDistance();
		
		if(hasTimeout(result)) {
			logger.info("Has timeout!");
			if(cfg == null) {
				d.approach = 20;
			} else {
				d.approach = cfg.getDiameter() + 2;
			}
			return d;
		}

		if(HOMObserver.wasTouched(mutation.getId())) {
			logger.info("Mutation was touched");
			return d;
		}
		
		if(cfg == null) {
			logger.warn("Have no cfg for method "+className+"."+methodName);
			for(MethodCall call : result.trace.finished_calls) {
				if(call.class_name.equals(""))
					continue;
				if((call.class_name+"."+call.method_name).equals(methodName)) {
					return d;
				}
			}
			d.approach = 1;
			return d;
		}
	
		d.approach = cfg.getDiameter() + 1;
		
		// Minimal distance between target node and path
		/*
		for(MethodCall m : result.trace.finished_calls) {
			if(m.class_name.equals(className) && m.method_name.equals(methodName)) {
				//logger.info("Found method call: "+className+"."+methodName);
				int distance = cfg.getControlDistance(m.branch_trace, mutation.getId());
				if(distance < d.approach) {
					d.approach = distance;
				}
			}
		}*/
		boolean method_executed = false;
		logger.debug(result.test.toCode());
		for(MethodCall call : result.trace.finished_calls) {
			if(call.class_name.equals(className) && call.method_name.equals(methodName)) {
				logger.debug("Found target call for mutant "+mutation.getId()+" in method "+className+"."+methodName);
				//logger.info(cfg.toString());
				//for(Integer i : call.branch_trace) {
				//	logger.info(" -> "+i);
				//}
				method_executed = true;
				ControlFlowDistance d2 = getDistance(call.branch_trace, call.true_distance_trace, call.false_distance_trace, call.line_trace);
				if(d2.compareTo(d) < 0) {
					d = d2;
				}
			}
		}
		if(!method_executed)
			logger.debug("Method not executed by test");
		//else
		//	cfg.toDot(className+"."+methodName.replace(";","").replace("(","").replace(")", "").replace("/",".")+".dot");
		return d;
	}
	
	private ControlFlowDistance getDistance(List<Integer> path, List<Double> true_distances, List<Double> false_distances, List<Integer> line_trace) {
		//CFGVertex m = cfg.getVertex(branch_id);
		CFGVertex m = cfg.getMutation(mutation.getId()); 
		ControlFlowDistance d = new ControlFlowDistance();
		if(m == null) {
			logger.error("Could not find mutant node ");
			return d;
		}
		
		int min_approach  = cfg.getDiameter();
		//int min_approach = cfg.getInitialDistance(m);
		logger.info("Initial distance: "+min_approach);
		double min_dist = 0.0;
		for(int i = 0; i<path.size(); i++) {
			CFGVertex v = cfg.getVertex(path.get(i));
			if(v != null) {
				int distance = cfg.getDistance(v, m);
				if(cfg.isSuccessor(m, v))
					distance = 0;
				
				logger.info("B: Path vertex "+i+"("+ v.toString()+") has distance: "+distance+" and branch distance "+Math.max(true_distances.get(i), false_distances.get(i)));

				if(distance <= min_approach && distance >= 0) {
					double branch_distance = true_distances.get(i) + false_distances.get(i);
					if(distance == 0) {
						min_approach = 0;
						min_dist = 0;
					}/*
					else if(distance == 1) {
						min_approach = 1;
						min_dist = 0;
					}*/
					else if(distance == min_approach) 
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
		d.branch   = min_dist;
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
		// TODO Auto-generated method stub
		return false;
	}

}
