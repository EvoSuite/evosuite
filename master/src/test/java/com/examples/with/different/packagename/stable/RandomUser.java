package com.examples.with.different.packagename.stable;

import java.util.Random;

public class RandomUser {

	private final Random random;

	public RandomUser() {
		random = new Random();
	}

	public String toString() {
		return random.toString();
	}

	public int nextInt() {
		return random.nextInt();
	}

	public long nextLong() {
		return random.nextLong();
	}

	public int nextInt(int n) {
		return random.nextInt(n);
	}

	public boolean nextBoolean() {
		return random.nextBoolean();
	}

	public double nextDouble() {
		return random.nextDouble();
	}

	public void nextBytes(byte[] bytes) {
		random.nextBytes(bytes);
	}

	public float nextFloat() {
		return random.nextFloat();
	}

	public double nextGaussian() {
		return random.nextGaussian();
	}

}
