package org.evosuite.testcase.fm;

import com.examples.with.different.packagename.fm.*;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 09/08/15.
 */
public class SimpleFM_SystemTest extends SystemTest{

    @Test
    public void testSimpleNonFinal(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        do100percentLineTest(SimpleFM_nonFinal.class);
    }

    @Test
    public void testSimpleFinalClass(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        do100percentLineTest(SimpleFM_finalClass.class);
    }

    @Test
    public void testSimpleFinalMethod(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        do100percentLineTest(SimpleFM_finalMethod.class);
    }

    @Test
    public void testSimpleBoolean(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator

        do100percentLineTest(SimpleFM_Boolean.class);
    }

    @Test
    public void testSimpleDoubleMock(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator

        do100percentLineTest(SimpleFM_DoubleMock.class);
    }


    @Test
    public void testSimpleInt(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator

        do100percentLineTest(SimpleFM_Int.class);
    }

    @Test
    public void testSimpleString(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator

        do100percentLineTest(SimpleFM_String.class);
    }

    @Test
    public void testSimpleDependency(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator
        Properties.P_REFLECTION_ON_PRIVATE = 0;

        do100percentLineTest(SimpleFM_Dependency.class);
    }
}
