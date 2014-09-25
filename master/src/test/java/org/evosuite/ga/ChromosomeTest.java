/**
 * 
 */
package org.evosuite.ga;

import static org.junit.Assert.assertEquals;

import org.evosuite.Properties;
import org.evosuite.Properties.Algorithm;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.exception.ExceptionCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodTraceCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageSuiteFitness;
import org.evosuite.coverage.output.OutputCoverageSuiteFitness;
import org.evosuite.coverage.statement.StatementCoverageSuiteFitness;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

/**
 * @author Jose Miguel Rojas
 *
 */
public class ChromosomeTest {

	private final double ANY_DOUBLE_1 = 2.0;
	private final double ANY_DOUBLE_2 = 5.0;
	private final double ANY_DOUBLE_3 = 6.0;
	private final double ANY_DOUBLE_4 = 3.0;

	@Test(expected=AssertionError.class)
	public void testGetFitnessShouldNotWorkForNSGAII() {
		Properties.ALGORITHM = Algorithm.NSGAII;
		TestSuiteChromosome c = new TestSuiteChromosome();
		c.getFitness();
	}

	public void testGetFitnessForOneFunctionNoCompositional() {
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
		Properties.COMPOSITIONAL_FITNESS = false;
        TestSuiteChromosome c = new TestSuiteChromosome();
        c.addFitness(new StatementCoverageSuiteFitness(), ANY_DOUBLE_1);
		c.addFitness(new BranchCoverageSuiteFitness(), ANY_DOUBLE_2);
		assertEquals(ANY_DOUBLE_1, c.getFitness(), 0.001);
	}
	
	@Test
	public void testGetFitnessForNoFunctionNoCompositional() {
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
		Properties.COMPOSITIONAL_FITNESS = false;
        TestSuiteChromosome c = new TestSuiteChromosome();
		assertEquals(0.0, c.getFitness(), 0.001);
	}
	
	@Test
	public void testCompositionalGetFitnessForNoFunction() {
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
		Properties.COMPOSITIONAL_FITNESS = true;
        TestSuiteChromosome c = new TestSuiteChromosome();
		assertEquals(0.0, c.getFitness(), 0.001);
	}
	
	@Test
	public void testCompositionalGetFitnessForOneFunction() {
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
		Properties.COMPOSITIONAL_FITNESS = true;
        TestSuiteChromosome c = new TestSuiteChromosome();
        StatementCoverageSuiteFitness f1 = new StatementCoverageSuiteFitness();
        c.addFitness(f1);
        c.setFitness(f1, ANY_DOUBLE_1);
		assertEquals(ANY_DOUBLE_1, c.getFitness(), 0.001);
	}
	
	@Test
	public void testCompositionalGetFitnessForTwoFunctions() {
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
		Properties.COMPOSITIONAL_FITNESS = true;
        TestSuiteChromosome c = new TestSuiteChromosome();
        StatementCoverageSuiteFitness f1 = new StatementCoverageSuiteFitness();
        BranchCoverageSuiteFitness f2 = new BranchCoverageSuiteFitness();
        c.addFitness(f1);
        c.addFitness(f2);
        c.setFitness(f1, ANY_DOUBLE_1);
        c.setFitness(f2, ANY_DOUBLE_2);
        c.setCoverage(f1, ANY_DOUBLE_3);
        c.setCoverage(f2, ANY_DOUBLE_4);
        assertEquals(ANY_DOUBLE_1, c.getFitnessInstanceOf(StatementCoverageSuiteFitness.class), 0.001);
        assertEquals(ANY_DOUBLE_2, c.getFitnessInstanceOf(BranchCoverageSuiteFitness.class), 0.001);
        assertEquals(ANY_DOUBLE_3, c.getCoverageInstanceOf(StatementCoverageSuiteFitness.class), 0.001);
        assertEquals(ANY_DOUBLE_4, c.getCoverageInstanceOf(BranchCoverageSuiteFitness.class), 0.001);
		assertEquals(ANY_DOUBLE_1 + ANY_DOUBLE_2, c.getFitness(), 0.001);
        assertEquals((ANY_DOUBLE_3 + ANY_DOUBLE_4) / 2, c.getCoverage(), 0.001);
	}
	
	@Test
	public void testCompositionalGetFitnessForSeveralFunctions() {
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
		Properties.COMPOSITIONAL_FITNESS = true;
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
