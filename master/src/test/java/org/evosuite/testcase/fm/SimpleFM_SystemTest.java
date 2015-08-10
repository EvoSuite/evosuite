package org.evosuite.testcase.fm;

import com.examples.with.different.packagename.fm.SimpleFM_Boolean;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 09/08/15.
 */
public class SimpleFM_SystemTest extends SystemTest{

    @Test
    public void testSimpleBoolean(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator

        do100percentLineTest(SimpleFM_Boolean.class);
    }
}
