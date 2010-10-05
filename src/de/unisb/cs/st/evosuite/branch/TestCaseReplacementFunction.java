/**
 * 
 */
package de.unisb.cs.st.evosuite.branch;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.SelectionFunction;
import de.unisb.cs.st.evosuite.ga.ReplacementFunction;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;


/**
 * @author Gordon Fraser
 *
 */
public class TestCaseReplacementFunction extends ReplacementFunction {

	private final static boolean PARENT_LENGTH = Properties.getPropertyOrDefault("check_parents_length", true);  
	
	public TestCaseReplacementFunction(SelectionFunction selection) {
		super(selection);
	}
	
	public TestCaseReplacementFunction(boolean maximize) {
		super(maximize);
	}
	
	public TestCaseReplacementFunction() {
		super(false);
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
		
		if(PARENT_LENGTH) {
			if((fitness_offspring == fitness_parents && 
					getLengthSum((TestChromosome)offspring1, (TestChromosome)offspring2) <= getLengthSum((TestChromosome)parent1, (TestChromosome)parent2))) {
				return true;
			}			
		} else {
			if(fitness_offspring == fitness_parents) {
				return true;
			}						
		}
		
		/*
		if(BEST_LENGTH && (fitness_offspring == fitness_parents && 
				getLengthSum((TestChromosome)offspring1, (TestChromosome)offspring2) <= getLengthSum((TestChromosome)parent1, (TestChromosome)parent2))) {
			return true;
		}
		*/
		
		if(fitness_offspring < fitness_parents) {
			return true;
		} else {
			return false;
		}
		
	}

	/* (non-Javadoc)
     * @see de.unisb.cs.st.evosuite.ga.ReplacementFunction#keepOffspring(de.unisb.cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome)
     */
    @Override
    public boolean keepOffspring(Chromosome parent, Chromosome offspring) {
	    return isBetter(offspring, parent);
    }

}
