package org.evosuite.ga.operators.selection;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.metaheuristics.NSGAII;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Binary Tournament Selection using Crowded Comparison
 * 
 * @author José Campos
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestBinaryTournamentSelectionCrowdedComparison
{
	@BeforeClass
	public static void setUp() {
		Properties.RANDOM_SEED = 1l;
	}

	@Test
	public void testNonDominationRankMinimize()
	{
		NSGAII<NSGAChromosome> ga = new NSGAII<NSGAChromosome>(null);
		BinaryTournamentSelectionCrowdedComparison ts = new BinaryTournamentSelectionCrowdedComparison(false);
		ts.setMaximize(false);
		ga.setSelectionFunction(ts);

		NSGAChromosome c1 = new NSGAChromosome();
		NSGAChromosome c2 = new NSGAChromosome();

		// Set Rank
		c1.setRank(1);
		c2.setRank(0);

		List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
		population.add(c1);
		population.add(c2);

		Assert.assertTrue(ts.getIndex(population) == 1);
	}

	@Test
	public void testNonDominationRankMaximize()
	{
		NSGAII<NSGAChromosome> ga = new NSGAII<NSGAChromosome>(null);
		BinaryTournamentSelectionCrowdedComparison ts = new BinaryTournamentSelectionCrowdedComparison(true);
		ts.setMaximize(true);
		ga.setSelectionFunction(ts);

		NSGAChromosome c1 = new NSGAChromosome();
		NSGAChromosome c2 = new NSGAChromosome();

		// Set Rank
		c1.setRank(1);
		c2.setRank(0);

		List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
		population.add(c1);
		population.add(c2);

		Assert.assertTrue(ts.getIndex(population) == 0);
	}

	@Test
	public void testCrowdingDistanceMinimize()
	{
		NSGAII<NSGAChromosome> ga = new NSGAII<NSGAChromosome>(null);
		BinaryTournamentSelectionCrowdedComparison ts = new BinaryTournamentSelectionCrowdedComparison(false);
		ts.setMaximize(false);
		ga.setSelectionFunction(ts);

		NSGAChromosome c1 = new NSGAChromosome();
		NSGAChromosome c2 = new NSGAChromosome();

		// Set Rank
		c1.setRank(0);
		c2.setRank(0);

		// Set Distance
		c1.setDistance(0.1);
		c2.setDistance(0.5);

		List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
		population.add(c1);
		population.add(c2);

		Assert.assertTrue(ts.getIndex(population) == 1);
	}

	@Test
	public void testCrowdingDistanceMaximize()
	{
		NSGAII<NSGAChromosome> ga = new NSGAII<NSGAChromosome>(null);
		BinaryTournamentSelectionCrowdedComparison ts = new BinaryTournamentSelectionCrowdedComparison(true);
		ts.setMaximize(true);
		ga.setSelectionFunction(ts);

		NSGAChromosome c1 = new NSGAChromosome();
		NSGAChromosome c2 = new NSGAChromosome();

		// Set Rank
		c1.setRank(0);
		c2.setRank(0);

		// Set Distance
		c1.setDistance(0.1);
		c2.setDistance(0.5);

		List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
		population.add(c1);
		population.add(c2);

		Assert.assertTrue(ts.getIndex(population) == 1);
	}
}
