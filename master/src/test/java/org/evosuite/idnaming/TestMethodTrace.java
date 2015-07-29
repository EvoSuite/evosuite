package org.evosuite.idnaming;

import static org.junit.Assert.*;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.sette.SnippetInputContainer;

public class TestMethodTrace extends SystemTest{

	@Test
	public void testIDNamingOn() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = SnippetInputContainer.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ID_NAMING = true;
		Properties.JUNIT_TESTS = true;
		StringBuilder analysisCriteria = new StringBuilder();
        analysisCriteria.append(Properties.Criterion.METHODTRACE); analysisCriteria.append(",");
      //  analysisCriteria.append(Properties.Criterion.METHOD); analysisCriteria.append(",");
    //    analysisCriteria.append(Properties.Criterion.OUTPUT); analysisCriteria.append(",");
        Properties.ANALYSIS_CRITERIA = analysisCriteria.toString();

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 15, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}


}
