/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package com.examples.with.different.packagename.testreader;

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

        public boolean executeCmd(int x) {
            if (song.equals("killSelf") && (x > 7)) {
                System.out.println("It's a sin to kill a mockingbird.");
                return true;
            }
            return false;
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

    public static BufferedImage createImage(final int width, final int height, final int color) {
        if ((width < 1) || (height < 1)) {
            throw new IllegalArgumentException("ERROR: bad width/height!");
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return setImageColor(image, color);
    }

    public static int createRGBInt(final int valR, final int valG, final int valB) {
        return (valR << 16) | (valG << 8) | (valB);
    }

    @BeforeClass
    public static void initializeAgain() {
        value = 42;
    }

    @BeforeClass
    public static void initializeOtherValue() {
        otherValue = -5;
    }

    public static BufferedImage setImageColor(final BufferedImage image, final int color) {
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

    public static void sysoutArray(String... args) {
        String result = "";
        for (String arg : args) {
            result += arg;
        }
        System.out.println(result);
    }

    protected static int doCalc(int x, int y) {
        return x + 5;
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
