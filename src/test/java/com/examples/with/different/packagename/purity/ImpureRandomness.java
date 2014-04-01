package com.examples.with.different.packagename.purity;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

public class ImpureRandomness {

	private final Random random;
	private final SecureRandom secureRandom;

	public ImpureRandomness() {
		random = new Random();
		secureRandom = new SecureRandom();
	}
	
	public int randomNextInt() {
		return random.nextInt();
	}
	
	public int secureRandomNextInt() {
		return secureRandom.nextInt();
	}
	
	public String randomUUIDToString() {
		UUID randomUUID = UUID.randomUUID();
		return randomUUID.toString();
	}
	
	public double randomMath() {
		return Math.random();
	}
}
