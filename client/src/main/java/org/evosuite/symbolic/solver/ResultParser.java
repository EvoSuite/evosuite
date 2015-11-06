package org.evosuite.symbolic.solver;

import java.math.BigDecimal;

public abstract class ResultParser {

	private static final int BIG_DECIMAL_SCALE = 100;
	protected static Double parseRational(boolean sign, String numeratorStr, String denominatorStr) {
		double value;
		try {
			double numerator = Double.parseDouble(numeratorStr);
			double denominator = Double.parseDouble(denominatorStr);
			value = (numerator / denominator);
		} catch (NumberFormatException ex) {
			// Perhaps the numerator or denominator are just bigger than
			// Long.MAX_VALUE
			BigDecimal bigNumerator = new BigDecimal(numeratorStr);
			BigDecimal bigDenominator = new BigDecimal(denominatorStr);
			BigDecimal rational = bigNumerator.divide(bigDenominator, BIG_DECIMAL_SCALE, BigDecimal.ROUND_UP);
			value = rational.doubleValue();
		}
		if (sign == true) {
			return -value;
		} else {
			return value;
		}
	}

}
