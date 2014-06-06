package com.examples.with.different.packagename.stable;

import java.util.UUID;

public class RandomUUIDUser {

	private final UUID uuid;

	public RandomUUIDUser() {
		uuid = UUID.randomUUID();
	}
	
	public String toString() {
		return uuid.toString();
	}
	
	public long getLSB() {
		return uuid.getLeastSignificantBits();
	}

	public long getMSB() {
		return uuid.getLeastSignificantBits();
	}
	
	public int clockSequence() {
		return uuid.clockSequence();
	}
	
}
