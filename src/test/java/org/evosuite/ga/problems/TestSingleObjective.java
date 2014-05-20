package org.evosuite.ga.problems;

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
import org.evosuite.ga.variables.DoubleVariable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestSingleObjective
{
    @BeforeClass
    public static void setUp() {
        Properties.POPULATION = 100;
        Properties.SEARCH_BUDGET = 250;
        Properties.CROSSOVER_RATE = 0.9;
        Properties.RANDOM_SEED = 1l;
    }

    @Test
    public void testSingleObjectiveFitness()
    {
        Problem p = new SingleObjective();
        FitnessFunction f1 = (FitnessFunction) p.getFitnessFunctions().get(0);

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
     * Testing NSGA-II with OneVariable Problem
     * 
     * @throws IOException 
     * @throws NumberFormatException 
     */
    @Test
    public void testSingleObjective() throws NumberFormatException, IOException
    {
        Properties.MUTATION_RATE = 1d / 2d; // 2 because, SingleObjective problem has 2 variable

        ChromosomeFactory<?> factory = new RandomFactory(false, 2, -10.0, 10.0);

        GeneticAlgorithm<?> ga = new NSGAII(factory);
        //GeneticAlgorithm<?> ga = new NSGAIIJMetal(factory);
        BinaryTournamentSelectionCrowdedComparison ts = new BinaryTournamentSelectionCrowdedComparison();
        //BinaryTournament ts = new BinaryTournament();
        ga.setSelectionFunction(ts);
        ga.setCrossOverFunction(new SBXCrossover());

        Problem p = new SingleObjective();
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

        // load Pareto Front
        /*double[] pareto_f1 = new double[Properties.POPULATION];
        double[] pareto_f2 = new double[Properties.POPULATION];
        int index = 0;

        BufferedReader br = new BufferedReader(new FileReader(ClassLoader.getSystemResource("SingleObjective.pf").getPath()));
        String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null) {
            String[] split = sCurrentLine.split("\t");
            pareto_f1[index] = Double.valueOf(split[0]);
            pareto_f2[index] = Double.valueOf(split[1]);
            index++;
        }
        br.close();

        // test
        index = 0;*/
        for (Chromosome chromosome : chromosomes)
        {
            NSGAChromosome nsga_c = (NSGAChromosome)chromosome;

            DoubleVariable x = (DoubleVariable) nsga_c.getVariables().get(0);
            DoubleVariable y = (DoubleVariable) nsga_c.getVariables().get(1);
            System.out.printf("%f,%f\n", x.getValue(), y.getValue());

            //Assert.assertEquals(chromosome.getFitness(f1), pareto_f1[index], 0.05);
            //Assert.assertEquals(chromosome.getFitness(f2), pareto_f2[index], 0.05);
            //index++;
        }

        System.out.println("---------------");
        for (Chromosome chromosome : chromosomes)
        {
            System.out.printf("%f\n", chromosome.getFitness(f1));
        }
    }
}
