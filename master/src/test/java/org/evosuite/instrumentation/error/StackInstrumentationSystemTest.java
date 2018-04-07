package org.evosuite.instrumentation.error;

import com.examples.with.different.packagename.errorbranch.StackPeek;
import com.examples.with.different.packagename.errorbranch.StackPop;
import org.evosuite.Properties;
import org.junit.Test;

public class StackInstrumentationSystemTest extends AbstractErrorBranchTest {

    @Test
    public void testStackPeekOperationWithoutErrorBranches() {

        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.STACK};
        checkErrorBranches(StackPeek.class, 2, 0, 2, 0);
    }

    @Test
    public void testStackPeekOperationWithErrorBranches() {

        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.STACK};
        checkErrorBranches(StackPeek.class, 2, 2, 2, 2);
    }

    @Test
    public void testStackPopOperationWithoutErrorBranches() {

        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.STACK};
        checkErrorBranches(StackPop.class, 2, 0, 2, 0);
    }

    @Test
    public void testStackPopOperationWithErrorBranches() {

        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.STACK};
        checkErrorBranches(StackPop.class, 2, 2, 2, 2);
    }
}
