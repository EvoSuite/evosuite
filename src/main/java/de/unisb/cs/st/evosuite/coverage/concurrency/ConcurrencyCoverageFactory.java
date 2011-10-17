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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.AbstractFitnessFactory;

/**
 * @author
 * 
 */
public class ConcurrencyCoverageFactory extends AbstractFitnessFactory {

	public static final int THREAD_COUNT = 2;

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ConcurrencyCoverageFactory.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();

		//#TODO let us assume for a moment, that we only use the true jump of an if()
		//#TODO also let us assume, that we only need on coverage goal
		//#TODO assume the TestDeadlock program
		//Also assume that controll flow is pretty straight forward

		for (Integer fieldAccessId : LockRuntime.fieldAccessIDToCFGBranch.keySet()) {
			for (Integer combination : LockRuntime.fieldAccessIDToCFGBranch.keySet()) {
				for (Integer comb1 : LockRuntime.fieldAccessIDToCFGBranch.keySet()) {
					if (fieldAccessId < combination) { //No difference if thread 0 runs through point A while 1 waits at B or the other way round

						List<SchedulingDecisionTuple> scheduleIDs = new ArrayList<SchedulingDecisionTuple>();
						scheduleIDs.add(new SchedulingDecisionTuple(0, fieldAccessId));
						scheduleIDs.add(new SchedulingDecisionTuple(1, combination));
						scheduleIDs.add(new SchedulingDecisionTuple(0, comb1));

						@SuppressWarnings("unused")
						List<BranchCoverageGoal> branches = new ArrayList<BranchCoverageGoal>();

						//#TODO branches need to be covered
						@SuppressWarnings("unused")
						ConcurrencyCoverageGoal c = new ConcurrencyCoverageGoal(
						        scheduleIDs);
					}
				}
			}
		}

		return goals;
	}

}
