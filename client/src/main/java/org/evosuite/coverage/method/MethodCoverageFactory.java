/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.method;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * MethodCoverageFactory class.
 * </p>
 * 
 * @author Gordon Fraser, Andre Mis, Jose Miguel Rojas
 */
public class MethodCoverageFactory extends
		AbstractFitnessFactory<MethodCoverageTestFitness> {

	private static final Logger logger = LoggerFactory.getLogger(MethodCoverageFactory.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public List<MethodCoverageTestFitness> getCoverageGoals() {
		List<MethodCoverageTestFitness> goals = new ArrayList<MethodCoverageTestFitness>();

		long start = System.currentTimeMillis();
		String targetClass = Properties.TARGET_CLASS;

		final MethodNameMatcher matcher = new MethodNameMatcher();
		for (String className : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownClasses()) {
			if (!(targetClass.equals("") || className.endsWith(targetClass)))
				continue ;
			for (String methodName : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownMethods(className)) {
				if (!matcher.methodMatches(methodName))
					continue ;
				logger.info("Adding goal for method " + className + "." + methodName);
				goals.add(new MethodCoverageTestFitness(className,methodName));
			}
		}
		goalComputationTime = System.currentTimeMillis() - start;
		return goals;
	}



	/**
	 * Create a fitness function for branch coverage aimed at covering the root
	 * branch of the given method in the given class. Covering a root branch
	 * means entering the method.
	 *
	 * @param className
	 *            a {@link String} object.
	 * @param method
	 *            a {@link String} object.
	 * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
	 *         object.
	 */
	public static MethodCoverageTestFitness createMethodTestFitness(
			String className, String method) {

		return new MethodCoverageTestFitness(className,
				method.substring(method.lastIndexOf(".") + 1));
	}

	/**
	 * Convenience method calling createMethodTestFitness(class,method) with
	 * the respective class and method of the given BytecodeInstruction.
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
	 *         object.
	 */
	public static MethodCoverageTestFitness createMethodTestFitness(
			BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");

		return createMethodTestFitness(instruction.getClassName(),
				instruction.getMethodName());
	}
}
