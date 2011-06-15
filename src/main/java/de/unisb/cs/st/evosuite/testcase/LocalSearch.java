/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;

/**
 * @author Gordon Fraser
 * 
 */
public interface LocalSearch {

	public void doSearch(TestChromosome test, int statement, LocalSearchObjective objective);

}
