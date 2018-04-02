package org.evosuite.instrumentation.error;

import com.examples.with.different.packagename.errorbranch.*;
import org.evosuite.Properties;
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
//        Not sure why realBranches is 3 and instrumentedBranches is 0
        checkErrorBranches(DequePop.class, 3, 0, 1, 0);
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
//        Not sure why realBranches is 3 and instrumentedBranches is 0
        checkErrorBranches(DequeGetFirst.class, 3, 0, 1, 0);
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
//        Not sure why realBranches is 3 and instrumentedBranches is 0
        checkErrorBranches(DequeRemoveFirst.class, 3, 0, 1, 0);
    }

    @Test
    public void testDequeRemoveWithOutErrorBranches() {

        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.DEQUE};
        checkErrorBranches(DequeRemove.class, 2, 0, 2, 0);
    }

    @Test
    public void testDequeRemoveWithErrorBranches() {

        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.DEQUE};
//        Not sure why realBranches is 3 and instrumentedBranches is 0
        checkErrorBranches(DequeRemove.class, 3, 0, 1, 0);
    }

    @Test
    public void testDequeElementWithOutErrorBranches() {

        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.DEQUE};
        checkErrorBranches(DequeElement.class, 2, 0, 2, 0);
    }

    @Test
    public void testDequeElementWithErrorBranches() {

        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.DEQUE};
//        Not sure why realBranches is 3 and instrumentedBranches is 0
        checkErrorBranches(DequeElement.class, 3, 0, 1, 0);
    }
}