package org.evosuite.coverage;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.ClassWithInnerClass;
import com.examples.with.different.packagename.ClassWithPrivateInnerClass;
import com.examples.with.different.packagename.ClassWithPrivateNonStaticInnerClass;

public class TestInnerClassGoals extends SystemTest {

	private double oldPPool = Properties.PRIMITIVE_POOL;

	@Before
	public void resetStuff() {
		Properties.PRIMITIVE_POOL = oldPPool;
	}
	
	@Test
	public void testPublicStaticInnerClassWithBranch(){
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ClassWithInnerClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.CRITERION = new Properties.Criterion[]{
				Properties.Criterion.BRANCH,
		};

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		System.out.println(best);
		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(6, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testPublicStaticInnerClassWithLine(){
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ClassWithInnerClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.CRITERION = new Properties.Criterion[]{
				Properties.Criterion.LINE,
		};

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		System.out.println(best);
		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		// Lines 6, 7, 8 in foo, plus line 10 for the (implicit) return statement at the end of the method
		// Lines 14, 15, 17 in inner class foo
		Assert.assertEquals(7, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testPrivateStaticInnerClassWithBranch(){
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ClassWithPrivateInnerClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.CRITERION = new Properties.Criterion[]{
				Properties.Criterion.BRANCH,
		};
		// Increase chances of using seeded values to make sure the test finishes in budget
		Properties.PRIMITIVE_POOL = 1.0;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		System.out.println(best);
		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		// TODO: Should constructors of private inner classes be tested?
		Assert.assertEquals(5, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testPrivateStaticInnerClassWithLine(){
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ClassWithPrivateInnerClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.CRITERION = new Properties.Criterion[]{
				Properties.Criterion.LINE,
		};

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		System.out.println(best);
		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(7, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testPrivateInnerClassWithBranch(){
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ClassWithPrivateNonStaticInnerClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.CRITERION = new Properties.Criterion[]{
				Properties.Criterion.BRANCH,
		};
		
		// Increase chances of using seeded values to make sure the test finishes in budget
		Properties.PRIMITIVE_POOL = 1.0;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		System.out.println(best);
		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function

		// TODO: Should constructors of private inner classes be tested?
		Assert.assertEquals(5, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testPrivateInnerClassWithLine(){
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ClassWithPrivateNonStaticInnerClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		Properties.CRITERION = new Properties.Criterion[]{
				Properties.Criterion.LINE,
		};

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		System.out.println(best);
		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(7, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
