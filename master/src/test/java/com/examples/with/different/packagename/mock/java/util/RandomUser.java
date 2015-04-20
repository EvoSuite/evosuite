package com.examples.with.different.packagename.mock.java.util;

import java.util.Random;

public class RandomUser {

	private final byte[] bytes0;
	private final double double0;
	private final boolean boolean0;
	public RandomUser() {
		Random random = new Random();
		double0 = random.nextGaussian();
		boolean0 = random.nextBoolean();
		bytes0 = new byte[5];
		random.nextBytes(new byte[5]);
	}

	public byte[] getBytes0() {
		return bytes0;
	}

	public double getDouble0() {
		return double0;
	}

	public boolean getBoolean0() {
		return boolean0;
	}

}
