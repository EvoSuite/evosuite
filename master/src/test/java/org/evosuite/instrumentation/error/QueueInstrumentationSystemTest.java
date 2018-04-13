package org.evosuite.instrumentation.error;

import com.examples.with.different.packagename.errorbranch.QueueAccess;
import com.examples.with.different.packagename.errorbranch.QueueRemove;
import org.evosuite.Properties;
import org.junit.Test;

public class QueueInstrumentationSystemTest extends AbstractErrorBranchTest {

    @Test
    public void testQueueElementOperationWithOutErrorBranches() {

        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.QUEUE};
        checkErrorBranches(QueueAccess.class, 2, 0, 2, 0);
    }

    @Test
    public void testQueueElementOperationWithErrorBranches() {

        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.QUEUE};
        checkErrorBranches(QueueAccess.class, 2, 2, 2, 2);
    }

    @Test
    public void testQueueRemoveOperationWithoutErrorBranches() {

        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.QUEUE};
        checkErrorBranches(QueueRemove.class, 2, 0, 2, 0);

    }

    @Test
    public void testQueueRemoveOperationWithErrorBranches() {

        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.QUEUE};
        checkErrorBranches(QueueRemove.class, 2, 2, 2, 2);

    }
}
