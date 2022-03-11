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
package org.evosuite.testcase;

import org.evosuite.Properties;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.ref.array.SymbolicArrayUtil;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.utils.StatementClassChecker;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.ArraySymbolicLengthName;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.Randomness;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test case generation and update helper methods.
 *
 * @author Ignacio Lebrero
 */
public class TestCaseUpdater {

    private static final Logger logger = LoggerFactory.getLogger(TestCaseUpdater.class);

    public static final int DEFAULT_STRING_LENGTH = 10;
    public static final int ARRAY_DIMENSION_LOWER_BOUND = 0;
    /**
     * TODO (ilebrero): At some point there can be an empirical study about the max length that arrays usually have in open source projects
     */
    public static final int DEFAULT_ARRAY_LENGTH_UPPER_BOUND = 20;

    public static final String NEW_VALUE = "New value: ";
    public static final String UNEXPECTED_VALUE = "Unexpected value: ";
    public static final String NEW_VALUE_IS_NULL = "New value is null";
    public static final String COULD_NOT_FIND_ARRAY = "Could not find array ";
    public static final String NEW_VALUE_IS_OF_AN_UNSUPPORTED_TYPE = "New value is of an unsupported type: ";
    public static final String NEW_REAL_VALUE_IS_OF_AN_UNSUPPORTED_TYPE = "New real value is of an unsupported type: ";
    public static final String NEW_INTEGER_VALUE_IS_OF_AN_UNSUPPORTED_TYPE = "New integer value is of an unsupported type: ";


    @SuppressWarnings({"rawtypes", "unchecked"})
    public static TestCase updateTest(TestCase test, Map<String, Object> updatedValues) {

        TestCase newTest = test.clone();
        newTest.clearCoveredGoals();

        for (String symbolicVariableName : updatedValues.keySet()) {
            Object updateValue = updatedValues.get(symbolicVariableName);
            if (updateValue != null) {
                logger.info(NEW_VALUE + symbolicVariableName + ": " + updateValue);

                if (ArraySymbolicLengthName.isArraySymbolicLengthVariableName(symbolicVariableName)) {
                    processArrayLengthValue(newTest, symbolicVariableName, (Long) updateValue);
                } else if (Properties.isLazyArraysImplementationSelected() && SymbolicArrayUtil.isArrayContentVariableName(symbolicVariableName)) {
                    processArrayElement(test, newTest, symbolicVariableName, updateValue);
                } else if (updateValue instanceof Long) {
                    processLongValue(newTest, symbolicVariableName, updateValue);
                } else if (updateValue instanceof String) {
                    processStringValue(test, newTest, symbolicVariableName, updateValue);
                } else if (updateValue instanceof Double) {
                    processRealValue(test, newTest, symbolicVariableName, updateValue);
                } else if (Properties.isArraysTheoryImplementationSelected() && updateValue.getClass().isArray()) {
                    processArray(test, newTest, symbolicVariableName, updateValue);
                } else {
                    logger.debug(NEW_VALUE_IS_OF_AN_UNSUPPORTED_TYPE + updateValue);
                }
            } else {
                logger.debug(NEW_VALUE_IS_NULL);

            }
        }

        return newTest;

    }

    private static void processArrayElement(TestCase test, TestCase newTest, String symbolicArrayVariableName, Object updateValue) {
        String[] names = symbolicArrayVariableName.split("\\_");
        String arrayVariableName = names[0].replace("__SYM", "");
        ArrayStatement arrayStatement = (ArrayStatement) getStatement(newTest, arrayVariableName, StatementClassChecker.ARRAY_STATEMENT);

        assert (arrayStatement != null) : COULD_NOT_FIND_ARRAY + arrayVariableName + " in test: " + newTest.toCode()
                + " / Orig test: " + test.toCode() + ", seed: " + Randomness.getSeed();

        AssignmentStatement s = (AssignmentStatement) getStatement(
                newTest,
                ArrayUtil.buildArrayIndexName(arrayVariableName, Collections.singletonList(Integer.parseInt(names[2]))),
                StatementClassChecker.ASSIGNMENT_STATEMENT);

        processArrayElement(
                newTest,
                arrayStatement.getArrayReference(),
                arrayVariableName,
                new int[]{Integer.parseInt(names[2])},
                updateValue
        );
    }

