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
package org.evosuite.instrumentation.error;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.evosuite.instrumentation.error.ErrorConditionChecker;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.Opcodes;

@RunWith(Parameterized.class)
public class TestFloatOverflow {

	private float x;
	private float y;
	
	// Creates the test data
	@Parameters
	public static Collection<Object[]> data() {
		Object[] values = new Object[] { (- Float.MAX_VALUE), (- Float.MAX_VALUE)/2, 0, Float.MAX_VALUE/ 2, Float.MAX_VALUE};
		List<Object[]> valuePairs = new ArrayList<Object[]>();
		for(Object val1 : values) {
			for(Object val2 : values) {
				valuePairs.add(new Object[] {val1, val2});
			}
		}
		return valuePairs;
	}
	
	public TestFloatOverflow(float x, float y) {
		this.x = x;
		this.y = y;
	}


	private void assertOverflow(double doubleResult, int distance, float floatResult) {
		if(doubleResult > Float.MAX_VALUE) {
			assertTrue("Expected negative value for "+x+" and "+y+": "+distance, distance <= 0);
			assertEquals("Expected result to be infinity for "+x+" and "+y+": "+floatResult, Float.POSITIVE_INFINITY, floatResult, 0.0F);

		} else {
			assertTrue("Expected positive value for "+x+" and "+y+": "+distance, distance > 0);
		}		
	}

	
	@Test
	public void testAddOverflow() {
		int result = ErrorConditionChecker.overflowDistance(x, y, Opcodes.FADD);
		assertOverflow((double)x + (double)y, result, x + y);
	}

	@Test
	public void testSubOverflow() {
		int result = ErrorConditionChecker.overflowDistance(x, y, Opcodes.FSUB);
		assertOverflow((double)x - (double)y, result, x - y);
	}

	@Test
	public void testMulOverflow() {
		int result = ErrorConditionChecker.overflowDistance(x, y, Opcodes.FMUL);
		assertOverflow((double)x * (double)y, result, x * y);
	}

	@Test
	public void testDivOverflow() {
		Assume.assumeTrue(y != 0F);
		int result = ErrorConditionChecker.overflowDistance(x, y, Opcodes.FDIV);
		assertOverflow((double)x / (double)y, result, x / y);
	}
}
