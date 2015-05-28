package org.evosuite.runtime.classhandling;

import org.junit.Assert;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.classhandling.ClassStateSupport;
import org.evosuite.runtime.instrumentation.InstrumentingClassLoader;
import org.junit.Test;

/**
 * Created by arcuri on 1/20/15.
 */
public class ClassStateSupportTest {

    @Test
    public void testInitializeClasses(){


        InstrumentingClassLoader loader = new InstrumentingClassLoader();
        String className = "com.examples.with.different.packagename.classhandling.TimeA";
        //no mocking
        RuntimeSettings.deactivateAllMocking();
        boolean problem = ClassStateSupport.initializeClasses(loader, className);
        Assert.assertFalse(problem);

        //with mocking
        RuntimeSettings.mockJVMNonDeterminism = true;
        className = "com.examples.with.different.packagename.classhandling.TimeB";
        problem = ClassStateSupport.initializeClasses(loader,className);
        Assert.assertFalse(problem);
    }
}
