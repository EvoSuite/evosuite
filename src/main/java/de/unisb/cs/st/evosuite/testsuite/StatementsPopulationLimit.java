/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.PopulationLimit;

/**
 * @author fraser
 * 
 */
public class StatementsPopulationLimit implements PopulationLimit {

	private static final long serialVersionUID = 4794704248615412859L;

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.PopulationLimit#isPopulationFull(java.util.List)
	 */
	@Override
	public boolean isPopulationFull(List<Chromosome> population) {
		int numStatements = 0;
		for (Chromosome chromosome : population) {
			TestSuiteChromosome suite = (TestSuiteChromosome) chromosome;
			numStatements += suite.totalLengthOfTestCases();
		}
		return numStatements >= Properties.POPULATION;
	}

}
