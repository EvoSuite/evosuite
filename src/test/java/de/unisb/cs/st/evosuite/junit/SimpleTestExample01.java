package de.unisb.cs.st.evosuite.junit;

import org.junit.Test;

import de.unisb.cs.st.evosuite.junit.TestExample.MockingBird;

public class SimpleTestExample01 {

	@Test
	public void test() {
		MockingBird bird = new MockingBird("killSelf");
		bird.executeCmd(10);
	}
}
