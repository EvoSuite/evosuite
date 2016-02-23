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
package org.evosuite.coverage.statement;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.testsuite.AbstractFitnessFactory;

public class StatementCoverageFactory extends
        AbstractFitnessFactory<StatementCoverageTestFitness> {

	private static boolean called = false;
	private static List<StatementCoverageTestFitness> goals = new ArrayList<StatementCoverageTestFitness>();

	/** {@inheritDoc} */
	@Override
	public List<StatementCoverageTestFitness> getCoverageGoals() {

		if (!called)
			computeGoals();

		return goals;
	}

	private static void computeGoals() {

		if (called)
			return;
		long start = System.currentTimeMillis();
		String targetClass = Properties.TARGET_CLASS;

		final MethodNameMatcher matcher = new MethodNameMatcher();
		
		for (String className : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownClasses()) {

			if (!(targetClass.equals("") || className.endsWith(targetClass)))
				continue;

			for (String methodName : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownMethods(className)) {

				if (!matcher.methodMatches(methodName))
					continue;

				for (BytecodeInstruction ins : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getInstructionsIn(className,
				                                                                                                                             methodName))
					if (isUsable(ins))
						goals.add(new StatementCoverageTestFitness(ins));
			}
		}
		long end = System.currentTimeMillis();
		goalComputationTime = end - start;
		called = true;
	}

	private static boolean isUsable(BytecodeInstruction ins) {

		return !ins.isLabel() && !ins.isLineNumber();
	}

	/**
	 * <p>
	 * retrieveCoverageGoals
	 * </p>
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public static List<StatementCoverageTestFitness> retrieveCoverageGoals() {
		if (!called)
			computeGoals();

		return goals;
	}
}
