package org.evosuite.ga;

import org.evosuite.Properties;
import org.evosuite.ga.variables.DoubleVariable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author José Campos
 */
public class TestNSGAChromosome
{
    @BeforeClass
    public static void setUp() {
        Properties.POPULATION = 100;
        Properties.SEARCH_BUDGET = 250;
        Properties.CROSSOVER_RATE = 0.9;
        Properties.RANDOM_SEED = 1l;
    }

    @Test
    public void testMutation()
    {
        double[] values = {-3.0};
        NSGAChromosome nsga = new NSGAChromosome(-5.0, 10.0, values);
        Assert.assertTrue(nsga.getNumberOfVariables() == 1);

        double v = ((DoubleVariable)nsga.getVariable(0)).getValue();
        Assert.assertEquals(v, -3.0, 0.0);

        nsga.mutate();
        v = ((DoubleVariable)nsga.getVariable(0)).getValue();
        Assert.assertEquals(v, -3.1, 0.1);
    }
}
