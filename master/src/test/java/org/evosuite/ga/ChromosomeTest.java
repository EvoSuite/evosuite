/**
 * 
 */
package org.evosuite.ga;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.Properties.Algorithm;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.NoSuchParameterException;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.statement.StatementCoverageSuiteFitness;
import org.evosuite.ga.comparators.SortByFitness;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SteadyStateGA;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.examples.with.different.packagename.Calculator;

/**
 * @author Jose Miguel Rojas
 *
 */
public class ChromosomeTest extends SystemTest {
	 
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
        double ANY_DOUBLE = 2.0;
        double ANY_DOUBLE2 = 5.0;
        c.addFitness(new StatementCoverageSuiteFitness(), ANY_DOUBLE);
		c.addFitness(new BranchCoverageSuiteFitness(), ANY_DOUBLE2);
		assertEquals(ANY_DOUBLE, c.getFitness(), 0.001);
	}
	
	@Test
	public void testGetFitnessForNoFunctionNoCompositional() {
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
		Properties.COMPOSITIONAL_FITNESS = false;
        TestSuiteChromosome c = new TestSuiteChromosome();
		assertEquals(0.0, c.getFitness(), 0.001);
	}
	
	@Test
	public void testCompositionalFitnessForNoFunction() {
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
		Properties.COMPOSITIONAL_FITNESS = true;
        TestSuiteChromosome c = new TestSuiteChromosome();
		assertEquals(0.0, c.getFitness(), 0.001);
	}
	
	@Test
	public void testCompositionalFitnessForOneFunction() {
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
		Properties.COMPOSITIONAL_FITNESS = true;
        TestSuiteChromosome c = new TestSuiteChromosome();
        StatementCoverageSuiteFitness f1 = new StatementCoverageSuiteFitness();
        c.addFitness(f1);
        c.setFitness(f1, 0.2);
		assertEquals(0.2, c.getFitness(), 0.001);
	}
	
	@Test
	public void testCompositionalFitnessForSeveralFunctions() {
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
		Properties.COMPOSITIONAL_FITNESS = true;
        TestSuiteChromosome c = new TestSuiteChromosome();
        StatementCoverageSuiteFitness f1 = new StatementCoverageSuiteFitness();
        c.addFitness(f1);
        c.setFitness(f1, 0.2);
		BranchCoverageSuiteFitness f2 = new BranchCoverageSuiteFitness(); 
		c.addFitness(f2);
		c.setFitness(f2, 0.4);
		assertEquals(0.6, c.getFitness(), 0.001);
	}
}
