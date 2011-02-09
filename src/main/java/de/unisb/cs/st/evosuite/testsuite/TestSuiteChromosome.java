/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.testsuite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.OUM.OUMTestChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.testcase.RandomLengthTestFactory;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class TestSuiteChromosome extends Chromosome {

	/** The genes are test cases */
	public List<TestChromosome> tests = new ArrayList<TestChromosome>();

	/** Maximum number of tests */
	protected int max_tests = Properties.getPropertyOrDefault("max_size", 50);

	protected double coverage = 0.0;

	/** Rate of test case addition */
	protected double mutation_rate = Properties.getPropertyOrDefault(
	        "mutation_rate", 0.1);

	/** Factory to manipulate and generate method sequences */
	private static ChromosomeFactory test_factory = null;

	static {
		String factory_name = Properties.getPropertyOrDefault("test_factory",
		        "Random");
		if (factory_name.equals("OUM"))
			test_factory = new OUMTestChromosomeFactory();
		else
			test_factory = new RandomLengthTestFactory();
	}

	public void addTest(TestCase test) {
		TestChromosome c = new TestChromosome();
		c.test = test;
		tests.add(c);
	}

	public void addTest(TestChromosome test) {
		tests.add(test);
	}

	/**
	 * Create a deep copy of this test suite
	 */
	@Override
	public Chromosome clone() {
		TestSuiteChromosome copy = new TestSuiteChromosome();
		for (TestChromosome test : tests) {
			// copy.tests.add((TestChromosome) test.clone());
			TestChromosome testCopy = (TestChromosome) test.clone();
			copy.tests.add(testCopy);
		}
		copy.setFitness(getFitness());
		copy.setChanged(isChanged());
		copy.coverage = coverage;
		return copy;
	}

	/**
	 * Keep up to position 1, copy from position 2 on
	 */
	@Override
	public void crossOver(Chromosome other, int position1, int position2)
	        throws ConstructionFailedException {

		TestSuiteChromosome chromosome = (TestSuiteChromosome) other;

		while (tests.size() > position1)
			tests.remove(position1);
		for (int num = position2; num < other.size(); num++) {
			// tests.add((TestChromosome) chromosome.tests.get(num).clone());
			TestChromosome testCopy = (TestChromosome) chromosome.tests
			        .get(num).clone();
			tests.add(testCopy);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof TestSuiteChromosome))
			return false;

		TestSuiteChromosome other = (TestSuiteChromosome) obj;
		if (other.size() != size())
			return false;

		for (int i = 0; i < size(); i++) {
			if (!tests.get(i).equals(other.tests.get(i)))
				return false;
		}

		return true;
	}

	/**
	 * Apply mutation on test suite level
	 */
	@Override
	public void mutate() {
		// Mutate test cases
		for (TestChromosome test : tests) {
			if (randomness.nextDouble() < 1.0 / tests.size()) {
				test.mutate();
			}
		}

		Iterator<TestChromosome> it = tests.iterator();
		int num = 0;
		while (it.hasNext()) {
			TestChromosome t = it.next();
			if (t.size() == 0) {
				it.remove();
				for (TestChromosome test : tests) {
					for (Statement s : test.test.getStatements()) {
						if (s instanceof TestCallStatement) {
							TestCallStatement call = (TestCallStatement) s;
							if (call.getTestNum() > num) {
								call.setTestNum(call.getTestNum() - 1);
							}
						}
					}
				}
			} else {
				num++;
			}
		}

		final double ALPHA = 0.1;
		int count = 1;

		while (randomness.nextDouble() <= Math.pow(ALPHA, count)
		        && size() < max_tests) {
			count++;
			// Insert at position as during initialization (i.e., using helper
			// sequences)
			// RandomLengthTestFactory factory = new RandomLengthTestFactory();
			// //TestChromosomeFactory();
			// OUMTestChromosomeFactory factory = new
			// OUMTestChromosomeFactory();
			TestChromosome test = (TestChromosome) test_factory.getChromosome();
			tests.add(test);
			// tests.add((TestChromosome) test_factory.getChromosome());
			logger.debug("Adding new test case ");
		}

		/*
		 * if(randomness.nextDouble() < mutation_rate) { TestSuiteChromosome
		 * best = BestChromosomeTracker.getInstance().getBest(); int diff =
		 * 2*best.length() - length(); if(diff > 0) { int length = 1 +
		 * randomness.nextInt(Math.min(max_test_length, diff));
		 * 
		 * // Add random test case RandomLengthTestFactory factory = new
		 * RandomLengthTestFactory(); // TestChromosomeFactory factory = new
		 * TestChromosomeFactory(length); tests.add((TestChromosome)
		 * factory.getChromosome());
		 * logger.debug("Adding new test case of max length "+length); } }
		 */

	}

	/**
	 * Number of test cases
	 */
	@Override
	public int size() {
		return tests.size();
	}

	/**
	 * 
	 * @return Sum of the lengths of the test cases
	 */
	public int length() {
		int length = 0;
		for (TestChromosome test : tests)
			length += test.size();
		return length;
	}

	/**
	 * Determine relative ordering of this chromosome to another chromosome If
	 * fitness is equal, the shorter chromosome comes first
	 */
	/*
	 * public int compareTo(Chromosome o) { if(RANK_LENGTH && getFitness() ==
	 * o.getFitness()) { return (int) Math.signum((length() -
	 * ((TestSuiteChromosome)o).length())); } else return (int)
	 * Math.signum(getFitness() - o.getFitness()); }
	 */

	@Override
	public String toString() {
		String result = "TestSuite: " + tests.size();
		for (TestChromosome test : tests) {
			result += test.test.toCode();
		}
		return result;
	}

	public List<TestCase> getTests() {
		List<TestCase> testcases = new ArrayList<TestCase>();
		for (TestChromosome test : tests) {
			testcases.add(test.test);
		}
		return testcases;
	}

	public double getCoverage() {
		return coverage;
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}
}
