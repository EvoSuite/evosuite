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
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.variable.VariableReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jose Rojas
 *
 */
public class ExplanatoryNamingStrategy implements VariableNamingStrategy{

    private static ExplanatoryNamingStrategy instance = null;

    // mapping from test case to variable names
    protected Map<TestCase,Map<VariableReference,String>> varNames = new HashMap<>();

    /**
     * Getter for the field {@code instance}
     *
     * @return a {@link org.evosuite.idNaming.ExplanatoryNamingStrategy}
     *         object.
     */
    public static synchronized ExplanatoryNamingStrategy getInstance() {
        if (instance == null)
            instance = new ExplanatoryNamingStrategy();
        return instance;
    }

    /**
     * Returns name for input variable reference {@code var} in test case {@code tc}
     * @param tc test case
     * @param var variable reference
     * @return a {@link String} object representing the variable reference name
     */
    public String getVariableName(TestCase tc, VariableReference var) {
	    ExplanatoryNamingStrategy generator = getInstance();
        if (generator.varNames.containsKey(tc))
            return generator.varNames.get(tc).get(var);
        else
            return null;
    }

    public static void execute(List<TestCase> testCases, List<ExecutionResult> results) {
	    ExplanatoryNamingStrategy generator = getInstance();

        VariableNamesTestVisitor visitor = new VariableNamesTestVisitor();
        for (int testIdx = 0; testIdx < testCases.size(); testIdx++) {
            TestCase test = testCases.get(testIdx);
            test.accept(visitor);
        }
        generator.varNames = visitor.getAllVariableNames();
        visitor.printAll();
        printVarNames();
    }

    private static void printVarNames() {
	    ExplanatoryNamingStrategy generator = getInstance();
        System.out.println("FINAL NAMES MAPPING");
        String format = "%-5s| %-10s| %s\n";
        System.out.printf(format, "test", "varRef", "name");
        for (Map.Entry<TestCase,Map<VariableReference,String>> entry : generator.varNames.entrySet()) {
            TestCase t = entry.getKey();
            for (Map.Entry<VariableReference,String> varEntry : entry.getValue().entrySet()) {
                VariableReference var = varEntry.getKey();
                System.out.printf(format, t.getID(), var, varEntry.getValue());
            }

        }
    }
}
