package org.evosuite.coverage.statement;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;
import com.examples.with.different.packagename.IntExample;

public class StatementCoverageFitnessFunctionSystemTest extends SystemTestBase {

  private void test() {
    Properties.CRITERION = new Properties.Criterion[] {Criterion.STATEMENT};

    String targetClass = IntExample.class.getCanonicalName();
    Properties.TARGET_CLASS = targetClass;

    EvoSuite evosuite = new EvoSuite();
    String[] command = new String[] {"-class", targetClass, "-generateSuite"};
    Object result = evosuite.parseCommandLine(command);
    GeneticAlgorithm<?> ga = getGAFromResult(result);
    TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

    System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
    System.out.println("EvolvedTestSuite:\n" + best);
    int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size();
    Assert.assertEquals(20, goals);
    Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
  }

  @Test
  public void test100StatementCoverageWithArchive() {
    Properties.TEST_ARCHIVE = true;
    this.test();
  }

  @Test
  public void test100StatementCoverageWithoutArchive() {
    Properties.TEST_ARCHIVE = false;
    this.test();
  }
}
