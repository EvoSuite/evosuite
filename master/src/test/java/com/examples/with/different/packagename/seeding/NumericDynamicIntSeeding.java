package com.examples.with.different.packagename.seeding;

/**
 * @author jmr
 */

public class NumericDynamicIntSeeding {

	private static int CONSTANT = 1500;
	public static int check(int a) {
		if(a == Integer.MAX_VALUE - CONSTANT)
		{
			return 1;
		} else
			return 2;		
	}
}
