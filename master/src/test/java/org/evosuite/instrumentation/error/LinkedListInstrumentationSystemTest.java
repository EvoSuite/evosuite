/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.instrumentation.error;

import com.examples.with.different.packagename.errorbranch.LinkedListAccess;
import com.examples.with.different.packagename.errorbranch.LinkedListAccessIndex;
import org.evosuite.Properties;
import org.junit.Test;

public class LinkedListInstrumentationSystemTest extends AbstractErrorBranchTest {

    @Test
    public void testLinkedListWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(LinkedListAccess.class, 3, 0, 3, 0);
    }

    @Test
    public void testLinkedListWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(LinkedListAccess.class, 3, 2, 3, 2);
    }

    @Test
    public void testLinkedListIndexWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(LinkedListAccessIndex.class, 3, 0, 3, 0);
    }

    @Test
    public void testLinkedListIndexWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.LIST};
        checkErrorBranches(LinkedListAccessIndex.class, 3, 4, 3, 4);
    }

}
