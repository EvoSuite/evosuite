package de.unisb.cs.st.evosuite.coverage.exception;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.ExecutableChromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testsuite.AbstractTestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

public class ExceptionCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 1565793073526627496L;

	private static Logger logger = LoggerFactory.getLogger(ExceptionCoverageSuiteFitness.class);

	protected TestSuiteFitnessFunction baseFF;

	public ExceptionCoverageSuiteFitness() {
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
		Map<String, Set<Class<?>>> implicitTypesOfExceptions = new HashMap<String, Set<Class<?>>>();
		Map<String, Set<Class<?>>> explicitTypesOfExceptions = new HashMap<String, Set<Class<?>>>();

		AbstractTestSuiteChromosome<ExecutableChromosome> suite = (AbstractTestSuiteChromosome<ExecutableChromosome>) individual;
		List<ExecutionResult> results = runTestSuite(suite);

		// for each test case
		for (ExecutionResult result : results) {
			//ExecutionTrace trace = result.getTrace();

			//iterate on the indexes of the statements that resulted in an exception
			for (Integer i : result.exceptions.keySet()) {
				if (i >= result.test.size()) {
					// Timeouts are put after the last statement if the process was forcefully killed
					continue;
				}
				Throwable t = result.exceptions.get(i);
				if (t instanceof SecurityException && Properties.SANDBOX)
					continue;

				String methodName = "";
				if (result.test.getStatement(i) instanceof MethodStatement) {
					MethodStatement ms = (MethodStatement) result.test.getStatement(i);
					Method method = ms.getMethod();
					methodName = method.getName() + Type.getMethodDescriptor(method);
				} else if (result.test.getStatement(i) instanceof ConstructorStatement) {
					ConstructorStatement cs = (ConstructorStatement) result.test.getStatement(i);
					Constructor<?> constructor = cs.getConstructor();
					methodName = "<init>" + Type.getConstructorDescriptor(constructor);
				}
				boolean notDeclared = !result.test.getStatement(i).getDeclaredExceptions().contains(t);
				if (notDeclared) {
					/*
					 * we need to distinguish whether it is explicit (ie "throw" in the code, eg for validating
					 * input for pre-condition) or implicit ("likely" a real fault).
					 */

					/*
					 * FIXME: need to find a way to calculate it
					 */
					boolean isExplicit = false;
					if (isExplicit) {
						if (!explicitTypesOfExceptions.containsKey(methodName))
							explicitTypesOfExceptions.put(methodName,
							                              new HashSet<Class<?>>());
						explicitTypesOfExceptions.get(methodName).add(t.getClass());
					} else {
						if (!implicitTypesOfExceptions.containsKey(methodName))
							implicitTypesOfExceptions.put(methodName,
							                              new HashSet<Class<?>>());
						implicitTypesOfExceptions.get(methodName).add(t.getClass());
					}
				}

			}
		}

		int nExc = getNumExceptions(implicitTypesOfExceptions)
		        + getNumExceptions(explicitTypesOfExceptions);

		double exceptionFitness = 1d / (1d + nExc);

		individual.setFitness(coverageFitness + exceptionFitness);
		return coverageFitness + exceptionFitness;
	}

	private static int getNumExceptions(Map<String, Set<Class<?>>> exceptions) {
		int total = 0;
		for (Set<Class<?>> exceptionSet : exceptions.values()) {
			total += exceptionSet.size();
		}
		return total;
	}
}
