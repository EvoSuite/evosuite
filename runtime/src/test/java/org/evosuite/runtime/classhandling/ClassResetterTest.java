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
package org.evosuite.runtime.classhandling;

import com.examples.with.different.packagename.classhandling.MutableEnum;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.instrumentation.EvoClassLoader;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;


public class ClassResetterTest {


    @Test
    public void testResetOfEnum() throws Exception {

        ClassLoader loader = new EvoClassLoader();
        boolean resetValue = RuntimeSettings.resetStaticState;
        RuntimeSettings.resetStaticState = true;
        ClassResetter.getInstance().setClassLoader(loader);

        String cut = "com.examples.with.different.packagename.classhandling.FooEnum";

        Class<?> klass = loader.loadClass(cut);
        Method m = klass.getDeclaredMethod("check");

        boolean val = false;

        val = (Boolean) m.invoke(null);
        Assert.assertTrue(val);

        ClassResetter.getInstance().reset(cut);

        //make sure that the reset does not create new enum instance values
        val = (Boolean) m.invoke(null);
        RuntimeSettings.resetStaticState = resetValue;
        Assert.assertTrue(val);
    }

    // TODO: We could consider providing a workaround to reset mutable enums.
    @Ignore
    @Test
    public void testResetOfMutableEnum() throws Exception {

        ClassLoader loader = new EvoClassLoader();
        RuntimeSettings.resetStaticState = true;
        ClassResetter.getInstance().setClassLoader(loader);

        String cut = MutableEnum.class.getCanonicalName();


        Class<?> klass = loader.loadClass(cut);
        Object[] enums = klass.getEnumConstants();
        Assert.assertEquals(2, enums.length);
        Method getter = klass.getDeclaredMethod("getLetter");
        Assert.assertEquals("a", getter.invoke(enums[0]));
        Assert.assertEquals("b", getter.invoke(enums[1]));

        Method m = klass.getDeclaredMethod("changeLetter");
        m.invoke(enums[0]);
        Assert.assertEquals("X", getter.invoke(enums[0]));
        Assert.assertEquals("b", getter.invoke(enums[1]));

        ClassResetter.getInstance().reset(cut);

        Assert.assertEquals("a", getter.invoke(enums[0]));
        Assert.assertEquals("b", getter.invoke(enums[1]));
    }
}
