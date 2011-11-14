package de.unisb.cs.st.evosuite.junit;

import org.junit.Test;

import de.unisb.cs.st.evosuite.junit.TestExample.MockingBird;

public class SimpleTestExample04 {

	@Test
	public void test() {
		MockingBird bird = new MockingBird(new String("killSelf"));
		bird.doIt(new String("You")).doIt("Me").doIt("Them").doIt("Everybody!");
	}
}
