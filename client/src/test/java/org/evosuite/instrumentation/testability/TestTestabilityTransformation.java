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
package org.evosuite.instrumentation.testability;

import com.examples.with.different.packagename.FlagExample1;
import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class TestTestabilityTransformation {

    private static final ClassLoader defaultClassloader = TestTestabilityTransformation.class.getClassLoader();
    private static final ClassLoader instrumentingClassloader = new TestabilityTransformationClassLoader();

    // TODO: Not yet working

    @BeforeClass
    public static void init() {
        String cp = System.getProperty("user.dir") + "/target/test-classes";
        ClassPathHandler.getInstance().addElementToTargetProjectClassPath(cp);
    }

    @Test
    public void testSimpleFlag() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, SecurityException, NoSuchMethodException,
            IllegalArgumentException, InvocationTargetException {

        Properties.TARGET_CLASS = FlagExample1.class.getCanonicalName();

        Class<?> originalClass = defaultClassloader.loadClass(FlagExample1.class.getCanonicalName());
        Class<?> instrumentedClass = instrumentingClassloader.loadClass(FlagExample1.class.getCanonicalName());

        Object originalInstance = originalClass.newInstance();
        Object instrumentedInstance = instrumentedClass.newInstance();

        Method originalMethod = originalClass.getMethod("testMe",
                new Class<?>[]{int.class});
        Method instrumentedMethod = instrumentedClass.getMethod("testMe",
                new Class<?>[]{int.class});

        boolean originalResult = (Boolean) originalMethod.invoke(originalInstance, 0);
        boolean instrumentedResult = ((Integer) instrumentedMethod.invoke(instrumentedInstance,
                0)) > 0;
        assertEquals(originalResult, instrumentedResult);
    }
}
