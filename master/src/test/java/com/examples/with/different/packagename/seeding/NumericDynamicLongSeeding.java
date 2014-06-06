package com.examples.with.different.packagename.seeding;

/**
 * @author jmr
 */

public class NumericDynamicLongSeeding {
	
	private static long CONSTANT = 15000;
	public static long check(long a) {
		
		if(a == Integer.MAX_VALUE * -CONSTANT)
		{
			return 1;
		} else
			return 2;		
	}
}
