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
/**
 * 
 */
package org.evosuite.instrumentation.testability;

import static org.junit.Assert.assertTrue;

import org.evosuite.instrumentation.testability.BooleanHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * @author fraser
 * 
 */
public class BooleanHelperTest {

	@Before
	public void setUp() {
		BooleanHelper.clearStack();
	}

	@Test
	public void test1() {
		int distance = BooleanHelper.getDistance(-1, 1, 0);
		assertTrue(distance <= 0);

		distance = BooleanHelper.getDistance(-1, 1, 1);
		assertTrue(distance > 0);
	}

	@Test
	public void test2() {
		int distance = BooleanHelper.getDistance(1, 1, 0);
		assertTrue(distance <= 0);

		distance = BooleanHelper.getDistance(1, 1, 1);
		assertTrue(distance > 0);

		BooleanHelper.pushPredicate(1, 1);

		distance = BooleanHelper.getDistance(1, 1, 0);
		assertTrue(distance <= 0);

		distance = BooleanHelper.getDistance(1, 1, 1);
		assertTrue(distance > 0);
	}

	@Test
	public void test3() {
		int distance = BooleanHelper.getDistance(1, 1, 0);
		assertTrue(distance <= 0);

		distance = BooleanHelper.getDistance(1, 1, 1);
		assertTrue(distance > 0);

		BooleanHelper.pushPredicate(-1, 1);

		distance = BooleanHelper.getDistance(1, 1, 0);
		assertTrue(distance <= 0);

		distance = BooleanHelper.getDistance(1, 1, 1);
		assertTrue(distance > 0);
	}

	@Test
	public void test4() {
		int distance = BooleanHelper.getDistance(1, 1, 0);
		assertTrue(distance <= 0);

		distance = BooleanHelper.getDistance(1, 1, 1);
		assertTrue(distance > 0);

		BooleanHelper.pushPredicate(-1, 1);
		BooleanHelper.pushPredicate(1, 1);

		distance = BooleanHelper.getDistance(1, 1, 0);
		assertTrue(distance <= 0);

		distance = BooleanHelper.getDistance(1, 1, 1);
		assertTrue(distance > 0);
	}

	@Test
	public void test5() {
		int distanceFalse1 = BooleanHelper.getDistance(1, 1, 0);
		int distanceTrue1 = BooleanHelper.getDistance(1, 1, 1);

		BooleanHelper.pushPredicate(-1, 1);

		int distanceFalse2 = BooleanHelper.getDistance(1, 1, 0);
		int distanceTrue2 = BooleanHelper.getDistance(1, 1, 1);

		assertTrue(distanceFalse1 < distanceFalse2);
		assertTrue(distanceTrue2 < distanceTrue1);

		BooleanHelper.pushPredicate(1, 1);

		int distanceFalse3 = BooleanHelper.getDistance(1, 1, 0);
		int distanceTrue3 = BooleanHelper.getDistance(1, 1, 1);

		assertTrue("Distances: " + distanceFalse2 + "/" + distanceFalse3,
		           distanceFalse2 == distanceFalse3);
		assertTrue("Distances: " + distanceTrue2 + "/" + distanceTrue3,
		           distanceTrue2 == distanceTrue3);

		BooleanHelper.pushPredicate(-100, 1);

		int distanceFalse4 = BooleanHelper.getDistance(1, 1, 0);
		int distanceTrue4 = BooleanHelper.getDistance(1, 1, 1);

		assertTrue(distanceFalse4 < distanceFalse3);
		assertTrue(distanceTrue4 > distanceTrue3);

		BooleanHelper.pushPredicate(100, 1);

		int distanceFalse5 = BooleanHelper.getDistance(1, 1, 0);
		int distanceTrue5 = BooleanHelper.getDistance(1, 1, 1);

		assertTrue(distanceFalse5 == distanceFalse4);
		assertTrue(distanceTrue5 == distanceTrue4);
	}

	@Test
	public void test6() {
		BooleanHelper.pushPredicate(-1, 1);

		int distanceFalse2 = BooleanHelper.getDistance(1, 1, 0);
		int distanceTrue2 = BooleanHelper.getDistance(1, 1, 1);

		int distanceFalse3 = BooleanHelper.getDistance(1, 1, 0);
		int distanceTrue3 = BooleanHelper.getDistance(1, 1, 1);

		assertTrue("Distances: " + distanceFalse2 + "/" + distanceFalse3,
		           distanceFalse2 == distanceFalse3);
		assertTrue("Distances: " + distanceTrue2 + "/" + distanceTrue3,
		           distanceTrue2 == distanceTrue3);

		BooleanHelper.pushPredicate(1, 1);

		int distanceFalse4 = BooleanHelper.getDistance(1, 1, 0);
		int distanceTrue4 = BooleanHelper.getDistance(1, 1, 1);

		assertTrue("Distances: " + distanceFalse4 + "/" + distanceFalse3,
		           distanceFalse4 == distanceFalse3);
		assertTrue("Distances: " + distanceTrue4 + "/" + distanceTrue3,
		           distanceTrue4 == distanceTrue3);
	}

	@Test
	public void test7() {
		BooleanHelper.pushPredicate(1, 1);
		int lastDistance = BooleanHelper.getDistance(1, 1, 1);
		assertTrue(lastDistance > 0);

		for (int i = 2; i < 10; i++) {
			BooleanHelper.pushPredicate(i, 1);
			int distance = BooleanHelper.getDistance(1, 1, 1);
			assertTrue("Iteration " + i + ": Expecting " + distance + " > "
			        + lastDistance, distance > lastDistance);
			lastDistance = distance;
		}
	}

	@Test
	public void test8() {
		BooleanHelper.pushPredicate(1, 1);
		int lastDistance = BooleanHelper.getDistance(1, 1, 1);
		assertTrue(lastDistance > 0);

		for (int i = 10; i < 100000; i += 100) {
			BooleanHelper.pushPredicate(i, 1);
			int distance = BooleanHelper.getDistance(1, 1, 1);
			assertTrue("Iteration " + i + ": Expecting " + distance + " > "
			        + lastDistance, distance > lastDistance);
			lastDistance = distance;
		}
	}

	@Test
	public void test9() {
		BooleanHelper.pushPredicate(1, 1);

		int distanceFalse1 = BooleanHelper.getDistance(1, 1, 0);
		int distanceTrue1 = BooleanHelper.getDistance(1, 1, 1);

		int distanceFalse2 = BooleanHelper.getDistance(1, 2, 0);
		int distanceTrue2 = BooleanHelper.getDistance(1, 2, 1);

		assertTrue("Distances: " + distanceFalse1 + "/" + distanceFalse2,
		           distanceFalse1 < distanceFalse2);
		assertTrue("Distances: " + distanceTrue1 + "/" + distanceTrue2,
		           distanceTrue1 > distanceTrue2);
	}

	@Test
	public void test10() {
		BooleanHelper.pushPredicate(34227, 1);
		int distanceFalse1 = BooleanHelper.getDistance(1, 1, 0);
		int distanceTrue1 = BooleanHelper.getDistance(1, 1, 1);

		BooleanHelper.pushPredicate(35608, 1);

		int distanceFalse2 = BooleanHelper.getDistance(1, 1, 0);
		int distanceTrue2 = BooleanHelper.getDistance(1, 1, 1);

		assertTrue("Distances: " + distanceFalse1 + "/" + distanceFalse2,
		           distanceFalse1 > distanceFalse2);
		assertTrue("Distances: " + distanceTrue1 + "/" + distanceTrue2,
		           distanceTrue1 < distanceTrue2);
	}

}
