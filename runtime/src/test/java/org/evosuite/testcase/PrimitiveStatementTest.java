package org.evosuite.testcase;

import org.junit.Assert;
import org.junit.Test;

public class PrimitiveStatementTest {

	@Test
	public void testSame(){
		
		TestCase tc = new DefaultTestCase();
		PrimitiveStatement<?> aInt = new IntPrimitiveStatement(tc,42);
		Assert.assertTrue(aInt.same(aInt));
		Assert.assertFalse(aInt.same(null));
		
		PrimitiveStatement<?> fooString = new StringPrimitiveStatement(tc,"foo");
		Assert.assertFalse(aInt.same(fooString));
		
		PrimitiveStatement<?> nullString = new StringPrimitiveStatement(tc,null);
		Assert.assertFalse(nullString.same(fooString));
		Assert.assertFalse(fooString.same(nullString));
		
		
		//TODO: how to make it work?
		//PrimitiveStatement<?> anotherNullString = new StringPrimitiveStatement(tc,null);
		//Assert.assertTrue(nullString.same(anotherNullString));
		//Assert.assertTrue(anotherNullString.same(nullString));
	}
}
