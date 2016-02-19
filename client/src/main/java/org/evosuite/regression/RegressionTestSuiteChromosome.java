/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.regression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class RegressionTestSuiteChromosome extends TestSuiteChromosome {

	private static final long serialVersionUID = 2279207996777829420L;

	public String fitnessData = "0,0,0,0,0,0,0,0,0,0,0";

	public double objDistance = 0.0;

	public int diffExceptions = 0;


	public RegressionTestSuiteChromosome() {
		super();
	}

	public RegressionTestSuiteChromosome(
			ChromosomeFactory<TestChromosome> testChromosomeFactory) {
		this.testChromosomeFactory = testChromosomeFactory;
	}

	protected RegressionTestSuiteChromosome(RegressionTestSuiteChromosome source) {
		super(source);
	}

	@Override
	public void addTest(TestChromosome test) {
		if (test instanceof RegressionTestChromosome) {
			tests.add(test);
		} else {
			RegressionTestChromosome rtc = new RegressionTestChromosome();
			try {
				rtc.setTest(test);
			} catch (NoClassDefFoundError e) {
				String classname = e.getMessage();
				if (classname != null) {
					// TODO: blacklist class
				}
				return;
			} catch (Error e) {
				return;
			} catch (Throwable e) {
				return;
			}
			tests.add(rtc);
		}
		this.setChanged(true);
	}

	@Override
	public void addTests(Collection<TestChromosome> tests) {
		for (TestChromosome test : tests) {
			test.setChanged(true);
			addTest(test);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.testsuite.AbstractTestSuiteChromosome#localSearch(org.evosuite
	 * .ga.LocalSearchObjective)
	 */
	@Override
	public boolean localSearch(LocalSearchObjective objective) {
		// Ignore for now
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.testsuite.AbstractTestSuiteChromosome#clone()
	 */
	@Override
	public TestSuiteChromosome clone() {
		RegressionTestSuiteChromosome c = new RegressionTestSuiteChromosome(
				this);
		c.fitnessData = fitnessData;
		// assert (c.testChromosomeFactory != null):
		// "Chromosome Factory was null";
		return c;
	}

	public List<TestCase> getTests() {
		List<TestCase> tests = new ArrayList<TestCase>();
		for (TestChromosome test : this.tests) {
			RegressionTestChromosome rtc = (RegressionTestChromosome) test;
			tests.add(rtc.getTheTest().getTestCase());
		}
		return tests;
	}

	public AbstractTestSuiteChromosome<TestChromosome> getTestSuite() {
		AbstractTestSuiteChromosome<TestChromosome> suite = new TestSuiteChromosome();
		for (TestChromosome regressionTest : tests) {
			RegressionTestChromosome rtc = (RegressionTestChromosome) regressionTest;
			suite.addTest(rtc.getTheTest());
		}
		return suite;
	}

	public AbstractTestSuiteChromosome<TestChromosome> getTestSuiteForTheOtherClassLoader() {
		AbstractTestSuiteChromosome<TestChromosome> suite = new TestSuiteChromosome();
		for (TestChromosome regressionTest : tests) {
			RegressionTestChromosome rtc = (RegressionTestChromosome) regressionTest;
			suite.addTest(rtc.getTheSameTestForTheOtherClassLoader());
		}
		return suite;
	}

	@Override
	public String toString() {
		String testSuiteString = "";
		for (TestChromosome test : tests) {
			testSuiteString += ((RegressionTestChromosome) test).getTheTest()
					.getTestCase().toCode();
			testSuiteString += "\n";
		}
		return testSuiteString;
	}

	public List<TestChromosome> getTestChromosomes() {
		return tests;
	}

}
