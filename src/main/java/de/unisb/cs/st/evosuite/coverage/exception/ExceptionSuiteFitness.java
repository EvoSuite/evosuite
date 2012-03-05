package de.unisb.cs.st.evosuite.coverage.exception;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutableChromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testsuite.AbstractTestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

public class ExceptionSuiteFitness extends TestSuiteFitnessFunction{

	private static Logger logger = LoggerFactory.getLogger(ExceptionSuiteFitness.class);
	
	protected TestSuiteFitnessFunction baseFF;
	
	public ExceptionSuiteFitness(){
		baseFF = new BranchCoverageSuiteFitness();
	}
	
	@Override
	public double getFitness(Chromosome individual) {
		logger.trace("Calculating exception fitness");
		
		/*
		 * We first calculate fitness based on coverage. this not only 
		 * has side-effect of changing "fitness" in individual, but also "coverage".
		 * but because "coverage" is only used for stats, no need to update it here, as
		 * anyway it d be bit difficult to define
		 */
		double coverageFitness = baseFF.getFitness(individual);
		
		/*
		 * keep track of which kind of exceptions were thrown. 
		 * for the moment, we only keep track of different kinds of exceptions, not 
		 * where they were thrown from.
		 * 
		 * As long as two methods share a single line of code (eg, a constructor) then, even if
		 * both fail, it could be due to the same fault. 
		 */
		Set<Class<?>> implicitTypesOfExceptions = new HashSet<Class<?>>();
		Set<Class<?>> explicitTypesOfExceptions = new HashSet<Class<?>>();
		
		AbstractTestSuiteChromosome<ExecutableChromosome> suite = (AbstractTestSuiteChromosome<ExecutableChromosome>) individual;
		List<ExecutionResult> results = runTestSuite(suite);
		
		// for each test case
		for(ExecutionResult result : results){
			//ExecutionTrace trace = result.getTrace();
			
			//iterate on the indexes of the statements that resulted in an exception
			for (Integer i : result.exceptions.keySet()) {
				Throwable t = result.exceptions.get(i);
				boolean notDeclared = !result.test.getStatement(i).getDeclaredExceptions().contains(t);
				if (notDeclared){
					/*
					 * we need to distinguish whether it is explicit (ie "throw" in the code, eg for validating
					 * input for pre-condition) or implicit ("likely" a real fault).
					 */
					
					/*
					 * FIXME: need to find a way to calculate it
					 */
					boolean isExplicit = false; 
					
					if(isExplicit){
						explicitTypesOfExceptions.add(t.getClass());
					} else {
						implicitTypesOfExceptions.add(t.getClass());
					} 
				}
				
			}
		}

		int nExc = implicitTypesOfExceptions.size() + explicitTypesOfExceptions.size();
		
		double exceptionFitness = 1d / (double) (1d + nExc);
		
		individual.setFitness(coverageFitness + exceptionFitness);
		return 0;
	}
	
}
