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
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ClassHierarchyIncludingInterfaces;

public class DynaMOSAGASystemTest extends SystemTestBase {

    public List<Chromosome<?>> setup(StoppingCondition sc, int budget, String cut) {
        System.out.println("ALGORITHM: " + Properties.ALGORITHM);
        System.out.println("STRATEGY: " + Properties.STRATEGY);
        System.out.println("SELECTION FUNCTION: " + Properties.SELECTION_FUNCTION);

        Properties.ALGORITHM = Algorithm.MONOTONIC_GA;

        EvoSuite evosuite = new EvoSuite();

        Properties.TARGET_CLASS = cut;

        String[] command = new String[]{"-generateSuite", "-class", cut};

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        System.out.println("# BEST INDIVIDUAL(S) " + ga.getBestIndividuals().size());

        System.out.println("\n\n########## GENERATED TESTS: ##########");
        System.out.println(ga.getBestIndividuals());
        System.out.println("######################################\n\n");

        return new ArrayList<>(ga.getBestIndividuals());
    }

    @Test
    public void testDynaMOSA() {
        List<Chromosome<?>> population = this.setup(StoppingCondition.MAXTIME, 15, ClassHierarchyIncludingInterfaces.class.getCanonicalName());

        Assert.assertNotEquals(population.size(), 0);
    }
}
