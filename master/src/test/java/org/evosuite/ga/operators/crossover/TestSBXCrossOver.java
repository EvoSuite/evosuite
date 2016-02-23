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
package org.evosuite.ga.operators.crossover;

import org.evosuite.Properties;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.variables.DoubleVariable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Simulated Binary CrossOver (SBX)
 * 
 * @author José Campos
 */
@SuppressWarnings("rawtypes")
public class TestSBXCrossOver
{
	@BeforeClass
	public static void setUp() {
		Properties.RANDOM_SEED = 1l;
	}

	@Test
	public void crossoverEqualChromosomes() throws Exception
	{
	    NSGAChromosome c1 = new NSGAChromosome();
        c1.addVariable(new DoubleVariable(0.5, 0.0, 1.0));
        NSGAChromosome c2 = new NSGAChromosome();
        c2.addVariable(new DoubleVariable(0.5, 0.0, 1.0));

        SBXCrossover sbx = new SBXCrossover();
        sbx.crossOver(c1, c2);

        double v_c1 = ((DoubleVariable)c1.getVariable(0)).getValue();
        Assert.assertEquals(v_c1, 0.5, 0.0);
        double v_c2 = ((DoubleVariable)c2.getVariable(0)).getValue();
        Assert.assertEquals(v_c2, 0.5, 0.0);
	}

	@Test
    public void crossoverDifferentChromosomes() throws Exception
    {
        NSGAChromosome c1 = new NSGAChromosome();
        c1.addVariable(new DoubleVariable(0.9, 0.0, 1.0));
        NSGAChromosome c2 = new NSGAChromosome();
        c2.addVariable(new DoubleVariable(0.1, 0.0, 1.0));

        SBXCrossover sbx = new SBXCrossover();
        sbx.crossOver(c1, c2);

        double v_c1 = ((DoubleVariable)c1.getVariable(0)).getValue();
        Assert.assertEquals(v_c1, 0.1, 0.01);
        double v_c2 = ((DoubleVariable)c2.getVariable(0)).getValue();
        Assert.assertEquals(v_c2, 0.9, 0.01);
    }
}
