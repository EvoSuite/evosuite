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
public class TestSCH
{
    @BeforeClass
    public static void setUp() {
        Properties.POPULATION = 100;
        Properties.SEARCH_BUDGET = 250;
        Properties.CROSSOVER_RATE = 0.9;
        Properties.RANDOM_SEED = 1l;
    }

    @Test
    public void testSCHFitnesses()
    {
        Problem p = new SCH();
        FitnessFunction f1 = (FitnessFunction) p.getFitnessFunctions().get(0);
        FitnessFunction f2 = (FitnessFunction) p.getFitnessFunctions().get(1);

        double[] values_n = {-10};
        NSGAChromosome c = new NSGAChromosome(Math.pow(-10.0, 3.0), Math.pow(10.0, 3.0), values_n);
        Assert.assertEquals(((DoubleVariable) c.getVariables().get(0)).getValue(), -10.0, 0.0);
        Assert.assertEquals(f1.getFitness(c), 100.0, 0.0);
        Assert.assertEquals(f2.getFitness(c), 144.0, 0.0);

        double[] values_z = {0};
        c = new NSGAChromosome(Math.pow(-10.0, 3.0), Math.pow(10.0, 3.0), values_z);
        Assert.assertEquals(((DoubleVariable) c.getVariables().get(0)).getValue(), 0.0, 0.0);
        Assert.assertEquals(f1.getFitness(c), 0.0, 0.0);
        Assert.assertEquals(f2.getFitness(c), 4.0, 0.0);

        double[] values_p = {10};
        c = new NSGAChromosome(Math.pow(-10.0, 3.0), Math.pow(10.0, 3.0), values_p);
        Assert.assertEquals(((DoubleVariable) c.getVariables().get(0)).getValue(), 10.0, 0.0);
        Assert.assertEquals(f1.getFitness(c), 100.0, 0.0);
        Assert.assertEquals(f2.getFitness(c), 64.0, 0.0);
    }

    /**
     * Testing NSGA-II with SCH Problem
     * 
     * @throws IOException 
     * @throws NumberFormatException 
     */
    @Test
    public void testSCH() throws NumberFormatException, IOException
    {
        Properties.MUTATION_RATE = 1d / 1d; // 1 because, SCH problem has 1 variable

        ChromosomeFactory<?> factory = new RandomFactory(false, 1, Math.pow(-10.0, 3.0), Math.pow(10.0, 3.0));

        GeneticAlgorithm<?> ga = new NSGAII(factory);
        //GeneticAlgorithm<?> ga = new NSGAIIJMetal(factory);
        BinaryTournamentSelectionCrowdedComparison ts = new BinaryTournamentSelectionCrowdedComparison();
        //BinaryTournament ts = new BinaryTournament();
        ts.setMaximize(false);
        ga.setSelectionFunction(ts);
        ga.setCrossOverFunction(new SBXCrossover());

        Problem p = new SCH();
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

        BufferedReader br = new BufferedReader(new FileReader(ClassLoader.getSystemResource("Schaffer.pf").getPath()));
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
            System.out.printf("%f\n", dv_0.getValue());

            /*Assert.assertEquals(chromosome.getFitness(f1), pareto_f1[index], 0.06);
            Assert.assertEquals(chromosome.getFitness(f2), pareto_f2[index], 0.06);
            index++;*/
        }
        System.out.println("---------------");
        for (Chromosome chromosome : chromosomes)
        {
            System.out.printf("%f,%f\n", chromosome.getFitness(f1), chromosome.getFitness(f2));
        }
    }
}
