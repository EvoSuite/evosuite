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

import static org.junit.Assert.assertEquals;

import org.evosuite.Properties;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.line.LineCoverageSuiteFitness;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.operators.crossover.SBXCrossover;
import org.evosuite.ga.operators.selection.BinaryTournamentSelectionCrowdedComparison;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.problems.metrics.GenerationalDistance;
import org.evosuite.ga.problems.metrics.Metrics;
import org.evosuite.ga.problems.metrics.Spacing;
import org.evosuite.ga.problems.multiobjective.ZDT4;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Unit tests for SPEA2's functions.
 *
 * @author José Campos
 */
public class SPEA2Test {

    @Test
    public void testEnvironmentalSelection_FitArchive() {
        List<TestSuiteChromosome> population = new ArrayList<>();

        TestSuiteChromosome t1 = new TestSuiteChromosome();
        t1.setDistance(0.0);
        population.add(t1);

        TestSuiteChromosome t2 = new TestSuiteChromosome();
        t2.setDistance(0.1);
        population.add(t2);

        TestSuiteChromosome t3 = new TestSuiteChromosome();
        t3.setDistance(0.2);
        population.add(t3);

        Properties.POPULATION = 3;

        SPEA2<TestSuiteChromosome> algorithm = new SPEA2<>(null);
        List<TestSuiteChromosome> archive = algorithm.environmentalSelection(population);
        assertEquals(3, archive.size());
        assertEquals(0.0, archive.get(0).getDistance(), 0.0);
        assertEquals(0.1, archive.get(1).getDistance(), 0.0);
        assertEquals(0.2, archive.get(2).getDistance(), 0.0);
    }

    @Test
    public void testEnvironmentalSelection_SmallArchive() {
        List<TestSuiteChromosome> population = new ArrayList<>();

        TestSuiteChromosome t1 = new TestSuiteChromosome();
        t1.setDistance(0.0);
        population.add(t1);

        TestSuiteChromosome t2 = new TestSuiteChromosome();
        t2.setDistance(0.1);
        population.add(t2);

        TestSuiteChromosome t3 = new TestSuiteChromosome();
        t3.setDistance(0.2);
        population.add(t3);

        Properties.POPULATION = 5;

        SPEA2<TestSuiteChromosome> algorithm = new SPEA2<>(null);
        List<TestSuiteChromosome> archive = algorithm.environmentalSelection(population);
        assertEquals(3, archive.size());

        population.clear();
        population.add(t1);
        population.add(t2);
        population.add(t3);

        TestSuiteChromosome t4 = new TestSuiteChromosome();
        t4.setDistance(1.0);
        population.add(t4);

        TestSuiteChromosome t5 = new TestSuiteChromosome();
        t5.setDistance(1.0);
        population.add(t5);

        archive = algorithm.environmentalSelection(population);
        assertEquals(5, archive.size());
    }

    @Test
    public void testEnvironmentalSelection_ArchiveTruncation() {
        List<TestSuiteChromosome> population = new ArrayList<>();

        BranchCoverageSuiteFitness branch = new BranchCoverageSuiteFitness();
        LineCoverageSuiteFitness line = new LineCoverageSuiteFitness();

        // fill population with good solutions

        TestSuiteChromosome t1 = new TestSuiteChromosome();
        t1.setFitness(branch, 0.0);
        t1.setFitness(line, 0.0);
        population.add(t1);

        TestSuiteChromosome t2 = new TestSuiteChromosome();
        t2.setFitness(branch, 0.01);
        t2.setFitness(line, 0.1);
        population.add(t2);

        TestSuiteChromosome t3 = new TestSuiteChromosome();
        t3.setFitness(branch, 0.5);
        t3.setFitness(line, 0.5);
        population.add(t3);

        TestSuiteChromosome t4 = new TestSuiteChromosome();
        t4.setFitness(branch, 0.75);
        t4.setFitness(line, 0.5);
        population.add(t4);

        TestSuiteChromosome t5 = new TestSuiteChromosome();
        t5.setFitness(branch, 0.75);
        t5.setFitness(line, 0.5);
        population.add(t5);

        TestSuiteChromosome t6 = new TestSuiteChromosome();
        t6.setFitness(branch, 0.80);
        t6.setFitness(line, 0.80);
        population.add(t6);

        Properties.POPULATION = 3; // max number of solutions

        SPEA2<TestSuiteChromosome> algorithm = new SPEA2<>(null);
        List<TestSuiteChromosome> archive = algorithm.environmentalSelection(population);
        assertEquals(3, archive.size());
        assertEquals(t1, archive.get(0));
        assertEquals(t3, archive.get(1));
        assertEquals(t6, archive.get(2));
    }

