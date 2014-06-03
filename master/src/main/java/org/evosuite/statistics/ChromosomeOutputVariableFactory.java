package org.evosuite.statistics;

import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Factory to create an output variable when given a test suite chromosome
 * 
 * @author gordon
 *
 * @param <T>
 */
public abstract class ChromosomeOutputVariableFactory<T>  {

	private RuntimeVariable variable;
	
	public ChromosomeOutputVariableFactory(RuntimeVariable variable) {
		this.variable = variable;
	}
	
	protected abstract T getData(TestSuiteChromosome individual);
	
	public OutputVariable<T> getVariable(TestSuiteChromosome chromosome) {
		return new OutputVariable<T>(variable.name(), getData(chromosome));
	}

}
