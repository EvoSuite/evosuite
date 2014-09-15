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
package org.evosuite.coverage.output;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jose Miguel Rojas
 *
 */
public class OutputCoverageFactory extends AbstractFitnessFactory<OutputCoverageTestFitness>  {

	private static final Logger logger = LoggerFactory.getLogger(OutputCoverageFactory.class);
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public List<OutputCoverageTestFitness> getCoverageGoals() {
		List<OutputCoverageTestFitness> goals = new ArrayList<OutputCoverageTestFitness>();

		long start = System.currentTimeMillis();
		String targetClass = Properties.TARGET_CLASS;

		final MethodNameMatcher matcher = new MethodNameMatcher();
		for (String className : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownClasses()) {
			if (!(targetClass.equals("") || className.endsWith(targetClass)))
				continue ;
			for (Method method : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getClass().getMethods()) {
				if (!matcher.methodMatches(method.getName()))
					continue ;
				logger.info("Adding goal for method " + className + "." + method.getName());
				
				Class<?> type = method.getReturnType();
				if (type.isPrimitive()) {
					if (type.equals(java.lang.Boolean.TYPE)) {
						// Create two goals True and False
						//goals.add(new OutputCoverageTestFitness(className,method.getName()));
					} else if  (type.equals(Number.class)) {
						// Create three goals negative, zero, positive
					}
				} else {
					// Create two goals: null and non-null
				}
			}
		}		
		goalComputationTime = System.currentTimeMillis() - start;
		return goals;
	}

}
