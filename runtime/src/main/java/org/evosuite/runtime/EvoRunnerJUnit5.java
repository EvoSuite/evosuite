/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime;

import org.evosuite.runtime.instrumentation.EvoClassLoader;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class EvoRunnerJUnit5 implements TestInstanceFactory {
    private static final Logger logger = LoggerFactory.getLogger(EvoRunnerJUnit5.class);

    /**
     * Dirty hack, to use with care.
     * In some very special cases, we want to skip agent instrumentation.
     * Still, we would need to use a classloader that will do such instrumentation.
     * This is for example done in -measureCoverage, as we need a more details instrumentation,
     * and, at the same time, we want to avoid a double instrumentation from agent
     */
    public static boolean useAgent = true;

    /**
     * Another dirty hack, to use with care. This is to make useSeparateClassLoader
     * work together with -measureCoverage
     */
    public static boolean useClassLoader = true;

    private final EvoClassLoader evoClassLoader = null;

    public EvoRunnerJUnit5(Class<?> _class) {
        EvoRunnerParameters ep = _class.getAnnotation(EvoRunnerParameters.class);

        if (ep == null)
            throw new IllegalStateException("EvoSuite test class " + _class.getName() + " is not annotated with " +
                    "EvoRunnerParameters");

        RuntimeSettings.resetStaticState = ep.resetStaticState();
        RuntimeSettings.mockJVMNonDeterminism = ep.mockJVMNonDeterminism();
        RuntimeSettings.mockGUI = ep.mockGUI();
        RuntimeSettings.useVFS = ep.useVFS();
        RuntimeSettings.useVNET = ep.useVNET();
        RuntimeSettings.useSeparateClassLoader = ep.separateClassLoader();
        RuntimeSettings.useJEE = ep.useJEE();

        if (RuntimeSettings.useSeparateClassLoader && useClassLoader)
            return;

        if (useAgent) org.evosuite.runtime.agent.InstrumentingAgent.initialize();
        org.evosuite.runtime.agent.InstrumentingAgent.activate();


        try {
            /*
             *  be sure that reflection on "klass" is executed here when
             *  the agent is active
             */
            _class.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            //shouldn't really happen
            logger.error("Failed to initialize test class " + _class.getName());
        }
        org.evosuite.runtime.agent.InstrumentingAgent.deactivate();
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) throws TestInstantiationException {
        Class<?> testClass = factoryContext.getTestClass();
        String name = testClass.getName();
        if (RuntimeSettings.useSeparateClassLoader && useClassLoader) {
            // JUnit does not allow us to instrument the tested class.
            // When this is supported we may implement this.
            logger.error("EvoSuite can't run JUnit 5 tests with a separate ClassLoader");
            throw new TestInstantiationException("Could not instantiate the class under test with a EvoClassLoader");
//            EvoClassLoader classLoader = evoClassLoader == null ? new EvoClassLoader() : evoClassLoader;
//            classLoader.skipInstrumentation(name);
//            Thread.currentThread().setContextClassLoader(classLoader);
//            try {
//                Class<?> evoClass = Class.forName(name, true, classLoader);
//                return evoClass.getConstructor().newInstance();
//            } catch (Exception e) {
//                 throw new TestInstantiationException("Could not instantiate the class under test with the EvoClassLoader",e);
//            }
        } else {
            try {
                return factoryContext.getTestClass().getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new TestInstantiationException("Could not instantiate test class");
            }
        }
    }
}
