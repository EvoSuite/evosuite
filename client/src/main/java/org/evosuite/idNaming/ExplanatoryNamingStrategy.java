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

import org.evosuite.testcase.ImportsTestCodeVisitor;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jose Rojas
 *
 */
public class ExplanatoryNamingStrategy extends AbstractVariableNamingStrategy{

    // mapping from test case to variable names
    protected Map<TestCase,Map<VariableReference,String>> varNames = new HashMap<>();

	public ExplanatoryNamingStrategy(ImportsTestCodeVisitor itv) {
		super(itv);
	}

    @Override
    public String getArrayReferenceName(TestCase testCase, ArrayReference var) {
        return getVariableName(testCase, var);
    }

    /**
     * Returns name for input variable reference {@code var} in test case {@code tc}
     * @param tc test case
     * @param var variable reference
     * @return a {@link String} object representing the variable reference name
     */
    public String getVariableName(TestCase tc, VariableReference var) {
        if (!varNames.containsKey(tc)) {
	        VariableNamesTestVisitor visitor = new VariableNamesTestVisitor();
	        tc.accept(visitor);
	        varNames.put(tc, visitor.getAllVariableNames().get(tc));
        }
	    return varNames.get(tc).get(var);
    }

    private void printVarNames() {
        System.out.println("FINAL NAMES MAPPING");
        String format = "%-5s| %-10s| %s\n";
        System.out.printf(format, "test", "varRef", "name");
        for (Map.Entry<TestCase,Map<VariableReference,String>> entry : varNames.entrySet()) {
            TestCase t = entry.getKey();
            for (Map.Entry<VariableReference,String> varEntry : entry.getValue().entrySet()) {
                VariableReference var = varEntry.getKey();
                System.out.printf(format, t.getID(), var, varEntry.getValue());
            }

        }
    }
}
