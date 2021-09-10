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
