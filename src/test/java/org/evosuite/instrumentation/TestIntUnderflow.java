package org.evosuite.instrumentation;

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
public class TestIntUnderflow {

	private int x;
	private int y;
	
	// Creates the test data
	@Parameters
	public static Collection<Object[]> data() {
		Object[] values = new Object[] { Integer.MIN_VALUE, Integer.MIN_VALUE/2, 0, Integer.MAX_VALUE/ 2, Integer.MAX_VALUE};
		List<Object[]> valuePairs = new ArrayList<Object[]>();
		for(Object val1 : values) {
			for(Object val2 : values) {
				valuePairs.add(new Object[] {val1, val2});
			}
		}
		return valuePairs;
	}
	
	public TestIntUnderflow(int x, int y) {
		this.x = x;
		this.y = y;
	}

	private void assertUnderflow(long longResult, int intResult) {
		if(longResult < Integer.MIN_VALUE) {
			assertTrue("Expected negative value for "+x+" and "+y+": "+intResult, intResult < 0);
		} else {
			assertTrue("Expected positive value for "+x+" and "+y+": "+intResult, intResult >= 0);
		}		
	}

	
	@Test
	public void testAddUnderflow() {
		int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.IADD);
		assertUnderflow((long)x + (long)y, result);
	}

	@Test
	public void testSubUnderflow() {
		int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.ISUB);
		assertUnderflow((long)x - (long)y, result);
	}

	@Test
	public void testMulUnderflow() {
		int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.IMUL);
		assertUnderflow((long)x * (long)y, result);
	}

	@Test
	public void testDivUnderflow() {
		Assume.assumeTrue(y != 0);
		int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.IDIV);
		assertUnderflow((long)x / (long)y, result);
	}
}
