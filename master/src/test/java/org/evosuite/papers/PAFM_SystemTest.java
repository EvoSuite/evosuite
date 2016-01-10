package org.evosuite.papers;

import com.examples.with.different.packagename.papers.pafm.PAFM;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.junit.Test;

public class PAFM_SystemTest extends SystemTestBase {

    @Test
    public void testPAFM(){
        Properties.P_REFLECTION_ON_PRIVATE = 0.5;
        Properties.REFLECTION_START_PERCENT = 0.3;
        Properties.P_FUNCTIONAL_MOCKING = 0.5;
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.3;
        do100percentLineTest(PAFM.class);
    }
}
