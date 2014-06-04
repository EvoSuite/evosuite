/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessProportionateSelection;
import org.evosuite.ga.RankSelection;
import org.evosuite.ga.SelectionFunction;
import org.evosuite.ga.TournamentSelection;
import org.evosuite.testcase.*;
import org.junit.*;
import java.util.*;


/**
 * @author Andrea Arcuri
 * 
 */
public class TestSelectionOperators 
{
	@Test
	public void testMaximizeVariable()
	{
		SelectionFunction[] v = new SelectionFunction[]{
				new TournamentSelection(),
				new FitnessProportionateSelection(),
				new RankSelection()
		};

		for(SelectionFunction sf : v)
		{
			sf.setMaximize(true);
			Assert.assertTrue(sf.isMaximize());
			sf.setMaximize(false);
			Assert.assertTrue(!sf.isMaximize());
		}
	}

	@Ignore
	@Test
	public void testProportions()
	{
		boolean[] maximize = new boolean[]{false,true};

		SelectionFunction[] v = new SelectionFunction[]{
				new TournamentSelection(),
				new FitnessProportionateSelection(),
				new RankSelection()
		};

		final int N = 10;
		
		for(boolean b : maximize)
		{
			List<Chromosome> population = new LinkedList<Chromosome>();
			for(int i=0; i<N; i++)
			{
				ExecutableChromosome ind = new TestChromosome();
				double fit = b ? N-i : i;
				ind.setFitness(fit);
				//Rank selection assumes the population in order, but for the others does not matter
				population.add(ind);
			}
			
			for(SelectionFunction sf : v)
			{
				sf.setMaximize(b);
				
				int[] counter = new int[N];
				
				for(int j=0; j<10000; j++)
				{
					int index = sf.getIndex(population);
					counter[index]++;
				}
				
				for(int j=0; j<N-1; j++)
				{
					Assert.assertTrue(""+counter[j]+" "+counter[j+1], counter[j] > counter[j+1]);
				}
				
				Assert.assertTrue(counter[N-1]>0);
			}
		}
	}
}
