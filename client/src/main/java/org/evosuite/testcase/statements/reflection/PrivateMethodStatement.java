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
package org.evosuite.testcase.statements.reflection;

import org.evosuite.runtime.PrivateAccess;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.GenericMethod;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Test case statement representing a reflection call to a private method of the SUT
 * <p>
 * Created by Andrea Arcuri on 22/02/15.
 */
public class PrivateMethodStatement extends MethodStatement {

    private static final long serialVersionUID = -4555899888145880432L;

    private final GenericMethod reflectedMethod;

    private boolean isStaticMethod = false;

    public PrivateMethodStatement(TestCase tc, Class<?> klass, Method method, VariableReference callee, List<VariableReference> params, boolean isStatic) {
        super(
                tc,
                new GenericMethod(PrivateAccess.getCallMethod(params.size()), PrivateAccess.class),
                null, //it is static
                getReflectionParams(tc, klass, method, callee, params)
        );
        reflectedMethod = new GenericMethod(method, klass);
        isStaticMethod = isStatic;
        List<GenericClass<?>> parameterTypes = new ArrayList<>();
        parameterTypes.add(GenericClassFactory.get(klass));
        this.method.setTypeParameters(parameterTypes);
    }

    private static List<VariableReference> getReflectionParams(TestCase tc, Class<?> klass, Method method,
                                                               VariableReference callee, List<VariableReference> inputs) {

        List<VariableReference> list = new ArrayList<>(3 + inputs.size() * 2);
        list.add(new ConstantValue(tc, GenericClassFactory.get(Class.class), klass));
        list.add(callee);
        list.add(new ConstantValue(tc, GenericClassFactory.get(String.class), method.getName()));

        Class<?>[] parameterTypes = method.getParameterTypes();
        assert (parameterTypes.length == inputs.size());
        for (int parameterNum = 0; parameterNum < parameterTypes.length; parameterNum++) {
            VariableReference vr = inputs.get(parameterNum);
            list.add(vr);
            list.add(new ConstantValue(tc, GenericClassFactory.get(Class.class), parameterTypes[parameterNum]));
        }

        return list;
    }

    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        // just for simplicity
        return false;
        //return super.mutate(test,factory); //tricky, as should do some restrictions
    }

    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        PrivateMethodStatement pm;
        List<VariableReference> newParams = new ArrayList<>();
        for (int i = 3; i < parameters.size(); i = i + 2) {
            newParams.add(parameters.get(i).copy(newTestCase, offset));
        }

        VariableReference newCallee = parameters.get(1).copy(newTestCase, offset);
        Class<?> klass = (Class<?>) ((ConstantValue) parameters.get(0)).getValue(); // TODO: Make this nice

        pm = new PrivateMethodStatement(newTestCase, klass, reflectedMethod.getMethod(), newCallee, newParams, isStaticMethod);

        assert pm.parameters.size() == this.parameters.size();

        return pm;
    }

    @Override
    public Throwable execute(final Scope scope, PrintStream out)
            throws InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, InstantiationException {
        if (!isStaticMethod) {
            // If the callee is null, then reflection will only lead to a NPE.
            VariableReference callee = parameters.get(1);
            try {
                Object calleeObject = callee.getObject(scope);
                if (calleeObject == null)
                    return new CodeUnderTestException(new NullPointerException());
            } catch (CodeUnderTestException e) {
                return e;
            }
        }
        return super.execute(scope, out);
    }

    @Override
    public boolean isReflectionStatement() {
        return true;
    }

}
