package com.examples.with.different.packagename.solver;

public class TestCaseFloatTrigonometry {

	public static int test(double x) {
		int c = 0;

		double acos = Math.acos(x);
		double asin = Math.asin(x);
		double atan = Math.atan(x);
		double cos = Math.cos(x);
		double cosh = Math.cosh(x);
		double tan = Math.tan(x);
		if (acos > 0) {
			c++;
		}
		if (asin > 0) {
			c++;
		}
		if (atan > 0) {
			c++;
		}
		if (cos > 0) {
			c++;
		}
		if (cosh > 0) {
			c++;
		}
		if (tan > 0) {
			c++;
		}
		return c;
	}
}
