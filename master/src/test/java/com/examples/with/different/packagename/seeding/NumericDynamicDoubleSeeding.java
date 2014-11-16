package com.examples.with.different.packagename.seeding;

/**
 * @author jmr
 */

public class NumericDynamicDoubleSeeding {
	private static double CONSTANT = 1500d;
	public static double check(double a) {
		
		if(a == Integer.MAX_VALUE * CONSTANT)
		{
			return 1;
		} else
			return 2;		
	}
}
