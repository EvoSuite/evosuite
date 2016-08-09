package com.examples.with.different.packagename.ncs;

public class Loops {

	public static int coverMe(int x) {
		if (x < 0) {
			return -1;
		}
		for (int i = 0; i < x; i++) {
			if (i == 10) {
				/*
				 * This branch demonstrates a limitation of our search strategy.
				 * Since some branches might be dependent on some previous
				 * branches, we might not be able to cover them.
				 */
				return 1;
			}
		}
		return 2;
	}
}
