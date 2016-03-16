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
package com.examples.with.different.packagename;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.examples.with.different.packagename.Calculator;

public class CalculatorTest {

	@Test
	public void testMul() {
		assertTrue(Calculator.mul(1, 2) == 2);
	}

	@Test
	public void testDiv() {
		assertTrue(Calculator.div(4, 2) == 2);
	}

	@Test
	public void testAdd() {
		assertTrue(Calculator.add(1, 2) == 3);
	}

	@Test
	public void testSub() {
		assertTrue(Calculator.sub(2, 1) == 1);
	}
}
