package org.evosuite.ga.metaheuristics.mosa;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.Properties.Algorithm;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.mosa.AbstractMOSA;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.testcase.TestChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.modalisa.Stack;

import javax.swing.*;

public class DynaMOSAGASystemTest extends SystemTestBase {

    public List<Chromosome<?>> setup(StoppingCondition sc, int budget, String cut) {
        System.out.println("ALGORITHM: " + Properties.ALGORITHM);
        System.out.println("STRATEGY: " + Properties.STRATEGY);
        System.out.println("SELECTION FUNCTION: " + Properties.SELECTION_FUNCTION);

        EvoSuite evosuite = new EvoSuite();

        Properties.TARGET_CLASS = cut;

        System.out.println("TARGET CLASS: " + cut);

        String[] command = new String[]{"-generateMOSuite", "-class", cut};

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        System.out.println("\n\n########## GENERATED TESTS: ##########");
        System.out.println(ga.getBestIndividuals());
        System.out.println("######################################\n\n");

        List<?> population = ga.getPopulation();
        System.out.println("######### FROM MOSA #########");
        System.out.println(population.get(0));
        System.out.println("######### END FROM MOSA #########");


        return new ArrayList<>(ga.getBestIndividuals());
    }

    @Test
    public void testDynaMOSA() {
        List<Chromosome<?>> population = this.setup(StoppingCondition.MAXTIME, 15, Stack.class.getCanonicalName());

        Assert.assertNotEquals(population.size(), 0);
    }
}
