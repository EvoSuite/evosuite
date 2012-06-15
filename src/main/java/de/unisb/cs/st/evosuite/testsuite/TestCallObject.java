/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
