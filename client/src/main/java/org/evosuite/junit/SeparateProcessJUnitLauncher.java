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
package org.evosuite.junit;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class SeparateProcessJUnitLauncher {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected result file and at least one test class name");
        }

        String resultFile = args[0];
        Class<?>[] testClasses = new Class<?>[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            testClasses[i - 1] = Class.forName(args[i]);
        }

        JUnitResult result = JUnitExecutor.runJUnit(testClasses);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(resultFile))) {
            out.writeObject(result);
        }
    }
}
