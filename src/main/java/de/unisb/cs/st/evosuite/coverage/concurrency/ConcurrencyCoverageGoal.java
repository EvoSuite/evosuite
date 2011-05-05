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

package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.coverage.ControlFlowDistance;
import de.unisb.cs.st.evosuite.coverage.TestCoverageGoal;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * A single branch coverage goal Either true/false evaluation of a jump
 * condition, or a method entry
 * 
 * @author Gordon Fraser
 * 
 */

public class ConcurrencyCoverageGoal extends TestCoverageGoal {

	private final List<SchedulingDecisionTuple> scheduleIDs;
	private final List<BranchCoverageGoal> branches;


	public ConcurrencyCoverageGoal(List<SchedulingDecisionTuple> scheduleIDs, List<BranchCoverageGoal> branches) {
		this.scheduleIDs=scheduleIDs;
		this.branches=branches;
		//cfg.toDot(className+"."+methodName.replace("/",".").replace(";",".").replace("(",".").replace(")",".")+".dot");
	}

	/**
	 * Methods that have no branches don't need a cfg, so we just set the cfg to
	 * null
	 * 
	 * @param className
	 * @param methodName
	 */
	public ConcurrencyCoverageGoal(List<SchedulingDecisionTuple> scheduleIDs) {
		this.scheduleIDs=scheduleIDs;
		this.branches=null;
	}

	/**
	 * Determine if there is an existing test case covering this goal
	 * 
	 * @return
	 */
	@Override
	public boolean isCovered(TestCase test) {
		//#TODO it would be nicer, if test cases where notified before being run and we could register our Controller somewhere. So that ExecutionResults would actually be a list of results and we could cast to the right one
		ExecutionResult result = runTest(test);
		ConcurrencyDistance d = getDistance(result, result.getTrace().concurrencyTracer); //tracer was set inside ExecutionTracer.clear()
		if (d.approach == 0 && d.branch == 0.0 && d.scheduleDistance==0)
			return true;
		else
			return false;
	}
	

	protected ConcurrencyDistance getDistance(ExecutionResult result, ConcurrencyTracer concurrencyTracer){
		ConcurrencyDistance distance = new ConcurrencyDistance();
		for(BranchCoverageGoal b : branches){
			ControlFlowDistance dist = b.getDistance(result);
			distance.approach+=dist.approach;
			distance.branch+=dist.branch;
		}
		distance.scheduleDistance = concurrencyTracer.getDistance(scheduleIDs);
		return distance;
	}


	/**
	 * Readable representation
	 */
	@Override
	public String toString() {
		return "some concurrent coverage goal";
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for(BranchCoverageGoal b : branches){
			hash+=b.hashCode();
		}
		for(SchedulingDecisionTuple t : scheduleIDs){
			hash+=t.hashCode();
		}
		return hash;
	}

	protected List<BranchCoverageGoal> getBranches(){
		if(branches!=null){
			return branches;
		}else{
			return new ArrayList<BranchCoverageGoal>();
		}
	}
	
	protected List<SchedulingDecisionTuple> getSchedule(){
		return scheduleIDs;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConcurrencyCoverageGoal other = (ConcurrencyCoverageGoal) obj;
		
		List<BranchCoverageGoal> otherBranches = other.getBranches();
		if(otherBranches.size()!=branches.size())
			return false;
		
		for(int i=0;i<branches.size();i++){
			if(!otherBranches.get(i).equals(branches.get(i)))
				return false;
		}
		
		List<SchedulingDecisionTuple> otherSchedule = other.getSchedule();
		if(otherSchedule.size()!=scheduleIDs.size())
			return false;
		
		for(int i=0;i<scheduleIDs.size();i++){
			if(!otherSchedule.get(i).equals(scheduleIDs.get(i)))
				return false;
		}
		
		return true;
	}

}
