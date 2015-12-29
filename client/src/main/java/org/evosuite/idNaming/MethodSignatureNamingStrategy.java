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
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Jose Rojas
 *
 */
public class MethodSignatureNamingStrategy extends DefaultNamingStrategy {

    private Set<String> usedNames = new LinkedHashSet<>();

    public MethodSignatureNamingStrategy(ImportsTestCodeVisitor itv) {
        super(itv);
    }

    @Override
    public String getArrayReferenceName(TestCase testCase, ArrayReference var) {
        return getVariableName(testCase, var) + "Array";
    }

    @Override
	public String getVariableName(TestCase testCase, VariableReference variableReference) {
        for(VariableReference ref : testCase.getReferences(variableReference)) {
            Statement statement = testCase.getStatement(ref.getStPosition());

            if(statement instanceof MethodStatement) {
                MethodStatement ms = (MethodStatement) statement;
                if(ms.getCallee() == ref)
                    continue;
                int numParam = 0;
                for(VariableReference param: ms.getParameterReferences()) {
                    if(param == variableReference) {
                        if(VariableNameCollector.getInstance().hasParameterName(ms.getMethod().getDeclaringClass().getCanonicalName(), ms.getMethod().getNameWithDescriptor(), numParam)) {
                            String name = VariableNameCollector.getInstance().getParameterName(ms.getMethod().getDeclaringClass().getCanonicalName(), ms.getMethod().getNameWithDescriptor(), numParam);
                            if(usedNames.contains(name)) {
                                int num = 1; // Starting at 1 because 0 is implicitly used by the first variable
                                while(usedNames.contains(name+num))
                                    num++;
                                name = name + num;
                            }
                            usedNames.add(name);
                            return name;
                        }
                    }
                    numParam++;
                }
            } else if(statement instanceof ConstructorStatement) {
                ConstructorStatement cs = (ConstructorStatement) statement;
                int numParam = 0;
                for(VariableReference param: cs.getParameterReferences()) {
                    if(param == variableReference) {
                        if(VariableNameCollector.getInstance().hasParameterName(cs.getConstructor().getDeclaringClass().getCanonicalName(), cs.getConstructor().getNameWithDescriptor(), numParam)) {
                            String name = VariableNameCollector.getInstance().getParameterName(cs.getConstructor().getDeclaringClass().getCanonicalName(), cs.getConstructor().getNameWithDescriptor(), numParam);
                            return name;
                        }
                    }
                    numParam++;
                }
            }
        }
		return super.getVariableName(testCase, variableReference);
	}

    @Override
    public void reset() {
        super.reset();
        usedNames.clear();
    }

}
