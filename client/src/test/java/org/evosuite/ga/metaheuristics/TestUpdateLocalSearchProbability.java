package org.evosuite.ga.metaheuristics;

import static org.junit.Assert.*;

import org.evosuite.Properties;
import org.junit.Test;

public class TestUpdateLocalSearchProbability {

	private static final double DELTA = 0.0000000001;

	@Test
	public void testNoChanges() {
		Properties.LOCAL_SEARCH_PROBABILITY = 0.5;
		Properties.LOCAL_SEARCH_ADAPTATION_RATE = 2.0;

		MonotonicGA<?> ga = new MonotonicGA<>(null);
		assertEquals(0.5, ga.localSearchProbability, DELTA);
	}

	@Test
	public void testDecrease() {
		Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
		Properties.LOCAL_SEARCH_ADAPTATION_RATE = 2.0;

		MonotonicGA<?> ga = new MonotonicGA<>(null);
		assertEquals(1.0, ga.localSearchProbability, DELTA);

		ga.updateProbability(false);
		assertEquals(0.5, ga.localSearchProbability, DELTA);

		ga.updateProbability(false);
		assertEquals(0.25, ga.localSearchProbability, DELTA);

		ga.updateProbability(false);
		assertEquals(0.125, ga.localSearchProbability, DELTA);

	}

	@Test
	public void testIncrease() {
		Properties.LOCAL_SEARCH_PROBABILITY = 0.125;
		Properties.LOCAL_SEARCH_ADAPTATION_RATE = 4.0;

		MonotonicGA<?> ga = new MonotonicGA<>(null);
		assertEquals(0.125, ga.localSearchProbability, DELTA);

		ga.updateProbability(true);
		assertEquals(0.5, ga.localSearchProbability, DELTA);

		ga.updateProbability(true);
		assertEquals(1.0, ga.localSearchProbability, DELTA);

	}
	
	@Test
	public void testIncreaseAndDecrease() {
		Properties.LOCAL_SEARCH_PROBABILITY = 0.5;
		Properties.LOCAL_SEARCH_ADAPTATION_RATE = 2.0;

		MonotonicGA<?> ga = new MonotonicGA<>(null);
		assertEquals(0.5, ga.localSearchProbability, DELTA);

		ga.updateProbability(true);
		assertEquals(1.0, ga.localSearchProbability, DELTA);

		ga.updateProbability(true);
		assertEquals(1.0, ga.localSearchProbability, DELTA);

		ga.updateProbability(true);
		assertEquals(1.0, ga.localSearchProbability, DELTA);

		ga.updateProbability(false);
		assertEquals(0.5, ga.localSearchProbability, DELTA);

		ga.updateProbability(false);
		assertEquals(0.25, ga.localSearchProbability, DELTA);

		ga.updateProbability(false);
		assertEquals(0.125, ga.localSearchProbability, DELTA);

		ga.updateProbability(false);
		assertEquals(0.0625, ga.localSearchProbability, DELTA);

		ga.updateProbability(false);
		assertEquals(0.03125, ga.localSearchProbability, DELTA);

	}

}
