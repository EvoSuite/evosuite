/**
 * 
 */
package org.evosuite.symbolic.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Gordon Fraser
 * 
 */
public class TestRegexDistance {

	@Test
	public void testEmptyRegex() {
		assertEquals(0.0, RegexDistance.getDistance("", ""), 0.0);
		assertEquals(3.0, RegexDistance.getDistance("abc", ""), 0.0);
		assertEquals(1.0, RegexDistance.getDistance("a", ""), 0.0);
		assertEquals(2.0, RegexDistance.getDistance("ab", ""), 0.0);
	}

	@Test
	public void testIdenticalChar() {
		assertEquals(0, RegexDistance.getDistance("a", "a"), 0.0);
		assertEquals(0, RegexDistance.getDistance("aa", "aa"), 0.0);
	}

	@Test
	public void testReplaceChar() {
		assertEquals(0.5, RegexDistance.getDistance("b", "a"), 0.0);
		assertEquals(0.5, RegexDistance.getDistance("ab", "aa"), 0.0);
	}

	@Test
	public void testDeleteChar() {
		assertEquals(1, RegexDistance.getDistance("aa", "a"), 0.0);
		assertEquals(2, RegexDistance.getDistance("aaa", "a"), 0.0);
		assertEquals(3, RegexDistance.getDistance("aaaa", "a"), 0.0);
		assertEquals(4, RegexDistance.getDistance("aaaaa", "a"), 0.0);
	}

	@Test
	public void testInsertCharInEmptyString() {
		assertEquals(1, RegexDistance.getDistance("", "a"), 0.0);
		assertEquals(2, RegexDistance.getDistance("", "aa"), 0.0);
		assertEquals(3, RegexDistance.getDistance("", "aaa"), 0.0);
	}

	@Test
	public void testInsertChar() {
		assertEquals(1, RegexDistance.getDistance("a", "aa"), 0.0);
		assertEquals(2, RegexDistance.getDistance("a", "aaa"), 0.0);
		assertEquals(3, RegexDistance.getDistance("a", "aaaa"), 0.0);
	}

	@Test
	public void testTwoChar() {
		assertEquals(0, RegexDistance.getDistance("ab", "ab"), 0.0);
		assertEquals(1, RegexDistance.getDistance("ab", "ba"), 0.0);
		assertEquals(1, RegexDistance.getDistance("ab", "bc"), 0.0);

		assertEquals(1, RegexDistance.getDistance("bb", "aa"), 0.0);
		assertEquals(1, RegexDistance.getDistance("bb", "cc"), 0.0);
		assertEquals(1, RegexDistance.getDistance("bb", "ac"), 0.0);
		assertEquals(1, RegexDistance.getDistance("bb", "ca"), 0.0);

		assertEquals(2, RegexDistance.getDistance("", "ab"), 0.0);
		assertEquals(2, RegexDistance.getDistance("ab", ""), 0.0);

		assertEquals(1, RegexDistance.getDistance("a", "ab"), 0.0);
		assertEquals(1.5, RegexDistance.getDistance("aaa", "ab"), 0.0);
		assertEquals(2.5, RegexDistance.getDistance("aaaa", "ab"), 0.0);

		assertEquals(1.5, RegexDistance.getDistance("bb", "bab"), 0.0);

		assertEquals(3, RegexDistance.getDistance("b", "bcab"), 0.0);
		assertEquals(4, RegexDistance.getDistance("b", "bcaab"), 0.0);

		assertEquals(1.5, RegexDistance.getDistance("xb", "xcb"), 0.0);
		assertEquals(1.5, RegexDistance.getDistance("b", "cb"), 0.0);
		assertEquals(2.5, RegexDistance.getDistance("b", "cab"), 0.0);
		assertEquals(3.5, RegexDistance.getDistance("b", "caab"), 0.0);

		assertEquals(1.5, RegexDistance.getDistance("b", "ab"), 0.0);
		assertEquals(2.5, RegexDistance.getDistance("b", "aab"), 0.0);

	}