    @Test
    public void testComputeStrength() {
        List<TestSuiteChromosome> population = new ArrayList<>();

        BranchCoverageSuiteFitness branch = new BranchCoverageSuiteFitness();
        LineCoverageSuiteFitness line = new LineCoverageSuiteFitness();

        // dominates all the other two chromosomes
        TestSuiteChromosome t1 = new TestSuiteChromosome();
        t1.setFitness(branch, 0.0);
        t1.setFitness(line, 0.0);
        population.add(t1);

        // t2 only dominates t3
        TestSuiteChromosome t2 = new TestSuiteChromosome();
        t2.setFitness(branch, 0.5);
        t2.setFitness(line, 0.5);
        population.add(t2);

        // t3 is dominated by all chromosomes
        TestSuiteChromosome t3 = new TestSuiteChromosome();
        t3.setFitness(branch, 1.0);
        t3.setFitness(line, 1.0);
        population.add(t3);

        SPEA2<TestSuiteChromosome> algorithm = new SPEA2<>(null);
        algorithm.computeStrength(population);

        assertEquals(0.36, t1.getDistance(), 0.01);
        assertEquals(2.36, t2.getDistance(), 0.01);
        assertEquals(3.36, t3.getDistance(), 0.01);

        // invert order of chromosomes

        population.clear();
        population.add(t3);
        population.add(t2);
        population.add(t1);

        algorithm.computeStrength(population);

        assertEquals(0.36, t1.getDistance(), 0.01);
        assertEquals(2.36, t2.getDistance(), 0.01);
        assertEquals(3.36, t3.getDistance(), 0.01);
    }

    @Test
    public void testEuclideanDistanceMatrix_EmptyPopulation() {
        SPEA2<?> algorithm = new SPEA2<>(null);
        double[][] matrix = algorithm.euclideanDistanceMatrix(new ArrayList<>());
        assertEquals(0, matrix.length);
    }

    @Test
    public void testEuclideanDistanceMatrix() {
        List<TestSuiteChromosome> population = new ArrayList<>();

        BranchCoverageSuiteFitness branch = new BranchCoverageSuiteFitness();
        LineCoverageSuiteFitness line = new LineCoverageSuiteFitness();

        TestSuiteChromosome t1 = new TestSuiteChromosome();
        t1.setFitness(branch, 0.5);
        t1.setFitness(line, 0.3);
        population.add(t1);

        TestSuiteChromosome t2 = new TestSuiteChromosome();
        t2.setFitness(branch, 0.3);
        t2.setFitness(line, 0.5);
        population.add(t2);

        SPEA2<TestSuiteChromosome> algorithm = new SPEA2<>(null);
        double[][] matrix = algorithm.euclideanDistanceMatrix(population);
        assertEquals(2, matrix.length);
        assertEquals(2, matrix[0].length);
        assertEquals(2, matrix[1].length);
        assertEquals(0.0, matrix[0][0], 0.0);
        assertEquals(0.0, matrix[1][1], 0.0);
        assertEquals(0.28, matrix[0][1], 0.01);
        assertEquals(0.28, matrix[1][0], 0.01);
    }

