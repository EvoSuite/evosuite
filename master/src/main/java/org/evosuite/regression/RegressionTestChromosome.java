/**
 * 
 */
package org.evosuite.regression;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.localsearch.LocalSearchObjective;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class RegressionTestChromosome extends ExecutableChromosome {

	private static final long serialVersionUID = -6345178117840330196L;

	private TestChromosome theTest;

	private TestChromosome theSameTestForTheOtherClassLoader;

	// TODO: This doesn't really belong here
	private ClassLoader theClassLoader = null;

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutableChromosome#copyCachedResults(org.evosuite.testcase.ExecutableChromosome)
	 */
	@Override
	protected void copyCachedResults(ExecutableChromosome other) {
		RegressionTestChromosome otherChromosome = (RegressionTestChromosome) other;
		theTest.copyCachedResults(otherChromosome.theTest);
		theSameTestForTheOtherClassLoader.copyCachedResults(otherChromosome.theSameTestForTheOtherClassLoader);

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutableChromosome#executeForFitnessFunction(org.evosuite.testsuite.TestSuiteFitnessFunction)
	 */
	@Override
	public ExecutionResult executeForFitnessFunction(
	        TestSuiteFitnessFunction testSuiteFitnessFunction) {
		// TODO Hmmmm...

		return null;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#clone()
	 */
	@Override
	public Chromosome clone() {
		RegressionTestChromosome copy = new RegressionTestChromosome();
		copy.theClassLoader = theClassLoader; // I don't think this should be a member of this class to be honest!
		copy.theTest = (TestChromosome) theTest.clone();
		copy.theSameTestForTheOtherClassLoader = (TestChromosome) theSameTestForTheOtherClassLoader.clone();
		return copy;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((theClassLoader == null) ? 0 : theClassLoader.hashCode());
		result = prime * result + ((theTest == null) ? 0 : theTest.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegressionTestChromosome other = (RegressionTestChromosome) obj;
		if (theClassLoader == null) {
			if (other.theClassLoader != null)
				return false;
		} else if (!theClassLoader.equals(other.theClassLoader))
			return false;
		if (theTest == null) {
			if (other.theTest != null)
				return false;
		} else if (!theTest.equals(other.theTest))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#compareSecondaryObjective(org.evosuite.ga.Chromosome)
	 */
	@Override
	public int compareSecondaryObjective(Chromosome o) {
		RegressionTestChromosome otherChromosome = (RegressionTestChromosome) o;
		return theTest.compareSecondaryObjective(otherChromosome.theTest);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#mutate()
	 */
	@Override
	public void mutate() {
		theTest.mutate();
		if (theTest.isChanged())
			updateClassloader();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#crossOver(org.evosuite.ga.Chromosome, int, int)
	 */
	@Override
	public void crossOver(Chromosome other, int position1, int position2)
	        throws ConstructionFailedException {
		RegressionTestChromosome otherChromosome = (RegressionTestChromosome) other;
		theTest.crossOver(otherChromosome.theTest, position1, position2);
		updateClassloader();
	}

	/**
     * 
     */
	private void updateClassloader() {
		if (theTest.isChanged()) {
			theSameTestForTheOtherClassLoader = (TestChromosome) theTest.clone();
			((DefaultTestCase) theSameTestForTheOtherClassLoader.getTestCase()).changeClassLoader(theClassLoader);
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#localSearch(org.evosuite.ga.LocalSearchObjective)
	 */
	@Override
	public boolean localSearch(LocalSearchObjective objective) {
		boolean result = theTest.localSearch(objective);
		updateClassloader();
		return result;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#size()
	 */
	@Override
	public int size() {
		return theTest.size();
	}

	/**
	 * @param theTest
	 *            the theTest to set
	 */
	public void setTest(TestChromosome theTest) {
		this.theTest = theTest;
		updateClassloader();
	}

	/**
	 * @return the theTest
	 */
	public TestChromosome getTheTest() {
		return theTest;
	}

	/**
	 * @return the theSameTestForTheOtherClassLoader
	 */
	public TestChromosome getTheSameTestForTheOtherClassLoader() {
		return theSameTestForTheOtherClassLoader;
	}

}