	@Test
	public void testThreeChar() {
		assertEquals(0, RegexDistance.getDistance("abc", "abc"), 0.0);
		assertEquals(1.5, RegexDistance.getDistance("abc", "bab"), 0.0);

		assertEquals(3, RegexDistance.getDistance("", "abc"), 0.0);
		assertEquals(3, RegexDistance.getDistance("abc", ""), 0.0);

		assertEquals(2, RegexDistance.getDistance("a", "abc"), 0.0);
		assertEquals(1.5, RegexDistance.getDistance("aa", "abc"), 0.0);
		assertEquals(2.0, RegexDistance.getDistance("aaaa", "abb"), 0.0);

	}

	@Test
	public void testOr() {
		assertEquals(0, RegexDistance.getDistance("ac", "(a|b)a*(c|d)"), 0.0);
		assertEquals(0, RegexDistance.getDistance("bc", "(a|b)a*(c|d)"), 0.0);
		assertEquals(0, RegexDistance.getDistance("ad", "(a|b)a*(c|d)"), 0.0);
		assertEquals(0, RegexDistance.getDistance("bd", "(a|b)a*(c|d)"), 0.0);
		assertEquals(0, RegexDistance.getDistance("aac", "(a|b)a*(c|d)"), 0.0);
		assertEquals(0, RegexDistance.getDistance("aad", "(a|b)a*(c|d)"), 0.0);
		assertEquals(0, RegexDistance.getDistance("bad", "(a|b)a*(c|d)"), 0.0);
		assertEquals(0, RegexDistance.getDistance("baac", "(a|b)a*(c|d)"), 0.0);
		assertEquals(0, RegexDistance.getDistance("aaaaad", "(a|b)a*(c|d)"), 0.0);

		assertEquals(2, RegexDistance.getDistance("", "(a|b)a*(c|d)"), 0.0);
		assertEquals(1, RegexDistance.getDistance("a", "(a|b)a*(c|d)"), 0.0);
		assertEquals(1, RegexDistance.getDistance("b", "(a|b)a*(c|d)"), 0.0);
		assertEquals(2d / 3d, RegexDistance.getDistance("aaa", "(a|b)a*(c|d)"), 0.0);
	}

	@Test
	public void testThreeOrFour() {
		assertEquals(0, RegexDistance.getDistance("AAA", "A{3,4}"), 0.0);
		assertEquals(0, RegexDistance.getDistance("AAAA", "A{3,4}"), 0.0);

		assertEquals(3, RegexDistance.getDistance("", "A{3,4}"), 0.0);
		assertEquals(2, RegexDistance.getDistance("A", "A{3,4}"), 0.0);
		assertEquals(1, RegexDistance.getDistance("AA", "A{3,4}"), 0.0);
		assertEquals(1, RegexDistance.getDistance("AAAAA", "A{3,4}"), 0.0);
		assertEquals(2, RegexDistance.getDistance("AAAAAA", "A{3,4}"), 0.0);
	}

	@Test
	public void testOptional() {
		assertEquals(0, RegexDistance.getDistance("ac", "a.?c"), 0.0);
		assertEquals(0, RegexDistance.getDistance("abc", "a.?c"), 0.0);
		assertEquals(0, RegexDistance.getDistance("a.c", "a.?c"), 0.0);
		assertEquals(0, RegexDistance.getDistance("acc", "a.?c"), 0.0);

		assertEquals(2, RegexDistance.getDistance("", "a.?c"), 0.0);
		assertEquals(0.5, RegexDistance.getDistance("acd", "a.?c"), 0.0);
		assertEquals(1, RegexDistance.getDistance("a", "a.?c"), 0.0);
		assertEquals(2.0 / 3.0, RegexDistance.getDistance("cc", "a.?c"), 0.0);
		assertEquals(1.5, RegexDistance.getDistance("addd", "a.?c"), 0.0);
	}

