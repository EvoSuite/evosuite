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

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.Criterion;
import de.unisb.cs.st.evosuite.coverage.concurrency.ConcurrencyTestCaseFactory;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.testcase.RandomLengthTestFactory;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class TestSuiteChromosomeFactory implements ChromosomeFactory {

	private static final long serialVersionUID = -3769862881038106087L;

	/** Factory to manipulate and generate method sequences */
	private ChromosomeFactory test_factory;

	public TestSuiteChromosomeFactory() {
		test_factory = new RandomLengthTestFactory();

		if (Properties.CRITERION == Criterion.CONCURRENCY) {
			//#TODO steenbuck we should wrap the original factory not replace it.
			test_factory = new ConcurrencyTestCaseFactory();
		}

		// test_factory = new RandomLengthTestFactory();
		// test_factory = new AllMethodsChromosomeFactory();
		// test_factory = new OUMTestChromosomeFactory();
	}

	public TestSuiteChromosomeFactory(ChromosomeFactory test_factory) {
		this.test_factory = test_factory;
	}

	public void setTestFactory(ChromosomeFactory factory) {
		test_factory = factory;
	}

	public void setNumberOfTests(int num) {
		Properties.NUM_TESTS = num;
	}

	@Override
	public Chromosome getChromosome() {

		TestSuiteChromosome chromosome = new TestSuiteChromosome();
		chromosome.tests.clear();
		CurrentChromosomeTracker<?> tracker = CurrentChromosomeTracker.getInstance();
		tracker.modification(chromosome);
		// ((AllMethodsChromosomeFactory)test_factory).clear();

		// TODO: Change to random number
		for (int i = 0; i < Properties.NUM_TESTS; i++) {
			TestChromosome test = (TestChromosome) test_factory.getChromosome();
			chromosome.tests.add(test);
		}
		// logger.info("Covered methods: "+((AllMethodsChromosomeFactory)test_factory).covered.size());
		// logger.trace("Generated new test suite:"+chromosome);
		return chromosome;
	}

}
