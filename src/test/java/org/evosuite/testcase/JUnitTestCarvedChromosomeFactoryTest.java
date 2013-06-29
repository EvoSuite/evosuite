package org.evosuite.testcase;

import org.evosuite.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JUnitTestCarvedChromosomeFactoryTest {

	private static final String defaultSelectedJUnit = Properties.SELECTED_JUNIT;
	private static final int defaultSeedMutations = Properties.SEED_MUTATIONS;
	private static final double defaultSeedClone = Properties.SEED_CLONE;

	@Before
	public void reset() {
		Properties.SELECTED_JUNIT = defaultSelectedJUnit;
		Properties.SEED_MUTATIONS = defaultSeedMutations;
		Properties.SEED_CLONE = defaultSeedClone;
	}

	@SuppressWarnings("unused")
	@Test
	public void testDefaultEmptySetting() {
		/*
		 * by default, no seeded test should be selected
		 */
		try {
			JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
			        null);
			Assert.fail("Expected IllegalStateException");
		} catch (IllegalStateException e) {
			//expected
		}
	}

	@Test
	public void testSimpleTest() {
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.SimpleTest.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.Simple.class.getCanonicalName();

		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
		        null);
		TestChromosome carved = factory.getChromosome();

		Assert.assertNotNull(carved);
		Assert.assertEquals("Shouble be: constructor, method, 2 variables, method, 1 variable, method",
		                    7, carved.test.size());
	}

	@Test
	public void testObjectSetWrapper() {
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.ObjectWrapperSetTest.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.ObjectWrapper.class.getCanonicalName();

		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
		        null);
		Assert.assertTrue(factory.hasCarvedTestCases());
		TestChromosome carved = factory.getChromosome();

		Assert.assertNotNull(carved);
		Assert.assertEquals("", 13, carved.test.size());
	}

	@Test
	public void testObjectWrapperSequence() {
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.ObjectWrapperSequenceTest.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.ObjectWrapper.class.getCanonicalName();

		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
		        null);
		Assert.assertTrue(factory.hasCarvedTestCases());
		TestChromosome carved = factory.getChromosome();

		Assert.assertNotNull(carved);
		Assert.assertEquals("", 6, carved.test.size());
	}

	@Test
	public void testObjectWrapperArray() {
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.ObjectWrapperArrayTest.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.ObjectWrapper.class.getCanonicalName();

		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
		        null);
		Assert.assertTrue(factory.hasCarvedTestCases());
		TestChromosome carved = factory.getChromosome();

		Assert.assertNotNull(carved);
		Assert.assertEquals("", 13, carved.test.size());
	}

	@Test
	public void testGenericParameter() {
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.GenericTest.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.ObjectWrapper.class.getCanonicalName();

		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
		        null);
		Assert.assertTrue(factory.hasCarvedTestCases());
		TestChromosome carved = factory.getChromosome();

		Assert.assertNotNull(carved);
		Assert.assertEquals("", 5, carved.test.size());

		for (int i = 0; i < carved.test.size(); i++) {
			StatementInterface stmt = carved.test.getStatement(i);
			boolean valid = stmt.isValid();
			Assert.assertTrue("Invalid stmt at position " + i, valid);
		}

		String code = carved.toString();
		String setLong = "HashSet<Long>";
		Assert.assertTrue("generated code does not contain " + setLong + "\n" + code,
		                  code.contains(setLong));
	}

	@Test
	public void testGenericClassSet() {
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.GenericObjectWrapperSetTest.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.GenericObjectWrapper.class.getCanonicalName();

		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
		        null);
		Assert.assertTrue(factory.hasCarvedTestCases());
		TestChromosome carved = factory.getChromosome();

		Assert.assertNotNull(carved);
		Assert.assertEquals("", 13, carved.test.size());

		for (int i = 0; i < carved.test.size(); i++) {
			StatementInterface stmt = carved.test.getStatement(i);
			boolean valid = stmt.isValid();
			Assert.assertTrue("Invalid stmt at position " + i, valid);
		}

		String code = carved.toString();
		String setLong = "GenericObjectWrapper<HashSet<Long>>";
		Assert.assertTrue("generated code does not contain " + setLong + "\n" + code,
		                  code.contains(setLong));

		code = carved.toString();
		setLong = "(Object)";
		Assert.assertFalse("generated code contains object cast " + setLong + "\n" + code,
		                   code.contains(setLong));

	}

	@Test
	public void testGenericClassSequence() {
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.GenericObjectWrapperSequenceTest.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.GenericObjectWrapper.class.getCanonicalName();

		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
		        null);
		Assert.assertTrue(factory.hasCarvedTestCases());
		TestChromosome carved = factory.getChromosome();

		Assert.assertNotNull(carved);
		Assert.assertEquals("", 6, carved.test.size());

		for (int i = 0; i < carved.test.size(); i++) {
			StatementInterface stmt = carved.test.getStatement(i);
			boolean valid = stmt.isValid();
			Assert.assertTrue("Invalid stmt at position " + i, valid);
		}

		String code = carved.toString();
		String setLong = "GenericObjectWrapper<GenericObjectWrapperSequenceTest.Foo>";
		Assert.assertTrue("generated code does not contain " + setLong + "\n" + code,
		                  code.contains(setLong));

		code = carved.toString();
		setLong = "(Object)";
		Assert.assertFalse("generated code contains object cast " + setLong + "\n" + code,
		                   code.contains(setLong));

	}

	@Test
	public void testGenericClassArray() {
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.GenericObjectWrapperArrayTest.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.GenericObjectWrapper.class.getCanonicalName();

		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
		        null);
		Assert.assertTrue(factory.hasCarvedTestCases());
		TestChromosome carved = factory.getChromosome();

		Assert.assertNotNull(carved);
		Assert.assertEquals("", 13, carved.test.size());

		for (int i = 0; i < carved.test.size(); i++) {
			StatementInterface stmt = carved.test.getStatement(i);
			boolean valid = stmt.isValid();
			Assert.assertTrue("Invalid stmt at position " + i, valid);
		}

		String code = carved.toString();
		String setLong = "GenericObjectWrapper<Long[]>";
		Assert.assertTrue("generated code does not contain " + setLong + "\n" + code,
		                  code.contains(setLong));
	}

	@Test
	public void testGenericClassList() {
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.GenericObjectWrapperWithListTest.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.GenericObjectWrapperWithList.class.getCanonicalName();

		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
		        null);
		Assert.assertTrue(factory.hasCarvedTestCases());
		TestChromosome carved = factory.getChromosome();

		Assert.assertNotNull(carved);
		Assert.assertEquals("", 10, carved.test.size());

		for (int i = 0; i < carved.test.size(); i++) {
			StatementInterface stmt = carved.test.getStatement(i);
			boolean valid = stmt.isValid();
			Assert.assertTrue("Invalid stmt at position " + i, valid);
		}

		String code = carved.toString();
		String setLong = "GenericObjectWrapperWithList<GenericObjectWrapperWithListTest.Foo>";
		Assert.assertTrue("generated code does not contain " + setLong + "\n" + code,
		                  code.contains(setLong));
	}


	@Test
	public void testGenericClassTwoParameter() {
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.GenericObjectWrapperTwoParameterTest.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.GenericObjectWrapperTwoParameter.class.getCanonicalName();

		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
		        null);
		Assert.assertTrue(factory.hasCarvedTestCases());
		TestChromosome carved = factory.getChromosome();

		Assert.assertNotNull(carved);
		Assert.assertEquals("", 8, carved.test.size());

		for (int i = 0; i < carved.test.size(); i++) {
			StatementInterface stmt = carved.test.getStatement(i);
			boolean valid = stmt.isValid();
			Assert.assertTrue("Invalid stmt at position " + i, valid);
		}

		String code = carved.toString();
		String setLong = "GenericObjectWrapperTwoParameter<String, String>";
		Assert.assertTrue("generated code does not contain " + setLong + "\n" + code,
		                  code.contains(setLong));
	}
	
	
	
	@Test
	public void testPrimitives() {
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.PrimitivesTest.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.ObjectWrapper.class.getCanonicalName();

		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
		        null);
		Assert.assertTrue(factory.hasCarvedTestCases());
		TestChromosome carved = factory.getChromosome();
		Assert.assertNotNull(carved);

		String code = carved.toString();

		Assert.assertEquals(code, 19, carved.test.size());

		String concatenated = "0123.04.0567";
		Assert.assertTrue("generated code does not contain " + concatenated + "\n" + code,
		                  code.contains(concatenated));
	}
	
	@Test
	public void testPersonExample() {
		Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.TestPerson.class.getCanonicalName();
		Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.Person.class.getCanonicalName();

		Properties.SEED_MUTATIONS = 1;
		Properties.SEED_CLONE = 1;

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
		        null);
		Assert.assertTrue(factory.hasCarvedTestCases());
		TestChromosome carved = factory.getChromosome();
		Assert.assertNotNull(carved);

		String code = carved.toString();

		Assert.assertEquals(code, 2, carved.test.size());
	}
}
