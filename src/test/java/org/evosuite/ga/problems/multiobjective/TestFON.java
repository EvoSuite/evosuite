package org.evosuite.ga.problems.multiobjective;

import java.io.BufferedReader;
import java.io.FileReader;
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
public class TestFON
{
    @BeforeClass
    public static void setUp() {
        Properties.POPULATION = 100;
        Properties.SEARCH_BUDGET = 10000;
        Properties.CROSSOVER_RATE = 0.9;
        Properties.RANDOM_SEED = 1l;
    }

    @Test
    public void testFONFitnesses()
    {
        Problem p = new FON();
        FitnessFunction f1 = (FitnessFunction) p.getFitnessFunctions().get(0);
        FitnessFunction f2 = (FitnessFunction) p.getFitnessFunctions().get(1);

        double[] values = {-2.0, 1.0, 3.0};
        NSGAChromosome c = new NSGAChromosome(-4.0, 4.0, values);
        Assert.assertEquals(((DoubleVariable) c.getVariables().get(0)).getValue(), -2.0, 0.0);
        Assert.assertEquals(((DoubleVariable) c.getVariables().get(1)).getValue(), 1.0, 0.0);
        Assert.assertEquals(((DoubleVariable) c.getVariables().get(2)).getValue(), 3.0, 0.0);

        Assert.assertEquals(f1.getFitness(c), 0.9999969200553233, 0.0);
        Assert.assertEquals(f2.getFitness(c), 0.9999999696175615, 0.0);
    }

    /**
     * Testing NSGA-II with FON Problem
     * 
     * @throws IOException 
     * @throws NumberFormatException 
     */
    @Test
    public void testFON() throws NumberFormatException, IOException
    {
        Properties.MUTATION_RATE = 1d / 3d;

        ChromosomeFactory<?> factory = new RandomFactory(false, 3, -4.0, 4.0);

        GeneticAlgorithm<?> ga = new NSGAII(factory);
        BinaryTournamentSelectionCrowdedComparison ts = new BinaryTournamentSelectionCrowdedComparison();
        ga.setSelectionFunction(ts);
        ga.setCrossOverFunction(new SBXCrossover());

        Problem p = new FON();
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

        // load Pareto Front
        double[] x = new double[Properties.POPULATION];
        double[] y = new double[Properties.POPULATION];
        int index = 0;

        BufferedReader br = new BufferedReader(new FileReader(ClassLoader.getSystemResource("Fonseca.pf").getPath()));
        String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null) {
            String[] split = sCurrentLine.split(",");
            x[index] = Double.valueOf(split[0]);
            y[index] = Double.valueOf(split[1]);
            index++;
        }
        br.close();

        // test
        index = 0;
        for (Chromosome chromosome : chromosomes)
        {
            System.out.printf("%f,%f\n", chromosome.getFitness(f1), chromosome.getFitness(f2));

            Assert.assertEquals(chromosome.getFitness(f1), x[index], 0.10);
            Assert.assertEquals(chromosome.getFitness(f2), y[index], 0.10);
            index++;
        }
    }
}