    /**
     * Updates the test case values that are stored on the array.
     * <p>
     * TODO (ilebrero):
     *     Is there a case where the empty array happends after the test case contains an already setted array?
     *
     * @param test
     * @param newTest
     * @param symbolicArrayVariableName
     * @param updatedArray
     */
    private static void processArray(TestCase test, TestCase newTest, String symbolicArrayVariableName, Object updatedArray) {
        String arrayVariableName = symbolicArrayVariableName.replace("__SYM", "");
        ArrayStatement arrayStatement = (ArrayStatement) getStatement(newTest, arrayVariableName, StatementClassChecker.ARRAY_STATEMENT);

        assert (arrayStatement != null) : COULD_NOT_FIND_ARRAY + arrayVariableName + " in test: " + newTest.toCode()
                + " / Orig test: " + test.toCode() + ", seed: " + Randomness.getSeed();

        ArrayUtil.MultiDimensionalArrayIterator arrayIterator = new ArrayUtil.MultiDimensionalArrayIterator(updatedArray);
        while (arrayIterator.hasNext()) {
            processArrayElement(
                    newTest,
                    arrayStatement.getArrayReference(),
                    arrayVariableName,
                    arrayIterator.getCurrentIndex(),
                    arrayIterator.getNextElement()
            );
        }
    }

    /**
     * Updates the assignment of an element of an array
     * <p>
     * ***** General algorithm *****
     * if (! exists assignment statement for the current index)
     * if (! current value is a default value (i.e. arr[i] = 0))
     * Create primitive variable and assign new value (i.e. x = val)     | PrimitiveStatement
     * Create assignment and use the previous variable (i.e. arr[i] = x) | AssignmentStatement
     * else
     * update current primitive variable value
     *
     * @param newTest
     * @param arrayReference
     * @param arrayVariableName
     * @param indexes
     * @param newValue
     */
    private static void processArrayElement(TestCase newTest, ArrayReference arrayReference, String arrayVariableName, int[] indexes, Object newValue) {
        // TODO (ilebrero): Improve this as we have to recreate the list on each iteration
        List<Integer> indexList = getIntegerList(indexes);
        String componentType = arrayReference.getComponentName();

        // New test case builder starting at the statement position
        TestCaseBuilder testCaseBuilder = new TestCaseBuilder((DefaultTestCase) newTest, getStatementPosition(newTest, arrayReference.getName()) + 1);

        // Possible assignment statement(if the variable was already used in a previous ran).
        AssignmentStatement s = (AssignmentStatement) getStatement(
                newTest,
                ArrayUtil.buildArrayIndexName(arrayVariableName, indexList),
                StatementClassChecker.ASSIGNMENT_STATEMENT);

        if (s == null) {
            if (!DefaultValueChecker.isDefaultValue(newValue)) {
                // In case there's no assignment already created, we create a new variable and the assignment itself
                VariableReference newArrayElement = buildArrayVariableElementReference(testCaseBuilder, componentType, newValue);
                testCaseBuilder.appendAssignment(arrayReference, indexList, newArrayElement);
            }
        } else {
            // In case there exists an statement (we already used that value before
            PrimitiveStatement valueStatement = getPrimitiveStatement(newTest, s.getValue().getName());
            updateStatement(newValue, valueStatement);
        }
    }

