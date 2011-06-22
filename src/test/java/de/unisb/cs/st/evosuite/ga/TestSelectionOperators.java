package de.unisb.cs.st.evosuite.ga;

import org.junit.*;
import java.util.*;

import de.unisb.cs.st.evosuite.testcase.*;

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
