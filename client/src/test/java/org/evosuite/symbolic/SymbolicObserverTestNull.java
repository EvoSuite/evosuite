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
package org.evosuite.symbolic;

import com.examples.with.different.packagename.concolic.TestCaseNullAssignment;
import org.evosuite.symbolic.dse.ConcolicExecutorImpl;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class SymbolicObserverTestNull {

    @Test
    public void testNullAssignment() throws NoSuchFieldException, SecurityException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference var0 = builder.appendNull(TestCaseNullAssignment.class);
        Field x_field = TestCaseNullAssignment.class.getField("x");
        VariableReference int0 = builder.appendIntPrimitive(10);
        builder.appendAssignment(var0, x_field, int0);
        DefaultTestCase testCase = builder.getDefaultTestCase();
        PathCondition pc = new ConcolicExecutorImpl().execute(testCase);
        List<BranchCondition> branch_conditions = pc.getBranchConditions();

        assertTrue(branch_conditions.isEmpty());
    }
}
