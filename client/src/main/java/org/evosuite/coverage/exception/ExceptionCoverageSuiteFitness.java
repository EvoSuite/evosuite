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
package org.evosuite.coverage.exception;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Exception fitness is different from the others, as we do not know a priori how
 * many exceptions could be thrown in the SUT. In other words, we cannot really
 * speak about coverage percentage here
 */
public class ExceptionCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 1565793073526627496L;

	private static Logger logger = LoggerFactory.getLogger(ExceptionCoverageSuiteFitness.class);

    private static int maxExceptionsCovered = 0;
    

	public ExceptionCoverageSuiteFitness() {
	}
	
    public static int getMaxExceptionsCovered() {
        return maxExceptionsCovered;
    }

	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		logger.trace("Calculating exception fitness");


		/*
		 * for each method in the SUT, we keep track of which kind of exceptions were thrown.
		 * we distinguish between "implicit", "explicit" and "declared"
		 */
		Map<String, Set<Class<?>>> implicitTypesOfExceptions = new HashMap<>();
        Map<String, Set<Class<?>>> explicitTypesOfExceptions = new HashMap<>();
        Map<String, Set<Class<?>>> declaredTypesOfExceptions = new HashMap<>();

		List<ExecutionResult> results = runTestSuite(suite);

		calculateExceptionInfo(results,implicitTypesOfExceptions,explicitTypesOfExceptions,declaredTypesOfExceptions, this);
		
		if(Properties.TEST_ARCHIVE) {
			// If we are using the archive, then fitness is by definition 0
			// as all assertions already covered are in the archive
			suite.setFitness(this,  0.0);
			suite.setCoverage(this, 1.0);
			maxExceptionsCovered = ExceptionCoverageFactory.getGoals().size();
			return 0.0;
		}
		
		int nExc = getNumExceptions(implicitTypesOfExceptions) + getNumExceptions(explicitTypesOfExceptions) +
                getNumExceptions(declaredTypesOfExceptions);

        if (nExc > maxExceptionsCovered) {
            logger.info("(Exceptions) Best individual covers " + nExc + " exceptions");
            maxExceptionsCovered = nExc;
        }

        // We cannot set a coverage here, as it does not make any sense
       	// suite.setCoverage(this, 1.0);

		double exceptionFitness = 1d / (1d + nExc);

        suite.setFitness(this, exceptionFitness);
        if(maxExceptionsCovered > 0)
        	suite.setCoverage(this, nExc / maxExceptionsCovered);
        else
        	suite.setCoverage(this, 1.0);
        
        return exceptionFitness;
	}

	
	
	/**
	 * Given the list of results, fill the 3 given (empty) maps with exception information.
	 * Also, add exception coverage goals to mapping in {@link ExceptionCoverageFactory}
	 * 
	 * @param results
	 * @param implicitTypesOfExceptions
	 * @param explicitTypesOfExceptions
     * @param declaredTypesOfExceptions
	 * @throws IllegalArgumentException
	 */
	public static void calculateExceptionInfo(List<ExecutionResult> results, 
			Map<String, Set<Class<?>>> implicitTypesOfExceptions, Map<String, Set<Class<?>>> explicitTypesOfExceptions,
            Map<String, Set<Class<?>>> declaredTypesOfExceptions, ExceptionCoverageSuiteFitness contextFitness)
		throws IllegalArgumentException{
		
		if(results==null || implicitTypesOfExceptions==null || explicitTypesOfExceptions==null ||
				!implicitTypesOfExceptions.isEmpty() || !explicitTypesOfExceptions.isEmpty() ||
                declaredTypesOfExceptions==null || !declaredTypesOfExceptions.isEmpty()){
			throw new IllegalArgumentException();
		}

		// for each test case
		for (ExecutionResult result : results) {

			// Using private reflection can lead to false positives
			// that represent unrealistic behaviour. Thus, we only
			// use reflection for basic criteria, not for exception
			if(result.calledReflection())
				continue;

			//iterate on the indexes of the statements that resulted in an exception
			for (Integer i : result.getPositionsWhereExceptionsWereThrown()) {
				if(ExceptionCoverageHelper.shouldSkip(result,i)){
					continue;
				}

				Class<?> exceptionClass = ExceptionCoverageHelper.getExceptionClass(result,i);
				String methodIdentifier = ExceptionCoverageHelper.getMethodIdentifier(result, i); //eg name+descriptor
				boolean sutException = ExceptionCoverageHelper.isSutException(result,i); // was the exception originated by a direct call on the SUT?

				/*
				 * We only consider exceptions that were thrown by calling directly the SUT (not the other
				 * used libraries). However, this would ignore cases in which the SUT is indirectly tested
				 * through another class
				 */

				if (sutException) {

					boolean notDeclared = ! ExceptionCoverageHelper.isDeclared(result,i);

                    if(notDeclared) {
                        /*
					     * we need to distinguish whether it is explicit (ie "throw" in the code, eg for validating
					     * input for pre-condition) or implicit ("likely" a real fault).
					     */

                        boolean isExplicit = ExceptionCoverageHelper.isExplicit(result,i);

                        if (isExplicit) {

                            if (!explicitTypesOfExceptions.containsKey(methodIdentifier)) {
                                explicitTypesOfExceptions.put(methodIdentifier, new HashSet<Class<?>>());
                            }
                            explicitTypesOfExceptions.get(methodIdentifier).add(exceptionClass);
                        } else {

                            if (!implicitTypesOfExceptions.containsKey(methodIdentifier)) {
                                implicitTypesOfExceptions.put(methodIdentifier, new HashSet<Class<?>>());
                            }
                            implicitTypesOfExceptions.get(methodIdentifier).add(exceptionClass);
                        }
                    } else {
                        if (!declaredTypesOfExceptions.containsKey(methodIdentifier)) {
                            declaredTypesOfExceptions.put(methodIdentifier, new HashSet<Class<?>>());
                        }
                        declaredTypesOfExceptions.get(methodIdentifier).add(exceptionClass);
                    }


					ExceptionCoverageTestFitness.ExceptionType type = ExceptionCoverageHelper.getType(result,i);
                    /*
                     * Add goal to ExceptionCoverageFactory
                     */
                    ExceptionCoverageTestFitness goal = new ExceptionCoverageTestFitness(Properties.TARGET_CLASS, methodIdentifier, exceptionClass, type);
                    String key = goal.getKey();
                    if(!ExceptionCoverageFactory.getGoals().containsKey(key)) {
                    	ExceptionCoverageFactory.getGoals().put(key, goal);
                    	if(Properties.TEST_ARCHIVE && contextFitness != null) {
                    		TestsArchive.instance.addGoalToCover(contextFitness, goal);
                    		TestsArchive.instance.putTest(contextFitness, goal, result);
                    	}
                    }
				}

			}
		}
	}
	
	public static int getNumExceptions(Map<String, Set<Class<?>>> exceptions) {
		int total = 0;
		for (Set<Class<?>> exceptionSet : exceptions.values()) {
			total += exceptionSet.size();
		}
		return total;
	}

	public static int getNumClassExceptions(Map<String, Set<Class<?>>> exceptions) {
		Set<Class<?>> classExceptions = new HashSet<Class<?>>();
		for (Set<Class<?>> exceptionSet : exceptions.values()) {
			classExceptions.addAll(exceptionSet);
		}
		return classExceptions.size();
	}
}
