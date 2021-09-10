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
package org.evosuite.ga.problems.singleobjective;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.NSGAII;
import org.evosuite.ga.metaheuristics.RandomFactory;
import org.evosuite.ga.operators.crossover.SBXCrossover;
import org.evosuite.ga.operators.selection.BinaryTournamentSelectionCrowdedComparison;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.variables.DoubleVariable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static java.util.Comparator.comparingDouble;

public class TestBooths {
    @BeforeClass
    public static void setUp() {
        Properties.POPULATION = 100;
        Properties.SEARCH_BUDGET = 250;
        Properties.CROSSOVER_RATE = 0.9;
        Properties.RANDOM_SEED = 1L;
    }

    @Test
    public void testBoothsFitness() {
        Problem<NSGAChromosome> p = new Booths();
        FitnessFunction<NSGAChromosome> f1 = p.getFitnessFunctions().get(0);

        double[] values = {-2.0, 1.0};
        NSGAChromosome c = new NSGAChromosome(-10.0, 10.0, values);
        Assert.assertEquals(((DoubleVariable) c.getVariables().get(0)).getValue(), -2.0, 0.0);
        Assert.assertEquals(((DoubleVariable) c.getVariables().get(1)).getValue(), 1.0, 0.0);

        Assert.assertEquals(f1.getFitness(c), 113.0, 0.0);

        double[] values_m = {1.0, 3.0};
        c = new NSGAChromosome(-10.0, 10.0, values_m);
        Assert.assertEquals(f1.getFitness(c), 0.0, 0.0);
    }

    /**
     * Testing NSGA-II with Booths Problem
     *
     * @throws IOException
     * @throws NumberFormatException
     */
    @Test
    public void testBooths() throws NumberFormatException, IOException {
        Properties.MUTATION_RATE = 1d / 2d;

        ChromosomeFactory<NSGAChromosome> factory = new RandomFactory(false, 2, -10.0, 10.0);

        GeneticAlgorithm<NSGAChromosome> ga = new NSGAII<>(factory);
        BinaryTournamentSelectionCrowdedComparison<NSGAChromosome> ts =
                new BinaryTournamentSelectionCrowdedComparison<>(false);
        //BinaryTournament ts = new BinaryTournament();
        ga.setSelectionFunction(ts);
        ga.setCrossOverFunction(new SBXCrossover());

        Problem<NSGAChromosome> p = new Booths();
        final FitnessFunction<NSGAChromosome> f1 = p.getFitnessFunctions().get(0);
        ga.addFitnessFunction(f1);

        // execute
        ga.generateSolution();

        List<NSGAChromosome> chromosomes = new ArrayList<>(ga.getPopulation());
        chromosomes.sort(comparingDouble(chr -> chr.getFitness(f1)));

        for (NSGAChromosome chromosome : chromosomes)
            Assert.assertEquals(chromosome.getFitness(f1), 0.000, 0.001);

        for (NSGAChromosome chromosome : chromosomes) {
            DoubleVariable x = (DoubleVariable) chromosome.getVariables().get(0);
            DoubleVariable y = (DoubleVariable) chromosome.getVariables().get(1);
            System.out.printf("%f,%f : %f\n", x.getValue(), y.getValue(), chromosome.getFitness(f1));
        }
    }
}
