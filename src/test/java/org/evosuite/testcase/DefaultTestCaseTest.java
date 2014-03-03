package org.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.assertion.Assertion;
import org.evosuite.utils.GenericAccessibleObject;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.commons.GeneratorAdapter;

public class DefaultTestCaseTest {

	@Test
	public void testClone(){
				
		DefaultTestCase tc = new DefaultTestCase();
		DefaultTestCase clone = (DefaultTestCase) tc.clone();
		Assert.assertTrue(tc.statements != clone.statements);
	}
}
