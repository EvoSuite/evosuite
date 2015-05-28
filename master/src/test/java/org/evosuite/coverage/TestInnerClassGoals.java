package org.evosuite.coverage;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ClassWithInnerClass;
import com.examples.with.different.packagename.ClassWithPrivateInnerClass;
import com.examples.with.different.packagename.ClassWithPrivateNonStaticInnerClass;

public class TestInnerClassGoals extends SystemTest {

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
		 int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
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
		 int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		 Assert.assertEquals(8, goals );
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

		 String[] command = new String[] { "-generateSuite", "-class", targetClass };
		 Object result = evosuite.parseCommandLine(command);
		 GeneticAlgorithm<?> ga = getGAFromResult(result);
		 TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		 System.out.println(best);
		 int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
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
		 int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		 Assert.assertEquals(8, goals );
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

		 String[] command = new String[] { "-generateSuite", "-class", targetClass };
		 Object result = evosuite.parseCommandLine(command);
		 GeneticAlgorithm<?> ga = getGAFromResult(result);
		 TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		 System.out.println(best);
		 int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		 
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
		 int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		 Assert.assertEquals(8, goals );
		 Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	 }
}
