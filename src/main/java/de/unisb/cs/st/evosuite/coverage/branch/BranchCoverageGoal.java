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

package de.unisb.cs.st.evosuite.coverage.branch;

import java.util.List;

import de.unisb.cs.st.evosuite.cfg.ActualControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.ControlFlowDistance;
import de.unisb.cs.st.evosuite.coverage.TestCoverageGoal;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;

/**
 * A single branch coverage goal Either true/false evaluation of a jump
 * condition, or a method entry
 * 
 * @author Gordon Fraser
 * 
 */
public class BranchCoverageGoal extends TestCoverageGoal {

	// TODO: this is really redundant, only the Branch should be referenced as it 
	//			already holds all the information from branch_id to methodName  
	
	Branch branch;

	int branch_id;

	int instruction_id;

	int line_number;

	boolean value;

	ActualControlFlowGraph cfg;

	String className;

	String methodName;

	public BranchCoverageGoal(Branch branch, boolean value, ActualControlFlowGraph cfg,
	        String className, String methodName) {
		this.branch = branch;
		this.branch_id = branch.getControlDependentBranchId();
		this.instruction_id = branch.getInstructionId();
		this.line_number = branch.getLineNumber();
		this.value = value;
		this.cfg = cfg;
		this.className = className;
		this.methodName = methodName;
		//cfg.toDot(className+"."+methodName.replace("/",".").replace(";",".").replace("(",".").replace(")",".")+".dot");
	}

	/**
	 * Methods that have no branches don't need a cfg, so we just set the cfg to
	 * null
	 * 
	 * @param className
	 * @param methodName
	 */
	public BranchCoverageGoal(String className, String methodName) {
		this.branch = null;
		this.branch_id = 0;
		this.instruction_id = 0;
		this.line_number = 0;
		this.value = true;
		this.cfg = null;
		this.className = className;
		this.methodName = methodName;
	}
	
	/**
	 * Determines whether this goals is connected to the given goal
	 * 
	 * This is the case when this goals target branch is control dependent on
	 * the target branch of the given goal or visa versa
	 * 
	 * This is used in the ChromosomeRecycler to determine if tests produced
	 * to cover one goal should be used initially when trying to cover the other goal 
	 */
	public boolean isConnectedTo(BranchCoverageGoal goal) {
		if(branch == null || goal.branch == null) {
			// one of the goals targets a root branch
			return goal.methodName.equals(methodName) && goal.className.equals(className);
		}
		
		// TODO map this to new CDG !
		
		return branch.isDirectlyControlDependentOn(goal.branch) || goal.branch.isDirectlyControlDependentOn(branch) ;
	}
	
	/**
	 * Returns the number of branches  
	 */
	public int getDifficulty() {
		int r = 1;
		
		// TODO map this to new CDG !
		
		if(branch!=null) {
			r+=branch.getCDGDepth();
		}
		return r;
	}


	/**
	 * Determine if there is an existing test case covering this goal
	 * 
	 * @return
	 */
	@Override
	public boolean isCovered(TestCase test) {
		ExecutionResult result = runTest(test);
		ControlFlowDistance d = getDistance(result);
		if (d.approach == 0 && d.branch == 0.0)
			return true;
		else
			return false;
	}

	public ControlFlowDistance getDistance(ExecutionResult result) {
		ControlFlowDistance d = new ControlFlowDistance();

		if (hasTimeout(result)) {
			//logger.info("Has timeout!");
			if (cfg == null) {
				d.approach = 20;
			} else {
				d.approach = cfg.getDiameter() + 2;
			}
			return d;
		}

		// Methods that have no cfg have no branches
		if (cfg == null) {
			logger.debug("Looking for method without branches " + methodName);
			for (MethodCall call : result.getTrace().finished_calls) {
				if (call.class_name.equals(""))
					continue;
				if ((call.class_name + "." + call.method_name).equals(className + "."
				        + methodName)) {
					return d;
				}
			}
			d.approach = 1;
			return d;
		}

		d.approach = cfg.getDiameter() + 1;
		logger.debug("Looking for method with branches " + methodName);

		// Minimal distance between target node and path
		for (MethodCall call : result.getTrace().finished_calls) {
			if (call.class_name.equals(className) && call.method_name.equals(methodName)) {
				ControlFlowDistance d2;
				d2 = getDistance(call.branch_trace, call.true_distance_trace,
				                 call.false_distance_trace, instruction_id);
				if (d2.compareTo(d) < 0) {
					d = d2;
				}
			}
		}

		return d;
	}

	private ControlFlowDistance getDistance(List<Integer> path,
	        List<Double> true_distances, List<Double> false_distances, int branch_id) {
		BytecodeInstruction m = cfg.getInstruction(branch_id);
		ControlFlowDistance d = new ControlFlowDistance();
		if (m == null) {
			logger.error("Could not find branch node");
			return d;
		}

		int min_approach = cfg.getDiameter() + 1;
		double min_dist = 0.0;
		for (int i = 0; i < path.size(); i++) {
			BytecodeInstruction v = cfg.getInstruction(path.get(i));
			if (v != null) {
				int approach = cfg.getDistance(v, m);
				//logger.debug("B: Path vertex "+i+" has approach: "+approach+" and branch distance "+distances.get(i));

				if (approach <= min_approach && approach >= 0) {
					double branch_distance = 0.0;

					if (approach > 0)
						branch_distance = true_distances.get(i) + false_distances.get(i);
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

	/**
	 * Readable representation
	 */
	@Override
	public String toString() {
		String name = className + "." + methodName + ":" + line_number;
		if(branch != null)
			name += " "+branch.toString();

		if (value)
			return name + " - true";
		else
			return name + " - false";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + branch_id;
		result = prime * result + instruction_id;
		result = prime * result + ((cfg == null) ? 0 : cfg.hashCode());
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + (value ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BranchCoverageGoal other = (BranchCoverageGoal) obj;
		if (branch_id != other.branch_id)
			return false;
		if (instruction_id != other.instruction_id)
			return false;
		if (cfg == null) {
			if (other.cfg != null)
				return false;
		} else if (!cfg.equals(other.cfg))
			return false;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		if (value != other.value)
			return false;
		return true;
	}

}
