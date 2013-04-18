package org.evosuite.instrumentation;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class TestLongUnderflow {

	private long x;
	private long y;
	
	// Creates the test data
	@Parameters
	public static Collection<Object[]> data() {
		Object[] values = new Object[] { Long.MIN_VALUE, Long.MIN_VALUE/2, 0, Long.MAX_VALUE/ 2, Long.MAX_VALUE};
		List<Object[]> valuePairs = new ArrayList<Object[]>();
		for(Object val1 : values) {
			for(Object val2 : values) {
				valuePairs.add(new Object[] {val1, val2});
			}
		}
		return valuePairs;
	}
	
	public TestLongUnderflow(long x, long y) {
		this.x = x;
		this.y = y;
	}


	private void assertUnderflow(BigDecimal preciseResult, int distance, long longResult) {
		BigDecimal maxResult = new BigDecimal(Long.MIN_VALUE);
		if(preciseResult.compareTo(maxResult) < 0) {
			assertTrue("Expected negative value for "+x+" and "+y+": "+distance+" for "+longResult, distance < 0);
		} else {
			assertTrue("Expected positive value for "+x+" and "+y+": "+distance, distance >= 0);
		}		
	}

	
	@Test
	public void testAddUnderflow() {
		int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.LADD);
		assertUnderflow(new BigDecimal(x).add(new BigDecimal(y)), result, x + y);
	}

	@Test
	public void testSubUnderflow() {
		int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.LSUB);
		assertUnderflow(new BigDecimal(x).subtract(new BigDecimal(y)), result, x - y);
	}

	@Test
	public void testMulUnderflow() {
		int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.LMUL);
		assertUnderflow(new BigDecimal(x).multiply(new BigDecimal(y)), result, x * y);
	}

	@Test
	public void testDivUnderflow() {
		Assume.assumeTrue(y != 0L);

		int result = ErrorConditionChecker.underflowDistance(x, y, Opcodes.LDIV);
		assertUnderflow(new BigDecimal(x).divide(new BigDecimal(y), 10, RoundingMode.HALF_UP), result, x / y);
	}
}
