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
public class TestFON
{
    @BeforeClass
    public static void setUp() {
        Properties.POPULATION = 100;
        Properties.SEARCH_BUDGET = 250;
        Properties.CROSSOVER_RATE = 0.9;
        Properties.RANDOM_SEED = 1l;
    }

    @Test
    public void testFONFitnesses()
    {
        Problem p = new FON();
        FitnessFunction f1 = (FitnessFunction) p.getFitnessFunctions().get(0);
        FitnessFunction f2 = (FitnessFunction) p.getFitnessFunctions().get(1);

        double[] values = {-2, 1, 3};
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
        Properties.MUTATION_RATE = 1d / 3d; // 3 because, FON problem has 3 variables

        ChromosomeFactory<?> factory = new RandomFactory(false, 3, -4.0, 4.0);

        GeneticAlgorithm<?> ga = new NSGAII(factory);
        //GeneticAlgorithm<?> ga = new NSGAIIJMetal(factory);
        BinaryTournamentSelectionCrowdedComparison ts = new BinaryTournamentSelectionCrowdedComparison();
        //BinaryTournament ts = new BinaryTournament();
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
        /*double[] pareto_f1 = new double[Properties.POPULATION];
        double[] pareto_f2 = new double[Properties.POPULATION];
        int index = 0;

        BufferedReader br = new BufferedReader(new FileReader(ClassLoader.getSystemResource("Fonseca.pf").getPath()));
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

            DoubleVariable dv_0 = (DoubleVariable) nsga_c.getVariables().get(0);
            DoubleVariable dv_1 = (DoubleVariable) nsga_c.getVariables().get(1);
            DoubleVariable dv_2 = (DoubleVariable) nsga_c.getVariables().get(2);
            System.out.printf("%f,%f,%f\n", dv_0.getValue(), dv_1.getValue(), dv_2.getValue());

            //Assert.assertEquals(chromosome.getFitness(f1), pareto_f1[index], 0.05);
            //Assert.assertEquals(chromosome.getFitness(f2), pareto_f2[index], 0.05);
            //index++;
        }

        System.out.println("---------------");
        for (Chromosome chromosome : chromosomes)
        {
            System.out.printf("%f,%f\n", chromosome.getFitness(f1), chromosome.getFitness(f2));
        }
    }
}
