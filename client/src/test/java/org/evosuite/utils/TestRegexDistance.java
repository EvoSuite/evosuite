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

package org.evosuite.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Gordon Fraser
 */
public class TestRegexDistance {

    @Test
    public void testLongRegex() {
        final String example = "-@0.AA";
        final String REGEX = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Assert.assertTrue(example.matches(REGEX));

        assertEquals(0.0, RegexDistanceUtils.getDistanceTailoredForStringAVM(example, REGEX), 0.0);
    }

    @Test
    public void testEmptyRegex() {
        assertEquals(0.0, RegexDistanceUtils.getDistanceTailoredForStringAVM("", ""), 0.0);
        assertEquals(1.0, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", ""), 0.0);
        assertEquals(2.0, RegexDistanceUtils.getDistanceTailoredForStringAVM("ab", ""), 0.0);
        assertEquals(3.0, RegexDistanceUtils.getDistanceTailoredForStringAVM("abc", ""), 0.0);
    }

    @Test
    public void testIdenticalChar() {
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", "a"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("aa", "aa"), 0.0);
    }

    @Test
    public void testReplaceChar() {
        assertEquals(0.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("b", "a"), 0.0);
        assertEquals(0.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("ab", "aa"), 0.0);
    }

    @Test
    public void testDeleteChar() {
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("aa", "a"), 0.0);
        assertEquals(2, RegexDistanceUtils.getDistanceTailoredForStringAVM("aaa", "a"), 0.0);
        assertEquals(3, RegexDistanceUtils.getDistanceTailoredForStringAVM("aaaa", "a"), 0.0);
        assertEquals(4, RegexDistanceUtils.getDistanceTailoredForStringAVM("aaaaa", "a"), 0.0);
    }

