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

import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.numeric.CharPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericField;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.FactoryExample;

import static org.junit.Assert.*;

/**
 * @author Gordon Fraser
 */
public class FactoryTestSystemTest extends SystemTestBase {

    private double P_OBJECT_REUSE = Properties.OBJECT_REUSE_PROBABILITY;
    private double P_PRIMITIVE_REUSE = Properties.PRIMITIVE_REUSE_PROBABILITY;
    private boolean ARCHIVE = Properties.TEST_ARCHIVE;

    @After
    public void restoreProperties() {
        Properties.OBJECT_REUSE_PROBABILITY = P_OBJECT_REUSE;
        Properties.PRIMITIVE_REUSE_PROBABILITY = P_PRIMITIVE_REUSE;
        Properties.TEST_ARCHIVE = ARCHIVE;
    }

    @Before
    public void setupCluster() {
        EvoSuite evosuite = new EvoSuite();

        // Archive will remove test calls,
        // invalidating testTestCalls
        Properties.TEST_ARCHIVE = false;

        String targetClass = FactoryExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.ASSERTIONS = false;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        // Object result =
        evosuite.parseCommandLine(command);
        // GeneticAlgorithm<?> ga = getGAFromResult(result);
        // TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        // System.out.println("EvolvedTestSuite:\n" + best);
    }

    @Test
    public void testTestCalls() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException {
        List<GenericAccessibleObject<?>> testCalls = TestCluster.getInstance().getTestCalls();
        System.out.println(testCalls.toString());
        assertEquals("Expected 5 test calls, but got: " + testCalls.size() + ": "
                + testCalls, 4, testCalls.size());
    }

    @Test
    public void testIntegerDependency() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException {
        TestFactory testFactory = TestFactory.getInstance();

        GenericMethod method = new GenericMethod(
                FactoryExample.class.getMethod("testByte", byte.class, byte.class),
                FactoryExample.class);
        DefaultTestCase test = new DefaultTestCase();
        Properties.PRIMITIVE_REUSE_PROBABILITY = 0.0;
        testFactory.addMethod(test, method, 0, 0);
        String code = test.toCode();
        System.out.println(code);
        assertEquals(4, test.size());
        assertTrue(code.contains("factoryExample0.testByte(byte0, byte1)"));
    }

