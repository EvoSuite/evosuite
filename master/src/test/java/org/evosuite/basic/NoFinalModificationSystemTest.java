package org.evosuite.basic;

import com.examples.with.different.packagename.InfeasibleFinalInt;
import com.examples.with.different.packagename.InfeasibleFinalString;
import org.evosuite.SystemTestBase;
import org.junit.Test;

public class NoFinalModificationSystemTest extends SystemTestBase {

    @Test
    public void testInt(){
        doNonOptimalLineTest(InfeasibleFinalInt.class);
    }

    @Test
    public void testString(){
        doNonOptimalLineTest(InfeasibleFinalString.class);
    }


}
