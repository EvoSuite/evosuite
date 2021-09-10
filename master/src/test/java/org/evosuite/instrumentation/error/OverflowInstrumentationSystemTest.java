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

import org.evosuite.Properties;
import org.junit.Test;

import com.examples.with.different.packagename.errorbranch.IntAddOverflow;
import com.examples.with.different.packagename.errorbranch.IntDivOverflow;
import com.examples.with.different.packagename.errorbranch.IntMulOverflow;
import com.examples.with.different.packagename.errorbranch.IntSubOverflow;

public class OverflowInstrumentationSystemTest extends AbstractErrorBranchTest {

    @Test
    public void testIntAddOverflowWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.OVERFLOW};
        checkErrorBranches(IntAddOverflow.class, 3, 0, 3, 0);
    }

    @Test
    public void testIntAddOverflowWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.OVERFLOW};
        checkErrorBranches(IntAddOverflow.class, 3, 4, 3, 4);
    }

    @Test
    public void testIntSubOverflowWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.OVERFLOW};
        checkErrorBranches(IntSubOverflow.class, 3, 0, 3, 0);
    }

    @Test
    public void testIntSubOverflowWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.OVERFLOW};
        checkErrorBranches(IntSubOverflow.class, 3, 4, 3, 4);
    }

    @Test
    public void testIntDivOverflowWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.OVERFLOW};
        checkErrorBranches(IntDivOverflow.class, 3, 0, 3, 0);
    }

    @Test
    public void testIntDivOverflowWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.OVERFLOW};
        checkErrorBranches(IntDivOverflow.class, 3, 2, 3, 2);
    }

    @Test
    public void testMulDivOverflowWithoutErrorBranches() {
        Properties.ERROR_BRANCHES = false;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.OVERFLOW};
        checkErrorBranches(IntMulOverflow.class, 3, 0, 3, 0);
    }

    @Test
    public void testIntMulOverflowWithErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        Properties.ERROR_INSTRUMENTATION = new Properties.ErrorInstrumentation[]{Properties.ErrorInstrumentation.OVERFLOW};
        checkErrorBranches(IntMulOverflow.class, 3, 4, 3, 4);
    }

}
