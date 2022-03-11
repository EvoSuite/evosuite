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
package org.evosuite.testcase.mutation;

import org.evosuite.testcase.TestCase;

/**
 * This interface must be implemented by all classes that modify test cases via insertion of
 * statements. Intended uses are for the implementation of mutation operators or for the generation
 * of initial populations.
 */
public interface InsertionStrategy {

    /**
     * Inserts one or possibly multiple statements into the given test case {@code test} after the
     * given position {@code lastPosition}. The test case must not be {@code null} but it can be
     * empty.
     * <p>
     * A test case might contain faulty statements, causing an exception when the test case is
     * executed. In such situations, the test case's sequence of statements is only valid up to the
     * first faulty statement. Callers have to specify the position of the last statement known to
     * be valid by supplying an appropriate index {@code lastPosition}.
     * <p>
     * All statements inserted by this method are inserted after {@code lastPosition}. In other
     * words, {@code lastPosition + 1} is considered the insertion point for new statements. If
     * {@code lastPosition + 1 < test.size()}, already existing statements located after the
     * insertion point will be moved towards the end of the sequence as necessary. None of those
     * statements will be deleted or overwritten.
     * <p>
     * If the given test case is empty (as is the case when generating an initial population) the
     * insertion point is assumed to be at index 0, regardless of the given {@code lastPosition}.
     * <p>
     * After a successful insertion, the position of the last known valid statement is updated and
     * returned. This might not necessarily be {@code lastPosition + 1}, e.g., when multiple
     * statements were inserted. If insertion was unsuccessful, a negative number is returned.
     *
     * @param test         the test case in which to insert
     * @param lastPosition the position of the last valid statement in the test case, defining the
     *                     insertion point for new statements
     * @return the updated position of the last valid statement after insertion, or a negative
     * number if insertion failed
     */
    int insertStatement(TestCase test, int lastPosition);
}
