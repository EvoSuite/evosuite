package org.evosuite.idnaming;

import com.examples.with.different.packagename.idnaming.gnu.trove.decorator.TByteListDecorator;
import com.examples.with.different.packagename.idnaming.gnu.trove.decorator.TIntShortMapDecorator;



import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class TestIdNamingAssertion extends SystemTest {

/*	@Test
public void testsMissingAssertions() {
		// non-deterministic
		// when calls are generating without saving the return value in an assignment,
		// the assertion generator ignores them and does not produce any assertion.
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TIntShortMapDecorator.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ID_NAMING = true;
		Properties.JUNIT_TESTS = true;
		StringBuilder analysisCriteria = new StringBuilder();
        analysisCriteria.append(Properties.Criterion.METHOD); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.OUTPUT); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.INPUT); analysisCriteria.append(",");
        analysisCriteria.append(Properties.Criterion.BRANCH);
        analysisCriteria.append(Properties.Criterion.EXCEPTION);
        Properties.ANALYSIS_CRITERIA = analysisCriteria.toString();
        
        Properties.CRITERION = new Properties.Criterion[5];
        Properties.CRITERION[0] = Properties.Criterion.METHOD;
        Properties.CRITERION[1] = Properties.Criterion.OUTPUT;
        Properties.CRITERION[2] = Properties.Criterion.INPUT;
        Properties.CRITERION[3] = Properties.Criterion.BRANCH;
        Properties.CRITERION[4] = Properties.Criterion.EXCEPTION;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 19, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.145834);
	}*/

	@Test
	public void testGeneratesDuplicatedNamesWhenExceptionIsThrown() {
		// non-deterministic
		// when two tests throw the same assertion, their names are duplicated.

		EvoSuite evosuite = new EvoSuite();

		String targetClass = TByteListDecorator.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ID_NAMING = true;
		Properties.JUNIT_TESTS = true;

		StringBuilder analysisCriteria = new StringBuilder();
		analysisCriteria.append(Properties.Criterion.METHOD); analysisCriteria.append(",");
		analysisCriteria.append(Properties.Criterion.OUTPUT); analysisCriteria.append(",");
		analysisCriteria.append(Properties.Criterion.INPUT); analysisCriteria.append(",");
		analysisCriteria.append(Properties.Criterion.BRANCH);
		analysisCriteria.append(Properties.Criterion.EXCEPTION);
		Properties.ANALYSIS_CRITERIA = analysisCriteria.toString();

		Properties.CRITERION = new Properties.Criterion[5];
		Properties.CRITERION[0] = Properties.Criterion.METHOD;
		Properties.CRITERION[1] = Properties.Criterion.OUTPUT;
		Properties.CRITERION[2] = Properties.Criterion.INPUT;
		Properties.CRITERION[3] = Properties.Criterion.BRANCH;
		Properties.CRITERION[4] = Properties.Criterion.EXCEPTION;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 10, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.145834);
	}

}
