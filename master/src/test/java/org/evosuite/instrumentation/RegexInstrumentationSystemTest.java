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
package org.evosuite.instrumentation;


import java.lang.reflect.Method;

import com.examples.with.different.packagename.RegexNullExample;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.instrumentation.testability.StringHelper;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.TrivialForDynamicSeedingRegex;

public class RegexInstrumentationSystemTest extends SystemTestBase {

    @Test
    public void testTrivialForDynamicSeedingRegex() throws Throwable {
        //first check if it works with Java directly
        String matching = "-@0.AA";
        Assert.assertTrue(TrivialForDynamicSeedingRegex.foo(matching));

        //now check the distance being 0
        Assert.assertEquals(0.0, org.evosuite.utils.RegexDistanceUtils.getDistanceTailoredForStringAVM(matching, TrivialForDynamicSeedingRegex.REGEX), 0.0);
        Assert.assertEquals(0.0, org.evosuite.instrumentation.RegexDistance.getDistance(matching, TrivialForDynamicSeedingRegex.REGEX), 0.0);

        //now check that what done in the instrumentation return a positive value
        int comp = StringHelper.StringMatches(matching, TrivialForDynamicSeedingRegex.REGEX);
        Assert.assertTrue("" + comp, comp > 0);

        //actually load the class, and see if it works
        String targetClass = TrivialForDynamicSeedingRegex.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        InstrumentingClassLoader loader = new InstrumentingClassLoader();
        Class<?> sut = loader.loadClass(targetClass);
        Method m = sut.getMethod("foo", String.class);
        Boolean b = (Boolean) m.invoke(null, matching);
        Assert.assertTrue(b);
    }

    @Test
    public void testRegexThrowsSameExceptionOnNull() throws Throwable {
        //first check if it works with Java directly
        String[] stringArray0 = new String[6];
        stringArray0[0] = "Kge^";
        stringArray0[1] = "--A=";

        // Method returns false if NPE occurred
        Assert.assertFalse(RegexNullExample.testMe(stringArray0));

        //actually load the class, and see if it works
        String targetClass = RegexNullExample.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        InstrumentingClassLoader loader = new InstrumentingClassLoader();
        Class<?> sut = loader.loadClass(targetClass);
        Method m = sut.getMethod("testMe", String[].class);
        Boolean b = (Boolean) m.invoke(null, new Object[]{stringArray0});
        Assert.assertFalse(b);

    }
}
