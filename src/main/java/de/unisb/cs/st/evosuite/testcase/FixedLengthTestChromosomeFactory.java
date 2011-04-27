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

package de.unisb.cs.st.evosuite.testcase;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;

public class FixedLengthTestChromosomeFactory implements ChromosomeFactory {

	protected static Logger logger = Logger.getLogger(FixedLengthTestChromosomeFactory.class);

	/** Attempts before giving up construction */
	protected int max_attempts = Properties.getIntegerValue("max_attempts");

	protected int chromosome_length = Properties.getIntegerValue("chromosome_length");

	/** Factory to manipulate and generate method sequences */
	private final DefaultTestFactory test_factory = DefaultTestFactory.getInstance();

	/**
	 * Constructor
	 * 
	 * @param m
	 *            : Target mutation
	 */
	public FixedLengthTestChromosomeFactory() {
	}

	/**
	 * Create a random individual
	 * 
	 * @param size
	 */
	private TestCase getRandomTestCase(int size) {
		TestCase test = new DefaultTestCase();
		int num = 0;

		num = 0;
		// Then add random stuff
		while (test.size() < size && num < max_attempts) {
			test_factory.insertRandomStatement(test);
			num++;
		}
		//logger.debug("Randomized test case:" + test.toCode());

		return test;
	}

	/**
	 * Generate a random chromosome
	 */
	@Override
	public Chromosome getChromosome() {
		TestChromosome c = new TestChromosome();
		c.test = getRandomTestCase(chromosome_length);
		return c;
	}

}
