package com.examples.with.different.packagename.seeding;

/**
 * @author jmr
 */

public class NumericDynamicFloatSeeding {
	private static float CONSTANT = 1500f;
	public static float check(float a) {
		
		if(a == Integer.MAX_VALUE * CONSTANT)
		{
			return 1;
		} else
			return 2;		
	}
}
