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
package org.evosuite.seeding;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.seeding.NumericDynamicDoubleSeeding;
import com.examples.with.different.packagename.seeding.NumericDynamicFloatSeeding;
import com.examples.with.different.packagename.seeding.NumericDynamicIntSeeding;
import com.examples.with.different.packagename.seeding.NumericDynamicLongSeeding;

/**
 * @author jmr
 *
 */
public class NumericDynamicSeedingSystemTest extends SystemTestBase {

	public static final double defaultDynamicPool = Properties.DYNAMIC_POOL;

	// DOUBLES
	
	@Test
	public void testDynamicSeedingDouble() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = NumericDynamicDoubleSeeding.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CLIENT_ON_THREAD = true;
		
		Properties.DYNAMIC_SEEDING = true;

		//Properties.ALGORITHM = Properties.Algorithm.ONEPLUSONEEA;
		
		Properties.DYNAMIC_POOL = 0.8d; // Probability of picking value from constants pool
		ConstantPoolManager.getInstance().reset();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass, "-Dprint_to_system=true" };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		System.out.println("ConstantPool:\n" + ConstantPoolManager.getInstance().getDynamicConstantPool().toString());
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	

	
	// FLOATS
	
	@Test
	public void testDynamicSeedingFloat() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = NumericDynamicFloatSeeding.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CLIENT_ON_THREAD = true;
		
		Properties.DYNAMIC_SEEDING = true;

		//Properties.ALGORITHM = Properties.Algorithm.ONEPLUSONEEA;
		
		Properties.DYNAMIC_POOL = 0.8f; // Probability of picking value from constants pool
		ConstantPoolManager.getInstance().reset();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass, "-Dprint_to_system=true" };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		System.out.println("ConstantPool:\n" + ConstantPoolManager.getInstance().getDynamicConstantPool().toString());
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	

	// LONGS
	
	@Test
	public void testDynamicSeedingLong() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = NumericDynamicLongSeeding.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CLIENT_ON_THREAD = true;
		
		Properties.DYNAMIC_SEEDING = true;

		//Properties.ALGORITHM = Properties.Algorithm.ONEPLUSONEEA;
		
		Properties.DYNAMIC_POOL = 0.8; // Probability of picking value from constants pool
		ConstantPoolManager.getInstance().reset();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass, "-Dprint_to_system=true" };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		System.out.println("ConstantPool:\n" + ConstantPoolManager.getInstance().getDynamicConstantPool().toString());
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	

	
	// INTS
	

	@Test
	public void testDynamicSeedingInt() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = NumericDynamicIntSeeding.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CLIENT_ON_THREAD = true;
		
		Properties.DYNAMIC_SEEDING = true;

		//Properties.ALGORITHM = Properties.Algorithm.ONEPLUSONEEA;

		Properties.DYNAMIC_POOL = 0.8; // Probability of picking value from constants pool
		ConstantPoolManager.getInstance().reset();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass, "-Dprint_to_system=true" };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		System.out.println("ConstantPool:\n" + ConstantPoolManager.getInstance().getDynamicConstantPool().toString());
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	

}
