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

	public boolean hasNotWorsened(Chromosome individual);

	public int hasChanged(Chromosome individual);

	public FitnessFunction getFitnessFunction();

}
