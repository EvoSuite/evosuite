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
package org.evosuite.symbolic.solver;

import com.examples.with.different.packagename.solver.Maze;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestMaze {

    @Test
    public void testMazeSolution() {
        char[] solution = "ssssddddwwaawwddddssssddwwww".toCharArray();
        int ret_val = Maze.walk(solution);
        assertEquals(1, ret_val);
    }

    @Test
    public void testIncompleteSolution() {
        char[] solution = "ssssddddwwaawwddddssssdd".toCharArray();
        int ret_val = Maze.walk(solution);
        assertEquals(3, ret_val);
    }

    @Test
    public void testCrashAgainstWall() {
        char[] solution = "sssss".toCharArray();
        int ret_val = Maze.walk(solution);
        assertEquals(2, ret_val);
    }

    @Test
    public void testInvalidMove() {
        char[] solution = "ssssddddwwaawwddddssssdX".toCharArray();
        int ret_val = Maze.walk(solution);
        assertEquals(0, ret_val);
    }

}
