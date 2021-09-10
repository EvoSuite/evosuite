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

import com.examples.with.different.packagename.solver.MazeClient;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestMazeClient {

    @Test
    public void testMazeSolution() {

        int ret_val = MazeClient.walk('s', 's', 's', 's', 'd', 'd', 'd', 'd', 'w', 'w', 'a', 'a', 'w',
                'w', 'd', 'd', 'd', 'd', 's', 's', 's', 's', 'd', 'd', 'w', 'w', 'w', 'w', (char) 0,
                (char) 0, 28);
        assertEquals(1, ret_val);
    }


}
