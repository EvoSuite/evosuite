package com.examples.with.different.packagename.concolic;

import java.math.BigInteger;

import org.evosuite.symbolic.Assertions;

public class TestCase91 extends TestCase90 {

	/**
	 * 
	 * @param string0
	 *            .equals("135")
	 * @param string1
	 *            .equals("20")
	 * @param catchCount
	 *            ==0
	 * 
	 */
	public static void test(String string0, String string1, int catchCount) {

		try {
			new BigInteger("Togliere sta roba");
		} catch (NumberFormatException ex) {
			catchCount++;
		}

		try {
			new BigInteger((String) null);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		Assertions.checkEquals(2, catchCount);

		BigInteger bigInteger0 = new BigInteger(string0);
		BigInteger bigInteger1 = new BigInteger(string1);

		int int0 = bigInteger0.intValue();
		int int1 = bigInteger1.intValue();

		Assertions.checkEquals(135, int0);
		Assertions.checkEquals(20, int1);

		BigInteger[] bigIntegerArray0 = bigInteger0
				.divideAndRemainder(bigInteger1);

		BigInteger quotient = bigIntegerArray0[0];
		BigInteger remainder = bigIntegerArray0[1];

		int quotientInteger = quotient.intValue();
		int remainderInteger = remainder.intValue();

		Assertions.checkEquals(6, quotientInteger);
		Assertions.checkEquals(15, remainderInteger);

		BigInteger min = quotient.min(remainder);
		Assertions.checkEquals(min.intValue(), quotient.intValue());

	}

}
