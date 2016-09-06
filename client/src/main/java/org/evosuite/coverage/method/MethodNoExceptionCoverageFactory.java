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
package org.evosuite.coverage.method;

import org.evosuite.Properties;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.setup.TestUsageChecker;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;



/**
 * <p>
 * MethodNoExceptionCoverageFactory class.
 * </p>
 *
 * @author Gordon Fraser, Andre Mis, Jose Miguel Rojas
 */
public class MethodNoExceptionCoverageFactory extends
		AbstractFitnessFactory<MethodNoExceptionCoverageTestFitness> {

	private static final Logger logger = LoggerFactory.getLogger(MethodNoExceptionCoverageFactory.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public List<MethodNoExceptionCoverageTestFitness> getCoverageGoals() {
		List<MethodNoExceptionCoverageTestFitness> goals = new ArrayList<MethodNoExceptionCoverageTestFitness>();

		long start = System.currentTimeMillis();

        String className = Properties.TARGET_CLASS;
		Class<?> clazz = Properties.getTargetClassAndDontInitialise();
		if (clazz != null) {
			goals.addAll(getCoverageGoals(clazz, className));
			Class<?>[] innerClasses = clazz.getDeclaredClasses();
			for (Class<?> innerClass : innerClasses) {
				String innerClassName = innerClass.getCanonicalName();
				goals.addAll(getCoverageGoals(innerClass, innerClassName));
			}
		}
		goalComputationTime = System.currentTimeMillis() - start;
		return goals;
	}


	private List<MethodNoExceptionCoverageTestFitness> getCoverageGoals(Class<?> clazz, String className) {
		List<MethodNoExceptionCoverageTestFitness> goals = new ArrayList<>();
		Constructor<?>[] allConstructors = clazz.getDeclaredConstructors();
		for (Constructor<?> c : allConstructors) {
			if (TestUsageChecker.canUse(c)) {
				String methodName = "<init>" + Type.getConstructorDescriptor(c);
				logger.info("Adding goal for constructor " + className + "." + methodName);
				goals.add(new MethodNoExceptionCoverageTestFitness(className, methodName));
			}
		}
		Method[] allMethods = clazz.getDeclaredMethods();
		for (Method m : allMethods) {
			if (TestUsageChecker.canUse(m)) {
				String methodName = m.getName() + Type.getMethodDescriptor(m);
				logger.info("Adding goal for method " + className + "." + methodName);
				goals.add(new MethodNoExceptionCoverageTestFitness(className, methodName));
			}
		}
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
	public static MethodNoExceptionCoverageTestFitness createMethodTestFitness(
			String className, String method) {

		return new MethodNoExceptionCoverageTestFitness(className,
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
	public static MethodNoExceptionCoverageTestFitness createMethodTestFitness(
			BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");

		return createMethodTestFitness(instruction.getClassName(),
				instruction.getMethodName());
	}
}
