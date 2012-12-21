package org.evosuite.statistics;

import org.evosuite.testsuite.TestSuiteChromosome;

public abstract class ChromosomeOutputVariableFactory<T>  {

	private String name;
	
	public ChromosomeOutputVariableFactory(String name) {
		this.name = name;
	}
	
	protected abstract T getData(TestSuiteChromosome individual);
	
	public OutputVariable<T> getVariable(TestSuiteChromosome chromosome) {
		return new OutputVariable<T>(name, getData(chromosome));
	}

}
