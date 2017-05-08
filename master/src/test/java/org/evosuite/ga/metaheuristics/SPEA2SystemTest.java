/**
 * Copyright (C) 2010-2017 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.metaheuristics;

import static org.junit.Assert.assertEquals;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Algorithm;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.coverage.rho.RhoCoverageFactory;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.problems.metrics.Spacing;
import org.evosuite.utils.Randomness;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.BMICalculator;
import com.examples.with.different.packagename.Calculator;

import java.util.ArrayList;
import java.util.List;

/**
 * SPEA2SystemTest.
 * 
 * @author José Campos
 */
public class SPEA2SystemTest extends SystemTestBase {

  @Before
  public void reset() {
    RhoCoverageFactory.getGoals().clear();
  }

  public double[][] test(String targetClass) {
    Properties.CRITERION = new Criterion[2];
    Properties.CRITERION[0] = Criterion.BRANCH;
    Properties.CRITERION[1] = Criterion.RHO;

    Properties.ALGORITHM = Algorithm.SPEA2;
    Properties.POPULATION = 100;
    Properties.SELECTION_FUNCTION = Properties.SelectionFunction.BINARY_TOURNAMENT;
    Properties.STOPPING_CONDITION = StoppingCondition.MAXGENERATIONS;
    Properties.SEARCH_BUDGET = 10;
    Properties.MINIMIZE = false;
    Randomness.setSeed(10);

    EvoSuite evosuite = new EvoSuite();

    Properties.TARGET_CLASS = targetClass;

    String[] command = new String[] {"-generateSuite", "-class", targetClass};

    Object result = evosuite.parseCommandLine(command);
    Assert.assertNotNull(result);

    GeneticAlgorithm<?> ga = getGAFromResult(result);

    final FitnessFunction<?> branch = ga.getFitnessFunctions().get(0);
    final FitnessFunction<?> rho = ga.getFitnessFunctions().get(1);

    List<Chromosome> population = new ArrayList<Chromosome>(ga.getBestIndividuals());

    double[][] front = new double[population.size()][2];
    for (int i = 0; i < population.size(); i++) {
      Chromosome c = population.get(i);
      front[i][0] = c.getFitness(branch);
      front[i][1] = c.getFitness(rho);
    }

    return front;
  }

  @Test
  public void minimalSolution() {
    String targetClass = Calculator.class.getCanonicalName();
    double[][] front = test(targetClass);

    assertEquals(0.0, front[0][0], 0.0);
    assertEquals(0.0, front[0][1], 0.0);

    Spacing sp = new Spacing();
    double[] max = sp.getMaximumValues(front);
    double[] min = sp.getMinimumValues(front);

    assertEquals(0.33, sp.evaluate(sp.getNormalizedFront(front, max, min)), 0.01);
  }

  @Test
  public void nonMinimalSolution() {
    String targetClass = BMICalculator.class.getCanonicalName();
    double[][] front = test(targetClass);

    Spacing sp = new Spacing();
    double[] max = sp.getMaximumValues(front);
    double[] min = sp.getMinimumValues(front);

    assertEquals(0.48, sp.evaluate(sp.getNormalizedFront(front, max, min)), 0.01);
  }
}