    @Test
    public void testDistanceOfEqualChromosomes() {
        SPEA2<TestSuiteChromosome> algorithm = new SPEA2<>(null);

        BranchCoverageSuiteFitness branch = new BranchCoverageSuiteFitness();
        LineCoverageSuiteFitness line = new LineCoverageSuiteFitness();

        TestSuiteChromosome t1 = new TestSuiteChromosome();
        t1.setFitness(branch, 0.5);
        t1.setFitness(line, 0.3);

        TestSuiteChromosome t2 = new TestSuiteChromosome();
        t2.setFitness(branch, 0.5);
        t2.setFitness(line, 0.3);

        assertEquals(0.0, algorithm.distanceBetweenObjectives(t1, t2), 0.0);
    }

    @Test
    public void testDistanceOfDifferentChromosomes() {
        SPEA2<TestSuiteChromosome> algorithm = new SPEA2<>(null);

        BranchCoverageSuiteFitness branch = new BranchCoverageSuiteFitness();
        LineCoverageSuiteFitness line = new LineCoverageSuiteFitness();

        TestSuiteChromosome t1 = new TestSuiteChromosome();
        t1.setFitness(branch, 0.5);
        t1.setFitness(line, 0.3);

        TestSuiteChromosome t2 = new TestSuiteChromosome();
        t2.setFitness(branch, 0.3);
        t2.setFitness(line, 0.5);

        assertEquals(0.28, algorithm.distanceBetweenObjectives(t1, t2), 0.01);
    }

    @Test
    public void testZDT4() throws IOException {
        Properties.MUTATION_RATE = 1d / 10d;
        Properties.POPULATION = 100;
        Properties.SEARCH_BUDGET = 250;
        Properties.STOPPING_CONDITION = StoppingCondition.MAXGENERATIONS;
        Properties.CROSSOVER_RATE = 0.9;
        Properties.RANDOM_SEED = 1L;

        ChromosomeFactory<NSGAChromosome> factory = new RandomFactory(true, 10, -5.0, 5.0);

        GeneticAlgorithm<NSGAChromosome> ga = new SPEA2<>(factory);
        BinaryTournamentSelectionCrowdedComparison<NSGAChromosome> ts =
                new BinaryTournamentSelectionCrowdedComparison<>();
        ts.setMaximize(false);
        ga.setSelectionFunction(ts);
        ga.setCrossOverFunction(new SBXCrossover());

        Problem<NSGAChromosome> p = new ZDT4();
        final FitnessFunction<NSGAChromosome> f1 = p.getFitnessFunctions().get(0);
        final FitnessFunction<NSGAChromosome> f2 = p.getFitnessFunctions().get(1);
        ga.addFitnessFunction(f1);
        ga.addFitnessFunction(f2);

        // execute
        ga.generateSolution();

        List<NSGAChromosome> chromosomes = new ArrayList<>(ga.getPopulation());
        chromosomes.sort(Comparator.comparingDouble(chr -> chr.getFitness(f1)));

        double[][] front = new double[Properties.POPULATION][2];
        for (int i = 0; i < chromosomes.size(); i++) {
            NSGAChromosome chromosome = chromosomes.get(i);
            System.out.printf("%f,%f\n", chromosome.getFitness(f1), chromosome.getFitness(f2));
            front[i][0] = chromosome.getFitness(f1);
            front[i][1] = chromosome.getFitness(f2);
        }

        // load True Pareto Front
        double[][] trueParetoFront = Metrics.readFront("ZDT4.pf");

        GenerationalDistance gd = new GenerationalDistance();
        double gdd = gd.evaluate(front, trueParetoFront);
        System.out.println("GenerationalDistance: " + gdd);
        assertEquals(0.00, gdd, 0.01);

        Spacing sp = new Spacing();
        double spd = sp.evaluate(front);
        System.out.println("SpacingFront: " + spd);
        assertEquals(0.71, spd, 0.01);
    }
}
