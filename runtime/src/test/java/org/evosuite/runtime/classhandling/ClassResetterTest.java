package org.evosuite.runtime.classhandling;

import com.examples.with.different.packagename.classhandling.FooEnum;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.instrumentation.EvoClassLoader;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;


public class ClassResetterTest {


    @Test
    public void testResetOfEnum() throws Exception{

        ClassLoader loader = new EvoClassLoader();
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
        Assert.assertTrue(val);
    }
}