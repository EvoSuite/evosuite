package com.examples.with.different.packagename.purity;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

public class ImpureRandomness {

	private final Random random;
	private final SecureRandom secureRandom;
	private final UUID randomUUID;

	public ImpureRandomness() {
		random = new Random();
		secureRandom = new SecureRandom();
		randomUUID = UUID.randomUUID();
	}
	
	public int randomNextInt() {
		return random.nextInt();
	}
	
	public int secureRandomNextInt() {
		return secureRandom.nextInt();
	}
	
	public String randomUUIDToString() {
		return randomUUID.toString();
	}
	
}
