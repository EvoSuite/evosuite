package com.examples.with.different.packagename;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

/**
 * Snippet from Lang project
 * (org.apache.commons.lang3.math.NumberUtilsTest)
 */
public class ClassNumberUtilsTest {

	@Test
    public void testCreateBigInteger() {
		assertEquals("createBigInteger(String) failed", new BigInteger("12345"), ClassNumberUtils.createBigInteger("12345"));
        assertEquals("createBigInteger(null) failed", null, ClassNumberUtils.createBigInteger(null));
        
        assertEquals("createBigInteger(String) failed", new BigInteger("255"), ClassNumberUtils.createBigInteger("0xff"));
        assertEquals("createBigInteger(String) failed", new BigInteger("255"), ClassNumberUtils.createBigInteger("0Xff"));
        assertEquals("createBigInteger(String) failed", new BigInteger("255"), ClassNumberUtils.createBigInteger("#ff"));
        assertEquals("createBigInteger(String) failed", new BigInteger("-255"), ClassNumberUtils.createBigInteger("-0xff"));
        assertEquals("createBigInteger(String) failed", new BigInteger("255"), ClassNumberUtils.createBigInteger("0377"));
        assertEquals("createBigInteger(String) failed", new BigInteger("-255"), ClassNumberUtils.createBigInteger("-0377"));
        assertEquals("createBigInteger(String) failed", new BigInteger("-255"), ClassNumberUtils.createBigInteger("-0377"));
        assertEquals("createBigInteger(String) failed", new BigInteger("-0"), ClassNumberUtils.createBigInteger("-0"));
        assertEquals("createBigInteger(String) failed", new BigInteger("0"), ClassNumberUtils.createBigInteger("0"));
    }
}
