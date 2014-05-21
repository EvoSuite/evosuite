package org.evosuite.seeding;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.seeding.ObjectCastExample;
import com.examples.with.different.packagename.seeding.ObjectInheritanceExample;
import com.examples.with.different.packagename.seeding.TypeExample;

public class TestTypeSeeding extends SystemTest {

	@Test
	public void testObjectToStringCase() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ObjectCastExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		Properties.SEED_TYPES = true;
		
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testObjectToStringCaseWithoutSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ObjectCastExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		Properties.SEED_TYPES = false;
		
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertTrue("Non-optimal coverage: ", best.getCoverage() < 1);
	}
	
	@Test
	public void testObjectInheritance() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ObjectInheritanceExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		Properties.SEED_TYPES = true;
		
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass};

		Object result = evosuite.parseCommandLine(command);
		System.out.println("Evosuite classpath: " + printArray(Properties.CLASSPATH));
		
		GeneticAlgorithm<?> ga = getGAFromResult(result);

		

		
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testObjectInheritanceWithoutSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ObjectInheritanceExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		Properties.SEED_TYPES = false;
		
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertTrue("Non-optimal coverage: ", best.getCoverage() < 1);
	}
	
	
	@Test
	public void testTypeExample () {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TypeExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEED_TYPES = true;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		//int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		//Assert.assertEquals("Wrong number of goals: ", 3, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testTypeExampleOff () {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TypeExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEED_TYPES = false;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		//int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		//Assert.assertEquals("Wrong number of goals: ", 3, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	private String printArray(String[] s) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < s.length ; i++) {
			if (i == 0)
				sb.append(s[i]);
			else
				sb.append(", " + s[i]);
		};
		sb.append("]");
		return sb.toString();
	}
	
}
