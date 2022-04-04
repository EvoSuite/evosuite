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
package org.evosuite.testcase;

import com.examples.with.different.packagename.coverage.MethodWithSeveralInputArguments;
import com.examples.with.different.packagename.coverage.TestMethodWithSeveralInputArguments;
import com.examples.with.different.packagename.testcarver.*;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.Properties.TestFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcarver.testcase.CarvedTestCase;
import org.evosuite.testcase.factories.JUnitTestCarvedChromosomeFactory;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class JUnitTestCarvedChromosomeFactorySystemTest extends SystemTestBase {

    private static final String defaultSelectedJUnit = Properties.SELECTED_JUNIT;
    private static final int defaultSeedMutations = Properties.SEED_MUTATIONS;
    private static final double defaultSeedClone = Properties.SEED_CLONE;
    private static final boolean defaultChopExceptions = Properties.CHOP_CARVED_EXCEPTIONS;

    @After
    public void reset() {
        Properties.SELECTED_JUNIT = defaultSelectedJUnit;
        Properties.SEED_MUTATIONS = defaultSeedMutations;
        Properties.SEED_CLONE = defaultSeedClone;
        Properties.CHOP_CARVED_EXCEPTIONS = defaultChopExceptions;
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
        Assert.assertEquals("Should be: constructor, method, 2 variables, method, 1 variable, method",
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
            Statement stmt = carved.test.getStatement(i);
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
            Statement stmt = carved.test.getStatement(i);
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
    public void testInnerConstructor() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.InnerConstructorTest.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.InnerConstructor.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertTrue(factory.hasCarvedTestCases());
        TestChromosome carved = factory.getChromosome();

        Assert.assertNotNull(carved);

        String code = carved.toString();
        Assert.assertNotNull(code);

        Assert.assertEquals(code, 2, carved.test.size());

        for (int i = 0; i < carved.test.size(); i++) {
            Statement stmt = carved.test.getStatement(i);
            boolean valid = stmt.isValid();
            Assert.assertTrue("Invalid stmt at position " + i, valid);
        }

        System.out.println(code);
    }

    @Test
    public void testInnerCalls() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.InnerCallsTest.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.InnerCalls.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertTrue(factory.hasCarvedTestCases());
        TestChromosome carved = factory.getChromosome();

        Assert.assertNotNull(carved);

        String code = carved.toString();
        Assert.assertNotNull(code);

        Assert.assertEquals(code, 4, carved.test.size());

        for (int i = 0; i < carved.test.size(); i++) {
            Statement stmt = carved.test.getStatement(i);
            boolean valid = stmt.isValid();
            Assert.assertTrue("Invalid stmt at position " + i, valid);
        }

        System.out.println(code);
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
            Statement stmt = carved.test.getStatement(i);
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
            Statement stmt = carved.test.getStatement(i);
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
            Statement stmt = carved.test.getStatement(i);
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
            Statement stmt = carved.test.getStatement(i);
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

        Assert.assertEquals(code, 3, carved.test.size());
    }

    @Test
    public void testJavaAgent() {
        Properties.SELECTED_JUNIT = PersonWithJavaAgentSystemTest.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.Person.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertTrue(factory.hasCarvedTestCases());
        TestChromosome carved = factory.getChromosome();
        Assert.assertNotNull(carved);

        String code = carved.toString();

        Assert.assertEquals(code, 3, carved.test.size());
    }

    @Test
    public void testBeanArrayConverterUtils() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.ArrayConverterTestCase.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.ArrayConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(6, factory.getNumCarvedTestCases());
    }

    @Test
    public void testBeanDateConverterUtils() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.DateConverterTestCase.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.DateConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(11, factory.getNumCarvedTestCases());
    }

    @Test
    public void testBeanDateConverterUtils1() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.DateConverterTest1.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.DateConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        String code = factory.getChromosome().getTestCase().toCode();
        System.out.println(code);
    }

    @Test
    public void testBeanDateConverterUtils2() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.DateConverterTest2.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.DateConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        String code = factory.getChromosome().getTestCase().toCode();
        System.out.println(code);
    }

    @Test
    public void testBeanDateConverterUtils3() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.DateConverterTest3.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.DateConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        String code = factory.getChromosome().getTestCase().toCode();
        System.out.println(code);
    }

    @Test
    public void testBeanDateConverterUtils4() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.DateConverterTest4.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.DateConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        String code = factory.getChromosome().getTestCase().toCode();
        System.out.println(code);
    }

    @Test
    public void testBeanDateConverterUtils5() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.DateConverterTest5.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.DateConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        String code = factory.getChromosome().getTestCase().toCode();
        System.out.println(code);
    }

    @Test
    public void testBeanDateConverterUtils6() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.DateConverterTest6.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.DateConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        String code = factory.getChromosome().getTestCase().toCode();
        System.out.println(code);
    }

    @Test
    public void testBeanDateConverterUtils7() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.DateConverterTest7.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.DateConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;
        Properties.CHOP_CARVED_EXCEPTIONS = false;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        String code = factory.getChromosome().getTestCase().toCode();
        System.out.println(code);
    }

    @Test
    public void testBeanDateConverterUtils8() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.DateConverterTest8.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.DateConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        String code = factory.getChromosome().getTestCase().toCode();
        System.out.println(code);
    }

    @Test
    public void testBeanDateConverterUtils9() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.DateConverterTest9.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.DateConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        String code = factory.getChromosome().getTestCase().toCode();
        System.out.println(code);
    }

    @Test
    public void testBeanDateConverterUtils10() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.DateConverterTest10.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.DateConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        String code = factory.getChromosome().getTestCase().toCode();
        System.out.println(code);
    }

    @Test
    public void testBeanDateConverterUtils11() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.DateConverterTest11.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.DateConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        String code = factory.getChromosome().getTestCase().toCode();
        System.out.println(code);
    }

    @Test
    public void testBeanIntegerConverterUtils() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.IntegerConverterTestCase.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.IntegerConverter.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(18, factory.getNumCarvedTestCases());

    }

    @Test
    public void testWritePublicField() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.ClassWithPublicFieldWritingTestCase.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.ClassWithPublicField.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 0;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        TestChromosome test = factory.getChromosome();
        String code = test.getTestCase().toCode();
        Assert.assertFalse(code.contains("XStream"));
        System.out.println(code);
        Assert.assertTrue(code.contains("classWithPublicField0.x"));
    }

    @Test
    public void testReadPublicField() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.ClassWithPublicFieldReadingTestCase.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.ClassWithPublicField.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 0;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        TestChromosome test = factory.getChromosome();
        String code = test.getTestCase().toCode();
        System.out.println(code);
        Assert.assertFalse(code.contains("XStream"));
        Assert.assertTrue(code.contains("classWithPublicField0.x") || code.contains("Locale.CHINESE"));
    }

    @Test
    public void testReadPublicStaticField() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.ClassWithPublicStaticFieldReadingTestCase.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.ClassWithPublicStaticField.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 0;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        TestChromosome test = factory.getChromosome();
        String code = test.getTestCase().toCode();
        System.out.println(code);
        Assert.assertFalse(code.contains("XStream"));
        Assert.assertTrue(code.contains("ClassWithPublicStaticField.x"));
    }

    @Test
    public void testReadPublicStaticFieldInOtherClass() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.ClassDependingOnStaticFieldInOtherClassTestCase.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.ClassDependingOnStaticFieldInOtherClass.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 0;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        TestChromosome test = factory.getChromosome();
        String code = test.getTestCase().toCode();
        System.out.println(code);
        Assert.assertFalse(code.contains("XStream"));
        Assert.assertTrue(code.contains("StaticFieldInOtherClass.x"));
    }

    @Test
    public void testClassWithStaticMethod() {
        Properties.SELECTED_JUNIT = com.examples.with.different.packagename.testcarver.ClassWithStaticMethodTestCase.class.getCanonicalName();
        Properties.TARGET_CLASS = com.examples.with.different.packagename.testcarver.ClassWithStaticMethod.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 0;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(1, factory.getNumCarvedTestCases());

        TestChromosome test = factory.getChromosome();
        String code = test.getTestCase().toCode();
        System.out.println(code);
        Assert.assertFalse(code.contains("XStream"));
        Assert.assertTrue(code.contains("classWithStaticMethod0.testMe"));
    }

    @Ignore // EvoSuite may also cover it without seeding now.
    @Test
    public void testDifficultClassWithWrongTestFails() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DifficultClassWithoutCarving.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.TEST_FACTORY = TestFactory.JUNIT;
        Properties.SELECTED_JUNIT = DifficultClassTest.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertTrue("Did not expect optimal coverage: ", best.getCoverage() < 1d);
    }

    @Test
    public void testDifficultClassWithRightTestPasses() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DifficultClassWithoutCarving.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.TEST_FACTORY = TestFactory.JUNIT;
        Properties.SELECTED_JUNIT = DifficultClassWithoutCarvingTest.class.getCanonicalName();

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Expected optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }


    @Test
    public void testConcreteClassWithFields() {
        Properties.SELECTED_JUNIT = ConcreteSubClassWithFieldsTestCase.class.getCanonicalName();
        Properties.TARGET_CLASS = ConcreteSubClassWithFields.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 0;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
                null);
        Assert.assertEquals(2, factory.getNumCarvedTestCases());

        TestChromosome test = factory.getChromosome();
        String code = test.getTestCase().toCode();
        System.out.println(code);
        Assert.assertFalse(code.contains("XStream"));
        Assert.assertTrue(code.contains("concreteSubClassWithFields0"));
    }

    @Test
    public void testCarvedTestNames() {

        Properties.TARGET_CLASS = MethodWithSeveralInputArguments.class.getCanonicalName();
        Properties.SELECTED_JUNIT = TestMethodWithSeveralInputArguments.class.getCanonicalName();

        Properties.SEED_MUTATIONS = 1;
        Properties.SEED_CLONE = 1;

        JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(null);

        Assert.assertEquals("Incorrect number of carved tests", 2, factory.getNumCarvedTestCases());
        CarvedTestCase tc1 = (CarvedTestCase) factory.getCarvedTestCases().get(0);
        Assert.assertEquals("Incorrect carved test name", "testWithNull", tc1.getName());
        System.out.println("Carved Test Case # " + tc1.getID() + ": " + tc1.getName());
        System.out.println(tc1.toCode());
        CarvedTestCase tc2 = (CarvedTestCase) factory.getCarvedTestCases().get(1);
        Assert.assertEquals("Incorrect carved test name", "testWithArray", tc2.getName());
        System.out.println("Carved Test Case # " + tc2.getID() + ": " + tc2.getName());
        System.out.println(tc2.toCode());
    }
}
