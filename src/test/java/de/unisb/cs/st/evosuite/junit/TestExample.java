package de.unisb.cs.st.evosuite.junit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestExample extends ParentTestExample {
	public static class MockingBird {

		public static MockingBird create(String song) {
			return new MockingBird(song);
		}

		private final String song;

		public MockingBird(String song) {
			this.song = song;
		}

		public void executeCmd(int x) {
			if (song.equals("killSelf") && (x > 7)) {
				throw new RuntimeException("It's a sin to kill a mockingbird.");
			}
		}
	}

	protected static Integer otherValue = 10;

	@BeforeClass
	public static void initializeOtherValue() {
		otherValue = -5;
	}

	@Before
	public void changeNeeded() {
		needed = "killSelf";
	}

	@Test
	public void test() {
		MockingBird bird = new MockingBird(needed);
		bird.executeCmd(value - otherValue);
	}
}
