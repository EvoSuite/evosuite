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

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * MethodTraceCoverageFactory class.
 * </p>
 *
 * Measures coverage of methods by analysing execution traces,
 * that is, the method can be covered by indirect calls, not
 * necessarily be an statement in a test case.
 *
 * @author Gordon Fraser, Andre Mis, Jose Miguel Rojas
 */
public class MethodTraceCoverageFactory extends
		AbstractFitnessFactory<MethodTraceCoverageTestFitness> {

	private static final Logger logger = LoggerFactory.getLogger(MethodTraceCoverageFactory.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public List<MethodTraceCoverageTestFitness> getCoverageGoals() {
		List<MethodTraceCoverageTestFitness> goals = new ArrayList<MethodTraceCoverageTestFitness>();

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
				goals.add(new MethodTraceCoverageTestFitness(className,methodName));
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
	 *            a {@link java.lang.String} object.
	 * @param method
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
	 *         object.
	 */
	public static MethodTraceCoverageTestFitness createMethodTestFitness(
			String className, String method) {

		return new MethodTraceCoverageTestFitness(className,
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
	public static MethodTraceCoverageTestFitness createMethodTestFitness(
			BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");

		return createMethodTestFitness(instruction.getClassName(),
				instruction.getMethodName());
	}
}
