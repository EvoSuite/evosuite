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


package de.unisb.cs.st.evosuite.coverage.branch;

import java.util.List;

import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.coverage.TestCoverageGoal;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;


/**
 * A single branch coverage goal
 * Either true/false evaluation of a jump condition, or a method entry
 * 
 * @author Gordon Fraser
 *
 */
public class BranchCoverageGoal extends TestCoverageGoal {

	int branch_id;
	
	int bytecode_id;
	
	boolean value;
	
	ControlFlowGraph cfg;
	
	String className;
	
	String methodName;
	
	@SuppressWarnings("unchecked")
	public class Distance implements Comparable {
		public int approach  = 0;
		public double branch = 0.0;

		public int compareTo(Object o) {
			if(o instanceof Distance) {
				Distance d = (Distance)o;
				if(approach < d.approach)
					return -1;
				else if(approach > d.approach)
					return 1;
				else {
					if(branch < d.branch)
						return -1;
					else if(branch > d.branch)
						return 1;
					else
						return 0;
				}
			}
			return 0;
		}
	}
	
	public BranchCoverageGoal(int branch_id, int bytecode_id, boolean value, ControlFlowGraph cfg, String className, String methodName) {
		this.branch_id = branch_id;
		this.bytecode_id = bytecode_id;
		this.value = value;
		this.cfg = cfg;
		this.className = className;
		this.methodName = methodName;
		cfg.toDot(className+"."+methodName.replace("/",".").replace(";",".").replace("(",".").replace(")",".")+".dot");
	}

	public BranchCoverageGoal(String className, String methodName) {
		this.branch_id = 0;
		this.bytecode_id = 0;
		this.value = true;
		this.cfg = null;
		this.className = className;
		this.methodName = methodName;
	}
	
	/**
	 * Determine if there is an existing test case covering this goal
	 * @return
	 */
	public boolean isCovered(TestCase test) {
		ExecutionResult result = runTest(test);
		Distance d = getDistance(result);
		if(d.approach == 0 && d.branch == 0.0)
			return true;
		else
			return false;
	}

	
	public Distance getDistance(ExecutionResult result) {
		Distance d = new Distance();
		
		if(hasTimeout(result)) {
			logger.info("Has timeout!");
			if(cfg == null) {
				d.approach = 20;
			} else {
				d.approach = cfg.getDiameter() + 2;
			}
			return d;
		}
		if(cfg == null) {
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
		for(MethodCall call : result.trace.finished_calls) {
			if(call.class_name.equals(className) && call.method_name.equals(methodName)) {
				Distance d2;
				d2 = getDistance(call.branch_trace, call.true_distance_trace, call.false_distance_trace, bytecode_id);
				if(d2.compareTo(d) < 0) {
					d = d2;
				}
			}
		}
		
		return d;
	}
	
	private Distance getDistance(List<Integer> path, List<Double> true_distances, List<Double> false_distances, int branch_id) {
		CFGVertex m = cfg.getVertex(branch_id);
		Distance d = new Distance();
		if(m == null) {
			logger.error("Could not find branch node");
			return d;
		}
		
		int min_approach  = cfg.getDiameter() + 1;
		double min_dist = 0.0;
		for(int i = 0; i<path.size(); i++) {
			CFGVertex v = cfg.getVertex(path.get(i));
			if(v != null) {
				int distance = cfg.getDistance(v, m);
				//logger.debug("B: Path vertex "+i+" has distance: "+distance+" and branch distance "+distances.get(i));

				if(distance <= min_approach && distance >= 0) {
					double branch_distance = 0.0;
					
					/*
					if(value)
						branch_distance = true_distances.get(i);
					else
						branch_distance = false_distances.get(i);
					min_approach = distance;
					min_dist = branch_distance;
					*/
					
					if(distance > 0)
						branch_distance = true_distances.get(i) + false_distances.get(i);
					else if(value)
						branch_distance = true_distances.get(i);
					else
						branch_distance = false_distances.get(i);
					
					if(distance == min_approach)
						min_dist = Math.min(min_dist, branch_distance);
					else {
						min_approach = distance;
						min_dist = branch_distance;						
					}
					
				}
			} else {
				logger.info("Path vertex does not exist in graph");
			}
		}

		d.approach = min_approach;
		d.branch   = min_dist;
		
		return d;
	}


	/**
	 * Readable representation
	 */
	public String toString() {
		String name = className+"."+methodName+":"+branch_id;
		if(cfg == null)
			return name;
		if(value)
			return name+" - true";
		else
			return name+" - false";
	}
}
