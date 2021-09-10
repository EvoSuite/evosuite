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
package org.evosuite.testcase.statements;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.fm.EvoAbstractMethodInvocationListener;
import org.evosuite.testcase.fm.EvoInvocationListener;
import org.evosuite.testcase.fm.MethodDescriptor;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericClass;
import org.mockito.MockSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.withSettings;

public class FunctionalMockForAbstractClassStatement extends FunctionalMockStatement {

    private static final long serialVersionUID = -3933543503326450446L;

    private static final Logger logger = LoggerFactory.getLogger(FunctionalMockForAbstractClassStatement.class);

    public FunctionalMockForAbstractClassStatement(TestCase tc, VariableReference retval, GenericClass<?> targetClass) throws IllegalArgumentException {
        super(tc, retval, targetClass);
    }

    public FunctionalMockForAbstractClassStatement(TestCase tc, Type retvalType, GenericClass<?> targetClass) throws IllegalArgumentException {
        super(tc, retvalType, targetClass);
    }

    protected void checkTarget() {
        if (!canBeFunctionalMockedIncludingSUT(targetClass.getRawClass())) {
            throw new IllegalArgumentException("Cannot create a basic functional mock for class " + targetClass);
        }
    }


    protected EvoInvocationListener createInvocationListener() {
        return new EvoAbstractMethodInvocationListener(retval.getGenericClass());
    }

    protected MockSettings createMockSettings() {
        return withSettings().defaultAnswer(CALLS_REAL_METHODS).invocationListeners(listener);
    }

    @Override
    public Statement copy(TestCase newTestCase, int offset) {


        FunctionalMockForAbstractClassStatement copy = new FunctionalMockForAbstractClassStatement(
                newTestCase, retval.getType(), targetClass);

        for (VariableReference r : this.parameters) {
            copy.parameters.add(r.copy(newTestCase, offset));
        }

        copy.listener = this.listener; //no need to clone, as only read, and created new instance at each new execution

        for (MethodDescriptor md : this.mockedMethods) {
            copy.mockedMethods.add(md.getCopy());
        }

        for (Map.Entry<String, int[]> entry : methodParameters.entrySet()) {
            int[] array = entry.getValue();
            int[] copiedArray = array == null ? null : new int[]{array[0], array[1]};
            copy.methodParameters.put(entry.getKey(), copiedArray);
        }

        return copy;
    }
}
