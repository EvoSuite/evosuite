/**
 * 
 */
package org.evosuite;

import static org.junit.Assert.assertEquals;

import com.examples.with.different.packagename.Compositional;
import org.evosuite.Properties.Algorithm;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.exception.ExceptionCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodTraceCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageSuiteFitness;
import org.evosuite.coverage.output.OutputCoverageSuiteFitness;
import org.evosuite.coverage.statement.StatementCoverageSuiteFitness;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.examples.with.different.packagename.defuse.DefUseExample1;
import com.examples.with.different.packagename.defuse.GCD;

import java.util.Map;

/**
 * @author Jose Miguel Rojas
 *
 */
public class TestCompositionalFitness extends SystemTest {

    private final double ANY_DOUBLE_1 = 2.0;
    private final double ANY_DOUBLE_2 = 5.0;
    private final double ANY_DOUBLE_3 = 6.0;
    private final double ANY_DOUBLE_4 = 3.0;

    private static final Criterion[] defaultCriterion = Properties.CRITERION;

	@Before
	public void beforeTest() {
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
        Properties.COMPOSITIONAL_FITNESS = true;
        //Properties.MINIMIZE = true;
        Properties.LOG_LEVEL = "debug";
        Properties.PRINT_TO_SYSTEM = true;
        Properties.CLIENT_ON_THREAD = true;
        Properties.ASSERTIONS = false;
	}

    @After
	public void afterTest() {
		Properties.CRITERION = defaultCriterion;
	}

    @Test
	public void testCompositionalTwoFunction() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DefUseExample1.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.CRITERION = new Properties.Criterion[2];
        Properties.CRITERION[0] = Criterion.DEFUSE;
        Properties.CRITERION[1] = Criterion.METHODNOEXCEPTION;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

        Map<FitnessFunction<?>, Double> fitnesses = best.getFitnesses();
        double sum = 0.0;
        double cov = 0.0;
        for (FitnessFunction<?> fitness : fitnesses.keySet()) {
            sum += fitnesses.get(fitness);
            cov += best.getCoverage(fitness);
            assert (fitnesses.get(fitness) == best.getFitness(fitness));
        }
        cov = cov / best.getCoverages().size();
		Assert.assertEquals("Inconsistent fitness: ", sum, best.getFitness(), 0.001);
        Assert.assertEquals("Inconsistent coverage: ", cov, best.getCoverage(), 0.001);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testGCDExample() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = GCD.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[2];
        Properties.CRITERION[0] = Criterion.ONLYBRANCH;
        Properties.CRITERION[1] = Criterion.ONLYMUTATION;
		Properties.ANALYSIS_CRITERIA = "OnlyBranch,ONLYMUTATION,METHOD,exception";

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}	

	@Test
	public void testGetFitnessForNoFunctionNoCompositional() {
		Properties.COMPOSITIONAL_FITNESS = false;
        TestSuiteChromosome c = new TestSuiteChromosome();
		assertEquals(0.0, c.getFitness(), 0.001);
	}
	
	@Test
	public void testCompositionalGetFitnessForNoFunction() {
        TestSuiteChromosome c = new TestSuiteChromosome();
		assertEquals(0.0, c.getFitness(), 0.001);
	}
	
	@Test
	public void testCompositionalGetFitnessForOneFunction() {
        TestSuiteChromosome c = new TestSuiteChromosome();
        StatementCoverageSuiteFitness f1 = new StatementCoverageSuiteFitness();
        c.addFitness(f1);
        c.setFitness(f1, ANY_DOUBLE_1);
		assertEquals(ANY_DOUBLE_1, c.getFitness(), 0.001);
	}
	
	@Test
	public void testCompositionalGetFitnessForTwoFunctions() {
        TestSuiteChromosome c = new TestSuiteChromosome();
        StatementCoverageSuiteFitness f1 = new StatementCoverageSuiteFitness();
        c.addFitness(f1);
        c.setFitness(f1, ANY_DOUBLE_1);
		BranchCoverageSuiteFitness f2 = new BranchCoverageSuiteFitness(); 
		c.addFitness(f2);
		c.setFitness(f2, ANY_DOUBLE_2);
		assertEquals(ANY_DOUBLE_1 + ANY_DOUBLE_2, c.getFitness(), 0.001);
	}
	
	@Test
	public void testCompositionalGetFitnessForSeveralFunctions() {
        Properties.TARGET_CLASS = Compositional.class.getCanonicalName();

        TestSuiteChromosome c = new TestSuiteChromosome();
        MethodTraceCoverageSuiteFitness f1 = new MethodTraceCoverageSuiteFitness();
        c.addFitness(f1);
        c.setFitness(f1, ANY_DOUBLE_1);
		MethodNoExceptionCoverageSuiteFitness f2 = new MethodNoExceptionCoverageSuiteFitness(); 
		c.addFitness(f2);
		c.setFitness(f2, ANY_DOUBLE_2);
		OutputCoverageSuiteFitness f3 = new OutputCoverageSuiteFitness(); 
		c.addFitness(f3);
		c.setFitness(f3, ANY_DOUBLE_3);
		ExceptionCoverageSuiteFitness f4 = new ExceptionCoverageSuiteFitness(); 
		c.addFitness(f4);
		c.setFitness(f4, ANY_DOUBLE_4);
		double sum = ANY_DOUBLE_1 + ANY_DOUBLE_2 + ANY_DOUBLE_3 + ANY_DOUBLE_4;
		assertEquals(sum, c.getFitness(), 0.001);
	}
}
