package de.unisb.cs.st.evosuite.junit;

import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.st.evosuite.junit.TestExample.MockingBird;

public class SimpleTestExample02 {

	@Ignore
	@Test
	public void test() {
		MockingBird bird = MockingBird.create("killSelf");
		bird.executeCmd(10);
	}
}
