package org.evosuite.instrumentation.error;

import com.examples.with.different.packagename.errorbranch.ArrayListAccess;
import org.evosuite.Properties;
import org.junit.Test;

public class ArrayListInstrumentationSystemTest extends AbstractErrorBranchTest {
    @Test
    public void testArrayListAccessWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.ARRAYLIST};
        checkErrorBranches(ArrayListAccess.class, 2, 0, 2, 0);

    }
    @Test
    public void testArrayListAccessWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.ARRAYLIST};
        // Instrumented Branches should have been 4
        checkErrorBranches(ArrayListAccess.class, 2, 0, 2, 0);
    }

    // ConcurrentModificationException for ArrayList testCase?
}
