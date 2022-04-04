/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
