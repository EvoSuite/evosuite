package org.evosuite.instrumentation.error;

import com.examples.with.different.packagename.errorbranch.*;
import org.evosuite.Properties;
import org.junit.Test;

public class ArrayListInstrumentationSystemTest extends AbstractErrorBranchTest {
    @Test
    public void testArrayListAccessWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(ArrayListAccess.class, 2, 0, 2, 0);

    }

    @Test
    public void testArrayListAccessWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(ArrayListAccess.class, 2, 4, 2, 4);
    }

    @Test
    public void testArrayListSetWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(ArrayListSet.class, 2, 0, 2, 0);

    }

    @Test
    public void testArrayListSetWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(ArrayListSet.class, 2, 4, 2, 4);
    }

    @Test
    public void testArrayListAddWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(ArrayListAdd.class, 2, 0, 2, 0);

    }

    @Test
    public void testArrayListAddWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(ArrayListAdd.class, 2, 4, 2, 4);
    }

    @Test
    public void testArrayListAddAllWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(ArrayListAddAll.class, 2, 0, 2, 0);

    }

    @Test
    public void testArrayListAddAllWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(ArrayListAddAll.class, 2, 4, 2, 4);
    }

    @Test
    public void testArrayListRemoveWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(ArrayListRemove.class, 2, 0, 2, 0);

    }

    @Test
    public void testArrayListRemoveWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(ArrayListRemove.class, 2, 4, 2, 4);
    }

    @Test
    public void testArrayListIteratorWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(ArrayListIterator.class, 2, 0, 2, 0);

    }

    @Test
    public void testArrayListIteratorWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(ArrayListIterator.class, 2, 4, 2, 4);
    }
}
