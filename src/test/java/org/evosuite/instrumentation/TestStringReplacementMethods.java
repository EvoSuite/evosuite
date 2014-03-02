package org.evosuite.instrumentation;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestStringReplacementMethods {

	@Test
	public void testEquals() {
		String str1 = "test";
		String str2 = "test";
		int result = BooleanHelper.StringEquals(str1, str2);
		assertTrue(result > 0);
	}

	@Test
	public void testEqualsNull() {
		String str1 = "test";
		String str2 = null;
		int result = BooleanHelper.StringEquals(str1, str2);
		assertTrue(result < 0);
	}

	@Test(expected=NullPointerException.class)
	public void testEqualsNullCallee() {
		String str1 = null;
		String str2 = "test";
		BooleanHelper.StringEquals(str1, str2);
	}

	@Test
	public void testNotEquals() {
		String str1 = "test1";
		String str2 = "test2";
		int result = BooleanHelper.StringEquals(str1, str2);
		assertTrue(result < 0);
	}
	
	@Test
	public void testEqualsRelativeOrdering() {
		String str1 = "test1";
		String str2 = "test2";
		String str3 = "something else";
		int result1 = BooleanHelper.StringEquals(str1, str2);
		int result2 = BooleanHelper.StringEquals(str1, str3);
		assertTrue(result2 < result1);
	}
	
	@Test
	public void testEqualsIgnoreCase() {
		String str1 = "test";
		String str2 = "TEST";
		int result = BooleanHelper.StringEqualsIgnoreCase(str1, str2);
		assertTrue(result > 0);
	}

	@Test
	public void testEqualsIgnoreCaseNull() {
		String str1 = "test";
		String str2 = null;
		int result = BooleanHelper.StringEqualsIgnoreCase(str1, str2);
		assertTrue(result < 0);
	}

	@Test(expected=NullPointerException.class)
	public void testEqualsIgnoreCaseNullCallee() {
		String str1 = null;
		String str2 = "test";
		BooleanHelper.StringEqualsIgnoreCase(str1, str2);
	}

	@Test
	public void testNotEqualsIgnoreCase() {
		String str1 = "test1";
		String str2 = "test2";
		int result = BooleanHelper.StringEqualsIgnoreCase(str1, str2);
		assertTrue(result < 0);
	}
	
	@Test
	public void testEqualsIgnoreCaseRelativeOrdering() {
		String str1 = "test1";
		String str2 = "test2";
		String str3 = "something else";
		int result1 = BooleanHelper.StringEqualsIgnoreCase(str1, str2);
		int result2 = BooleanHelper.StringEqualsIgnoreCase(str1, str3);
		assertTrue(result2 < result1);
	}

	@Test
	public void testStartsWithAtStart() {
		String prefix = "test";
		String str = "testchen";
		int result = BooleanHelper.StringStartsWith(str, prefix, 0);
		assertTrue(result > 0);
	}

	@Test
	public void testStartsWith() {
		String prefix = "test";
		String str = "blahtestchen";
		int result = BooleanHelper.StringStartsWith(str, prefix, 4);
		assertTrue(result > 0);
	}

	@Test
	public void testNotStartsWithAtStart() {
		String prefix = "foo";
		String str = "testchen";
		int result = BooleanHelper.StringStartsWith(str, prefix, 0);
		assertTrue(result < 0);
	}

	@Test
	public void testNotStartsWith() {
		String prefix = "foo";
		String str = "blahtestchen";
		int result = BooleanHelper.StringStartsWith(str, prefix, 4);
		assertTrue(result < 0);
	}

	@Test
	public void testEndsWith() {
		String suffix = "test";
		String str = "foo bar test";
		int result = BooleanHelper.StringEndsWith(str, suffix);
		assertTrue(result > 0);
	}

	@Test
	public void testNotEndsWith() {
		String suffix = "foo";
		String str = "blahtestchen";
		int result = BooleanHelper.StringEndsWith(str, suffix);
		assertTrue(result < 0);
	}

	@Test
	public void testEmpty() {
		String str = "";
		int result = BooleanHelper.StringIsEmpty(str);
		assertTrue(result > 0);
	}

	@Test
	public void testNotEmpty() {
		String str = "blahtestchen";
		int result = BooleanHelper.StringIsEmpty(str);
		assertTrue(result < 0);
	}

	@Test
	public void testMatches() {
		String str = "hahafoo";
		String regex = ".*foo";
		int result = BooleanHelper.StringMatches(str, regex);
		assertTrue(result > 0);
	}

	@Test
	public void testNotMatches() {
		String str = "";
		String regex = "foo";
		int result = BooleanHelper.StringMatches(str, regex);
		assertTrue(result < 0);
	}

	@Test
	public void testRegionMatches() {
		String str = "hahafoobar";
		String match = "foog";
		int result = BooleanHelper.StringRegionMatches(str, 4, match, 0, 3);
		assertTrue(str.regionMatches(4, match, 0, 3));
		assertTrue(result > 0);
	}

	@Test
	public void testNotRegionMatchesStart1() {
		String str = "hahafoobar";
		String match = "foog";
		int result = BooleanHelper.StringRegionMatches(str, 5, match, 0, 3);
		assertFalse(str.regionMatches(5, match, 0, 3));
		assertTrue(result < 0);
	}

	@Test
	public void testNotRegionMatchesStart2() {
		String str = "hahafoobar";
		String match = "foog";
		int result = BooleanHelper.StringRegionMatches(str, 4, match, 1, 3);
		assertFalse(str.regionMatches(4, match, 1, 3));
		assertTrue(result < 0);
	}

	@Test
	public void testNotRegionMatchesLength() {
		String str = "hahafoobar";
		String match = "foo";
		int result = BooleanHelper.StringRegionMatches(str, 5, match, 0, 4);
		assertFalse(str.regionMatches(5, match, 0, 4));
		assertTrue(result < 0);
	}

	@Test
	public void testNotRegionMatchesString() {
		String str = "hahafoobar";
		String match = "bar";
		int result = BooleanHelper.StringRegionMatches(str, 5, match, 0, 3);
		assertFalse(str.regionMatches(5, match, 0, 3));
		assertTrue(result < 0);
	}

}
