package com.examples.with.different.packagename.stable;

import java.security.SecureRandom;

public class SecureRandomUser {

	private final SecureRandom secureRandom;

	public SecureRandomUser() {
		secureRandom = new java.security.SecureRandom();
	}

	public String toString() {
		return secureRandom.toString();
	}

	public int nextInt() {
		return secureRandom.nextInt();
	}

	public long nextLong() {
		return secureRandom.nextLong();
	}

	public int nextInt(int n) {
		return secureRandom.nextInt(n);
	}

	public boolean nextBoolean() {
		return secureRandom.nextBoolean();
	}

	public double nextDouble() {
		return secureRandom.nextDouble();
	}

	public void nextBytes(byte[] bytes) {
		secureRandom.nextBytes(bytes);
	}

	public float nextFloat() {
		return secureRandom.nextFloat();
	}

	public double nextGaussian() {
		return secureRandom.nextGaussian();
	}

}
