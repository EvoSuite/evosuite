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
 * 
 * @author Gordon Fraser
 */
package org.evosuite.testcase;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedLengthTestChromosomeFactory implements
        ChromosomeFactory<TestChromosome> {

	private static final long serialVersionUID = -3860201346772188495L;

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(FixedLengthTestChromosomeFactory.class);

	/**
	 * Constructor
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
		TestFactory testFactory = TestFactory.getInstance();

		// Then add random stuff
		while (test.size() < size && num < Properties.MAX_ATTEMPTS) {
			testFactory.insertRandomStatement(test, test.size());
			num++;
		}
		//logger.debug("Randomized test case:" + test.toCode());

		return test;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Generate a random chromosome
	 */
	@Override
	public TestChromosome getChromosome() {
		TestChromosome c = new TestChromosome();
		c.test = getRandomTestCase(Properties.CHROMOSOME_LENGTH);
		return c;
	}

}
