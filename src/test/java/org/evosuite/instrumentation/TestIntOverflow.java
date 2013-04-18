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
public class TestIntOverflow {

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
	
	public TestIntOverflow(int x, int y) {
		this.x = x;
		this.y = y;
	}


	private void assertOverflow(long longResult, int intResult) {
		if(longResult > Integer.MAX_VALUE) {
			assertTrue("Expected negative value for "+x+" and "+y+": "+intResult, intResult <= 0);
		} else {
			assertTrue("Expected positive value for "+x+" and "+y+": "+intResult, intResult > 0);
		}		
	}

	
	@Test
	public void testAddOverflow() {
		int result = ErrorConditionChecker.overflowDistance(x, y, Opcodes.IADD);
		assertOverflow((long)x + (long)y, result);
	}

	@Test
	public void testSubOverflow() {
		int result = ErrorConditionChecker.overflowDistance(x, y, Opcodes.ISUB);
		assertOverflow((long)x - (long)y, result);
	}

	@Test
	public void testMulOverflow() {
		int result = ErrorConditionChecker.overflowDistance(x, y, Opcodes.IMUL);
		assertOverflow((long)x * (long)y, result);
	}

	@Test
	public void testDivOverflow() {
		Assume.assumeTrue(y != 0);

		int result = ErrorConditionChecker.overflowDistance(x, y, Opcodes.IDIV);
		assertOverflow((long)x / (long)y, result);
	}
}
