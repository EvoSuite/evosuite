/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

/**
 * @author Gordon Fraser
 * 
 */
public interface LocalSearchObjective {

	public FitnessFunction getFitnessFunction();

	public boolean hasImproved(Chromosome individual);

}
