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

package de.unisb.cs.st.evosuite.mutation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.CFGPool;
import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.mutation.HOM.HOMSwitcher;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * @author Gordon Fraser
 * 
 */
public class MutationGoalFactory implements TestFitnessFactory {

	private static Logger logger = Logger.getLogger(TestFitnessFactory.class);

	private boolean isDeprecated(Mutation mutation) {
		// FIXME: @Deprecated annotation seems to get lost somewhere!
		/*
		Class<?> mutationClass = Properties.getTargetClass();
		for (Method method : mutationClass.getDeclaredMethods()) {
			String name = method.getName() + Type.getMethodDescriptor(method);
			if (name.equals(methodName)) {
				logger.info("Found mutant method: " + methodName);
				if (method.getAnnotation(Deprecated.class) != null)
					return true;
			}
		}
		*/
		if (CFGPool.getMinimizedCFG(mutation.getClassName(),
		                                     mutation.getMethodName()) == null)
			return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		HOMSwitcher hom_switcher = new HOMSwitcher();
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();
		for (Mutation mutation : hom_switcher.getMutants()) {

			if (mutation.getMethodName().equals("<clinit>()V")) {
				logger.info("Skipping mutant in static constructor");
				continue;
			}

			if (!Properties.USE_DEPRECATED && isDeprecated(mutation)) {
				logger.info("Skipping mutant in deprecated method "
				        + mutation.getMethodName());
				continue;
			}
			goals.add(new MutationTestFitness(mutation));
		}

		return goals;
	}
}
