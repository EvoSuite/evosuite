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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.seeding.ObjectPool;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.GenericClassImpl;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.examples.with.different.packagename.pool.ClassDependingOnExceptionClass;
import com.examples.with.different.packagename.pool.DependencyClass;
import com.examples.with.different.packagename.pool.DependencyClassWithException;
import com.examples.with.different.packagename.pool.DependencySubClass;
import com.examples.with.different.packagename.pool.OtherClass;

public class PoolSystemTest extends SystemTestBase {

    private String pools = "";

    private double pPool = 0.0;

    private long budget = 0;

    @Before
    public void storeProperties() {
        pools = Properties.OBJECT_POOLS;
        pPool = Properties.P_OBJECT_POOL;
        budget = Properties.SEARCH_BUDGET;
    }

    @After
    public void restoreProperties() {
        Properties.OBJECT_POOLS = pools;
        Properties.P_OBJECT_POOL = pPool;
        Properties.SEARCH_BUDGET = budget;
    }

    @Test
    public void testPoolDependency() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = DependencyClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 100000;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    @Test
    public void testPool() throws IOException {
        File f = File.createTempFile("EvoSuiteTestPool", null, FileUtils.getTempDirectory());
        String filename = f.getAbsolutePath();
        f.delete();
        System.out.println(filename);


        EvoSuite evosuite = new EvoSuite();

        String targetClass = DependencyClass.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 100000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        ObjectPool pool = ObjectPool.getPoolFromTestSuite(best);
        pool.writePool(filename);
        System.out.println("EvolvedTestSuite:\n" + best);
        resetStaticVariables();
        setDefaultPropertiesForTestCases();

        targetClass = OtherClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_OBJECT_POOL = 1.0;
        Properties.OBJECT_POOLS = filename;
        Properties.SEARCH_BUDGET = 10000;
        ObjectPoolManager.getInstance().initialisePool();
        //Properties.SEARCH_BUDGET = 50000;

        command = new String[]{"-generateSuite", "-class", targetClass, "-Dobject_pools=" + filename};

        result = evosuite.parseCommandLine(command);
        ga = getGAFromResult(result);
        TestSuiteChromosome best2 = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best2);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best2.getCoverage(), 0.001);
        f = new File(filename);
        f.delete();

    }

    @Ignore
    @Test
    public void testNoPool() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = OtherClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_OBJECT_POOL = 0.0;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertTrue("Expected non-optimal coverage: ", best.getCoverage() < 1.0);
        // Seems to pass now even without pool...
    }

    @Test
    public void testPoolWithSubClass() throws IOException {
        File f = File.createTempFile("EvoSuiteTestPool", null, FileUtils.getTempDirectory());
        String filename = f.getAbsolutePath();
        f.delete();
        System.out.println(filename);

        EvoSuite evosuite = new EvoSuite();

        String targetClass = DependencySubClass.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        // It takes a bit longer to cover the branch here
        Properties.SEARCH_BUDGET = 50000;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        ObjectPool pool = ObjectPool.getPoolFromTestSuite(best);
        pool.writePool(filename);
        System.out.println("EvolvedTestSuite:\n" + best);
        resetStaticVariables();
        setDefaultPropertiesForTestCases();

        targetClass = OtherClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_OBJECT_POOL = 1.0;
        Properties.OBJECT_POOLS = filename;
        ObjectPoolManager.getInstance().initialisePool();

        command = new String[]{"-generateSuite", "-class", targetClass, "-Dobject_pools=" + filename};

        result = evosuite.parseCommandLine(command);

        ga = getGAFromResult(result);
        best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
        f = new File(filename);
        f.delete();

    }

    @Test
    public void testPoolWithException() throws IOException, NoSuchMethodException, SecurityException {
        File f = File.createTempFile("EvoSuiteTestPool", null, FileUtils.getTempDirectory());
        String filename = f.getAbsolutePath();
        f.delete();
        System.out.println(filename);

        EvoSuite evosuite = new EvoSuite();

        String targetClass = DependencyClassWithException.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.getTargetClassAndDontInitialise();
        TestCase test = new DefaultTestCase();
        VariableReference instance = test.addStatement(new ConstructorStatement(test, new GenericConstructor(DependencyClassWithException.class.getConstructors()[0], DependencyClassWithException.class),
                new ArrayList<>()));
        VariableReference int42 = test.addStatement(new IntPrimitiveStatement(test, 42));
        GenericMethod foo = new GenericMethod(DependencyClassWithException.class.getMethod("foo", int.class), DependencyClassWithException.class);
        test.addStatement(new MethodStatement(test, foo, instance, Arrays.asList(int42)));
        test.addStatement(new MethodStatement(test, foo, instance, Arrays.asList(int42)));
        test.addStatement(new MethodStatement(test, foo, instance, Arrays.asList(int42)));
        test.addStatement(new MethodStatement(test, foo, instance, Arrays.asList(int42)));
        test.addStatement(new MethodStatement(test, foo, instance, Arrays.asList(int42)));
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
        TestSuiteChromosome best = new TestSuiteChromosome();
        best.addTest(test);
        ObjectPool pool = new ObjectPool();
        pool.addSequence(GenericClassFactory.get(DependencyClassWithException.class), test);
        pool.writePool(filename);
        System.out.println("EvolvedTestSuite:\n" + best);

        resetStaticVariables();
        setDefaultPropertiesForTestCases();

        targetClass = ClassDependingOnExceptionClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_OBJECT_POOL = 0.8;
        Properties.OBJECT_POOLS = filename;
        ObjectPoolManager.getInstance().initialisePool();
        //Properties.SEARCH_BUDGET = 50000;

        command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
        f = new File(filename);
        f.delete();

    }

    @Ignore // Can now pass even without pool...
    @Test
    public void testNoPoolWithException() throws IOException {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassDependingOnExceptionClass.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.P_OBJECT_POOL = 0.0;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertTrue("Non-optimal coverage: ", best.getCoverage() < 1.0);

    }
}
