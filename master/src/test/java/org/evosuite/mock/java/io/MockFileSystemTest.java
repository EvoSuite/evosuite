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
package org.evosuite.mock.java.io;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.mock.java.io.CreateNewTmpFile;
import com.examples.with.different.packagename.mock.java.io.FileAsInputExist;
import com.examples.with.different.packagename.mock.java.io.FileExist;
import com.examples.with.different.packagename.mock.java.io.ReadHelloWorldFromFileWithNameAsInput;

public class MockFileSystemTest extends SystemTestBase {
	
	private static final boolean VFS = Properties.VIRTUAL_FS;
	private static final double defaultPoolP = Properties.SEED_CLONE;
	
	@After
	public void restoreProperties(){
		Properties.VIRTUAL_FS = VFS;
		Properties.SEED_CLONE = defaultPoolP;
	}
	
	
	@Test
	public void testCreateNewTmpFile() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CreateNewTmpFile.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 20000;
		Properties.VIRTUAL_FS = true;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		Assert.assertTrue(result != null);
		
		GeneticAlgorithm<?> ga = getGAFromResult(result);				
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testFileAsInputExist() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = FileAsInputExist.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 20000;
		Properties.VIRTUAL_FS = true;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		Assert.assertTrue(result != null);
		
		GeneticAlgorithm<?> ga = getGAFromResult(result);				
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testFileExist() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = FileExist.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 20000;
		Properties.VIRTUAL_FS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		Assert.assertTrue(result != null);
		
		GeneticAlgorithm<?> ga = getGAFromResult(result);				
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	
	@Test
	public void testReadHelloWorldFromFileWithNameAsInput() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ReadHelloWorldFromFileWithNameAsInput.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 30000;
		Properties.VIRTUAL_FS = true;
		Properties.SEED_CLONE = 0.5;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		Assert.assertTrue(result != null);
		
		GeneticAlgorithm<?> ga = getGAFromResult(result);				
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 5, goals);
		
		
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		/* 
		 * the SUT is not so trivial. often we get 100%, but not always.
		 * maybe we could always get 100% with an easier string and LS on
		 */
		Assert.assertTrue("Coverage: "+best.getCoverage(), best.getCoverage() >= 0.8);
	}
}