    @Test
    public void testObjectDependencyReuse() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestFactory testFactory = TestFactory.getInstance();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());
        GenericMethod method = new GenericMethod(
                sut.getMethod("testByte", byte.class, byte.class),
                sut);
        DefaultTestCase test = new DefaultTestCase();
        Properties.PRIMITIVE_REUSE_PROBABILITY = 1.0;
        Properties.OBJECT_REUSE_PROBABILITY = 1.0;
        testFactory.addMethod(test, method, 0, 0);
        testFactory.addMethod(test, method, 3, 0);
        String code = test.toCode();
        System.out.println(code);

        // With object reuse being 0, there should be no new instance of this object
        assertEquals(4, test.size());
        assertTrue(code.contains("factoryExample0.testByte(byte0, byte0)"));
        assertFalse(code.contains("factoryExample1"));
    }

    @Test
    public void testObjectDependencyNoReuse() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestFactory testFactory = TestFactory.getInstance();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericMethod method = new GenericMethod(sut.getMethod("testByte", byte.class, byte.class), sut);
        DefaultTestCase test = new DefaultTestCase();
        Properties.PRIMITIVE_REUSE_PROBABILITY = 0.0;
        Properties.OBJECT_REUSE_PROBABILITY = 0.0;
        testFactory.addMethod(test, method, 0, 0);
        testFactory.reset();
        testFactory.addMethod(test, method, 4, 0);
        String code = test.toCode();
        System.out.println(code);

        // With object reuse being 0, there should be no new instance of this object
        assertEquals(8, test.size());
        assertTrue(code.contains("factoryExample0.testByte(byte0, byte1)"));
        // byte2 is the first return value
        assertTrue(code.contains("factoryExample1.testByte(byte3, byte4"));
    }

    @Test
    public void testStaticMethod() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestFactory testFactory = TestFactory.getInstance();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericMethod method = new GenericMethod(sut.getMethod("testStatic"), sut);
        DefaultTestCase test = new DefaultTestCase();
        testFactory.addMethod(test, method, 0, 0);
        assertEquals(1, test.size());
        testFactory.reset();

        testFactory.addMethod(test, method, 1, 0);
        assertEquals(2, test.size());
        String code = test.toCode();
        System.out.println(code);

        // With object reuse being 0, there should be no new instance of this object
        assertTrue(code.contains("FactoryExample.testStatic()"));
        // No instance
        assertFalse(code.contains("FactoryExample0"));
    }

    @Test
    public void testMethodFor() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestFactory testFactory = TestFactory.getInstance();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericConstructor constructor = new GenericConstructor(sut.getConstructor(), sut);
        GenericMethod method = new GenericMethod(sut.getMethod("testByte", byte.class, byte.class), sut);
        DefaultTestCase test = new DefaultTestCase();
        Properties.PRIMITIVE_REUSE_PROBABILITY = 0.0;
        Properties.OBJECT_REUSE_PROBABILITY = 0.0;
        VariableReference var1 = testFactory.addConstructor(test, constructor, 0, 0);
        testFactory.reset();
        VariableReference var2 = testFactory.addConstructor(test, constructor, 1, 0);
        testFactory.addMethodFor(test, var1, method, 2);
        testFactory.reset();
        testFactory.addMethodFor(test, var2, method, 3);
        String code = test.toCode();
        System.out.println(code);

        assertEquals(8, test.size());
        assertTrue(code.contains("factoryExample0.testByte"));
        // byte2 is the first return value
        assertTrue(code.contains("factoryExample1.testByte"));
    }

    @Test(expected = ConstructionFailedException.class)
    public void testMethodForWrongPosition() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestFactory testFactory = TestFactory.getInstance();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericConstructor constructor = new GenericConstructor(sut.getConstructor(), sut);
        GenericMethod method = new GenericMethod(sut.getMethod("testByte", byte.class, byte.class), sut);
        DefaultTestCase test = new DefaultTestCase();
        Properties.PRIMITIVE_REUSE_PROBABILITY = 0.0;
        Properties.OBJECT_REUSE_PROBABILITY = 0.0;
        VariableReference var1 = testFactory.addConstructor(test, constructor, 0, 0);
        testFactory.reset();
        testFactory.addMethodFor(test, var1, method, 0);
    }

    @Test
    public void testAddConstructor() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, ClassNotFoundException {
        TestFactory testFactory = TestFactory.getInstance();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericConstructor method = new GenericConstructor(sut.getConstructor(), sut);
        DefaultTestCase test = new DefaultTestCase();
        Properties.PRIMITIVE_REUSE_PROBABILITY = 0.0;
        Properties.OBJECT_REUSE_PROBABILITY = 0.0;
        testFactory.addConstructor(test, method, 0, 0);
        testFactory.reset();
        assertEquals(1, test.size());
        testFactory.addConstructor(test, method, 0, 0);
        assertEquals(2, test.size());
        String code = test.toCode();
        System.out.println(code);

        assertTrue(code.contains("factoryExample0"));
        assertTrue(code.contains("factoryExample1"));
    }

    @Test
    public void testAddField() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException {
        TestFactory testFactory = TestFactory.getInstance();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericField field = new GenericField(sut.getField("setMe"), sut);
        DefaultTestCase test = new DefaultTestCase();
        testFactory.addField(test, field, 0, 0);
        assertEquals(2, test.size());
        String code = test.toCode();
        System.out.println(code);

        assertTrue(code.contains("factoryExample0.setMe"));
        assertFalse(code.contains("factoryExample1"));
    }

    @Test
    public void testAddFieldReuse() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException {
        TestFactory testFactory = TestFactory.getInstance();

        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericField field = new GenericField(sut.getField("setMe"), sut);
        DefaultTestCase test = new DefaultTestCase();
        testFactory.addField(test, field, 0, 0);
        assertEquals(2, test.size());

        Properties.PRIMITIVE_REUSE_PROBABILITY = 1.0;
        Properties.OBJECT_REUSE_PROBABILITY = 1.0;
        testFactory.reset();
        testFactory.addField(test, field, 2, 0);
        assertEquals(3, test.size());

        String code = test.toCode();
        System.out.println(code);

        assertTrue(code.contains("factoryExample0.setMe"));
        assertFalse(code.contains("factoryExample1"));
    }

    @Test
    public void testAddFieldNoreuse() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException {
        TestFactory testFactory = TestFactory.getInstance();

        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericField field = new GenericField(sut.getField("setMe"), sut);
        DefaultTestCase test = new DefaultTestCase();
        testFactory.addField(test, field, 0, 0);
        assertEquals(2, test.size());

        testFactory.reset();
        Properties.PRIMITIVE_REUSE_PROBABILITY = 0.0;
        Properties.OBJECT_REUSE_PROBABILITY = 0.0;
        testFactory.addField(test, field, 2, 0);
        System.out.println(test.toCode());
        assertEquals(4, test.size());

        String code = test.toCode();
        System.out.println(code);

        assertTrue(code.contains("factoryExample0.setMe"));
        assertTrue(code.contains("factoryExample1.setMe"));
        assertFalse(code.contains("factoryExample2"));
    }

    @Test
    public void testFieldFor() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, ClassNotFoundException, NoSuchFieldException {
        TestFactory testFactory = TestFactory.getInstance();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericConstructor constructor = new GenericConstructor(sut.getConstructor(), sut);
        GenericField field = new GenericField(sut.getField("setMe"), sut);
        DefaultTestCase test = new DefaultTestCase();
        Properties.PRIMITIVE_REUSE_PROBABILITY = 0.0;
        Properties.OBJECT_REUSE_PROBABILITY = 0.0;
        VariableReference var1 = testFactory.addConstructor(test, constructor, 0, 0);
        testFactory.reset();
        VariableReference var2 = testFactory.addConstructor(test, constructor, 1, 0);
        testFactory.addFieldFor(test, var1, field, 2);
        testFactory.reset();
        testFactory.addFieldFor(test, var2, field, 3);
        String code = test.toCode();
        System.out.println(code);

        assertEquals(6, test.size());
        assertTrue(code.contains("factoryExample0.setMe"));
        // byte2 is the first return value
        assertTrue(code.contains("factoryExample1.setMe"));
    }

    @Test(expected = ConstructionFailedException.class)
    public void testFieldForWrongPosition() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, ClassNotFoundException, NoSuchFieldException {
        TestFactory testFactory = TestFactory.getInstance();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericConstructor constructor = new GenericConstructor(sut.getConstructor(), sut);
        GenericField field = new GenericField(sut.getField("setMe"), sut);
        DefaultTestCase test = new DefaultTestCase();
        Properties.PRIMITIVE_REUSE_PROBABILITY = 0.0;
        Properties.OBJECT_REUSE_PROBABILITY = 0.0;
        VariableReference var1 = testFactory.addConstructor(test, constructor, 0, 0);
        testFactory.reset();
        testFactory.addFieldFor(test, var1, field, 0);
    }

    @Test
    public void testAddFieldAssignment() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException {
        TestFactory testFactory = TestFactory.getInstance();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericField field = new GenericField(sut.getField("setMe"), sut);
        DefaultTestCase test = new DefaultTestCase();
        testFactory.addFieldAssignment(test, field, 0, 0);
        assertEquals(3, test.size());
        String code = test.toCode();
        System.out.println(code);

        assertTrue(code.contains("factoryExample0.setMe"));
        assertFalse(code.contains("factoryExample1"));
    }

    @Test
    public void testAddFieldAssignmentReuse() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException {
        TestFactory testFactory = TestFactory.getInstance();

        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericField field = new GenericField(sut.getField("setMe"), sut);
        DefaultTestCase test = new DefaultTestCase();
        testFactory.addFieldAssignment(test, field, 0, 0);
        assertEquals(3, test.size());

        Properties.PRIMITIVE_REUSE_PROBABILITY = 1.0;
        Properties.OBJECT_REUSE_PROBABILITY = 1.0;
        testFactory.reset();
        testFactory.addFieldAssignment(test, field, 3, 0);
        assertEquals(4, test.size());

        String code = test.toCode();
        System.out.println(code);

        assertTrue(code.contains("factoryExample0.setMe"));
        assertFalse(code.contains("factoryExample1"));
    }

    @Test
    public void testAddFieldAssignmentNoreuse() throws ConstructionFailedException,
            NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException {
        TestFactory testFactory = TestFactory.getInstance();

        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        GenericField field = new GenericField(sut.getField("setMe"), sut);
        DefaultTestCase test = new DefaultTestCase();
        testFactory.addFieldAssignment(test, field, 0, 0);
        assertEquals(3, test.size());

        testFactory.reset();
        Properties.PRIMITIVE_REUSE_PROBABILITY = 0.0;
        Properties.OBJECT_REUSE_PROBABILITY = 0.0;
        testFactory.addFieldAssignment(test, field, 3, 0);
        assertEquals(6, test.size());

        String code = test.toCode();
        System.out.println(code);

        assertTrue(code.contains("factoryExample0.setMe"));
        assertTrue(code.contains("factoryExample1.setMe"));
        assertFalse(code.contains("factoryExample2"));
    }

    @Test
    public void testGetCandidatesForReuse() throws ClassNotFoundException, NoSuchFieldException, ConstructionFailedException, NoSuchMethodException {
        TestFactory testFactory = TestFactory.getInstance();
        Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(FactoryExample.class.getCanonicalName());

        Properties.PRIMITIVE_REUSE_PROBABILITY = 1.0;
        Properties.OBJECT_REUSE_PROBABILITY = 1.0;

        DefaultTestCase test = new DefaultTestCase();
        GenericConstructor constructor = new GenericConstructor(sut.getConstructor(), sut);
        VariableReference var1 = testFactory.addConstructor(test, constructor, 0, 0);
        test.addStatement(new CharPrimitiveStatement(test, '-'));
        GenericMethod method = new GenericMethod(sut.getMethod("testInt", int.class), sut);
        testFactory.addMethodFor(test, var1, method, 2);

        MethodStatement stmt = (MethodStatement) test.getStatement(test.size() - 1);
        VariableReference var = stmt.getParameterReferences().get(0);
        assertNotEquals("Char should not be passed as Integer", var.getType(), char.class);
        assertEquals("Incorrect test size", 4, test.size());

    }
}
