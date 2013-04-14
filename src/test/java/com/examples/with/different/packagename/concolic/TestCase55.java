package com.examples.with.different.packagename.concolic;

public class TestCase55 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int int0 = 1515;
		int int1 = 45451;
		if (int0 == int1) {
			return;
		}
		int int2 = 1541;
		if (int2 == int0) {
			return;
		}

	}

}