    @Test
    public void testInsertCharInEmptyString() {
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("", "a"), 0.0);
        assertEquals(2, RegexDistanceUtils.getDistanceTailoredForStringAVM("", "aa"), 0.0);
        assertEquals(3, RegexDistanceUtils.getDistanceTailoredForStringAVM("", "aaa"), 0.0);
    }

    @Test
    public void testInsertChar() {
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", "aa"), 0.0);
        assertEquals(2, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", "aaa"), 0.0);
        assertEquals(3, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", "aaaa"), 0.0);
    }

    @Test
    public void testTwoChar() {
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("ab", "ab"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("ab", "ba"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("ab", "bc"), 0.0);

        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("bb", "aa"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("bb", "cc"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("bb", "ac"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("bb", "ca"), 0.0);

        assertEquals(2, RegexDistanceUtils.getDistanceTailoredForStringAVM("", "ab"), 0.0);
        assertEquals(2, RegexDistanceUtils.getDistanceTailoredForStringAVM("ab", ""), 0.0);

        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", "ab"), 0.0);
        assertEquals(1.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("aaa", "ab"), 0.0);
        assertEquals(2.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("aaaa", "ab"), 0.0);

        assertEquals(1.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("bb", "bab"), 0.0);

        assertEquals(3, RegexDistanceUtils.getDistanceTailoredForStringAVM("b", "bcab"), 0.0);
        assertEquals(4, RegexDistanceUtils.getDistanceTailoredForStringAVM("b", "bcaab"), 0.0);

        assertEquals(1.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("xb", "xcb"), 0.0);
        assertEquals(1.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("b", "cb"), 0.0);
        assertEquals(2.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("b", "cab"), 0.0);
        assertEquals(3.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("b", "caab"), 0.0);

        assertEquals(1.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("b", "ab"), 0.0);
        assertEquals(2.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("b", "aab"), 0.0);

    }

    @Test
    public void testThreeChar() {
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("abc", "abc"), 0.0);
        assertEquals(1.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("abc", "bab"), 0.0);

        assertEquals(3, RegexDistanceUtils.getDistanceTailoredForStringAVM("", "abc"), 0.0);
        assertEquals(3, RegexDistanceUtils.getDistanceTailoredForStringAVM("abc", ""), 0.0);

        assertEquals(2, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", "abc"), 0.0);
        assertEquals(1.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("aa", "abc"), 0.0);
        assertEquals(2.0, RegexDistanceUtils.getDistanceTailoredForStringAVM("aaaa", "abb"), 0.0);

    }

    @Test
    public void testOr() {
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("ac", "(a|b)a*(c|d)"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("bc", "(a|b)a*(c|d)"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("ad", "(a|b)a*(c|d)"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("bd", "(a|b)a*(c|d)"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("aac", "(a|b)a*(c|d)"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("aad", "(a|b)a*(c|d)"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("bad", "(a|b)a*(c|d)"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("baac", "(a|b)a*(c|d)"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("aaaaad", "(a|b)a*(c|d)"), 0.0);

        assertEquals(2, RegexDistanceUtils.getDistanceTailoredForStringAVM("", "(a|b)a*(c|d)"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", "(a|b)a*(c|d)"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("b", "(a|b)a*(c|d)"), 0.0);
        assertEquals(2d / 3d, RegexDistanceUtils.getDistanceTailoredForStringAVM("aaa", "(a|b)a*(c|d)"), 0.0);
    }

    @Test
    public void testThreeOrFour() {
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("AAA", "A{3,4}"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("AAAA", "A{3,4}"), 0.0);

        assertEquals(3, RegexDistanceUtils.getDistanceTailoredForStringAVM("", "A{3,4}"), 0.0);
        assertEquals(2, RegexDistanceUtils.getDistanceTailoredForStringAVM("A", "A{3,4}"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("AA", "A{3,4}"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("AAAAA", "A{3,4}"), 0.0);
        assertEquals(2, RegexDistanceUtils.getDistanceTailoredForStringAVM("AAAAAA", "A{3,4}"), 0.0);
    }

    @Test
    public void testOptional() {
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("ac", "a.?c"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("abc", "a.?c"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("a.c", "a.?c"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("acc", "a.?c"), 0.0);

        assertEquals(2, RegexDistanceUtils.getDistanceTailoredForStringAVM("", "a.?c"), 0.0);
        assertEquals(0.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("acd", "a.?c"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", "a.?c"), 0.0);
        assertEquals(2.0 / 3.0, RegexDistanceUtils.getDistanceTailoredForStringAVM("cc", "a.?c"), 0.0);
    }

    @Test
    public void testDeletionFollowedByInsertion() {
        /*
         * this does not work, as expected.
         * Cannot delete last 'd' and _then_ replace second 'd' with a 'c'
         * in the distance calculation. Even if distance is not precise,
         *  AVM should still be able to solve the constraint
         */

        double addd = RegexDistanceUtils.getDistanceTailoredForStringAVM("addd", "a.?c");
        double add = RegexDistanceUtils.getDistanceTailoredForStringAVM("add", "a.?c");

        assertTrue(addd != 1.5d);
        assertTrue(add < addd);
        assertEquals(0.5, RegexDistanceUtils.getDistanceTailoredForStringAVM("add", "a.?c"), 0.0);
    }

    @Test
    public void testRange() {
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("A", "[A-Z-0-9]+"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("1", "[A-Z-0-9]+"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("A1", "[A-Z-0-9]+"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("A1B2", "[A-Z-0-9]+"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("3H8J2", "[A-Z-0-9]+"), 0.0);

        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("", "[A-Z-0-9]+"), 0.0);
        assertEquals(0.875, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", "[A-Z-0-9]+"), 0.0);
        assertEquals(0.875, RegexDistanceUtils.getDistanceTailoredForStringAVM("1a", "[A-Z-0-9]+"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("A1By", "[A-Z-0-9]+"), 0.1);
        assertEquals(1.75, RegexDistanceUtils.getDistanceTailoredForStringAVM("1aa", "[A-Z-0-9]+"), 0.0);
    }

    @Test
    public void testEmail() {
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("ZhiX@Hhhh",
                "[A-Za-z]{4,10}\\@[A-Za-z]{4,10}"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("ZhiX@Hhh",
                "[A-Za-z]{4,10}\\@[A-Za-z]{4,10}"), 0.0);
        assertEquals(2, RegexDistanceUtils.getDistanceTailoredForStringAVM("ZhiX@Hh",
                "[A-Za-z]{4,10}\\@[A-Za-z]{4,10}"), 0.0);
        assertEquals(3, RegexDistanceUtils.getDistanceTailoredForStringAVM("ZhiX@H",
                "[A-Za-z]{4,10}\\@[A-Za-z]{4,10}"), 0.0);

        //2 replacements and 4 insertions
        assertEquals(5,
                RegexDistanceUtils.getDistanceTailoredForStringAVM("hiX@H", "[A-Za-z]{4,10}\\@[A-Za-z]{4,10}"),
                1.0);

        assertEquals(5,
                RegexDistanceUtils.getDistanceTailoredForStringAVM("ZhiXH", "[A-Za-z]{4,10}\\@[A-Za-z]{4,10}"),
                0.3);

    }

    @Test
    public void tetsAutomaton() {
        //TODO
        RegexDistanceUtils.getAndCacheAutomaton("[a0]");
        RegexDistanceUtils.getAndCacheAutomaton("[a0]*");
        RegexDistanceUtils.getAndCacheAutomaton("[a0]+");

        RegexDistanceUtils.getAndCacheAutomaton("a.?c");
    }

    @Test
    public void testClosure() {
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("", "[a0]*"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", "[a0]"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("0", "[a0]"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", "[a0]+"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("0", "[a0]+"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("a", "[a0]*"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("0", "[a0]*"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("test", "[a0]*test"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("atest", "[a0]*test"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("0test", "[a0]*test"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("a0a0test", "[a0]*test"), 0.0);
        assertEquals(0, RegexDistanceUtils.getDistanceTailoredForStringAVM("aaaaa0a0test", "[a0]*test"), 0.0);

        assertEquals(4, RegexDistanceUtils.getDistanceTailoredForStringAVM("", "[a0]*test"), 0.0);
        assertEquals(3, RegexDistanceUtils.getDistanceTailoredForStringAVM("t", "[a0]*test"), 0.0);
        assertEquals(2, RegexDistanceUtils.getDistanceTailoredForStringAVM("te", "[a0]*test"), 0.0);
        assertEquals(1, RegexDistanceUtils.getDistanceTailoredForStringAVM("tes", "[a0]*test"), 0.0);
    }

    @Test
    public void testReplacement() {
        assertEquals("test", RegexDistanceUtils.expandRegex("test"));
        assertEquals("[a-zA-Z_0-9]", RegexDistanceUtils.expandRegex("\\w"));
        assertEquals("[^a-zA-Z_0-9]", RegexDistanceUtils.expandRegex("\\W"));
    }

    @Test
    public void testGroups() {
        assertEquals(0,
                RegexDistanceUtils.getDistanceTailoredForStringAVM("tue",
                        "((mon)|(tue)|(wed)|(thur)|(fri)|(sat)|(sun))"),
                0.0);

    }

    @Test
    public void testWordBoundaries() {
        String regex = ".*\\bhallo\\b.*";
        String str = "hallo test";
        Pattern p = Pattern.compile(regex);
        if (p.matcher(str).matches()) {
            assertEquals(0, RegexDistanceUtils.getStandardDistance(str, regex));
        } else {
            assertTrue(0 < RegexDistanceUtils.getStandardDistance(str, regex));
        }
    }
}
