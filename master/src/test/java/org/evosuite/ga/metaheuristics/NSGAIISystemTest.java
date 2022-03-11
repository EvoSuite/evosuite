/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Algorithm;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.comparators.RankAndCrowdingDistanceComparator;
import org.evosuite.ga.comparators.SortByFitness;
import org.evosuite.ga.operators.ranking.CrowdingDistance;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.problems.multiobjective.SCH;
import org.evosuite.ga.problems.singleobjective.Booths;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.Calculator;

/**
 * NSGA-II test
 *
 * @author José Campos
 */
public class NSGAIISystemTest extends SystemTestBase {
    @BeforeClass
    public static void setUp() {
        Properties.CROSSOVER_RATE = 0.9;
        Properties.RANDOM_SEED = 1L;
    }

    @Test
    public void testUnionEmptyPopulation() {
        NSGAII<NSGAChromosome> ga = new NSGAII<>(null);

        List<NSGAChromosome> pop = new ArrayList<>();
        List<NSGAChromosome> off = new ArrayList<>();
        List<NSGAChromosome> union = ga.union(pop, off);

        Assert.assertTrue(union.isEmpty());
    }

    @Test
    public void testUnion() {
        NSGAII<NSGAChromosome> ga = new NSGAII<>(null);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();
        NSGAChromosome c3 = new NSGAChromosome();

        List<NSGAChromosome> pop = new ArrayList<>();
        pop.add(c1);
        pop.add(c2);

        List<NSGAChromosome> off = new ArrayList<>();
        off.add(c3);

        List<NSGAChromosome> union = ga.union(pop, off);
        Assert.assertEquals(union.size(), 3);
    }

    @Test
    public void testFastNonDominatedSort() {
        NSGAII<NSGAChromosome> ga = new NSGAII<>(null);

        Problem<NSGAChromosome> p = new Booths();
        List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
        ga.addFitnessFunctions(fitnessFunctions);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();
        NSGAChromosome c3 = new NSGAChromosome();
        NSGAChromosome c4 = new NSGAChromosome();
        NSGAChromosome c5 = new NSGAChromosome();
        NSGAChromosome c6 = new NSGAChromosome();
        NSGAChromosome c7 = new NSGAChromosome();
        NSGAChromosome c8 = new NSGAChromosome();
        NSGAChromosome c9 = new NSGAChromosome();
        NSGAChromosome c10 = new NSGAChromosome();

        // Set Fitness
        c1.setFitness(fitnessFunctions.get(0), 0.6);
        c2.setFitness(fitnessFunctions.get(0), 0.2);
        c3.setFitness(fitnessFunctions.get(0), 0.4);
        c4.setFitness(fitnessFunctions.get(0), 0.0);
        c5.setFitness(fitnessFunctions.get(0), 0.8);
        c6.setFitness(fitnessFunctions.get(0), 0.8);
        c7.setFitness(fitnessFunctions.get(0), 0.2);
        c8.setFitness(fitnessFunctions.get(0), 0.4);
        c9.setFitness(fitnessFunctions.get(0), 0.6);
        c10.setFitness(fitnessFunctions.get(0), 0.0);

        List<NSGAChromosome> population = new ArrayList<>();
        population.add(c1);
        population.add(c2);
        population.add(c3);
        population.add(c4);
        population.add(c5);
        population.add(c6);
        population.add(c7);
        population.add(c8);
        population.add(c9);
        population.add(c10);

        ga.getRankingFunction().computeRankingAssignment(population, new LinkedHashSet<>(fitnessFunctions));

        // Total number of Fronts
        Assert.assertEquals(ga.getRankingFunction().getNumberOfSubfronts(), 5);

        // Front 0
        Assert.assertEquals(0.0, ga.getRankingFunction().getSubfront(0).get(0).getFitness(), 0.0);
        //Assert.assertTrue(ga.getRankingFunction().getSubfront(0).get(1).getFitness() == 0.0);

        // Front 1
        Assert.assertEquals(0.2, ga.getRankingFunction().getSubfront(1).get(0).getFitness(), 0.0);
        Assert.assertEquals(0.2, ga.getRankingFunction().getSubfront(1).get(1).getFitness(), 0.0);

        // Front 2
        Assert.assertEquals(0.4, ga.getRankingFunction().getSubfront(2).get(0).getFitness(), 0.0);
        Assert.assertEquals(0.4, ga.getRankingFunction().getSubfront(2).get(1).getFitness(), 0.0);

        // Front 3
        Assert.assertEquals(0.6, ga.getRankingFunction().getSubfront(3).get(0).getFitness(), 0.0);
        Assert.assertEquals(0.6, ga.getRankingFunction().getSubfront(3).get(1).getFitness(), 0.0);

        // Front 4
        Assert.assertEquals(0.8, ga.getRankingFunction().getSubfront(4).get(0).getFitness(), 0.0);
        Assert.assertEquals(0.8, ga.getRankingFunction().getSubfront(4).get(1).getFitness(), 0.0);
    }

