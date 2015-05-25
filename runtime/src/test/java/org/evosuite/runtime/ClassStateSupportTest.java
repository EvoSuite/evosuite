package org.evosuite.runtime;

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
        String className = "com.examples.with.different.packagename.agent.TimeA";
        //no mocking
        ClassStateSupport.initializeClasses(loader, className);

        //with mocking
        final boolean df = RuntimeSettings.mockJVMNonDeterminism;
        try {
            RuntimeSettings.mockJVMNonDeterminism = true;
            className = "com.examples.with.different.packagename.agent.TimeB";
            ClassStateSupport.initializeClasses(loader,className);
        }finally{
            RuntimeSettings.mockJVMNonDeterminism = df;
        }
    }
}
