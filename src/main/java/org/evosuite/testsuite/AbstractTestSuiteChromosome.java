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
package org.evosuite.testsuite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.LocalSearchObjective;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.utils.Randomness;


public abstract class AbstractTestSuiteChromosome<T extends ExecutableChromosome> extends
        Chromosome {
	private static final long serialVersionUID = 1L;

	protected List<T> tests = new ArrayList<T>();
	protected List<Boolean> unmodifiableTests = new ArrayList<Boolean>();
	protected ChromosomeFactory<T> testChromosomeFactory;

	/*
	 * coverage is used only for output/statistics purposes
	 */
	protected double coverage = 0.0;


	/**
	 * only used for testing/debugging
	 */
	protected AbstractTestSuiteChromosome(){
		super();
	}
	
	protected AbstractTestSuiteChromosome(ChromosomeFactory<T> testChromosomeFactory) {
		this.testChromosomeFactory = testChromosomeFactory;
	}

	public ChromosomeFactory<T> getTestChromosomeFactory() {
		return testChromosomeFactory;
	}

	/**
	 * Creates a deep copy of source.
	 * 
	 * @param source
	 */
	@SuppressWarnings("unchecked")
	protected AbstractTestSuiteChromosome(AbstractTestSuiteChromosome<T> source) {
		this(source.testChromosomeFactory);

		for (T test : source.tests) {
			addTest((T) test.clone());
		}

		this.setFitness(source.getFitness());
		this.setChanged(source.isChanged());
		this.coverage = source.coverage;
	}

	public void addTest(T test) {
		tests.add(test);
		unmodifiableTests.add(false);
		this.setChanged(true);
	}

	public void addTests(Collection<T> tests) {
		for (T test : tests) {
			tests.add(test);
			unmodifiableTests.add(false);
		}
		if (!tests.isEmpty())
			this.setChanged(true);
	}

	public void addUnmodifiableTest(T test) {
		tests.add(test);
		unmodifiableTests.add(true);
		this.setChanged(true);
	}

	/**
	 * Keep up to position1, append copy of other from position2 on
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void crossOver(Chromosome other, int position1, int position2)
	        throws ConstructionFailedException {
		if (!(other instanceof AbstractTestSuiteChromosome<?>)) {
			throw new IllegalArgumentException(
			        "AbstractTestSuiteChromosome.crossOver() called with parameter of unsupported type "
			                + other.getClass());
		}

		AbstractTestSuiteChromosome<T> chromosome = (AbstractTestSuiteChromosome<T>) other;

		while (tests.size() > position1) {
			tests.remove(position1);
			unmodifiableTests.remove(position1);
		}

		for (int num = position2; num < other.size(); num++) {
			tests.add((T) chromosome.tests.get(num).clone());
			unmodifiableTests.add(chromosome.unmodifiableTests.get(num).booleanValue());
		}

		this.setChanged(true);
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

	@Override
	public int hashCode() {
		return tests.hashCode();
	}

	/**
	 * Apply mutation on test suite level
	 */
	@Override
	public void mutate() {
		boolean changed = false;

		// Mutate existing test cases
		for (int i = 0; i < tests.size(); i++) {
			T test = tests.get(i);
			if (unmodifiableTests.get(i) == true)
				continue;

			if (Randomness.nextDouble() < 1.0 / tests.size()) {
				test.mutate();
				changed = true;
			}
		}

		// Add new test cases
		final double ALPHA = Properties.P_TEST_INSERTION; //0.1;

		for (int count = 1; Randomness.nextDouble() <= Math.pow(ALPHA, count)
		        && size() < Properties.MAX_SIZE; count++) {
			tests.add(testChromosomeFactory.getChromosome());
			unmodifiableTests.add(false);
			logger.debug("Adding new test case");
			changed = true;
		}

		if (changed) {
			this.setChanged(true);
		}
	}

	/**
	 * @return Sum of the lengths of the test cases
	 */
	public int totalLengthOfTestCases() {
		int length = 0;
		for (T test : tests)
			length += test.size();
		return length;
	}

	@Override
	public int size() {
		return tests.size();
	}

	@Override
	public abstract void localSearch(LocalSearchObjective objective);

	@Override
	public abstract AbstractTestSuiteChromosome<T> clone();

	public T getTestChromosome(int index) {
		return tests.get(index);
	}

	public List<T> getTestChromosomes() {
		return tests;
	}

	public void setTestChromosome(int index, T test) {
		tests.set(index, test);
		this.setChanged(true);
	}

	public double getCoverage() {
		return coverage;
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}
}
