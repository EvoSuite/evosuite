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
package org.evosuite.strategy;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.ProgressMonitor;
import org.evosuite.Properties;
import org.evosuite.Properties.Algorithm;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.rmi.ClientServices;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxFitnessEvaluationsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxGenerationStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import org.evosuite.setup.TestCluster;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.LoggingUtils;

/**
 * This is the abstract superclass of all techniques to generate a set of tests
 * for a target class, which does not neccessarily require the use of a GA.
 * 
 * Postprocessing is not done as part of the test generation strategy.
 * 
 * @author gordon
 *
 */
public abstract class TestGenerationStrategy {

	/**
	 * Generate a set of tests; assume that all analyses are already completed
	 * @return
	 */
	public abstract TestSuiteChromosome generateTests();
	
	/** There should only be one */
	protected final ProgressMonitor progressMonitor = new ProgressMonitor();

	/** There should only be one */
	protected ZeroFitnessStoppingCondition zeroFitness = new ZeroFitnessStoppingCondition();
	
	/** There should only be one */
	protected StoppingCondition globalTime = new GlobalTimeStoppingCondition();

    protected void sendExecutionStatistics() {
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Statements_Executed, MaxStatementsStoppingCondition.getNumExecutedStatements());
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Tests_Executed, MaxTestsStoppingCondition.getNumExecutedTests());
    }
    
    /**
     * Convert criterion names to test suite fitness functions
     * @return
     */
	protected List<TestSuiteFitnessFunction> getFitnessFunctions() {
	    List<TestSuiteFitnessFunction> ffs = new ArrayList<TestSuiteFitnessFunction>();
	    for (int i = 0; i < Properties.CRITERION.length; i++) {
	    	TestSuiteFitnessFunction newFunction = FitnessFunctions.getFitnessFunction(Properties.CRITERION[i]);
	    	
	    	// If this is compositional fitness, we need to make sure
	    	// that all functions are consistently minimization or 
	    	// maximization functions
	    	if(Properties.ALGORITHM != Algorithm.NSGAII) {
	    		for(TestSuiteFitnessFunction oldFunction : ffs) {			
	    			if(oldFunction.isMaximizationFunction() != newFunction.isMaximizationFunction()) {
	    				StringBuffer sb = new StringBuffer();
	    				sb.append("* Invalid combination of fitness functions: ");
	    				sb.append(oldFunction.toString());
	    				if(oldFunction.isMaximizationFunction())
	    					sb.append(" is a maximization function ");
	    				else
	    					sb.append(" is a minimization function ");
	    				sb.append(" but ");
	    				sb.append(newFunction.toString());
	    				if(newFunction.isMaximizationFunction())
	    					sb.append(" is a maximization function ");
	    				else
	    					sb.append(" is a minimization function ");
	    				LoggingUtils.getEvoLogger().info(sb.toString());
	    				throw new RuntimeException("Invalid combination of fitness functions");
	    			}
	    		}
	    	}
	        ffs.add(newFunction);

	    }

		return ffs;
	}
	
	/**
	 * Convert criterion names to factories for test case fitness functions
	 * @return
	 */
	public static List<TestFitnessFactory<? extends TestFitnessFunction>> getFitnessFactories() {
	    List<TestFitnessFactory<? extends TestFitnessFunction>> goalsFactory = new ArrayList<TestFitnessFactory<? extends TestFitnessFunction>>();
	    for (int i = 0; i < Properties.CRITERION.length; i++) {
	        goalsFactory.add(FitnessFunctions.getFitnessFactory(Properties.CRITERION[i]));
	    }

		return goalsFactory;
	}
	
	/**
	 * Check if the budget has been used up. The GA will do this check
	 * on its own, but other strategies (e.g. random) may depend on this function.
	 * 
	 * @param chromosome
	 * @param stoppingCondition
	 * @return
	 */
	protected boolean isFinished(TestSuiteChromosome chromosome, StoppingCondition stoppingCondition) {
		if (stoppingCondition.isFinished())
			return true;

		if (Properties.STOP_ZERO) {
			if (chromosome.getFitness() == 0.0)
				return true;
		}

		if (!(stoppingCondition instanceof MaxTimeStoppingCondition)) {
			if (globalTime.isFinished())
				return true;
		}

		return false;
	}
	
	/**
	 * Convert property to actual stopping condition
	 * @return
	 */
	protected StoppingCondition getStoppingCondition() {
		switch (Properties.STOPPING_CONDITION) {
		case MAXGENERATIONS:
			return new MaxGenerationStoppingCondition();
		case MAXFITNESSEVALUATIONS:
			return new MaxFitnessEvaluationsStoppingCondition();
		case MAXTIME:
			return new MaxTimeStoppingCondition();
		case MAXTESTS:
			return new MaxTestsStoppingCondition();
		case MAXSTATEMENTS:
			return new MaxStatementsStoppingCondition();
		default:
			return new MaxGenerationStoppingCondition();
		}
	}

	protected boolean canGenerateTestsForSUT() {
		if (TestCluster.getInstance().getNumTestCalls() == 0) {
			if(Properties.P_REFLECTION_ON_PRIVATE <= 0.0 || CFGMethodAdapter.getNumMethods(TestGenerationContext.getInstance().getClassLoaderForSUT()) == 0) {
				return false;
			}
		}
		return true;
	}
}
