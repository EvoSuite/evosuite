package org.evosuite.runtime.mock.java.util;

import java.util.UUID;

import org.evosuite.runtime.mock.StaticReplacementMock;

public class MockUUID implements StaticReplacementMock {

	@Override
	public String getMockedClassName() {
		return UUID.class.getName();
	}

	public static UUID randomUUID() {
		return org.evosuite.runtime.Random.randomUUID();
	}
	
	public static UUID fromString(String s) {
		return org.evosuite.runtime.Random.randomUUID();
	}

}
