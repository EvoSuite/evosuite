/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 * 
 */
public class TestCallObject extends AccessibleObject {

	public TestCase testCase = null;

	private Type returnType;

	private int num;

	public TestCallObject(int num) {
		this.num = num;
		this.returnType = Properties.getTargetClass();
		testCase = getTest().clone();
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	/*
	 * public TestCallObject(TestSuiteChromosome suite, TestChromosome test) {
	 * this.testSuite = suite; this.testCase = test; this.returnType =
	 * Properties.getTargetClass(); }
	 */
	public TestCase getTest() {
		CurrentChromosomeTracker<?> tracker = CurrentChromosomeTracker.getInstance();
		TestSuiteChromosome suite = (TestSuiteChromosome) tracker.getCurrentChromosome();
		if (num >= suite.tests.size()) {
			System.out.println("Current chromosome only has " + suite.tests.size()
			        + " chromosomes, looking for " + num);
			return null;
		} else
			return suite.tests.get(num).getTestCase();
	}

	public TestSuiteChromosome getSuite() {
		CurrentChromosomeTracker<?> tracker = CurrentChromosomeTracker.getInstance();
		TestSuiteChromosome suite = (TestSuiteChromosome) tracker.getCurrentChromosome();
		return suite;
	}

	/**
	 * @param returnType
	 *            the returnType to set
	 */
	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}

	/**
	 * @return the returnType
	 */
	public Type getReturnType() {
		return returnType;
	}

}
