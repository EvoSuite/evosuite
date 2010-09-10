/**
 * 
 */
package de.unisb.cs.st.evosuite.branch;

import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.ga.Chromosome;
import de.unisb.cs.st.ga.GAProperties;
import de.unisb.cs.st.ga.SelectionFunction;
import de.unisb.cs.st.ga.SteadyStateReplacementFunction;


/**
 * @author fraser
 *
 */
public class TestCaseReplacementFunction extends SteadyStateReplacementFunction {

	private final static boolean BEST_LENGTH = GAProperties.getPropertyOrDefault("check_best_length", true);  
	
	public TestCaseReplacementFunction(SelectionFunction selection) {
		super(selection);
	}
	
	public int getLengthSum(TestChromosome chromosome1, TestChromosome chromosome2) {
		return chromosome1.size() + chromosome2.size();
	}
	
	protected double getBestFitness(TestChromosome chromosome1, TestChromosome chromosome2) {
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

		double fitness_offspring = getBestFitness((TestChromosome)offspring1, (TestChromosome)offspring2);
		double fitness_parents   = getBestFitness((TestChromosome)parent1, (TestChromosome)parent2);
		
		if(BEST_LENGTH && (fitness_offspring == fitness_parents && 
				getLengthSum((TestChromosome)offspring1, (TestChromosome)offspring2) <= getLengthSum((TestChromosome)parent1, (TestChromosome)parent2)))
			return true;
		
		if(fitness_offspring < fitness_parents) {
			return true;
		} else {
			return false;
		}
		
	}

}
