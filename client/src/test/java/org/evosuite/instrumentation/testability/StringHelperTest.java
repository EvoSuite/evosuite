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
package org.evosuite.instrumentation.testability;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class StringHelperTest {

    @Test
    public void testEquals() {
        String str1 = "test";
        String str2 = "test";
        int result = StringHelper.StringEquals(str1, str2);
        assertTrue(result > 0);
    }

    @Test
    public void testEqualsNull() {
        String str1 = "test";
        String str2 = null;
        int result = StringHelper.StringEquals(str1, str2);
        assertTrue(result < 0);
    }

    @Test(expected = NullPointerException.class)
    public void testEqualsNullCallee() {
        String str1 = null;
        String str2 = "test";
        StringHelper.StringEquals(str1, str2);
    }

    @Test
    public void testNotEquals() {
        String str1 = "test1";
        String str2 = "test2";
        int result = StringHelper.StringEquals(str1, str2);
        assertTrue(result < 0);
    }

    @Test
    public void testEqualsRelativeOrdering() {
        String str1 = "test1";
        String str2 = "test2";
        String str3 = "something else";
        int result1 = StringHelper.StringEquals(str1, str2);
        int result2 = StringHelper.StringEquals(str1, str3);
        assertTrue(result2 < result1);
    }

    @Test
    public void testEqualsIgnoreCase() {
        String str1 = "test";
        String str2 = "TEST";
        int result = StringHelper.StringEqualsIgnoreCase(str1, str2);
        assertTrue(result > 0);
    }

    @Test
    public void testEqualsIgnoreCaseNull() {
        String str1 = "test";
        String str2 = null;
        int result = StringHelper.StringEqualsIgnoreCase(str1, str2);
        assertTrue(result < 0);
    }

    @Test(expected = NullPointerException.class)
    public void testEqualsIgnoreCaseNullCallee() {
        String str1 = null;
        String str2 = "test";
        StringHelper.StringEqualsIgnoreCase(str1, str2);
    }

    @Test
    public void testNotEqualsIgnoreCase() {
        String str1 = "test1";
        String str2 = "test2";
        int result = StringHelper.StringEqualsIgnoreCase(str1, str2);
        assertTrue(result < 0);
    }

    @Test
    public void testEqualsIgnoreCaseRelativeOrdering() {
        String str1 = "test1";
        String str2 = "test2";
        String str3 = "something else";
        int result1 = StringHelper.StringEqualsIgnoreCase(str1, str2);
        int result2 = StringHelper.StringEqualsIgnoreCase(str1, str3);
        assertTrue(result2 < result1);
    }

    @Test
    public void testStartsWithAtStart() {
        String prefix = "test";
        String str = "testchen";
        int result = StringHelper.StringStartsWith(str, prefix, 0);
        assertTrue(result > 0);
    }

    @Test
    public void testStartsWith() {
        String prefix = "test";
        String str = "blahtestchen";
        int result = StringHelper.StringStartsWith(str, prefix, 4);
        assertTrue(result > 0);
    }

    @Test
    public void testNotStartsWithAtStart() {
        String prefix = "foo";
        String str = "testchen";
        int result = StringHelper.StringStartsWith(str, prefix, 0);
        assertTrue(result < 0);
    }

    @Test
    public void testNotStartsWith() {
        String prefix = "foo";
        String str = "blahtestchen";
        int result = StringHelper.StringStartsWith(str, prefix, 4);
        assertTrue(result < 0);
    }

    @Test
    public void testEndsWith() {
        String suffix = "test";
        String str = "foo bar test";
        int result = StringHelper.StringEndsWith(str, suffix);
        assertTrue(result > 0);
    }

    @Test
    public void testNotEndsWith() {
        String suffix = "foo";
        String str = "blahtestchen";
        int result = StringHelper.StringEndsWith(str, suffix);
        assertTrue(result < 0);
    }

    @Test
    public void testEmpty() {
        String str = "";
        int result = StringHelper.StringIsEmpty(str);
        assertTrue(result > 0);
    }

    @Test
    public void testNotEmpty() {
        String str = "blahtestchen";
        int result = StringHelper.StringIsEmpty(str);
        assertTrue(result < 0);
    }

    @Test
    public void testMatches() {
        String str = "hahafoo";
        String regex = ".*foo";
        int result = StringHelper.StringMatches(str, regex);
        assertTrue(result > 0);
    }

    @Test
    public void testNotMatches() {
        String str = "";
        String regex = "foo";
        int result = StringHelper.StringMatches(str, regex);
        assertTrue(result < 0);
    }

    @Test
    public void testRegionMatches() {
        String str = "hahafoobar";
        String match = "foog";
        int result = StringHelper.StringRegionMatches(str, 4, match, 0, 3);
        assertTrue(str.regionMatches(4, match, 0, 3));
        assertTrue(result > 0);
    }

    /*
     * i, true, 0, I, 0, 1
     * I, true, 0, i, 0, 1
     * ς, true, 0, σ, 0, 1
     */
    @Test
    public void testRegionMatches2() {
        checkRegionMatches("i", true, 0, "I", 0, 1);
        checkRegionMatches("I", true, 0, "i", 0, 1);
        checkRegionMatches("ς", true, 0, "σ", 0, 1);
    }

    private void checkRegionMatches(String str, boolean ignoreCase, int thisStart, String match, int otherStart, int len) {
        int result = StringHelper.StringRegionMatches(str, ignoreCase, thisStart, match, otherStart, len);
        boolean expectedResult = str.regionMatches(ignoreCase, thisStart, match, otherStart, len);
        assertEquals(expectedResult, result > 0);
    }

    @Test
    public void testNotRegionMatchesStart1() {
        String str = "hahafoobar";
        String match = "foog";
        int result = StringHelper.StringRegionMatches(str, 5, match, 0, 3);
        assertFalse(str.regionMatches(5, match, 0, 3));
        assertTrue(result < 0);
    }

    @Test
    public void testNotRegionMatchesStart2() {
        String str = "hahafoobar";
        String match = "foog";
        int result = StringHelper.StringRegionMatches(str, 4, match, 1, 3);
        assertFalse(str.regionMatches(4, match, 1, 3));
        assertTrue(result < 0);
    }

    @Test
    public void testNotRegionMatchesLength() {
        String str = "hahafoobar";
        String match = "foo";
        int result = StringHelper.StringRegionMatches(str, 5, match, 0, 4);
        assertFalse(str.regionMatches(5, match, 0, 4));
        assertTrue(result < 0);
    }

    @Test
    public void testNotRegionMatchesString() {
        String str = "hahafoobar";
        String match = "bar";
        int result = StringHelper.StringRegionMatches(str, 5, match, 0, 3);
        assertFalse(str.regionMatches(5, match, 0, 3));
        assertTrue(result < 0);
    }

    @Test
    public void testStringEquals() {
        int dist = StringHelper.StringEquals("foo", "foo");
        Assert.assertTrue("Distance should be positive, but obtained " + dist, dist > 0);

        int nullDist = StringHelper.StringEquals("foo", null);
        Assert.assertTrue("Distance should be negative, but obtained " + nullDist,
                nullDist <= 0);
        int emptyDist = StringHelper.StringEquals("foo", "");
        Assert.assertTrue("Distance should be negative, but obtained " + emptyDist,
                emptyDist <= 0);
        Assert.assertTrue("Empty string should be closer than null", nullDist < emptyDist);

        try {
            dist = StringHelper.StringEquals(null, "foo");
            Assert.fail();
        } catch (NullPointerException e) {
        }

        int closer = StringHelper.StringEquals("foo1", "foo2");
        int larger = StringHelper.StringEquals("xyz", "foo");
        Assert.assertTrue("Invalid distances " + closer + " and " + larger,
                closer > larger && closer <= 0);

        dist = StringHelper.StringEquals(" foo", "foo ");
        Assert.assertTrue("Distance should be negative, but obtained " + dist, dist <= 0);

        int first = StringHelper.StringEquals("foo123", "foo");
        int last = StringHelper.StringEquals("foo123", "123");
        Assert.assertTrue("Invalid distances " + first + " and " + last, first >= last
                && first <= 0);
    }

    @Test
    public void testStringEquals2() {
        double dist = StringHelper.StringEqualsCharacterDistance("foo", "foo");
        Assert.assertTrue("Distance should be positive, but obtained " + dist, dist > 0);

        double nullDist = StringHelper.StringEqualsCharacterDistance("foo", null);
        Assert.assertTrue("Distance should be negative, but obtained " + nullDist,
                nullDist <= 0);
        double emptyDist = StringHelper.StringEqualsCharacterDistance("foo", "");
        Assert.assertTrue("Distance should be negative, but obtained " + emptyDist,
                emptyDist <= 0);
        Assert.assertTrue("Empty string should be closer than null", nullDist < emptyDist);

        try {
            dist = StringHelper.StringEqualsCharacterDistance(null, "foo");
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }

        double closer = StringHelper.StringEqualsCharacterDistance("foo1", "foo2");
        double larger = StringHelper.StringEqualsCharacterDistance("xyz", "foo");
        Assert.assertTrue("Invalid distances " + closer + " and " + larger,
                closer > larger && closer <= 0);

        closer = StringHelper.StringEqualsCharacterDistance("foo", "fop");
        larger = StringHelper.StringEqualsCharacterDistance("foo", "foq");
        Assert.assertTrue(closer <= 0);
        Assert.assertTrue(larger <= 0);
        Assert.assertTrue("Invalid distances " + closer + " and " + larger,
                closer > larger);

        dist = StringHelper.StringEqualsCharacterDistance(" foo", "foo ");
        Assert.assertTrue("Distance should be negative, but obtained " + dist, dist <= 0);

        double first = StringHelper.StringEqualsCharacterDistance("foo123", "foo");
        double last = StringHelper.StringEqualsCharacterDistance("foo123", "123");
        Assert.assertTrue("Invalid distances " + first + " and " + last, first >= last
                && first <= 0);
    }

    @Test
    public void testCharacterDistanceUsed() {
        String str1 = "test1";
        String str2 = "test2";
        String str4 = "test4";
        int result1 = StringHelper.StringEquals("test3", str1);
        int result2 = StringHelper.StringEquals("test3", str2);
        int result4 = StringHelper.StringEquals("test3", str4);
        assertTrue(result1 < result2);
        assertEquals(result2, result4);
        assertTrue(result1 < result4);

    }
}