	@Test
	public void testRange() {
		assertEquals(0, RegexDistance.getDistance("A", "[A-Z-0-9]+"), 0.0);
		assertEquals(0, RegexDistance.getDistance("1", "[A-Z-0-9]+"), 0.0);
		assertEquals(0, RegexDistance.getDistance("A1", "[A-Z-0-9]+"), 0.0);
		assertEquals(0, RegexDistance.getDistance("A1B2", "[A-Z-0-9]+"), 0.0);
		assertEquals(0, RegexDistance.getDistance("3H8J2", "[A-Z-0-9]+"), 0.0);

		assertEquals(1, RegexDistance.getDistance("", "[A-Z-0-9]+"), 0.0);
		assertEquals(0.875, RegexDistance.getDistance("a", "[A-Z-0-9]+"), 0.0);
		assertEquals(0.875, RegexDistance.getDistance("1a", "[A-Z-0-9]+"), 0.0);
		assertEquals(1, RegexDistance.getDistance("A1By", "[A-Z-0-9]+"), 0.1);
		assertEquals(1.75, RegexDistance.getDistance("1aa", "[A-Z-0-9]+"), 0.0);
	}

	@Test
	public void testEmail() {
		assertEquals(0, RegexDistance.getDistance("ZhiX@Hhhh",
		                                          "[A-Za-z]{4,10}\\@[A-Za-z]{4,10}"), 0.0);
		assertEquals(1, RegexDistance.getDistance("ZhiX@Hhh",
		                                          "[A-Za-z]{4,10}\\@[A-Za-z]{4,10}"), 0.0);
		assertEquals(2, RegexDistance.getDistance("ZhiX@Hh",
		                                          "[A-Za-z]{4,10}\\@[A-Za-z]{4,10}"), 0.0);
		assertEquals(3, RegexDistance.getDistance("ZhiX@H",
		                                          "[A-Za-z]{4,10}\\@[A-Za-z]{4,10}"), 0.0);
		assertEquals(4,
		             RegexDistance.getDistance("hiX@H", "[A-Za-z]{4,10}\\@[A-Za-z]{4,10}"),
		             0.0);

		assertEquals(5,
		             RegexDistance.getDistance("ZhiXH", "[A-Za-z]{4,10}\\@[A-Za-z]{4,10}"),
		             0.3);

	}

	@Test
	public void testClosure() {
		assertEquals(0, RegexDistance.getDistance("test", "[a0]*test"), 0.0);
		assertEquals(0, RegexDistance.getDistance("atest", "[a0]*test"), 0.0);
		assertEquals(0, RegexDistance.getDistance("0test", "[a0]*test"), 0.0);
		assertEquals(0, RegexDistance.getDistance("a0a0test", "[a0]*test"), 0.0);
		assertEquals(0, RegexDistance.getDistance("aaaaa0a0test", "[a0]*test"), 0.0);

		assertEquals(4, RegexDistance.getDistance("", "[a0]*test"), 0.0);
		assertEquals(3, RegexDistance.getDistance("t", "[a0]*test"), 0.0);
		assertEquals(2, RegexDistance.getDistance("te", "[a0]*test"), 0.0);
		assertEquals(1, RegexDistance.getDistance("tes", "[a0]*test"), 0.0);
	}

	@Test
	public void testReplacement() {
		assertEquals("test", RegexDistance.expandRegex("test"));
		assertEquals("[a-zA-Z_0-9]", RegexDistance.expandRegex("\\w"));
		assertEquals("[^a-zA-Z_0-9]", RegexDistance.expandRegex("\\W"));
	}

	@Test
	public void testGroups() {
		assertEquals(0,
		             RegexDistance.getDistance("tue",
		                                       "((mon)|(tue)|(wed)|(thur)|(fri)|(sat)|(sun))"),
		             0.0);

	}
}
