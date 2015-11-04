package org.evosuite.idNaming;

import static org.junit.Assert.*;

import org.evosuite.testcase.ConstraintVerifier;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;
//import org.evosuite.utils.GenericConstructor;
import org.junit.Assert;
import org.junit.Test;

public class TestNameGeneratorTest {

	@Test
	public void test() {
		TestNameGenerator a= new TestNameGenerator();
		String test="public void testBaseTest() throws Exception{\n"+
        "TestChromosome tc = new TestChromosome();\n"+
        "TestFactory factory = TestFactory.getInstance();\n"+
        "factory.addConstructor(tc.getTestCase(), new GenericConstructor(Object.class.getConstructor(), Object.class), 0, 0);\n"+
        "Assert.assertEquals(1, tc.size());\n"+
        "Assert.assertTrue(ConstraintVerifier.verifyTest(tc));\n"+
    "}";
	}

}
