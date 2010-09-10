/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import de.unisb.cs.st.ga.BloatControlFunction;
import de.unisb.cs.st.ga.Chromosome;
import de.unisb.cs.st.ga.GAProperties;

/**
 * @author Gordon Fraser
 *
 */
public class MaxLengthBloatControl implements BloatControlFunction {
	
	/**
	 * Maximum number of attempts in generating/adding/mutating things
	 */
	protected int max_length = Integer.parseInt(GAProperties.getPropertyOrDefault("GA.max_length", "100"));
	
	
	/**
	 * Check whether the chromosome is bigger than the max length constant 
	 */
	public boolean isTooLong(Chromosome chromosome) {
		return ((TestSuiteChromosome)chromosome).length() > max_length;
	}
}
