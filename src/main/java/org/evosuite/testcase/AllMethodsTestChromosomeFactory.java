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
package org.evosuite.testcase;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.setup.TestCluster;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * AllMethodsTestChromosomeFactory class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class AllMethodsTestChromosomeFactory implements ChromosomeFactory<TestChromosome> {

	private static final long serialVersionUID = -420224349882780856L;

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(AllMethodsTestChromosomeFactory.class);

	/** Methods we have already seen */
	private static Set<AccessibleObject> attemptedMethods = new LinkedHashSet<AccessibleObject>();

	/** Methods we have not already seen */
	private static Set<AccessibleObject> remainingMethods = new LinkedHashSet<AccessibleObject>();

	/** Methods we have to cover */
	private static List<AccessibleObject> allMethods = new LinkedList<AccessibleObject>();

	/**
	 * Create a list of all methods
	 */
	public AllMethodsTestChromosomeFactory() {
		allMethods.addAll(TestCluster.getInstance().getTestCalls());
		Randomness.shuffle(allMethods);
		reset();
	}

	/**
	 * Create a random individual
	 * 
	 * @param size
	 */
	private TestCase getRandomTestCase(int size) {
		boolean tracerEnabled = ExecutionTracer.isEnabled();
		if (tracerEnabled)
			ExecutionTracer.disable();

		TestCase test = getNewTestCase();
		int num = 0;

		// Choose a random length in 0 - size
		int length = Randomness.nextInt(size);
		while (length == 0)
			length = Randomness.nextInt(size);

		// Then add random stuff
		while (test.size() < length && num < Properties.MAX_ATTEMPTS) {
			// Select an uncovered method and add it

			if (remainingMethods.size() == 0) {
				reset();
			}
			AccessibleObject call = Randomness.choice(remainingMethods);
			attemptedMethods.add(call);
			remainingMethods.remove(call);

			try {
				TestFactory testFactory = TestFactory.getInstance();

				if (call instanceof Method) {
					testFactory.addMethod(test, (Method) call, test.size(), 0);
				} else if (call instanceof Constructor<?>) {
					testFactory.addConstructor(test, (Constructor<?>) call, test.size(),
					                           0);
				} else {
					assert (false) : "Found test call that is neither method nor constructor";
				}
			} catch (ConstructionFailedException e) {
			}
			num++;
		}
		if (logger.isDebugEnabled())
			logger.debug("Randomized test case:" + test.toCode());

		if (tracerEnabled)
			ExecutionTracer.enable();

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

	/**
	 * Provided so that subtypes of this factory type can modify the returned
	 * TestCase
	 * 
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
	protected TestCase getNewTestCase() {
		return new DefaultTestCase();
	}

	/**
	 * How many methods do we still need to cover?
	 * 
	 * @return a int.
	 */
	public int getNumUncoveredMethods() {
		return remainingMethods.size();
	}

	/**
	 * Forget which calls we have already attempted
	 */
	public void reset() {
		remainingMethods.addAll(allMethods);
		attemptedMethods.clear();
	}
}
