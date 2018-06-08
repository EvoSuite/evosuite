package org.evosuite.instrumentation.error;

import com.examples.with.different.packagename.errorbranch.*;
import org.evosuite.Properties;
import org.junit.Ignore;
import org.junit.Test;

public class DequeInstrumentationSystemTest extends AbstractErrorBranchTest {

    @Test
    public void testDequePopWithOutErrorBranches() {

        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.DEQUE};
        checkErrorBranches(DequePop.class, 2, 0, 2, 0);
    }

    @Test
    public void testDequePopWithErrorBranches() {

        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.DEQUE};
        checkErrorBranches(DequePop.class, 2, 2, 2, 2);
    }

    @Test
    public void testDequeGetFirstWithOutErrorBranches() {

        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.DEQUE};
        checkErrorBranches(DequeGetFirst.class, 2, 0, 2, 0);
    }

    @Test
    public void testDequeGetFirstWithErrorBranches() {

        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.DEQUE};
        checkErrorBranches(DequeGetFirst.class, 2, 2, 2, 2);
    }

    @Test
    public void testDequeRemoveFirstWithOutErrorBranches() {

        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.DEQUE};
        checkErrorBranches(DequeRemoveFirst.class, 2, 0, 2, 0);
    }

    @Test
    public void testDequeRemoveFirstWithErrorBranches() {

        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.DEQUE};
        checkErrorBranches(DequeRemoveFirst.class, 2, 2, 2, 2);
    }

    @Test
    public void testDequeRemoveWithOutErrorBranches() {

        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.QUEUE};
        checkErrorBranches(DequeRemove.class, 2, 0, 2, 0);
    }

    @Test
    public void testDequeRemoveWithErrorBranches() {

        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.QUEUE};
        checkErrorBranches(DequeRemove.class, 2, 2, 2, 2);
    }

    @Test
    public void testDequeElementWithOutErrorBranches() {

        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.QUEUE};
        checkErrorBranches(DequeElement.class, 2, 0, 2, 0);
    }

    @Test
    public void testDequeElementWithErrorBranches() {

        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.QUEUE};
        checkErrorBranches(DequeElement.class, 2, 2, 2, 2);
    }
}
