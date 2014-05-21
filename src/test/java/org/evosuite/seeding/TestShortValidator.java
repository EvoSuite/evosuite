package org.evosuite.seeding;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.seeding.AbstractFormatValidator;
import com.examples.with.different.packagename.seeding.AbstractNumberValidator;
import com.examples.with.different.packagename.seeding.ShortValidator;
import com.examples.with.different.packagename.seeding.ShortValidatorMin;


public class TestShortValidator extends SystemTest {
	
	private String MLIST = "minValue(SS)Z"
			+ ":minValue(Ljava/lang/Short;S)Z"
			+ ":maxValue(SS)Z"
			+ ":maxValue(Ljava/lang/Short;S)Z"
			+ ":isInRange(SSS)Z"
			+ ":isInRange(Ljava/lang/Short;SS)Z";
	/**
	+ ";getInstance()Lcom/examples/with/different/packagename/seeding/ShortValidator;"
	+ ";ShortValidator()V"
	+ ";ShortValidator(ZI)V"
	+ ";validate(Ljava/lang/String;)Ljava/lang/Short;"
	+ ";validate(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Short;"
	+ ";validate(Ljava/lang/String;Ljava/util/Locale;)Ljava/lang/Short;"
	+ ";validate(Ljava/lang/String;Ljava/lang/String;Ljava/util/Locale;)Ljava/lang/Short;"
	+ ";processParsedValue(Ljava/lang/Object;Ljava/text/Format;)Ljava/lang/Object;";
	**/
	@Test
	public void testShortValidatorTypeSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ShortValidator.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.TARGET_METHOD_LIST = MLIST;
		
		Properties.SEED_TYPES = true;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	@Test
	public void testShortValidatorTypeSeedingOff() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ShortValidator.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.TARGET_METHOD_LIST = MLIST;
		Properties.SEED_TYPES = false;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testShortValidatorMinTypeS() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ShortValidatorMin.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEED_TYPES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	@Test
	public void testShortValidatorMinTypeSOff() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ShortValidatorMin.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEED_TYPES = false;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testAbstractNumberValidatorTypeSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = AbstractNumberValidator.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		
		Properties.SEED_TYPES = true;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	@Test
	public void testAbstractNumberValidatorTypeSeedingOff() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = AbstractNumberValidator.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		Properties.SEED_TYPES = false;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testAbstractFormatValidatorTypeSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = AbstractFormatValidator.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		
		Properties.SEED_TYPES = true;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	@Test
	public void testAbstractFormatValidatorTypeSeedingOff() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = AbstractFormatValidator.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		Properties.SEED_TYPES = false;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}