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
package org.evosuite.ga.comparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.problems.multiobjective.FON;
import org.evosuite.ga.problems.multiobjective.FONIntTest;
import org.evosuite.ga.problems.singleobjective.Booths;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author José Campos
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestDominanceComparator
{
    @Test
    public void testDominanceComparatorOneFitness()
    {
        Problem p = new Booths<NSGAChromosome>();
        List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
        FitnessFunction<NSGAChromosome> ff = fitnessFunctions.get(0);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();

        // Set Fitness
        c1.setFitness(ff, 0.7);
        c2.setFitness(ff, 0.3);

        List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
        population.add(c1);
        population.add(c2);

        DominanceComparator dc = new DominanceComparator();
        Collections.sort(population, dc);

        Assert.assertEquals(population.get(0).getFitness(ff), 0.3, 0.0);
        Assert.assertEquals(population.get(1).getFitness(ff), 0.7, 0.0);
    }

    @Test
    public void testDominanceComparatorSeveralFitnessesNoDomination()
    {
        Problem p = new FON();
        List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
        FitnessFunction<NSGAChromosome> ff_1 = fitnessFunctions.get(0);
        FitnessFunction<NSGAChromosome> ff_2 = fitnessFunctions.get(1);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();

        // Set Fitness
        c1.setFitness(ff_1, 0.7);
        c1.setFitness(ff_2, 0.2);
        c2.setFitness(ff_1, 0.3);
        c2.setFitness(ff_2, 0.5);

        List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
        population.add(c1);
        population.add(c2);

        DominanceComparator dc = new DominanceComparator();
        Collections.sort(population, dc);

        Assert.assertEquals(population.get(0).getFitness(ff_1), 0.7, 0.0);
        Assert.assertEquals(population.get(0).getFitness(ff_2), 0.2, 0.0);
        Assert.assertEquals(population.get(1).getFitness(ff_1), 0.3, 0.0);
        Assert.assertEquals(population.get(1).getFitness(ff_2), 0.5, 0.0);
    }

    @Test
    public void testDominanceComparatorSeveralFitnessesDomination()
    {
        Problem p = new FON();
        List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
        FitnessFunction<NSGAChromosome> ff_1 = fitnessFunctions.get(0);
        FitnessFunction<NSGAChromosome> ff_2 = fitnessFunctions.get(1);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();

        // Set Fitness
        c1.setFitness(ff_1, 0.7);
        c1.setFitness(ff_2, 0.6);
        c2.setFitness(ff_1, 0.3);
        c2.setFitness(ff_2, 0.5);

        List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
        population.add(c1);
        population.add(c2);

        DominanceComparator dc = new DominanceComparator();
        Collections.sort(population, dc);

        Assert.assertEquals(population.get(0).getFitness(ff_1), 0.3, 0.0);
        Assert.assertEquals(population.get(0).getFitness(ff_2), 0.5, 0.0);
        Assert.assertEquals(population.get(1).getFitness(ff_1), 0.7, 0.0);
        Assert.assertEquals(population.get(1).getFitness(ff_2), 0.6, 0.0);
    }
}
