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
package org.evosuite.coverage.exception;

import org.evosuite.PackageInfo;
import org.evosuite.Properties;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by Andrea Arcuri on 08/05/15.
 */
public class ExceptionCoverageHelper {

    public static Class<?> getExceptionClass(ExecutionResult result, int exceptionPosition) {
        Throwable t = result.getExceptionThrownAtPosition(exceptionPosition);
        if (t instanceof OverrideMock) {
            return t.getClass().getSuperclass();
        }
        return t.getClass();
    }

    public static ExceptionCoverageTestFitness.ExceptionType getType(ExecutionResult result, int exceptionPosition) {
        if (isDeclared(result, exceptionPosition)) {
            return ExceptionCoverageTestFitness.ExceptionType.DECLARED;
        } else {
            if (isExplicit(result, exceptionPosition)) {
                return ExceptionCoverageTestFitness.ExceptionType.EXPLICIT;
            } else {
                return ExceptionCoverageTestFitness.ExceptionType.IMPLICIT;
            }
        }
    }

    public static boolean isExplicit(ExecutionResult result, int exceptionPosition) {
        return result.explicitExceptions.containsKey(exceptionPosition)
                && result.explicitExceptions.get(exceptionPosition);
    }

    public static boolean isDeclared(ExecutionResult result, int exceptionPosition) {
        Throwable t = result.getExceptionThrownAtPosition(exceptionPosition);

        // Check if thrown exception is declared, or subclass of a declared exception
        for (Class<?> declaredExceptionClass : result.test.getStatement(exceptionPosition).getDeclaredExceptions()) {
            if (declaredExceptionClass.isAssignableFrom(t.getClass())) {
                return true;
            }
        }

        return false;
    }

    public static String getMethodIdentifier(ExecutionResult result, int exceptionPosition) {
        if (result.test.getStatement(exceptionPosition) instanceof MethodStatement) {
            MethodStatement ms = (MethodStatement) result.test.getStatement(exceptionPosition);
            Method method = ms.getMethod().getMethod();
            return method.getName() + Type.getMethodDescriptor(method);
        } else if (result.test.getStatement(exceptionPosition) instanceof ConstructorStatement) {
            ConstructorStatement cs = (ConstructorStatement) result.test.getStatement(exceptionPosition);
            Constructor<?> constructor = cs.getConstructor().getConstructor();
            return "<init>" + Type.getConstructorDescriptor(constructor);
        }
        return "";
    }

    public static boolean isSutException(ExecutionResult result, int exceptionPosition) {
        if (result.test.getStatement(exceptionPosition) instanceof MethodStatement) {
            MethodStatement ms = (MethodStatement) result.test.getStatement(exceptionPosition);
            Method method = ms.getMethod().getMethod();
            Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
            return method.getDeclaringClass().equals(targetClass);
        } else if (result.test.getStatement(exceptionPosition) instanceof ConstructorStatement) {
            ConstructorStatement cs = (ConstructorStatement) result.test.getStatement(exceptionPosition);
            Constructor<?> constructor = cs.getConstructor().getConstructor();
            Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
            return constructor.getDeclaringClass().equals(targetClass);
        }
        return false;
    }

    public static boolean shouldSkip(ExecutionResult result, int exceptionPosition) {
        if (exceptionPosition >= result.test.size()) {
            // Timeouts are put after the last statement if the process was forcefully killed
            return true;
        }

        //not interested in security exceptions when Sandbox is active
        Throwable t = result.getExceptionThrownAtPosition(exceptionPosition);
        if (t instanceof SecurityException && Properties.SANDBOX) {
            return true;
        }

        /*
			Ignore exceptions thrown in the test code itself. Eg, due to mutation we
			can end up with tests like:

			Foo foo = null:
			foo.bar();
		 */
        if (t instanceof CodeUnderTestException) {
            return true;
        }

        if (t.getStackTrace() != null && t.getStackTrace().length > 0 && t.getStackTrace()[0] != null) {
            // This is to cover cases not handled by CodeUnderTestException, or if bug in EvoSuite itself
            if (t.getStackTrace()[0].getClassName().startsWith(PackageInfo.getEvoSuitePackage() + ".testcase"))
                return true;

            // Enum valueOf exceptions are not interesting, they just result from invalid strings
            return t.getStackTrace()[0].getClassName().startsWith(Enum.class.getCanonicalName()) && t.getStackTrace()[0].getMethodName().startsWith("valueOf");
        }

        return false;
    }

}
