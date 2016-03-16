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
package org.evosuite.ga.problems.singleobjective;

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
import org.evosuite.ga.variables.DoubleVariable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestThreeHump
{
    @BeforeClass
    public static void setUp() {
        Properties.POPULATION = 100;
        Properties.SEARCH_BUDGET = 250;
        Properties.CROSSOVER_RATE = 0.9;
        Properties.RANDOM_SEED = 1l;
    }

    @Test
    public void testThreeHumpFitness()
    {
        Problem p = new ThreeHump();
        FitnessFunction f1 = (FitnessFunction) p.getFitnessFunctions().get(0);

        double[] values = {-2.0, 3.0};
        NSGAChromosome c = new NSGAChromosome(-5.0, 5.0, values);
        Assert.assertEquals(((DoubleVariable) c.getVariables().get(0)).getValue(), -2.0, 0.0);
        Assert.assertEquals(((DoubleVariable) c.getVariables().get(1)).getValue(), 3.0, 0.0);

        Assert.assertEquals(f1.getFitness(c), 4.87, 0.01);
    }

    /**
     * Testing NSGA-II with ThreeHump Problem
     * 
     * @throws IOException 
     * @throws NumberFormatException 
     */
    @Test
    public void testThreeHump() throws NumberFormatException, IOException
    {
        Properties.MUTATION_RATE = 1d / 2d;

        ChromosomeFactory<?> factory = new RandomFactory(false, 2, -5.0, 5.0);

        //GeneticAlgorithm<?> ga = new NSGAII(factory);
        GeneticAlgorithm<?> ga = new NSGAII(factory);
        BinaryTournamentSelectionCrowdedComparison ts = new BinaryTournamentSelectionCrowdedComparison();
        //BinaryTournament ts = new BinaryTournament();
        ga.setSelectionFunction(ts);
        ga.setCrossOverFunction(new SBXCrossover());

        Problem p = new ThreeHump();
        final FitnessFunction f1 = (FitnessFunction) p.getFitnessFunctions().get(0);
        ga.addFitnessFunction(f1);

        // execute
        ga.generateSolution();

        List<Chromosome> chromosomes = (List<Chromosome>) ga.getPopulation();
        Collections.sort(chromosomes, new Comparator<Chromosome>() {
            @Override
            public int compare(Chromosome arg0, Chromosome arg1) {
                return Double.compare(arg0.getFitness(f1), arg1.getFitness(f1));
            }
        });

        for (Chromosome chromosome : chromosomes)
            Assert.assertEquals(chromosome.getFitness(f1), 0.000, 0.001);

        for (Chromosome chromosome : chromosomes) {
            NSGAChromosome nsga_c = (NSGAChromosome)chromosome;

            DoubleVariable x = (DoubleVariable) nsga_c.getVariables().get(0);
            DoubleVariable y = (DoubleVariable) nsga_c.getVariables().get(1);
            System.out.printf("%f,%f : %f\n", x.getValue(), y.getValue(), chromosome.getFitness(f1));
        }
    }
}
