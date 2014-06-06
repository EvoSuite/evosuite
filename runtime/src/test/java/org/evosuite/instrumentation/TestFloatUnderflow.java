package org.evosuite.instrumentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.Opcodes;

@RunWith(Parameterized.class)
public class TestFloatUnderflow {

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
	
	public TestFloatUnderflow(float x, float y) {
		this.x = x;
		this.y = y;
	}


	private void assertUnderflow(double doubleResult, int distance, float floatResult) {
		if(doubleResult < -Float.MAX_VALUE) {
			assertTrue("Expected negative value for "+x+" and "+y+": "+distance, distance < 0);
			assertEquals(Float.NEGATIVE_INFINITY, floatResult, 0.0F);
		} else {
			assertTrue("Expected positive value for "+x+" and "+y+": "+distance, distance >= 0);
		}		
	}

	
	@Test
	public void testAddUnderflow() {
		int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.FADD);
		assertUnderflow((double)x + (double)y, result, x + y);
	}

	@Test
	public void testSubUnderflow() {
		int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.FSUB);
		assertUnderflow((double)x - (double)y, result, x - y);
	}

	@Test
	public void testMulUnderflow() {
		int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.FMUL);
		assertUnderflow((double)x * (double)y, result, x * y);
	}

	@Test
	public void testDivUnderflow() {
		Assume.assumeTrue(y != 0F);

		int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.FDIV);
		assertUnderflow((double)x / (double)y, result, x / y);
	}
}