    @Test
    public void testCrowingDistanceAssignment_OneVariable() {
        NSGAII<NSGAChromosome> ga = new NSGAII<>(null);

        Problem<NSGAChromosome> p = new Booths();
        List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
        ga.addFitnessFunctions(fitnessFunctions);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();
        NSGAChromosome c3 = new NSGAChromosome();
        NSGAChromosome c4 = new NSGAChromosome();
        NSGAChromosome c5 = new NSGAChromosome();
        NSGAChromosome c6 = new NSGAChromosome();
        NSGAChromosome c7 = new NSGAChromosome();
        NSGAChromosome c8 = new NSGAChromosome();
        NSGAChromosome c9 = new NSGAChromosome();
        NSGAChromosome c10 = new NSGAChromosome();

        // Set Fitness
        c1.setFitness(fitnessFunctions.get(0), 0.0);
        c2.setFitness(fitnessFunctions.get(0), 0.2);
        c3.setFitness(fitnessFunctions.get(0), 0.4);
        c4.setFitness(fitnessFunctions.get(0), 0.6);
        c5.setFitness(fitnessFunctions.get(0), 0.8);
        c6.setFitness(fitnessFunctions.get(0), 0.0);
        c7.setFitness(fitnessFunctions.get(0), 0.2);
        c8.setFitness(fitnessFunctions.get(0), 0.4);
        c9.setFitness(fitnessFunctions.get(0), 0.6);
        c10.setFitness(fitnessFunctions.get(0), 0.8);

        List<NSGAChromosome> population = new ArrayList<>();
        population.add(c1);
        population.add(c2);
        population.add(c3);
        population.add(c4);
        population.add(c5);
        population.add(c6);
        population.add(c7);
        population.add(c8);
        population.add(c9);
        population.add(c10);

        CrowdingDistance<NSGAChromosome> crowdingDistance = new CrowdingDistance<>();
        crowdingDistance.crowdingDistanceAssignment(population, fitnessFunctions);
        population.sort(new RankAndCrowdingDistanceComparator<>(true));

        Assert.assertEquals(population.get(0).getDistance(), Double.POSITIVE_INFINITY, 0.0);
        Assert.assertEquals(population.get(1).getDistance(), Double.POSITIVE_INFINITY, 0.0);

        double epsilon = 1e-10;
        Assert.assertTrue(Math.abs(0.25 - population.get(2).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.25 - population.get(3).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.25 - population.get(4).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.25 - population.get(5).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.25 - population.get(6).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.25 - population.get(7).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.25 - population.get(8).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.25 - population.get(9).getDistance()) < epsilon);
    }

