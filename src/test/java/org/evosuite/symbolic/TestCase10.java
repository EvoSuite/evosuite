package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase10 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MathDouble mathDouble0 = new MathDouble();
		double double0 = ConcolicMarker.mark(-2020.5367255717083,"var1");
		double double1 = ConcolicMarker.mark(698.931685369782,"var2");
		double double2 = (double)mathDouble0.castToLong(double0);
		mathDouble0.unreach();
		double double3 = ConcolicMarker.mark(1.8078644807328579,"var3");
		int int0 = mathDouble0.castToInt(double3);
		char char0 = mathDouble0.castToChar(double3);
		double double4 = ConcolicMarker.mark(1756.567093813958,"var4");
		long long0 = mathDouble0.castToLong(double4);
		double double5 = mathDouble0.substract((double) int0, (double) int0);
		if (double5==double4) {
			mathDouble0.castToFloat(double5);
		}
		
	}

}
