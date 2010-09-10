/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import de.unisb.cs.st.ga.Chromosome;
import de.unisb.cs.st.ga.SelectionFunction;
import de.unisb.cs.st.ga.SteadyStateReplacementFunction;


/**
 * @author Gordon Fraser
 *
 */
public class TestSuiteReplacementFunction extends
		SteadyStateReplacementFunction {

	/**
	 * @param selectionFunction
	 */
	public TestSuiteReplacementFunction(SelectionFunction selectionFunction) {
		super(selectionFunction);
	}

	public int getLengthSum(TestSuiteChromosome chromosome1, TestSuiteChromosome chromosome2) {
		return chromosome1.length() + chromosome2.length();
	}
	
	protected double getBestFitness(TestSuiteChromosome chromosome1, TestSuiteChromosome chromosome2) {
		if(maximize) {
			return Math.max(chromosome1.getFitness(), chromosome2.getFitness());
		} else {
			return Math.min(chromosome1.getFitness(), chromosome2.getFitness());
		}		
	}
	
	/**
	 * min(d(O1),d(O2)) < min(d(P1),d(P2))Ê ||
     * ( min(d(O1),d(O2)) == min(d(P1),d(P2))ÊÊ &&ÊÊ Z= (l(O1)+l(O2) <= l(P1)+l(P2)) ) 
	 */
	public boolean keepOffspring(Chromosome parent1, Chromosome parent2,
			Chromosome offspring1, Chromosome offspring2) {

		double fitness_offspring = getBestFitness((TestSuiteChromosome)offspring1, (TestSuiteChromosome)offspring2);
		double fitness_parents   = getBestFitness((TestSuiteChromosome)parent1, (TestSuiteChromosome)parent2);
		
		if(fitness_offspring < fitness_parents || (fitness_offspring == fitness_parents && 
				getLengthSum((TestSuiteChromosome)offspring1, (TestSuiteChromosome)offspring2) <= getLengthSum((TestSuiteChromosome)parent1, (TestSuiteChromosome)parent2))) {
			return true;
		} else {
			return false;
		}
		
	}

}
