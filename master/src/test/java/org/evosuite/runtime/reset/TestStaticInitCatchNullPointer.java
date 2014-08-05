package org.evosuite.runtime.reset;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.reset.StaticInitCatchNullPointer;

public class TestStaticInitCatchNullPointer extends SystemTest {

	/*
	 * These tests are based on issues found on project 44_summa, which is using the lucene API.
	 * those have issues when for example classes uses org.apache.lucene.util.Constants which has:
	 * 
	  try {
        Collections.class.getMethod("emptySortedSet");
      } catch (NoSuchMethodException nsme) {
        v8 = false;
      }
      *
      * in its static initializer
	 */
	
	@Test
	public void testWithNoReset(){
		runTheTest(false);
	}

	@Test
	public void testWithReset(){
		runTheTest(true);
	}

	private void runTheTest(boolean reset){
		Properties.RESET_STATIC_FIELDS = reset;

		EvoSuite evosuite = new EvoSuite();

		String targetClass = StaticInitCatchNullPointer.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
	}
}
