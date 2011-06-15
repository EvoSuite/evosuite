/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

/**
 * @author Gordon Fraser
 * 
 */
public interface LocalSearchObjective {

	public boolean hasImproved(Chromosome individual);

	public FitnessFunction getFitnessFunction();

}
