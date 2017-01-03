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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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


	protected static boolean isUsable(Method m) {
		return !m.isSynthetic() &&
		       !m.isBridge() &&
		       !Modifier.isNative(m.getModifiers()) &&
		       !m.getName().contains("<clinit>");
	}
	
	protected static boolean isUsable(Constructor<?> c) {
		return !c.isSynthetic() &&
		       !Modifier.isNative(c.getModifiers()) &&
		       !c.getName().contains("<clinit>");
	}
	
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

        String className = Properties.TARGET_CLASS;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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

	private List<MethodTraceCoverageTestFitness> getCoverageGoals(Class<?> clazz, String className) {
		List<MethodTraceCoverageTestFitness> goals = new ArrayList<>();
		Constructor<?>[] allConstructors = clazz.getDeclaredConstructors();
		for (Constructor<?> c : allConstructors) {
			if (TestUsageChecker.canUse(c)) {
				String methodName = "<init>" + Type.getConstructorDescriptor(c);
				logger.info("Adding goal for constructor " + className + "." + methodName);
				goals.add(new MethodTraceCoverageTestFitness(className, methodName));
			}
		}
		Method[] allMethods = clazz.getDeclaredMethods();
		for (Method m : allMethods) {
			if (TestUsageChecker.canUse(m)) {
				String methodName = m.getName() + Type.getMethodDescriptor(m);
				logger.info("Adding goal for method " + className + "." + methodName);
				goals.add(new MethodTraceCoverageTestFitness(className, methodName));
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
