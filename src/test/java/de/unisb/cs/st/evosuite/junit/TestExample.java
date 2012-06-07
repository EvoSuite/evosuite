package de.unisb.cs.st.evosuite.junit;

import java.awt.image.BufferedImage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

		public MockingBird doIt(String song) {
			return this;
		}

		public void executeCmd(int x) {
			if (song.equals("killSelf") && (x > 7)) {
				throw new RuntimeException("It's a sin to kill a mockingbird.");
			}
		}

		public void thisIsIt(String... args) {
			if (args.length > 0) {
				System.out.println(args);
			}
		}
	}

	protected static Integer otherValue = 10;

	static {
		value = 4;
	}

	@BeforeClass
	public static void initializeAgain() {
		value = 42;
	}

	@BeforeClass
	public static void initializeOtherValue() {
		otherValue = -5;
	}

	protected static int doCalc(int x, int y) {
		return x + 5;
	}

	private static BufferedImage createImage(final int width, final int height, final int color) {
		if ((width < 1) || (height < 1)) {
			throw new IllegalArgumentException("ERROR: bad width/height!");
		}
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		return setImageColor(image, color);
	}

	private static int createRGBInt(final int valR, final int valG, final int valB) {
		return (valR << 16) | (valG << 8) | (valB);
	}

	private static BufferedImage setImageColor(final BufferedImage image, final int color) {
		if (image == null) {
			throw new IllegalArgumentException("ERROR: image == null!");
		}
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				image.setRGB(x, y, color);
			}
		}
		return image;
	}

	public TestExample() {
		otherValue = 38;
	}

	@Before
	public void goForIt() {
		needed = "convert";
	}

	@Override
	@Before
	public void setupNeeded() {
		needed = "killSelf";
	}

	@Override
	// @Ignore
	@Test
	public void test01() {
		MockingBird bird = new MockingBird(needed);
		bird.executeCmd(otherValue);
	}

	@Ignore
	@Test
	public void testArrayFor() {
		BufferedImage image = createImage(3, 3, 0);

		final int[][] colors = new int[image.getWidth()][image.getHeight()];
		colors[0][0] = createRGBInt(0, 255, 255);
		colors[1][0] = createRGBInt(13, 100, 0);
		colors[2][0] = createRGBInt(13, 100, 0);
		colors[0][1] = createRGBInt(255, 100, 65);
		colors[1][1] = createRGBInt(0, 65, 55);
		colors[2][1] = createRGBInt(100, 180, 10);
		colors[0][2] = createRGBInt(255, 7, 44);
		colors[1][2] = createRGBInt(255, 14, 99);
		colors[2][2] = createRGBInt(255, 255, 3);

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				image.setRGB(x, y, colors[x][y]);
			}
		}
	}

	protected int doOtherCalc(int x) {
		// return doCalc(x, 5);
		return 6;
	}
}
