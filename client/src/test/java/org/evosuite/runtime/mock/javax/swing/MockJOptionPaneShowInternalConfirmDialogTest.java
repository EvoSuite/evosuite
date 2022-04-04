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
package org.evosuite.runtime.mock.javax.swing;

import com.examples.with.different.packagename.mock.javax.swing.ShowInternalConfirmDialogExample;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

public class MockJOptionPaneShowInternalConfirmDialogTest {

    private static final String TARGET_CLASS = ShowInternalConfirmDialogExample.class.getCanonicalName();
    private static final boolean DEFAULT_MOCK_GUI = RuntimeSettings.mockGUI;
    private static final boolean DEFAULT_REPLACE_GUI = Properties.REPLACE_GUI;

    @BeforeClass
    public static void init() {
        String cp = System.getProperty("user.dir") + "/target/test-classes";
        ClassPathHandler.getInstance().addElementToTargetProjectClassPath(cp);
    }

    @Before
    public void setUp() {
        Properties.CRITERION = new Properties.Criterion[]{Criterion.BRANCH};
        Properties.TARGET_CLASS = TARGET_CLASS;
        Properties.REPLACE_GUI = true;
        RuntimeSettings.mockGUI = true;
        TestGenerationContext.getInstance().resetContext();
    }

    @After
    public void tearDown() {
        RuntimeSettings.mockGUI = DEFAULT_MOCK_GUI;
        Properties.REPLACE_GUI = DEFAULT_REPLACE_GUI;
        TestGenerationContext.getInstance().resetContext();
    }

    @Test
    public void testShowInternalConfirmDialog() throws Exception {

        TestSuiteChromosome suite = new TestSuiteChromosome();
        InstrumentingClassLoader cl = TestGenerationContext.getInstance().getClassLoaderForSUT();
        TestCase t0 = buildTestCase0(cl);
        TestCase t1 = buildTestCase1(cl);
        suite.addTest(t0);
        suite.addTest(t1);

        BranchCoverageSuiteFitness ff = new BranchCoverageSuiteFitness(cl);
        ff.getFitness(suite);

        Set<TestFitnessFunction> coveredGoals = suite.getCoveredGoals();
        Assert.assertEquals(3, coveredGoals.size());
    }

    private static TestCase buildTestCase1(InstrumentingClassLoader cl)
            throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        TestCaseBuilder builder = new TestCaseBuilder();

        Class<?> clazz = cl.loadClass(TARGET_CLASS);
        Constructor<?> constructor = clazz.getConstructor();
        VariableReference showMessageDialogExample0 = builder.appendConstructor(constructor);

        VariableReference int0 = builder.appendIntPrimitive(0);
        Method showMessageDialogMethod = clazz.getMethod("showInternalConfirmDialog", int.class);
        builder.appendMethod(showMessageDialogExample0, showMessageDialogMethod, int0);

        return builder.getDefaultTestCase();
    }

    private static TestCase buildTestCase0(InstrumentingClassLoader cl)
            throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        TestCaseBuilder builder = new TestCaseBuilder();

        Class<?> clazz = cl.loadClass(TARGET_CLASS);
        Constructor<?> constructor = clazz.getConstructor();
        VariableReference showMessageDialogExample0 = builder.appendConstructor(constructor);

        VariableReference int0 = builder.appendIntPrimitive(1);
        Method showMessageDialogMethod = clazz.getMethod("showInternalConfirmDialog", int.class);
        builder.appendMethod(showMessageDialogExample0, showMessageDialogMethod, int0);

        return builder.getDefaultTestCase();
    }


}
