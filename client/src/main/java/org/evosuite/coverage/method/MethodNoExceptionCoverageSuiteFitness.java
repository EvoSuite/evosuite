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


import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.testcase.*;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Fitness function for a whole test suite for all methods considering only normal behaviour (no exceptions)
 * 
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class MethodNoExceptionCoverageSuiteFitness extends MethodCoverageSuiteFitness {

	private static final long serialVersionUID = -704561530935529634L;

	private final static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

	/**
	 * Initialize the set of known coverage goals
	 */
	@Override
	protected void determineCoverageGoals() {
		List<MethodNoExceptionCoverageTestFitness> goals = new MethodNoExceptionCoverageFactory().getCoverageGoals();
		for (MethodNoExceptionCoverageTestFitness goal : goals) {
            methodCoverageMap.put(goal.getClassName() + "." + goal.getMethod(), goal);
			if(Properties.TEST_ARCHIVE)
				TestsArchive.instance.addGoalToCover(this, goal);
		}
	}

	/**
	 * Iterate over all execution results and summarize statistics
	 * 
	 * @param results
	 * @param calledMethods
	 * @return
	 */
	@Override
	protected boolean analyzeTraces(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
			List<ExecutionResult> results,
	        Set<String> calledMethods) {
		boolean hasTimeoutOrTestException = false;

		for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                hasTimeoutOrTestException = true;
            }


            List<Integer> exceptionPositions = asSortedList(result.getPositionsWhereExceptionsWereThrown());
            for (Statement stmt : result.test) {
                if (! isValidPosition(exceptionPositions, stmt.getPosition()))
                    break;
                if ((stmt instanceof MethodStatement || stmt instanceof ConstructorStatement)
                        && (! exceptionPositions.contains(stmt.getPosition()))) {
					EntityWithParametersStatement ps = (EntityWithParametersStatement)stmt;
					String className  = ps.getDeclaringClassName();
					String methodDesc = ps.getDescriptor();
					String methodName = ps.getMethodName() + methodDesc;
                    String fullName = className + "." + methodName;
    				if(!methods.contains(fullName)||removedMethods.contains(fullName)) continue;
                    if (methodCoverageMap.containsKey(fullName)) {
                        calledMethods.add(fullName);
                        result.test.addCoveredGoal(methodCoverageMap.get(fullName));
    					if(Properties.TEST_ARCHIVE) {
    						TestsArchive.instance.putTest(this, methodCoverageMap.get(fullName), result);
    						toRemoveMethods.add(fullName);
    						suite.isToBeUpdated(true);
    					}

                    }
                }
            }
        }
		return hasTimeoutOrTestException;
	}

    private boolean isValidPosition(List<Integer> exceptionPositions, Integer position) {
        if (Properties.BREAK_ON_EXCEPTION) {
            return exceptionPositions.isEmpty() ? true : position > exceptionPositions.get(0);
        } else
            return true;


    }


    @Override
    protected void handleConstructorExceptions(
    		AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
    		List<ExecutionResult> results, Set<String> calledMethods) {
    	return; // No-op
    }
	

	/**
	 * Some useful debug information
	 *
	 * @param coveredMethods
	 * @param fitness
	 */
	@Override
	protected void printStatusMessages(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
	        int coveredMethods, double fitness) {
		if (coveredMethods > maxCoveredMethods) {
			logger.info("(Methods No-Exc) Best individual covers " + coveredMethods + "/"
			        + totalMethods + " methods");
			maxCoveredMethods = coveredMethods;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
		if (fitness < bestFitness) {
			logger.info("(Fitness) Best individual covers " + coveredMethods + "/"
			        + totalMethods + " methods");
			bestFitness = fitness;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());

		}
	}

}
