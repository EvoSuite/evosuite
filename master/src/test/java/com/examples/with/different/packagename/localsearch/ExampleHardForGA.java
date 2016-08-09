package com.examples.with.different.packagename.localsearch;

public class ExampleHardForGA {

	/**
	 * Solution a==600, b==54000
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean coverMe(int a, int b) {
		if (a<0 || b<0) {
			return false;
		}
		if (a==0 || b==0) {
			return false;
		}
		final int c1 = 2 * 3 * 5;
		final int c2 = 4 * 27 * 25;
		if (a==c1 || a==c2) {
			return false;
		}
		if (b==c1 || b==c2) {
			return false;
		}
		
		if ((a % c1) != 0)
			return false;
		if ((b % c2) != 0)
			return false;
		final int c3 = a / c1;
		final int c4 = b / c2;
		if (c3 == c4)
			return true;

		return false;
	}

//	public static void main(String[] args) {
//		IntegerDSE dse = new IntegerDSE();
//		dse.coverMe(600, 54000);
//	}
}