    /**
     * Updates a statement
     *
     * @param newValue
     * @param valueStatement
     */
    private static void updateStatement(Object newValue, PrimitiveStatement valueStatement) {
        if (Long.class.getName().equals(newValue.getClass().getName())) {
            updateIntegerValueStatement((Long) newValue, valueStatement);
        } else if (Double.class.getName().equals(newValue.getClass().getName())) {
            updateRealValueStatement((Double) newValue, valueStatement);
        } else if (String.class.getName().equals(newValue.getClass().getName())) {
            updateStringValueStatement((String) newValue, valueStatement);
        } else {
            throw new UnsupportedOperationException(
                    "Update array statemnt for type: "
                            + newValue.getClass().getName()
                            + " not yet supported.");
        }
    }

    /**
     * Transforms an array of integer to a list of Integer objects
     *
     * @param indexes
     * @return
     */
    private static List<Integer> getIntegerList(int[] indexes) {
        List<Integer> elements = new ArrayList();

        for (int index : indexes) {
            elements.add(index);
        }

        return elements;
    }

    /**
     * Updates the corresponding real statement given a symbolic variable and its updated value
     *
     * @param test
     * @param newTest
     * @param symbolicVariableName
     * @param updateValue
     */
    private static void processRealValue(TestCase test, TestCase newTest, String symbolicVariableName, Object updateValue) {
        Double value = (Double) updateValue;
        String name = symbolicVariableName.replace("__SYM", "");
        PrimitiveStatement p = getPrimitiveStatement(newTest, name);
        // logger.warn("New double value for " + name + " is " +
        // value);
        assert (p != null) : "Could not find variable " + name + " in test: " + newTest.toCode()
                + " / Orig test: " + test.toCode() + ", seed: " + Randomness.getSeed();

        updateRealValueStatement(value, p);
    }

    /**
     * Updates the corresponding string statement given a symbolic variable and its updated value
     *
     * @param test
     * @param newTest
     * @param symbolicVariableName
     * @param updateValue
     */
    private static void processStringValue(TestCase test, TestCase newTest, String symbolicVariableName, Object updateValue) {
        String name = symbolicVariableName.replace("__SYM", "");
        PrimitiveStatement p = getPrimitiveStatement(newTest, name);
        // logger.warn("New string value for " + name + " is " +
        // val);
        assert (p != null) : "Could not find variable " + name + " in test: " + newTest.toCode()
                + " / Orig test: " + test.toCode() + ", seed: " + Randomness.getSeed();
        if (p.getValue().getClass().equals(Character.class))
            p.setValue((char) Integer.parseInt(updateValue.toString()));
        else
            p.setValue(updateValue.toString());
    }

    /**
     * Updates the corresponding long statement given a symbolic variable and its updated value
     *
     * @param newTest
     * @param symbolicVariableName
     * @param updateValue
     */
    private static void processLongValue(TestCase newTest, String symbolicVariableName, Object updateValue) {
        Long value = (Long) updateValue;

        String name = symbolicVariableName.replace("__SYM", "");
        // logger.warn("New long value for " + name + " is " +
        // value);
        PrimitiveStatement p = (PrimitiveStatement) getStatement(newTest, name, StatementClassChecker.PRIMITIVE_STATEMENT);
        updateIntegerValueStatement(value, p);
    }

    /**
     * Updates the corresponding array length statement given a symbolic variable and its updated value
     *
     * @param newTest
     * @param symbolicVariableName
     * @param updateValue
     */
    private static void processArrayLengthValue(TestCase newTest, String symbolicVariableName, Long updateValue) {
        ArraySymbolicLengthName arraySymbolicLengthName = new ArraySymbolicLengthName(symbolicVariableName);
        ArrayStatement arrayStatement = (ArrayStatement) getStatement(newTest, arraySymbolicLengthName.getArrayReferenceName(), StatementClassChecker.ARRAY_STATEMENT);
        arrayStatement.setLength(
                updateValue.intValue(),
                arraySymbolicLengthName.getDimension()
        );
    }

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