    @Test
    public void testCrowingDistanceAssignment_SeveralVariables() {
        NSGAII<NSGAChromosome> ga = new NSGAII<>(null);

        Problem<NSGAChromosome> p = new SCH();
        List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
        ga.addFitnessFunctions(fitnessFunctions);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();
        NSGAChromosome c3 = new NSGAChromosome();
        NSGAChromosome c4 = new NSGAChromosome();
        NSGAChromosome c5 = new NSGAChromosome();
        NSGAChromosome c6 = new NSGAChromosome();
        NSGAChromosome c7 = new NSGAChromosome();
        NSGAChromosome c8 = new NSGAChromosome();
        NSGAChromosome c9 = new NSGAChromosome();
        NSGAChromosome c10 = new NSGAChromosome();

        // Set Fitness 1
        c1.setFitness(fitnessFunctions.get(0), 0.0);
        c2.setFitness(fitnessFunctions.get(0), 0.2);
        c3.setFitness(fitnessFunctions.get(0), 0.4);
        c4.setFitness(fitnessFunctions.get(0), 0.6);
        c5.setFitness(fitnessFunctions.get(0), 0.8);
        c6.setFitness(fitnessFunctions.get(0), 0.0);
        c7.setFitness(fitnessFunctions.get(0), 0.2);
        c8.setFitness(fitnessFunctions.get(0), 0.4);
        c9.setFitness(fitnessFunctions.get(0), 0.6);
        c10.setFitness(fitnessFunctions.get(0), 0.8);

        // Set Fitness 2
        c1.setFitness(fitnessFunctions.get(1), 0.1);
        c2.setFitness(fitnessFunctions.get(1), 0.3);
        c3.setFitness(fitnessFunctions.get(1), 0.5);
        c4.setFitness(fitnessFunctions.get(1), 0.7);
        c5.setFitness(fitnessFunctions.get(1), 0.9);
        c6.setFitness(fitnessFunctions.get(1), 0.1);
        c7.setFitness(fitnessFunctions.get(1), 0.3);
        c8.setFitness(fitnessFunctions.get(1), 0.5);
        c9.setFitness(fitnessFunctions.get(1), 0.7);
        c10.setFitness(fitnessFunctions.get(1), 0.9);

        List<NSGAChromosome> population = new ArrayList<>();
        population.add(c1);
        population.add(c2);
        population.add(c3);
        population.add(c4);
        population.add(c5);
        population.add(c6);
        population.add(c7);
        population.add(c8);
        population.add(c9);
        population.add(c10);

        CrowdingDistance<NSGAChromosome> crowdingDistance = new CrowdingDistance<>();
        crowdingDistance.crowdingDistanceAssignment(population, fitnessFunctions);
        population.sort(new RankAndCrowdingDistanceComparator<>(true));

        Assert.assertEquals(population.get(0).getDistance(), Double.POSITIVE_INFINITY, 0.0);
        Assert.assertEquals(population.get(1).getDistance(), Double.POSITIVE_INFINITY, 0.0);

        double epsilon = 1e-10;
        Assert.assertTrue(Math.abs(0.5 - population.get(2).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(3).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(4).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(5).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(6).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(7).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(8).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(9).getDistance()) < epsilon);
    }

    @Test
    public void testIntegration() {
        Properties.MUTATION_RATE = 1d / 1d;
        Properties.CRITERION = new Criterion[2];
        Properties.CRITERION[0] = Criterion.RHO;
        Properties.CRITERION[1] = Criterion.AMBIGUITY;
        Properties.ALGORITHM = Algorithm.NSGAII;
        Properties.SELECTION_FUNCTION = Properties.SelectionFunction.BINARY_TOURNAMENT;
        Properties.MINIMIZE = false;
        Properties.INLINE = false;
        Properties.STOP_ZERO = false;
        Properties.RANKING_TYPE = Properties.RankingType.FAST_NON_DOMINATED_SORTING;

        EvoSuite evosuite = new EvoSuite();

        String targetClass = Calculator.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{
                "-generateSuite",
                "-class", targetClass
        };

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        @SuppressWarnings("unchecked")
        GeneticAlgorithm<TestSuiteChromosome> ga =
                (GeneticAlgorithm<TestSuiteChromosome>) getGAFromResult(result);

        final FitnessFunction<TestSuiteChromosome> rho = ga.getFitnessFunctions().get(0);
        final FitnessFunction<TestSuiteChromosome> ag = ga.getFitnessFunctions().get(1);

        List<TestSuiteChromosome> population = new ArrayList<>(ga.getBestIndividuals());
        population.sort(new SortByFitness<>(rho, false));

        // find the lowest rank id
        int minRank = population.stream().mapToInt(v -> v.getRank()).min().getAsInt();

        // get rid of all solutions but the ones in front id == lowest rank id
        Iterator<TestSuiteChromosome> it = population.iterator();
        while (it.hasNext()) {
            TestSuiteChromosome next = it.next();
            if (next.getRank() != minRank) {
                it.remove();
            }
        }
        Assert.assertFalse(population.isEmpty());

        TestSuiteChromosome best = population.get(0);
        Assert.assertEquals(0.0, best.getFitness(rho), 0.0);
        Assert.assertEquals(0.0, best.getFitness(ag), 0.0);
    }
}
