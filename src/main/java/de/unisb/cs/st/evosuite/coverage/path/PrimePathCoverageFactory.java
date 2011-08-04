/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.path;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.AbstractFitnessFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class PrimePathCoverageFactory extends AbstractFitnessFactory {

	private static List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		if (!goals.isEmpty())
			return goals;

		String targetMethod = Properties.TARGET_METHOD;

		for (String className : PrimePathPool.primePathMap.keySet()) {
			for (String methodName : PrimePathPool.primePathMap.get(className).keySet()) {

				if (!targetMethod.equals("") && !methodName.equals(targetMethod)) {
					continue;
				}
				for (PrimePath path : PrimePathPool.primePathMap.get(className).get(methodName)) {
					goals.add(new PrimePathTestFitness(path, className, methodName));
				}
			}
		}

		return goals;
	}

}
