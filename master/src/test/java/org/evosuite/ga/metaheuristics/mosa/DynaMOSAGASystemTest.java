package org.evosuite.ga.metaheuristics.mosa;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.mosalisa.Stack;

public class DynaMOSAGASystemTest extends SystemTestBase {

    public List<Chromosome<?>> setup(StoppingCondition sc, int budget, String cut) {
        Properties.SELECTION_FUNCTION = Properties.SelectionFunction.RANK_CROWD_DISTANCE_TOURNAMENT;
        Properties.CRITERION = new Criterion[]{ Criterion.LINE, Criterion.BRANCH, Criterion.EXCEPTION,
                Criterion.WEAKMUTATION, Criterion.OUTPUT, Criterion.METHOD, Criterion.METHODNOEXCEPTION,
                Criterion.CBRANCH};

        System.out.println("\n\n########## EVOSUITE PROPERTIES: ##########");
        System.out.println("ALGORITHM: " + Properties.ALGORITHM);
        System.out.println("STRATEGY: " + Properties.STRATEGY);
        System.out.println("CRITERION: " + Properties.CRITERION.length + ":");
        for (int i = 0; i < Properties.CRITERION.length; i++) {
            System.out.println("- " + Properties.CRITERION[i]);
        }
        System.out.println("SELECTION FUNCTION: " + Properties.SELECTION_FUNCTION);
        System.out.println("TARGET CLASS: " + cut);
        System.out.println("##############################################\n\n");

        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[]{"-generateMOSuite", "-class", cut};

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        System.out.println("\n\n########## GENERATED TESTS: ##########");
        System.out.println(ga.getBestIndividuals());
        System.out.println("######################################\n\n");

        System.out.println("RANKING FUNCTION FROM GA: " + ga.getRankingFunction());

        return new ArrayList<>(ga.getBestIndividuals());
    }

    @Test
    public void testMOSALisa() {
        List<Chromosome<?>> population = this.setup(null, 0, Stack.class.getCanonicalName());

        Assert.assertNotEquals(population.size(), 0);
    }
}
