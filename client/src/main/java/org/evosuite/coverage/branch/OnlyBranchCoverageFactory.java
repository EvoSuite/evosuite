/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * BranchCoverageFactory class.
 * </p>
 * 
 * @author Gordon Fraser, Andre Mis, Jose Miguel Rojas
 */
public class OnlyBranchCoverageFactory extends
		AbstractFitnessFactory<OnlyBranchCoverageTestFitness> {

	private static final Logger logger = LoggerFactory.getLogger(OnlyBranchCoverageFactory.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public List<OnlyBranchCoverageTestFitness> getCoverageGoals() {
		long start = System.currentTimeMillis();
		List<OnlyBranchCoverageTestFitness> goals = new ArrayList<>();

		// logger.info("Getting branches");
		for (String className : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownClasses()) {
			if(!Properties.TARGET_CLASS.equals("")&&!className.equals(Properties.TARGET_CLASS)) continue;
			final MethodNameMatcher matcher = new MethodNameMatcher();

			// Branches
			for (String methodName : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownMethods(className)) {
				if (!matcher.methodMatches(methodName)) {
					logger.info("Method " + methodName
							+ " does not match criteria. ");
					continue;
				}

				for (Branch b : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).retrieveBranchesInMethod(className,
						methodName)) {
					if(!b.isInstrumented()) {
						goals.add(createOnlyBranchCoverageTestFitness(b, true));
						//if (!b.isSwitchCaseBranch())
						goals.add(createOnlyBranchCoverageTestFitness(b, false));
					}
				}
			}
		}
		goalComputationTime = System.currentTimeMillis() - start;
		
		return goals;
	}

	/**
	 * Create a fitness function for branch coverage aimed at executing the
	 * given ControlDependency.
	 * 
	 * @param cd
	 *            a {@link org.evosuite.graphs.cfg.ControlDependency} object.
	 * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
	 *         object.
	 */
	public static OnlyBranchCoverageTestFitness createOnlyBranchCoverageTestFitness(
			ControlDependency cd) {
		return createOnlyBranchCoverageTestFitness(cd.getBranch(),
				cd.getBranchExpressionValue());
	}

	/**
	 * Create a fitness function for branch coverage aimed at executing the
	 * Branch identified by b as defined by branchExpressionValue.
	 * 
	 * @param b
	 *            a {@link org.evosuite.coverage.branch.Branch} object.
	 * @param branchExpressionValue
	 *            a boolean.
	 * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
	 *         object.
	 */
	public static OnlyBranchCoverageTestFitness createOnlyBranchCoverageTestFitness(
			Branch b, boolean branchExpressionValue) {

		return new OnlyBranchCoverageTestFitness(new BranchCoverageGoal(b,
				branchExpressionValue, b.getClassName(), b.getMethodName()));
	}

}
