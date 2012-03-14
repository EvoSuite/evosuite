package de.unisb.cs.st.evosuite.junit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.st.evosuite.junit.TestExample.MockingBird;

public class ParentTestExample {
	protected static Integer value = 0;

	static {
		value = 5;
	}

	@BeforeClass
	public static void initializeOtherValue() {
		value = Integer.MAX_VALUE;
	}
	
	@BeforeClass
	public static void someInitialization() {
		value = 7;
	}
	
	protected String needed = null;
	
	public ParentTestExample(){
		needed = "break free!";
	}

	@Before
	public void otherSetup() {
		value = 3;
	}

	@Before
	public void setupNeeded() {
		needed = "escape";
	}

	@Ignore
	@Test
	public void test01() {
		MockingBird bird = MockingBird.create(needed);
		bird.executeCmd(value);
	}
}
