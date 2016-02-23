/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.ga.problems.multiobjective;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.NSGAII;
import org.evosuite.ga.metaheuristics.RandomFactory;
import org.evosuite.ga.operators.crossover.SBXCrossover;
import org.evosuite.ga.operators.selection.BinaryTournamentSelectionCrowdedComparison;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.problems.metrics.GenerationalDistance;
import org.evosuite.ga.problems.metrics.Metrics;
import org.evosuite.ga.problems.metrics.Spacing;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author José Campos
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ZDT6IntTest
{
    @Before
    public void setUp() {
        Properties.POPULATION = 100;
        Properties.SEARCH_BUDGET = 50000;
        Properties.CROSSOVER_RATE = 0.9;
        Properties.RANDOM_SEED = 1l;
    }

    @Test
    public void testZDT6Fitnesses()
    {
        Problem p = new ZDT6();
        FitnessFunction f1 = (FitnessFunction) p.getFitnessFunctions().get(0);
        FitnessFunction f2 = (FitnessFunction) p.getFitnessFunctions().get(1);

        double[] values = {0.541, 0.585, 0.915, 0.624, 0.493, 0.142, 0.971, 0.836, 0.763, 0.323};
        NSGAChromosome c = new NSGAChromosome(0.0, 1.0, values);

        Assert.assertEquals(f1.getFitness(c), 0.9866973935066625, 0.0);
        Assert.assertEquals(f2.getFitness(c), 8.903810335418541, 0.0);
    }

    /**
     * Testing NSGA-II with ZDT6 Problem
     * 
     * @throws IOException 
     * @throws NumberFormatException 
     */
    @Test
    public void testZDT6() throws NumberFormatException, IOException
    {
        Properties.MUTATION_RATE = 1d / 10d;

        ChromosomeFactory<?> factory = new RandomFactory(false, 10, 0.0, 1.0);

        GeneticAlgorithm<?> ga = new NSGAII(factory);
        BinaryTournamentSelectionCrowdedComparison ts = new BinaryTournamentSelectionCrowdedComparison();
        ts.setMaximize(false);
        ga.setSelectionFunction(ts);
        ga.setCrossOverFunction(new SBXCrossover());

        Problem p = new ZDT6();
        final FitnessFunction f1 = (FitnessFunction) p.getFitnessFunctions().get(0);
        final FitnessFunction f2 = (FitnessFunction) p.getFitnessFunctions().get(1);
        ga.addFitnessFunction(f1);
        ga.addFitnessFunction(f2);

        // execute
        ga.generateSolution();

        List<Chromosome> chromosomes = (List<Chromosome>) ga.getPopulation();
        Collections.sort(chromosomes, new Comparator<Chromosome>() {
            @Override
            public int compare(Chromosome arg0, Chromosome arg1) {
                return Double.compare(arg0.getFitness(f1), arg1.getFitness(f1));
            }
        });

        double[][] front = new double[Properties.POPULATION][2];
        int index = 0;

        for (Chromosome chromosome : chromosomes) {
            System.out.printf("%f,%f\n", chromosome.getFitness(f1), chromosome.getFitness(f2));
            front[index][0] = Double.valueOf(chromosome.getFitness(f1));
            front[index][1] = Double.valueOf(chromosome.getFitness(f2));

            index++;
        }

        // load True Pareto Front
        double[][] trueParetoFront = Metrics.readFront("ZDT6.pf");

        GenerationalDistance gd = new GenerationalDistance();
        double gdd = gd.evaluate(front, trueParetoFront);
        System.out.println("GenerationalDistance: " + gdd);
        Assert.assertEquals(gdd, 0.0005, 0.0005);

        Spacing sp = new Spacing();
        double spd = sp.evaluate(front);
        double spdt = sp.evaluate(trueParetoFront);
        System.out.println("SpacingFront (" + spd + ") - SpacingTrueFront (" + spdt + ") = "
                            + Math.abs(spd - spdt));
        Assert.assertEquals(Math.abs(spd - spdt), 0.15, 0.05);
    }
}
