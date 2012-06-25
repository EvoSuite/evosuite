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
package org.evosuite.coverage.statement;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.evosuite.utils.LoggingUtils;


public class StatementCoverageFactory extends AbstractFitnessFactory {

	private static boolean called = false;
	private static List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();
	
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		
		if(!called)
			computeGoals();

		return goals;
	}
	
	private static void computeGoals() {
		
		if(called)
			return;
		long start = System.currentTimeMillis();
		String targetMethod = Properties.TARGET_METHOD;
		String targetClass = Properties.TARGET_CLASS;

		for (String className : BytecodeInstructionPool.knownClasses()) {

			if (!(targetClass.equals("") || className.endsWith(targetClass)))
				continue;

			for (String methodName : BytecodeInstructionPool
					.knownMethods(className)) {

				if (!targetMethod.equals("")
						&& !methodName.equals(targetMethod))
					continue;

				for (BytecodeInstruction ins : BytecodeInstructionPool.getInstructionsIn(className, methodName))
					if(isUsable(ins))
						goals.add(new StatementCoverageTestFitness(ins));
			}
		}
		long end = System.currentTimeMillis();
		LoggingUtils.getEvoLogger().info("* Total number of coverage goals: "+goals.size()+" took "+(end-start)+"ms");
		goalComputationTime = end - start;
		called = true;		
	}


	private static boolean isUsable(BytecodeInstruction ins) {
		
		return !ins.isLabel() && !ins.isLineNumber();
	}

	public static List<TestFitnessFunction> retrieveCoverageGoals() {
		if(!called)
			computeGoals();
		
		return goals;
	}
}
