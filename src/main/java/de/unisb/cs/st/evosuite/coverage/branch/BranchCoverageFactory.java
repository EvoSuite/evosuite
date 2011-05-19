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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.ActualControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.CFGPool;
import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.testability.TransformationHelper;

/**
 * @author Gordon Fraser
 * 
 */
public class BranchCoverageFactory implements TestFitnessFactory {

	private static Logger logger = Logger.getLogger(BranchCoverageFactory.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();

		String targetMethod = Properties.TARGET_METHOD;

		// Branchless methods
		String class_name = Properties.TARGET_CLASS;
		for (String method : BranchPool.getBranchlessMethods()) {
			if (targetMethod.equals("") || method.endsWith(targetMethod))
				goals.add(new BranchCoverageTestFitness(new BranchCoverageGoal(
				        class_name, method.substring(method.lastIndexOf(".") + 1))));
		}
		// Branches
		logger.info("Getting branches");
		for (String className : BranchPool.knownClasses()) {
			for (String methodName : BranchPool.knownMethods(className)) {

				if (!targetMethod.equals("") && !methodName.equals(targetMethod)) {
					logger.info("Method " + methodName + " does not equal target method "
					        + targetMethod);
					continue;
				}

				if (Properties.TESTABILITY_TRANSFORMATION) {
					String vname = methodName.replace("(", "|(");
					if (TransformationHelper.hasValkyrieMethod(className, vname)) {
						logger.info("Skipping branch in transformed method: " + vname);
						continue;
					} else {
						logger.info("Keeping branch in untransformed method: " + vname);
					}
				}

				// Get CFG of method
				ActualControlFlowGraph cfg = CFGPool.getActualCFG(className,methodName);

				for (Branch b : BranchPool.retrieveBranchesInMethod(className,methodName)) {

					// Identify vertex in CFG
					goals.add(new BranchCoverageTestFitness(new BranchCoverageGoal(b,
					        true, cfg, className, methodName)));
					goals.add(new BranchCoverageTestFitness(new BranchCoverageGoal(b,
					        false, cfg, className, methodName)));
				}
			}
		}

		return goals;
	}

}