        ArrayList<VariableReference> arguments = new ArrayList<>();
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
                    VariableReference arrayVariable = testCaseBuilder.appendArrayStmt(argumentClass, ArrayUtil.buildDimensionsArray(argumentType));
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
                arguments.toArray(new VariableReference[]{}));
        DefaultTestCase testCase = testCaseBuilder.getDefaultTestCase();

        return testCase;
    }

    /**
     * Builds a random test case for a static target method
     *
     * @param targetStaticMethod
     * @return
     */
    public static DefaultTestCase buildTestCaseWithRandomValues(Method targetStaticMethod) {
        TestCaseBuilder testCaseBuilder = new TestCaseBuilder();

        Type[] argumentTypes = Type.getArgumentTypes(targetStaticMethod);
        Class<?>[] argumentClasses = targetStaticMethod.getParameterTypes();

        ArrayList<VariableReference> arguments = new ArrayList<>();
        for (int i = 0; i < argumentTypes.length; i++) {

            Type argumentType = argumentTypes[i];
            Class<?> argumentClass = argumentClasses[i];

            switch (argumentType.getSort()) {
                case Type.BOOLEAN: {
                    VariableReference booleanVariable = testCaseBuilder.appendBooleanPrimitive(Randomness.nextBoolean());
                    arguments.add(booleanVariable);
                    break;
                }
                case Type.BYTE: {
                    VariableReference byteVariable = testCaseBuilder.appendBytePrimitive((byte) Randomness.nextInt());
                    arguments.add(byteVariable);
                    break;
                }
                case Type.CHAR: {
                    VariableReference charVariable = testCaseBuilder.appendCharPrimitive((char) Randomness.nextInt());
                    arguments.add(charVariable);
                    break;
                }
                case Type.SHORT: {
                    VariableReference shortVariable = testCaseBuilder.appendShortPrimitive((short) Randomness.nextInt());
                    arguments.add(shortVariable);
                    break;
                }
                case Type.INT: {
                    VariableReference intVariable = testCaseBuilder.appendIntPrimitive(Randomness.nextInt());
                    arguments.add(intVariable);
                    break;
                }
                case Type.LONG: {
                    VariableReference longVariable = testCaseBuilder.appendLongPrimitive(Randomness.nextLong());
                    arguments.add(longVariable);
                    break;
                }
                case Type.FLOAT: {
                    VariableReference floatVariable = testCaseBuilder.appendFloatPrimitive(Randomness.nextFloat());
                    arguments.add(floatVariable);
                    break;
                }
                case Type.DOUBLE: {
                    VariableReference doubleVariable = testCaseBuilder.appendDoublePrimitive(Randomness.nextDouble());
                    arguments.add(doubleVariable);
                    break;
                }
                case Type.ARRAY: {
                    VariableReference arrayVariable = testCaseBuilder.appendArrayStmt(argumentClass, ArrayUtil.buildRandomDimensionsArray(argumentType));
                    arguments.add(arrayVariable);
                    break;
                }
                case Type.OBJECT: {
                    if (argumentClass.equals(String.class)) {
                        VariableReference stringVariable = testCaseBuilder.appendStringPrimitive(Randomness.nextString(DEFAULT_STRING_LENGTH));
                        arguments.add(stringVariable);
                    } else {
                        // TODO: complete the randomness
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
                arguments.toArray(new VariableReference[]{}));
        DefaultTestCase testCase = testCaseBuilder.getDefaultTestCase();

        return testCase;
    }

    /**
     * Get the statement that defines this variable
     *
     * @param test
     * @param name
     * @return
     */
    public static PrimitiveStatement<?> getPrimitiveStatement(TestCase test, String name) {
        for (Statement statement : test) {

            if (statement instanceof PrimitiveStatement<?>) {
                if (statement.getReturnValue().getName().equals(name))
                    return (PrimitiveStatement<?>) statement;
            }
        }
        return null;
    }

    /**
     * Get the statement that defines this variable
     *
     * @param test
     * @param name
     * @param typeCheckFunction
     * @return
     */
    public static Statement getStatement(TestCase test, String name, StatementClassChecker typeCheckFunction) {
        for (Statement statement : test) {
            if (typeCheckFunction.checkClassType(statement)) {
                if (statement.getReturnValue().getName().equals(name))
                    return statement;
            }
        }
        return null;
    }

    /**
     * Finds the position of a given statement on the Test Case.
     * In case the statement is not there, we return the first position after the last one.
     *
     * @param newTest
     * @param name
     * @return
     */
    public static int getStatementPosition(TestCase newTest, String name) {
        int index = 0;

        for (Statement testStatement : newTest) {
            if (testStatement.getReturnValue().getName().equals(name)) {
                return index;
            }
            index++;
        }

        // If the element was not found we just return the end.
        return index;
    }

    /**
     * Updates an integer statement
     *
     * @param value
     * @param statement
     */
    private static void updateIntegerValueStatement(Long value, PrimitiveStatement statement) {
        if (statement.getValue().getClass().equals(Character.class)) {
            char charValue = (char) value.intValue();
            statement.setValue(charValue);
        } else if (statement.getValue().getClass().equals(Long.class)) {
            statement.setValue(value);
        } else if (statement.getValue().getClass().equals(Integer.class)) {
            statement.setValue(value.intValue());
        } else if (statement.getValue().getClass().equals(Short.class)) {
            statement.setValue(value.shortValue());
        } else if (statement.getValue().getClass().equals(Boolean.class)) {
            statement.setValue(value.intValue() > 0);
        } else if (statement.getValue().getClass().equals(Byte.class)) {
            statement.setValue(value.byteValue());

        } else
            logger.warn(NEW_INTEGER_VALUE_IS_OF_AN_UNSUPPORTED_TYPE + statement.getValue().getClass() + value);
    }

    /**
     * Updates a real statement
     *
     * @param value
     * @param p
     */
    private static void updateRealValueStatement(Double value, PrimitiveStatement p) {
        if (p.getValue().getClass().equals(Double.class))
            p.setValue(value);
        else if (p.getValue().getClass().equals(Float.class))
            p.setValue(value.floatValue());
        else
            logger.warn(NEW_REAL_VALUE_IS_OF_AN_UNSUPPORTED_TYPE + value);
    }

    /**
     * Updates a string statement
     *
     * @param newValue
     * @param p
     */
    private static void updateStringValueStatement(String newValue, PrimitiveStatement p) {
        p.setValue(newValue);
    }

    private static VariableReference buildArrayVariableElementReference(TestCaseBuilder testCaseBuilder, String componentName, Object newValue) {
        if (int.class.getName().equals(componentName)) {
            return testCaseBuilder.appendIntPrimitive(((Long) newValue).intValue());
        } else if (short.class.getName().equals(componentName)) {
            return testCaseBuilder.appendShortPrimitive(((Long) newValue).shortValue());
        } else if (byte.class.getName().equals(componentName)) {
            return testCaseBuilder.appendBytePrimitive(((Long) newValue).byteValue());
        } else if (char.class.getName().equals(componentName)) {
            return testCaseBuilder.appendCharPrimitive((char) ((Long) newValue).longValue());
        } else if (boolean.class.getName().equals(componentName)) {
            return testCaseBuilder.appendBooleanPrimitive(((Long) newValue) > 1);
        } else if (long.class.getName().equals(componentName)) {
            return testCaseBuilder.appendLongPrimitive(((Long) newValue));
        } else if (String.class.getName().equals(componentName)) {
            return testCaseBuilder.appendStringPrimitive(((String) newValue));
        } else if (float.class.getName().equals(componentName)) {
            return testCaseBuilder.appendFloatPrimitive(((Double) newValue).floatValue());
        } else if (double.class.getName().equals(componentName)) {
            return testCaseBuilder.appendDoublePrimitive((Double) newValue);
        }

        throw new IllegalStateException(UNEXPECTED_VALUE + componentName);

    }
}
