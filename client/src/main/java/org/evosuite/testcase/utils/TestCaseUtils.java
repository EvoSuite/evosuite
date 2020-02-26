/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.testcase.utils;

import org.objectweb.asm.Type;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class TestCaseUtils {

   /**
   * Builds a default test case for a static target method
   *
   * @param targetStaticMethod
   * @return
   */
  public static DefaultTestCase buildTestCaseWithDefaultValues(Method targetStaticMethod) {
    TestCaseBuilder testCaseBuilder = new TestCaseBuilder();

    Type[] argumentTypes = Type.getArgumentTypes(targetStaticMethod);
    Class<?>[] argumentClasses = targetStaticMethod.getParameterTypes();

    ArrayList<VariableReference> arguments = new ArrayList<VariableReference>();
    for (int i = 0; i < argumentTypes.length; i++) {

      Type argumentType = argumentTypes[i];
      Class<?> argumentClass = argumentClasses[i];

      switch (argumentType.getSort()) {
        case Type.BOOLEAN: {
          VariableReference booleanVariable = testCaseBuilder.appendBooleanPrimitive(false);
          arguments.add(booleanVariable);
          break;
        }
        case Type.BYTE: {
          VariableReference byteVariable = testCaseBuilder.appendBytePrimitive((byte) 0);
          arguments.add(byteVariable);
          break;
        }
        case Type.CHAR: {
          VariableReference charVariable = testCaseBuilder.appendCharPrimitive((char) 0);
          arguments.add(charVariable);
          break;
        }
        case Type.SHORT: {
          VariableReference shortVariable = testCaseBuilder.appendShortPrimitive((short) 0);
          arguments.add(shortVariable);
          break;
        }
        case Type.INT: {
          VariableReference intVariable = testCaseBuilder.appendIntPrimitive(0);
          arguments.add(intVariable);
          break;
        }
        case Type.LONG: {
          VariableReference longVariable = testCaseBuilder.appendLongPrimitive(0L);
          arguments.add(longVariable);
          break;
        }
        case Type.FLOAT: {
          VariableReference floatVariable = testCaseBuilder.appendFloatPrimitive((float) 0.0);
          arguments.add(floatVariable);
          break;
        }
        case Type.DOUBLE: {
          VariableReference doubleVariable = testCaseBuilder.appendDoublePrimitive(0.0);
          arguments.add(doubleVariable);
          break;
        }
        case Type.ARRAY: {
          VariableReference arrayVariable = testCaseBuilder.appendArrayStmt(argumentClass, 0);
          arguments.add(arrayVariable);
          break;
        }
        case Type.OBJECT: {
          if (argumentClass.equals(String.class)) {
            VariableReference stringVariable = testCaseBuilder.appendStringPrimitive("");
            arguments.add(stringVariable);
          } else {
            VariableReference objectVariable = testCaseBuilder.appendNull(argumentClass);
            arguments.add(objectVariable);
          }
          break;
        }
        default: {
          throw new UnsupportedOperationException();
        }
      }
    }

    testCaseBuilder.appendMethod(null, targetStaticMethod,
        arguments.toArray(new VariableReference[] {}));
    DefaultTestCase testCase = testCaseBuilder.getDefaultTestCase();

    return testCase;
  }
}
