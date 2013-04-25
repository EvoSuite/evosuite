package com.examples.with.different.packagename.testreader;

import org.junit.BeforeClass;
import org.junit.Test;

public class InheritanceExample extends TestExample {

	protected static Integer otherValue = 11;

	static {
		initializeAgain();
	}

	@BeforeClass
	public static void meanWhile() {
		otherValue = doCalc(value, otherValue);
	}

	public InheritanceExample() {
		doOtherCalc(value);
	}

	@Test
	public void testInheritance() {
		super.setupNeeded();
		MockingBird bird = new MockingBird(needed + "me");
		bird.executeCmd(value - otherValue);
	}
}
