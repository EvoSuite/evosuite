/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.idNaming;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmr on 06/08/15.
 */
public class VariableNamesGenerator {

    private static VariableNamesGenerator instance = null;

    // mapping from test case to *list* of candidate names
    protected Map<TestCase,Map<VariableReference,String>> varNames = new HashMap<TestCase,Map<VariableReference,String>>();

    /**
     * Getter for the field {@code instance}
     *
     * @return a {@link org.evosuite.idNaming.VariableNamesGenerator}
     *         object.
     */
    public static synchronized VariableNamesGenerator getInstance() {
        if (instance == null)
            instance = new VariableNamesGenerator();
        return instance;
    }

    /**
     * Returns name for input variable reference {@code var} in test case {@code tc}
     * @param tc test case
     * @param var variable reference
     * @return a {@link String} object representing the variable reference name
     */
    public static String getVariableName(TestCase tc, VariableReference var) {
        VariableNamesGenerator generator = getInstance();
        if (generator.varNames.containsKey(tc))
            return generator.varNames.get(tc).get(var);
        else
            return null;
    }
}
