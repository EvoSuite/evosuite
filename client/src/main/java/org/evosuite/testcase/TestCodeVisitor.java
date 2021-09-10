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

import com.googlecode.gentyref.CaptureType;
import com.googlecode.gentyref.GenericTypeReflector;
import dk.brics.automaton.RegExp;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.PackageInfo;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.assertion.*;
import org.evosuite.classpath.ResourceList;
import org.evosuite.runtime.TooManyResourcesException;
import org.evosuite.runtime.ViolatedAssumptionAnswer;
import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.testcase.fm.MethodDescriptor;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.statements.environment.EnvironmentDataStatement;
import org.evosuite.testcase.variable.*;
import org.evosuite.utils.NumberFormatter;
import org.evosuite.utils.StringUtil;
import org.evosuite.utils.generic.*;

import java.lang.reflect.*;
import java.net.URLClassLoader;
import java.util.*;

import static java.util.stream.Collectors.toCollection;

/**
 * The TestCodeVisitor is a visitor that produces a String representation of a
 * test case. This is the preferred way to produce executable code from EvoSuite
 * tests.
 *
 * @author Gordon Fraser
 */
public class TestCodeVisitor extends TestVisitor {

    protected String testCode = "";

    protected static final String NEWLINE = System.getProperty("line.separator");

    protected final Map<Integer, Throwable> exceptions = new HashMap<>();

    protected TestCase test = null;

    protected final Map<VariableReference, String> variableNames = new HashMap<>();

    protected final Map<Class<?>, String> classNames = new HashMap<>();

    protected final Map<String, Integer> nextIndices = new HashMap<>();

    /**
     * <p>
     * getCode
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCode() {
        return testCode;
    }

    /**
     * Retrieve a list of classes that need to be imported to make this unit
     * test compile
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Class<?>> getImports() {
        return classNames.keySet().stream()
                // If there's a dot in the name, then we assume this is the
                // fully qualified name and we don't need to import
                .filter(clazz -> !classNames.get(clazz).contains("."))
                .collect(toCollection(HashSet::new));
    }

    /**
     * <p>
     * clearExceptions
     * </p>
     */
    public void clearExceptions() {
        this.exceptions.clear();
    }

    /**
     * <p>
     * Setter for the field <code>exceptions</code>.
     * </p>
     *
     * @param exceptions a {@link java.util.Map} object.
     */
    public void setExceptions(Map<Integer, Throwable> exceptions) {
        this.exceptions.putAll(exceptions);
    }

    /**
     * <p>
     * setException
     * </p>
     *
     * @param statement a {@link org.evosuite.testcase.statements.Statement} object.
     * @param exception a {@link java.lang.Throwable} object.
     */
    public void setException(Statement statement, Throwable exception) {
        exceptions.put(statement.getPosition(), exception);
    }

    /**
     * <p>
     * getException
     * </p>
     *
     * @param statement a {@link org.evosuite.testcase.statements.Statement} object.
     * @return a {@link java.lang.Throwable} object.
     */
    protected Throwable getException(Statement statement) {
        return exceptions.getOrDefault(statement.getPosition(), null);
    }

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @param var a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @return a {@link java.lang.String} object.
     */
    public String getClassName(VariableReference var) {
        return getTypeName(var.getType());
    }

    private String getTypeName(ParameterizedType type) {
        String name = getClassName((Class<?>) type.getRawType());
        Type[] types = type.getActualTypeArguments();
        boolean isDefined = false;
        for (Type parameterType : types) {
            if (parameterType instanceof Class<?> ||
                    parameterType instanceof ParameterizedType ||
                    parameterType instanceof WildcardType ||
                    parameterType instanceof GenericArrayType) {
                isDefined = true;
                break;
            }
        }
        if (isDefined) {
            if (types.length > 0) {
                name += "<";
                for (int i = 0; i < types.length; i++) {
                    if (i != 0)
                        name += ", ";

                    name += getTypeParameterName(types[i]);
                }
                name += ">";
            }
        }
        return name;
    }

    public String getTypeName(Type type) {
        if (type instanceof Class<?>) {
            return getClassName((Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            return getTypeName((ParameterizedType) type);
        } else if (type instanceof WildcardType) {
            String ret = "Object";
            return ret;
        } else if (type instanceof TypeVariable) {
            return "Object";
        } else if (type instanceof CaptureType) {
            CaptureType captureType = (CaptureType) type;
            if (captureType.getLowerBounds().length == 0)
                return "Object";
            else
                return getTypeName(captureType.getLowerBounds()[0]);
        } else if (type instanceof GenericArrayType) {
            return getTypeName(((GenericArrayType) type).getGenericComponentType())
                    + "[]";
        } else {
            throw new RuntimeException("Unsupported type:" + type + ", class"
                    + type.getClass());
        }
    }

    public String getTypeParameterName(Type type) {
        if (type instanceof Class<?>) {
            return getClassName((Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            return getTypeName((ParameterizedType) type);
        } else if (type instanceof WildcardType) {
            String ret = "?";
            boolean first = true;
            for (Type bound : ((WildcardType) type).getLowerBounds()) {
                // If there are lower bounds we need to state them, even if Object
                if (bound == null) // || GenericTypeReflector.erase(bound).equals(Object.class))
                    continue;

                if (!first)
                    ret += ", ";
                ret += " super " + getTypeParameterName(bound);
                first = false;
            }
            for (Type bound : ((WildcardType) type).getUpperBounds()) {
                if (bound == null
                        || (!(bound instanceof CaptureType) && GenericTypeReflector.erase(bound).equals(Object.class)))
                    continue;

                if (!first)
                    ret += ", ";
                ret += " extends " + getTypeParameterName(bound);
                first = false;
            }
            return ret;
        } else if (type instanceof TypeVariable) {
            return "?";
        } else if (type instanceof CaptureType) {
            CaptureType captureType = (CaptureType) type;
            if (captureType.getLowerBounds().length == 0)
                return "?";
            else
                return getTypeName(captureType.getLowerBounds()[0]);
        } else if (type instanceof GenericArrayType) {
            return getTypeName(((GenericArrayType) type).getGenericComponentType())
                    + "[]";
        } else {
            throw new RuntimeException("Unsupported type:" + type + ", class"
                    + type.getClass());
        }
    }

    public String getTypeName(VariableReference var) {

        GenericClass<?> clazz = var.getGenericClass();
        return getTypeName(clazz.getType());
    }

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @return a {@link java.lang.String} object.
     */
    public String getClassName(Class<?> clazz) {
        if (classNames.containsKey(clazz))
            return classNames.get(clazz);

        if (clazz.isArray()) {
            return getClassName(clazz.getComponentType()) + "[]";
        }

        GenericClass<?> c = GenericClassFactory.get(clazz);
        String name = c.getSimpleName();
        if (classNames.containsValue(name)) {
            name = clazz.getCanonicalName();
        } else {
            /*
             * If e.g. there is a foo.bar.IllegalStateException with
             * foo.bar being the SUT package, then we need to use the
             * full package name for java.lang.IllegalStateException
             */
            String fullName = Properties.CLASS_PREFIX + "." + name;
            if (!fullName.equals(clazz.getCanonicalName())) {
                try {
                    if (ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).hasClass(fullName)) {
                        name = clazz.getCanonicalName();
                    }
                } catch (IllegalArgumentException e) {
                    // If the classpath is not correct, then we just don't check
                    // because that cannot happen in regular EvoSuite use, only
                    // from test cases
                }
            }
        }
        // Ensure outer classes are imported as well
        Class<?> outerClass = clazz.getEnclosingClass();
        if (outerClass != null) {
            String enclosingName = getClassName(outerClass);
            String simpleOuterName = outerClass.getSimpleName() + ".";
            if (simpleOuterName.equals(enclosingName)) {
                name = enclosingName + name.substring(simpleOuterName.length());
            } else {
                name = enclosingName + name.substring(name.lastIndexOf(simpleOuterName) + simpleOuterName.length() - 1);
            }
        }

        Class<?> declaringClass = clazz.getDeclaringClass();
        if (declaringClass != null) {
            getClassName(declaringClass);
        }

        // We can't use "Test" because of JUnit
        if (name.equals("Test")) {
            name = clazz.getCanonicalName();
        }
        classNames.put(clazz, name);

        return name;
    }

    /**
     * <p>
     * getVariableName
     * </p>
     *
     * @param var a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @return a {@link java.lang.String} object.
     */
    public String getVariableName(VariableReference var) {
        if (var instanceof ConstantValue) {
            ConstantValue cval = (ConstantValue) var;
            if (cval.getValue() != null && cval.getVariableClass().equals(Class.class)) {
                return getClassName((Class<?>) cval.getValue()) + ".class";
            }
            return var.getName();
        } else if (var instanceof FieldReference) {
            VariableReference source = ((FieldReference) var).getSource();
            GenericField field = ((FieldReference) var).getField();
            if (source != null) {
                String ret = "";
                // If the method is not public and this is a subclass in a different package we need to cast
                if (!field.isPublic() && !field.getDeclaringClass().equals(source.getVariableClass()) && source.isAssignableTo(field.getDeclaringClass())) {
                    String packageName1 = ClassUtils.getPackageName(field.getDeclaringClass());
                    String packageName2 = ClassUtils.getPackageName(source.getVariableClass());
                    if (!packageName1.equals(packageName2)) {
                        ret += "((" + getClassName(field.getDeclaringClass())
                                + ")" + getVariableName(source) + ")";
                    } else {
                        ret += getVariableName(source);
                    }
                } else if (!source.isAssignableTo(field.getField().getDeclaringClass())) {
                    try {
                        // If the concrete source class has that field then it's ok
                        source.getVariableClass().getDeclaredField(field.getName());
                        ret = getVariableName(source);
                    } catch (NoSuchFieldException e) {
                        // If not we need to cast to the subtype
                        ret = "((" + getTypeName(field.getField().getDeclaringClass()) + ") " + getVariableName(source) + ")";
                    }
                } else {
                    ret += getVariableName(source);
                }

                return ret + "." + field.getName();
            } else
                return getClassName(field.getField().getDeclaringClass()) + "."
                        + field.getName();
        } else if (var instanceof ArrayIndex) {
            VariableReference array = ((ArrayIndex) var).getArray();
            List<Integer> indices = ((ArrayIndex) var).getArrayIndices();
            String result = getVariableName(array);
            for (Integer index : indices) {
                result += "[" + index + "]";
            }
            return result;
        } else if (var instanceof ArrayReference) {
            String className = var.getSimpleClassName();
            // int num = 0;
            // for (VariableReference otherVar : variableNames.keySet()) {
            // if (!otherVar.equals(var)
            // && otherVar.getVariableClass().equals(var.getVariableClass()))
            // num++;
            // }
            String variableName = className.substring(0, 1).toLowerCase()
                    + className.substring(1) + "Array";
            variableName = variableName.replace('.', '_').replace("[]", "");
            if (!variableNames.containsKey(var)) {
                if (!nextIndices.containsKey(variableName)) {
                    nextIndices.put(variableName, 0);
                }

                int index = nextIndices.get(variableName);
                nextIndices.put(variableName, index + 1);

                variableName += index;

                variableNames.put(var, variableName);
            }

        } else if (!variableNames.containsKey(var)) {
            String className = var.getSimpleClassName();
            // int num = 0;
            // for (VariableReference otherVar : variableNames.keySet()) {
            // if (otherVar.getVariableClass().equals(var.getVariableClass()))
            // num++;
            // }
            String variableName = className.substring(0, 1).toLowerCase()
                    + className.substring(1);
            if (variableName.contains("[]")) {
                variableName = variableName.replace("[]", "Array");
            }
            variableName = variableName.replace(".", "_");

            // Need a way to check for exact types, not assignable
            // int numObjectsOfType = test != null ? test.getObjects(var.getType(),
            //                                                      test.size()).size() : 2;
            // if (numObjectsOfType > 1 || className.equals(variableName)) {
            if (CharUtils.isAsciiNumeric(variableName.charAt(variableName.length() - 1)))
                variableName += "_";

            if (!nextIndices.containsKey(variableName)) {
                nextIndices.put(variableName, 0);
            }

            int index = nextIndices.get(variableName);
            nextIndices.put(variableName, index + 1);

            variableName += index;
            // }

            variableNames.put(var, variableName);
        }
        return variableNames.get(var);
    }

    /**
     * Retrieve the names of all known variables
     *
     * @return
     */
    public Collection<String> getVariableNames() {
        return variableNames.values();
    }

    /**
     * Retrieve the names of all known classes
     *
     * @return
     */
    public Collection<String> getClassNames() {
        return classNames.values();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.testcase.TestVisitor#visitTestCase(org.evosuite.testcase.TestCase)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitTestCase(TestCase test) {
        this.test = test;
        this.testCode = "";
        this.variableNames.clear();
        this.nextIndices.clear();
    }

    /**
     * <p>
     * visitPrimitiveAssertion
     * </p>
     *
     * @param assertion a {@link org.evosuite.assertion.PrimitiveAssertion} object.
     */
    protected void visitPrimitiveAssertion(PrimitiveAssertion assertion) {
        VariableReference source = assertion.getSource();
        Object value = assertion.getValue();

        String stmt = "";

        if (value == null) {
            stmt += "assertNull(" + getVariableName(source) + ");";
        } else if (source.getVariableClass().equals(float.class)) {
            stmt += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", "
                    + getVariableName(source) + ", " + NumberFormatter.getNumberString(Properties.FLOAT_PRECISION, this) + ");";
        } else if (source.getVariableClass().equals(double.class)) {
            stmt += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", "
                    + getVariableName(source) + ", " + NumberFormatter.getNumberString(Properties.DOUBLE_PRECISION, this) + ");";
        } else if (value.getClass().isEnum()) {
            stmt += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", "
                    + getVariableName(source) + ");";
            // Make sure the enum is imported in the JUnit test
            getClassName(value.getClass());
        } else if (source.getVariableClass().equals(boolean.class) || source.getVariableClass().equals(Boolean.class)) {
            Boolean flag = (Boolean) value;
            if (flag) {
                stmt += "assertTrue(";
            } else {
                stmt += "assertFalse(";
            }
            stmt += "" + getVariableName(source) + ");";
        } else if (source.isWrapperType()) {
            if (source.getVariableClass().equals(Float.class)) {
                stmt += "assertEquals(" + NumberFormatter.getNumberString(value, this)
                        + ", (float)" + getVariableName(source) + ", " + NumberFormatter.getNumberString(Properties.FLOAT_PRECISION, this) + ");";
            } else if (source.getVariableClass().equals(Double.class)) {
                stmt += "assertEquals(" + NumberFormatter.getNumberString(value, this)
                        + ", (double)" + getVariableName(source) + ", " + NumberFormatter.getNumberString(Properties.DOUBLE_PRECISION, this) + ");";
            } else if (value.getClass().isEnum()) {
                stmt += "assertEquals(" + NumberFormatter.getNumberString(value, this)
                        + ", " + getVariableName(source) + ");";
            } else
                stmt += "assertEquals(" + NumberFormatter.getNumberString(value, this)
                        + ", (" + NumberFormatter.getBoxedClassName(value) + ")"
                        + getVariableName(source) + ");";
        } else {
            stmt += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", "
                    + getVariableName(source) + ");";
        }

        testCode += stmt;
    }


    protected void visitArrayEqualsAssertion(ArrayEqualsAssertion assertion) {
        VariableReference source = assertion.getSource();
        Object[] value = (Object[]) assertion.getValue();

        String stmt = "";

        if (source.getComponentClass().equals(Boolean.class) || source.getComponentClass().equals(boolean.class)) {
            stmt += "assertTrue(Arrays.equals(";
            // Make sure that the Arrays class is imported
            getClassName(Arrays.class);
        } else {
            stmt += "assertArrayEquals(";
        }
        stmt += "new " + getTypeName(source.getComponentType()) + "[] {";
        boolean first = true;
        for (Object o : value) {
            if (!first)
                stmt += ", ";
            else
                first = false;

            stmt += NumberFormatter.getNumberString(o, this);

        }
        stmt += "}" + ", " + getVariableName(source);
        if (source.getComponentClass().equals(Float.class) || source.getComponentClass().equals(float.class))
            stmt += ", " + NumberFormatter.getNumberString(Properties.FLOAT_PRECISION, this) + ");";
        else if (source.getComponentClass().equals(Double.class) || source.getComponentClass().equals(double.class))
            stmt += ", " + NumberFormatter.getNumberString(Properties.DOUBLE_PRECISION, this) + ");";
        else if (source.getComponentClass().equals(Boolean.class) || source.getComponentClass().equals(boolean.class))
            stmt += "));";
        else
            stmt += ");";

        testCode += stmt;
    }

    protected void visitArrayLengthAssertion(ArrayLengthAssertion assertion) {
        VariableReference source = assertion.getSource();
        int length = assertion.length;

        String stmt = "assertEquals(";
        stmt += length + ", " + getVariableName(source) + ".length);";

        testCode += stmt;
    }

    protected void visitContainsAssertion(ContainsAssertion assertion) {
        VariableReference containerObject = assertion.getSource();
        VariableReference containedObject = assertion.getContainedVariable();

        Boolean contains = (Boolean) assertion.getValue();

        String stmt = "";
        if (contains) {
            stmt += "assertTrue(";
        } else {
            stmt += "assertFalse(";
        }
        stmt += getVariableName(containerObject) + ".contains(" + getVariableName(containedObject) + "));";

        testCode += stmt;
    }

    /**
     * <p>
     * visitPrimitiveFieldAssertion
     * </p>
     *
     * @param assertion a {@link org.evosuite.assertion.PrimitiveFieldAssertion}
     *                  object.
     */
    protected void visitPrimitiveFieldAssertion(PrimitiveFieldAssertion assertion) {
        VariableReference source = assertion.getSource();
        Object value = assertion.getValue();
        Field field = assertion.getField();

        String target = "";
        if (Modifier.isStatic(field.getModifiers())) {
            target = getClassName(field.getDeclaringClass()) + "." + field.getName();
        } else {
            target = getVariableName(source) + "." + field.getName();
        }

        if (value == null) {
            testCode += "assertNull(" + target
                    + ");";
        } else if (value.getClass().equals(Long.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", "
                    + target + ");";
        } else if (value.getClass().equals(Float.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", "
                    + target + ", " + NumberFormatter.getNumberString(Properties.FLOAT_PRECISION, this) + ");";
        } else if (value.getClass().equals(Double.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", "
                    + target + ", " + NumberFormatter.getNumberString(Properties.DOUBLE_PRECISION, this) + ");";
        } else if (value.getClass().equals(Character.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", "
                    + target + ");";
        } else if (value.getClass().equals(String.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", "
                    + target + ");";
        } else if (value.getClass().equals(Boolean.class)) {
            Boolean flag = (Boolean) value;
            if (flag) {
                testCode += "assertTrue(";
            } else {
                testCode += "assertFalse(";
            }
            testCode += "" + target + ");";
        } else if (value.getClass().isEnum()) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", "
                    + target + ");";
            // Make sure the enum is imported in the JUnit test
            getClassName(value.getClass());

        } else
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", "
                    + target + ");";
    }

    /**
     * <p>
     * visitInspectorAssertion
     * </p>
     *
     * @param assertion a {@link org.evosuite.assertion.InspectorAssertion} object.
     */
    protected void visitInspectorAssertion(InspectorAssertion assertion) {
        VariableReference source = assertion.getSource();
        Object value = assertion.getValue();
        Inspector inspector = assertion.getInspector();
        Class<?> generatedType = inspector.getReturnType();

        if (value == null) {
            testCode += "assertNull(" + getVariableName(source) + "."
                    + inspector.getMethodCall() + "());";
        } else if (value.getClass().equals(Long.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", ";
            if (ClassUtils.isPrimitiveWrapper(generatedType))
                testCode += "(long)";
            testCode += getVariableName(source) + "." + inspector.getMethodCall() + "());";
        } else if (value.getClass().equals(Short.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", ";
            if (ClassUtils.isPrimitiveWrapper(generatedType))
                testCode += "(short)";
            testCode += getVariableName(source) + "." + inspector.getMethodCall() + "());";
        } else if (value.getClass().equals(Integer.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", ";
            if (ClassUtils.isPrimitiveWrapper(generatedType))
                testCode += "(int)";
            testCode += getVariableName(source) + "." + inspector.getMethodCall() + "());";
        } else if (value.getClass().equals(Byte.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", ";
            if (ClassUtils.isPrimitiveWrapper(generatedType))
                testCode += "(byte)";
            testCode += getVariableName(source) + "." + inspector.getMethodCall() + "());";
        } else if (value.getClass().equals(Float.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", ";
            testCode += getVariableName(source) + "." + inspector.getMethodCall()
                    + "(), " + NumberFormatter.getNumberString(Properties.FLOAT_PRECISION, this) + ");";
        } else if (value.getClass().equals(Double.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", ";
            testCode += getVariableName(source) + "." + inspector.getMethodCall()
                    + "(), " + NumberFormatter.getNumberString(Properties.DOUBLE_PRECISION, this) + ");";
        } else if (value.getClass().equals(Character.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", ";
            if (ClassUtils.isPrimitiveWrapper(generatedType))
                testCode += "(char)";
            testCode += getVariableName(source) + "." + inspector.getMethodCall() + "());";
        } else if (value.getClass().equals(String.class)) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", ";
            testCode += getVariableName(source) + "." + inspector.getMethodCall() + "());";
        } else if (value.getClass().isEnum() || value instanceof Enum) {
            testCode += "assertEquals(" + NumberFormatter.getNumberString(value, this) + ", "
                    + getVariableName(source) + "." + inspector.getMethodCall() + "());";
            // Make sure the enum is imported in the JUnit test
            getClassName(value.getClass());

        } else if (value.getClass().equals(boolean.class) || value.getClass().equals(Boolean.class)) {
            if ((Boolean) value)
                testCode += "assertTrue(" + getVariableName(source) + "."
                        + inspector.getMethodCall() + "());";
            else
                testCode += "assertFalse(" + getVariableName(source) + "."
                        + inspector.getMethodCall() + "());";

        } else
            testCode += "assertEquals(" + value + ", " + getVariableName(source) + "."
                    + inspector.getMethodCall() + "());";
    }

    /**
     * <p>
     * visitNullAssertion
     * </p>
     *
     * @param assertion a {@link org.evosuite.assertion.NullAssertion} object.
     */
    protected void visitNullAssertion(NullAssertion assertion) {
        VariableReference source = assertion.getSource();
        Boolean value = (Boolean) assertion.getValue();
        if (value) {
            testCode += "assertNull(" + getVariableName(source) + ");";
        } else
            testCode += "assertNotNull(" + getVariableName(source) + ");";
    }

    /**
     * <p>
     * visitCompareAssertion
     * </p>
     *
     * @param assertion a {@link org.evosuite.assertion.CompareAssertion} object.
     */
    protected void visitCompareAssertion(CompareAssertion assertion) {
        VariableReference source = assertion.getSource();
        VariableReference dest = assertion.getDest();
        Object value = assertion.getValue();

        if (source.getType().equals(Integer.class)) {
            if ((Integer) value == 0)
                testCode += "assertTrue(" + getVariableName(source) + " == "
                        + getVariableName(dest) + ");";
            else if ((Integer) value < 0)
                testCode += "assertTrue(" + getVariableName(source) + " < "
                        + getVariableName(dest) + ");";
            else
                testCode += "assertTrue(" + getVariableName(source) + " > "
                        + getVariableName(dest) + ");";

        } else {
            testCode += "assertEquals(" + getVariableName(source) + ".compareTo("
                    + getVariableName(dest) + "), " + value + ");";
        }
    }

    /**
     * <p>
     * visitEqualsAssertion
     * </p>
     *
     * @param assertion a {@link org.evosuite.assertion.EqualsAssertion} object.
     */
    protected void visitEqualsAssertion(EqualsAssertion assertion) {
        VariableReference source = assertion.getSource();
        VariableReference dest = assertion.getDest();
        Object value = assertion.getValue();

        if (source.isPrimitive() || source.isWrapperType()) {
            if (source.getVariableClass().equals(float.class)) {
                if ((Boolean) value)
                    testCode += "assertEquals(" + getVariableName(source) + ", "
                            + getVariableName(dest) + ", " + NumberFormatter.getNumberString(Properties.FLOAT_PRECISION, this) + ");";
                else
                    testCode += "assertNotEquals(" + getVariableName(source) + ", "
                            + getVariableName(dest) + ", " + NumberFormatter.getNumberString(Properties.FLOAT_PRECISION, this) + ");";
            } else if (source.getVariableClass().equals(Float.class)) {
                if ((Boolean) value)
                    testCode += "assertEquals((float)" + getVariableName(source) + ", (float)"
                            + getVariableName(dest) + ", " + NumberFormatter.getNumberString(Properties.FLOAT_PRECISION, this) + ");";
                else
                    testCode += "assertNotEquals((float)" + getVariableName(source) + ", (float)"
                            + getVariableName(dest) + ", " + NumberFormatter.getNumberString(Properties.FLOAT_PRECISION, this) + ");";
            } else if (source.getVariableClass().equals(double.class)) {
                if ((Boolean) value)
                    testCode += "assertEquals(" + getVariableName(source) + ", "
                            + getVariableName(dest) + ", " + NumberFormatter.getNumberString(Properties.DOUBLE_PRECISION, this) + ");";
                else
                    testCode += "assertNotEquals(" + getVariableName(source) + ", "
                            + getVariableName(dest) + ", " + NumberFormatter.getNumberString(Properties.DOUBLE_PRECISION, this) + ");";
            } else if (source.getVariableClass().equals(Double.class)) {
                if ((Boolean) value)
                    testCode += "assertEquals((double)" + getVariableName(source) + ", (double)"
                            + getVariableName(dest) + ", " + NumberFormatter.getNumberString(Properties.DOUBLE_PRECISION, this) + ");";
                else
                    testCode += "assertNotEquals((double)" + getVariableName(source) + ", (double)"
                            + getVariableName(dest) + ", " + NumberFormatter.getNumberString(Properties.DOUBLE_PRECISION, this) + ");";
            } else if (source.isWrapperType()) {
                if ((Boolean) value)
                    testCode += "assertTrue(" + getVariableName(source) + ".equals((" + this.getClassName(Object.class) + ")"
                            + getVariableName(dest) + "));";
                else
                    testCode += "assertFalse(" + getVariableName(source) + ".equals((" + this.getClassName(Object.class) + ")"
                            + getVariableName(dest) + "));";
            } else if (dest.isWrapperType()) {
                if ((Boolean) value)
                    testCode += "assertTrue(" + getVariableName(dest) + ".equals((" + this.getClassName(Object.class) + ")"
                            + getVariableName(source) + "));";
                else
                    testCode += "assertFalse(" + getVariableName(dest) + ".equals((" + this.getClassName(Object.class) + ")"
                            + getVariableName(source) + "));";
            } else {
                if ((Boolean) value)
                    testCode += "assertTrue(" + getVariableName(source) + " == "
                            + getVariableName(dest) + ");";
                else
                    testCode += "assertFalse(" + getVariableName(source) + " == "
                            + getVariableName(dest) + ");";
            }
        } else {
            if ((Boolean) value)
                testCode += "assertTrue(" + getVariableName(source) + ".equals((" + this.getClassName(Object.class) + ")"
                        + getVariableName(dest) + "));";
            else
                testCode += "assertFalse(" + getVariableName(source) + ".equals((" + this.getClassName(Object.class) + ")"
                        + getVariableName(dest) + "));";
        }
    }

    /**
     * <p>
     * visitSameAssertion
     * </p>
     *
     * @param assertion a {@link org.evosuite.assertion.SameAssertion} object.
     */
    protected void visitSameAssertion(SameAssertion assertion) {
        VariableReference source = assertion.getSource();
        VariableReference dest = assertion.getDest();
        Object value = assertion.getValue();

        if ((Boolean) value)
            testCode += "assertSame(" + getVariableName(source) + ", "
                    + getVariableName(dest) + ");";
        else
            testCode += "assertNotSame(" + getVariableName(source) + ", "
                    + getVariableName(dest) + ");";
    }

    private String getUnstableTestComment() {
        return " // Unstable assertion";
    }

    private boolean isTestUnstable() {
        return test != null && test.isUnstable();
    }


    protected void visitAssertion(Assertion assertion) {

        if (isTestUnstable()) {
            /*
             * if the current test is unstable, then comment out all of its assertions.
             */
            testCode += "// " + getUnstableTestComment() + ": ";
        }

        if (assertion instanceof PrimitiveAssertion) {
            visitPrimitiveAssertion((PrimitiveAssertion) assertion);
        } else if (assertion instanceof PrimitiveFieldAssertion) {
            visitPrimitiveFieldAssertion((PrimitiveFieldAssertion) assertion);
        } else if (assertion instanceof InspectorAssertion) {
            visitInspectorAssertion((InspectorAssertion) assertion);
        } else if (assertion instanceof NullAssertion) {
            visitNullAssertion((NullAssertion) assertion);
        } else if (assertion instanceof CompareAssertion) {
            visitCompareAssertion((CompareAssertion) assertion);
        } else if (assertion instanceof EqualsAssertion) {
            visitEqualsAssertion((EqualsAssertion) assertion);
        } else if (assertion instanceof SameAssertion) {
            visitSameAssertion((SameAssertion) assertion);
        } else if (assertion instanceof ArrayEqualsAssertion) {
            visitArrayEqualsAssertion((ArrayEqualsAssertion) assertion);
        } else if (assertion instanceof ArrayLengthAssertion) {
            visitArrayLengthAssertion((ArrayLengthAssertion) assertion);
        } else if (assertion instanceof ContainsAssertion) {
            visitContainsAssertion((ContainsAssertion) assertion);
        } else {
            throw new RuntimeException("Unknown assertion type: " + assertion);
        }
        if (assertion.hasComment())
            testCode += assertion.getComment();
    }

    private void addAssertions(Statement statement) {
        boolean assertionAdded = false;
        if (getException(statement) != null) {
            // Assumption: The statement that throws an exception is the last statement of a test.
            VariableReference returnValue = statement.getReturnValue();
            for (Assertion assertion : statement.getAssertions()) {
                if (assertion != null
                        && !assertion.getReferencedVariables().contains(returnValue)) {
                    visitAssertion(assertion);
                    testCode += NEWLINE;
                    assertionAdded = true;
                }
            }
        } else {
            for (Assertion assertion : statement.getAssertions()) {
                if (assertion != null) {
                    visitAssertion(assertion);
                    testCode += NEWLINE;
                    assertionAdded = true;
                }
            }
        }
        if (assertionAdded)
            testCode += NEWLINE;
    }

    protected String getEnumValue(EnumPrimitiveStatement<?> statement) {
        Object value = statement.getValue();
        Class<?> clazz = statement.getEnumClass();
        String className = getClassName(clazz);

        try {
            if (value.getClass().getField(value.toString()) != null)
                return className + "." + value;

        } catch (NoSuchFieldException e) {
            // Ignore
        }

        for (Field field : value.getClass().getDeclaredFields()) {
            if (field.isEnumConstant()) {
                try {
                    if (field.get(value).equals(value)) {
                        return className + "." + field.getName();
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return className + ".valueOf(\"" + value + "\")";

    }



    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.testcase.TestVisitor#visitPrimitiveStatement(org.evosuite.testcase.PrimitiveStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitPrimitiveStatement(PrimitiveStatement<?> statement) {
        VariableReference retval = statement.getReturnValue();
        Object value = statement.getValue();

        if (statement instanceof StringPrimitiveStatement) {
            if (value == null) {
                testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
                        + getVariableName(retval) + " = null;" + NEWLINE;

            } else {
                String escapedString = StringUtil.getEscapedString((String) value);
                testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
                        + getVariableName(retval) + " = \"" + escapedString + "\";" + NEWLINE;
            }
            // testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
            // + getVariableName(retval) + " = \""
            // + StringEscapeUtils.escapeJava((String) value) + "\";\n";
        } else if (statement instanceof EnvironmentDataStatement) {
            testCode += ((EnvironmentDataStatement<?>) statement).getTestCode(getVariableName(retval));
        } else if (statement instanceof ClassPrimitiveStatement) {
            StringBuilder builder = new StringBuilder();
            String className = getClassName(retval);
            className = className.replaceAll("Class<(.*)(<.*>)>", "Class<$1>");
            builder.append(className);
            builder.append(" ");
            builder.append(getVariableName(retval));
            builder.append(" = ");
            builder.append(getClassName(((Class<?>) value)));
            builder.append(".class;");
            builder.append(NEWLINE);
            testCode += builder.toString();
        } else {
            testCode += getClassName(retval) + " " + getVariableName(retval) + " = "
                    + NumberFormatter.getNumberString(value, this) + ";" + NEWLINE;
        }
        addAssertions(statement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitPrimitiveExpression(PrimitiveExpression statement) {
        VariableReference retval = statement.getReturnValue();
        String expression = ((Class<?>) retval.getType()).getSimpleName() + " "
                + getVariableName(retval) + " = ";
        expression += getVariableName(statement.getLeftOperand()) + " "
                + statement.getOperator().toCode() + " "
                + getVariableName(statement.getRightOperand());
        testCode += expression + ";" + NEWLINE;
        addAssertions(statement);
    }



    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.testcase.TestVisitor#visitFieldStatement(org.evosuite.testcase.FieldStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitFieldStatement(FieldStatement statement) {
        Throwable exception = getException(statement);

        String cast_str = "";
        StringBuilder builder = new StringBuilder();

        VariableReference retval = statement.getReturnValue();
        GenericField field = statement.getField();

        if (!retval.isAssignableFrom(field.getFieldType())) {
            cast_str += "(" + getClassName(retval) + ")";
        }

        if (exception != null) {
            builder.append(getClassName(retval));
            builder.append(" ");
            builder.append(getVariableName(retval));
            builder.append(" = null;");
            builder.append(NEWLINE);
            builder.append("try {  ");
            builder.append(NEWLINE);
        } else {
            builder.append(getClassName(retval));
            builder.append(" ");
        }
        if (!field.isStatic()) {
            VariableReference source = statement.getSource();
            builder.append(getVariableName(retval));
            builder.append(" = ");
            builder.append(cast_str);
            builder.append(getVariableName(source));
        } else {
            builder.append(getVariableName(retval));
            builder.append(" = ");
            builder.append(cast_str);
            builder.append(getClassName(field.getField().getDeclaringClass()));
        }
        builder.append(".");
        builder.append(field.getName());
        builder.append(";");
        if (exception != null) {
            Class<?> ex = exception.getClass();
            while (!Modifier.isPublic(ex.getModifiers()))
                ex = ex.getSuperclass();
            builder.append(NEWLINE);
            builder.append("} catch(");
            builder.append(getClassName(ex));
            builder.append(" e) {}");
        }
        builder.append(NEWLINE);

        testCode += builder.toString();
        addAssertions(statement);
    }

    private String getPrimitiveNullCast(Class<?> declaredParamType) {
        String castString = "";
        castString += "(" + getTypeName(declaredParamType) + ") ";
        castString += "(" + getTypeName(ClassUtils.primitiveToWrapper(declaredParamType))
                + ") ";

        return castString;
    }

    private String getParameterString(Type[] parameterTypes,
                                      List<VariableReference> parameters, boolean isGenericMethod,
                                      boolean isOverloaded, int startPos) {
        String parameterString = "";

        for (int i = startPos; i < parameters.size(); i++) {
            if (i > startPos) {
                parameterString += ", ";
            }
            Type declaredParamType = parameterTypes[i];
            Type actualParamType = parameters.get(i).getType();
            String name = getVariableName(parameters.get(i));
            Class<?> rawParamClass = declaredParamType instanceof WildcardType ? Object.class : GenericTypeReflector.erase(declaredParamType);
            if (rawParamClass.isPrimitive() && name.equals("null")) {
                parameterString += getPrimitiveNullCast(rawParamClass);
            } else if (isGenericMethod && !(declaredParamType instanceof WildcardType)) {
                if (!declaredParamType.equals(actualParamType) || name.equals("null")) {
                    parameterString += "(" + getTypeName(declaredParamType) + ") ";
                    if (name.contains("(short"))
                        name = name.replace("(short)", "");
                    if (name.contains("(byte"))
                        name = name.replace("(byte)", "");

                }
            } else if (name.equals("null")) {
                parameterString += "(" + getTypeName(declaredParamType) + ") ";
            } else if (!GenericClassUtils.isAssignable(declaredParamType, actualParamType)) {

                if (TypeUtils.isArrayType(declaredParamType)
                        && TypeUtils.isArrayType(actualParamType)) {
                    Class<?> componentClass = GenericTypeReflector.erase(declaredParamType).getComponentType();
                    if (componentClass.equals(Object.class)) {
                        GenericClass<?> genericComponentClass = GenericClassFactory.get(componentClass);
                        if (genericComponentClass.hasWildcardOrTypeVariables()) {
                            // If we are assigning a generic array, then we don't need to cast

                        } else {
                            // If we are assigning a non-generic array, then we do need to cast
                            parameterString += "(" + getTypeName(declaredParamType)
                                    + ") ";
                        }
                    } else { //if (!GenericClass.isAssignable(GenericTypeReflector.getArrayComponentType(declaredParamType), GenericTypeReflector.getArrayComponentType(actualParamType))) {
                        parameterString += "(" + getTypeName(declaredParamType) + ") ";
                    }
                } else if (!(actualParamType instanceof ParameterizedType)) {
                    parameterString += "(" + getTypeName(declaredParamType) + ") ";
                }
                if (name.contains("(short"))
                    name = name.replace("(short)", "");
                if (name.contains("(byte"))
                    name = name.replace("(byte)", "");
                //}
            } else {
                // We have to cast between wrappers and primitives in case there
                // are overloaded signatures. This could be optimized by checking
                // if there actually is a problem of overloaded signatures
                GenericClass<?> parameterClass = GenericClassFactory.get(declaredParamType);
                if (parameterClass.isWrapperType() && parameters.get(i).isPrimitive()) {
                    parameterString += "(" + getTypeName(declaredParamType) + ") ";
                } else if (parameterClass.isPrimitive()
                        && parameters.get(i).isWrapperType()) {
                    parameterString += "(" + getTypeName(declaredParamType) + ") ";
                } else if (isOverloaded) {
                    // If there is an overloaded method, we need to cast to make sure we use the right version
                    if (!declaredParamType.equals(actualParamType)) {
                        parameterString += "(" + getTypeName(declaredParamType) + ") ";
                    }
                }
            }

            parameterString += name;
        }

        return parameterString;
    }


    @Override
    public void visitFunctionalMockStatement(FunctionalMockStatement st) {

        VariableReference retval = st.getReturnValue();

        // If it is not used, then minimizer will delete the statement anyway
//		boolean unused = test!=null && !test.hasReferences(retval);
//		if(unused){
//			//no point whatsoever in creating a mock that is never used
//			return;
//		}

        String result = "";

        //by construction, we should avoid cases like:
        //  Object obj = mock(Foo.class);
        //as it leads to problems when setting up "when(...)", and anyway it would make no sense
        Class<?> rawClass = GenericClassFactory.get(retval.getType()).getRawClass();
        Class<?> targetClass = st.getTargetClass();
        assert rawClass.getName().equals(targetClass.getName()) :
                "Mismatch between variable raw type " + rawClass + " and mocked " + targetClass;
        String rawClassName = getClassName(rawClass);


        //Foo foo = mock(Foo.class);
        String variableType = getClassName(retval);
        result += variableType + " " + getVariableName(retval);

        result += " = ";
        if (!variableType.equals(rawClassName)) {
            //this can happen in case of generics, eg
            //Foo<String> foo = (Foo<String>) mock(Foo.class);
            result += "(" + variableType + ") ";
        }

			/*
				Tricky situation. Ideally, we would want to throw assumption error if a non-mocked method
				is called, as to avoid false-positives when SUTs evolve.
				However, it might well be that a test case is not updated, leaving mocks using the default
				"null" return values. This would crash the JUnit check. Activating the  ViolatedAssumptionAnswer
				during the search would just make things worse, as negatively effecting the search.
				So we could just skip it, but this would effect false-positive preventions
			 */
        if (st.doesNeedToUpdateInputs()) {
            try {
                st.updateMockedMethods();
            } catch (Exception e) {
            }
            st.fillWithNullRefs();

            //result += "mock(" + rawClassName + ".class);" + NEWLINE;
        } else {
            //result += "mock(" + rawClassName + ".class, new " + ViolatedAssumptionAnswer.class.getSimpleName() + "());" + NEWLINE;
        }

        if (st instanceof FunctionalMockForAbstractClassStatement) {
            result += "mock(" + rawClassName + ".class, CALLS_REAL_METHODS);" + NEWLINE;
        } else {
            result += "mock(" + rawClassName + ".class, new " + ViolatedAssumptionAnswer.class.getSimpleName() + "());" + NEWLINE;
        }

        //when(...).thenReturn(...)
        for (MethodDescriptor md : st.getMockedMethods()) {
            if (!md.shouldBeMocked()) {
                continue;
            }

            List<VariableReference> params = st.getParameters(md.getID());

            GenericClass<?> returnType = md.getReturnClass();
            // Class<?> returnType = md.getMethod().getReturnType();

            String parameter_string;

            if (!returnType.isPrimitive()) {
                Type[] types = new Type[params.size()];
                boolean isOverloaded = false;
                for (int i = 0; i < types.length; i++) {
                    if (types.length > 1 && returnType.isArray()) {
                        types[i] = Object.class;
                        isOverloaded = true;
                    } else {
                        types[i] = params.get(i).getType();
                    }
                }

                parameter_string = getParameterString(types, params, false, isOverloaded, 0);//TODO unsure of these parameters
            } else {

                //if return type is a primitive, then things can get complicated due to autoboxing :(

                parameter_string = getParameterStringForFMthatReturnPrimitive(returnType.getRawClass(), params);
            }

            //this does not work when throwing exception as default answer
//			result += "when("+getVariableName(retval)+"."+md.getMethodName()+"("+md.getInputParameterMatchers()+"))";
//			result += ".thenReturn( ";
//			result += parameter_string + " );"+NEWLINE;

            // Mockito doReturn() only takes single arguments. So we need to make sure that in the generated
            // tests we import MockitoExtension class
            //parameter_string = "doReturn(" + parameter_string.replaceAll(", ", ").doReturn(") + ")";
            //result += parameter_string+".when("+getVariableName(retval)+")";
            result += "doReturn(" + parameter_string + ").when(" + getVariableName(retval) + ")";
            result += "." + md.getMethodName() + "(" + md.getInputParameterMatchers() + ");";
            result += NEWLINE;
        }

        testCode += result;
    }

    private String getParameterStringForFMthatReturnPrimitive(Class<?> returnType, List<VariableReference> parameters) {

        assert returnType.isPrimitive();
        String parameterString = "";

        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                parameterString += ", ";
            }
            String name = getVariableName(parameters.get(i));
            Class<?> parameterType = parameters.get(i).getVariableClass();

            if (returnType.equals(parameterType)) {
                parameterString += name;
                continue;
            }

            GenericClass<?> parameterClass = GenericClassFactory.get(parameterType);
            if (parameterClass.isWrapperType()) {

                boolean isRightWrapper = false;

                if (Integer.class.equals(parameterClass.getRawClass())) {
                    isRightWrapper = returnType.equals(Integer.TYPE);
                } else if (Character.class.equals(parameterClass.getRawClass())) {
                    isRightWrapper = returnType.equals(Character.TYPE);
                } else if (Boolean.class.equals(parameterClass.getRawClass())) {
                    isRightWrapper = returnType.equals(Boolean.TYPE);
                } else if (Float.class.equals(parameterClass.getRawClass())) {
                    isRightWrapper = returnType.equals(Float.TYPE);
                } else if (Double.class.equals(parameterClass.getRawClass())) {
                    isRightWrapper = returnType.equals(Double.TYPE);
                } else if (Long.class.equals(parameterClass.getRawClass())) {
                    isRightWrapper = returnType.equals(Long.TYPE);
                } else if (Short.class.equals(parameterClass.getRawClass())) {
                    isRightWrapper = returnType.equals(Short.TYPE);
                } else if (Byte.class.equals(parameterClass.getRawClass())) {
                    isRightWrapper = returnType.equals(Byte.TYPE);
                }

                if (isRightWrapper) {
                    parameterString += name;
                    continue;
                }
            }

            //if we arrive here, it means types are different and not a right wrapper (eg Integer for int)
            parameterString += "(" + returnType.getName() + ")" + name;

            if (parameterClass.isWrapperType()) {
                if (Integer.class.equals(parameterClass.getRawClass())) {
                    parameterString += ".intValue()";
                } else if (Character.class.equals(parameterClass.getRawClass())) {
                    parameterString += ".charValue()";
                } else if (Boolean.class.equals(parameterClass.getRawClass())) {
                    parameterString += ".booleanValue()";
                } else if (Float.class.equals(parameterClass.getRawClass())) {
                    parameterString += ".floatValue()";
                } else if (Double.class.equals(parameterClass.getRawClass())) {
                    parameterString += ".doubleValue()";
                } else if (Long.class.equals(parameterClass.getRawClass())) {
                    parameterString += ".longValue()";
                } else if (Short.class.equals(parameterClass.getRawClass())) {
                    parameterString += ".shortValue()";
                } else if (Byte.class.equals(parameterClass.getRawClass())) {
                    parameterString += ".byteValue()";
                }
            }
        }


        return parameterString;
    }



    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.testcase.TestVisitor#visitMethodStatement(org.evosuite.testcase.MethodStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitMethodStatement(MethodStatement statement) {
        String result = "";
        VariableReference retval = statement.getReturnValue();
        GenericMethod method = statement.getMethod();
        Throwable exception = getException(statement);
        List<VariableReference> parameters = statement.getParameterReferences();
        boolean isGenericMethod = method.hasTypeParameters();

        if (exception != null && !statement.isDeclaredException(exception)) {
            result += "// Undeclared exception!" + NEWLINE;
        }

        boolean lastStatement = statement.getPosition() == statement.getTestCase().size() - 1;
        boolean unused = !Properties.ASSERTIONS ? exception != null : test != null
                && !test.hasReferences(retval);

        if (!retval.isVoid() && retval.getAdditionalVariableReference() == null
                && !unused) {
            if (exception != null) {
                if (!lastStatement || statement.hasAssertions())
                    result += getClassName(retval) + " " + getVariableName(retval)
                            + " = " + retval.getDefaultValueString() + ";" + NEWLINE;
            } else {
                result += getClassName(retval) + " ";
            }
        }
        if (shouldUseTryCatch(exception, statement.isDeclaredException(exception))) {
            result += "try { " + NEWLINE + "  ";
        }


        String parameter_string = getParameterString(method.getParameterTypes(),
                parameters, isGenericMethod,
                method.isOverloaded(parameters), 0);

        String callee_str = "";
        if (!unused && !retval.isAssignableFrom(method.getReturnType())
                && !retval.getVariableClass().isAnonymousClass()
                // Static generic methods are a special case where we shouldn't add a cast
                && !(isGenericMethod && method.getParameterTypes().length == 0 && method.isStatic())) {
            String name = getClassName(retval);
            if (!name.matches(".*\\.\\d+$")) {
                callee_str = "(" + name + ")";
            }
        }
        if (method.isStatic()) {
            callee_str += getClassName(method.getMethod().getDeclaringClass());
        } else {
            VariableReference callee = statement.getCallee();

            if (callee instanceof ConstantValue) {
                callee_str += "((" + getClassName(method.getMethod().getDeclaringClass())
                        + ")" + getVariableName(callee) + ")";
            } else {
                // If the method is not public and this is a subclass in a different package we need to cast
                if (!method.isPublic() && !method.getDeclaringClass().equals(callee.getVariableClass()) && callee.isAssignableTo(method.getMethod().getDeclaringClass())) {
                    String packageName1 = ClassUtils.getPackageName(method.getDeclaringClass());
                    String packageName2 = ClassUtils.getPackageName(callee.getVariableClass());
                    if (!packageName1.equals(packageName2)) {
                        callee_str += "((" + getClassName(method.getMethod().getDeclaringClass())
                                + ")" + getVariableName(callee) + ")";
                    } else {
                        callee_str += getVariableName(callee);
                    }
                } else if (!callee.isAssignableTo(method.getMethod().getDeclaringClass())) {
                    try {
                        // If the concrete callee class has that method then it's ok
                        callee.getVariableClass().getDeclaredMethod(method.getName(), method.getRawParameterTypes());
                        callee_str += getVariableName(callee);
                    } catch (NoSuchMethodException e) {
                        // If not we need to cast to the subtype
                        callee_str += "((" + getTypeName(method.getMethod().getDeclaringClass()) + ") " + getVariableName(callee) + ")";
                        // TODO: Here we could check if this is actually possible
                        // ...but what would we do?
                        // if(!ClassUtils.getAllSuperclasses(method.getMethod().getDeclaringClass()).contains(callee.getVariableClass())) {
                        //}
                    }
                } else {
                    callee_str += getVariableName(callee);
                }
            }
        }

        if (retval.isVoid()) {
            result += callee_str + "." + method.getName() + "(" + parameter_string + ");";
        } else {
            // if (exception == null || !lastStatement)
            if (!unused)
                result += getVariableName(retval) + " = ";
            // If unused, then we don't want to print anything:
            //else
            //	result += getClassName(retval) + " " + getVariableName(retval) + " = ";

            result += callee_str + "." + method.getName() + "(" + parameter_string + ");";
        }

        if (shouldUseTryCatch(exception, statement.isDeclaredException(exception))) {
            if (Properties.ASSERTIONS) {
                result += generateFailAssertion(statement, exception);
            }

            result += NEWLINE + "}";// end try block

            result += generateCatchBlock(statement, exception);
        }

        testCode += result + NEWLINE;
        addAssertions(statement);
    }

    /**
     * Returns a catch block for an exception that can be thrown by this
     * statement. The caught exception type is the actual class of the exception
     * object passed as parameter (or one of its superclass if the type is not
     * public). This method can be overridden to inject code in the catch block
     **/
    public String generateCatchBlock(AbstractStatement statement, Throwable exception) {
        String result = "";

        Class<?> ex = getExceptionClassToUse(exception);

        // preparing the catch block
        if (!(exception instanceof RuntimeException) && !(exception instanceof Error)) {
            // This is a checked exception.
            if (statement.isDeclaredException(exception)) {
                result += " catch(" + getClassName(ex) + " e) {" + NEWLINE;
            } else {
                // A checked exception that is not declared cannot be thrown according to the JVM spec.
                // And yet, it is possible, which is probably a bug in Java. See class org.apache.commons.lang3.time.FastDatePrinter:
                //     @Override
                //     public <B extends Appendable> B format(final Date date, final B buf) {
                //     	final Calendar c = newCalendar();  // hard code GregorianCalendar
                //     	c.setTime(date);
                //     	return applyRules(c, buf);
                //     }
                // Passing in a PipeWriter will lead to an IOException.
                // As a workaround, we'll just check for Throwable
                //
                result += " catch(" + getClassName(Throwable.class) + " e) {" + NEWLINE;
            }
        } else {
            result += " catch(" + getClassName(ex) + " e) {" + NEWLINE;
        }

        // adding the message of the exception
        String exceptionMessage;
        try {
            if (exception.getMessage() != null) {
                exceptionMessage = exception.getMessage().replace("*/", "*_/");
            } else {
                exceptionMessage = "no message in exception (getMessage() returned null)";
            }
        } catch (Exception exceptionThownExecutionGetMessage) {
            exceptionMessage = "no message (getMessage() has thrown an exception)";
        }

        String sourceClass = getSourceClassName(exception);

        if (sourceClass == null || isValidSource(sourceClass)) {
			/*
				do not print comments if it was a non-valid source.
				however, if source is undefined, then it should be OK
			 */
            result += "   //" + NEWLINE;
            for (String msg : exceptionMessage.split("\n")) {
                result += "   // " + StringEscapeUtils.escapeJava(msg) + NEWLINE;
            }
            result += "   //" + NEWLINE;
        }

        if (sourceClass != null && isValidSource(sourceClass) && isExceptionToAssertThrownBy(ex) && !Properties.NO_RUNTIME_DEPENDENCY) {
				/*
					do not check source if it comes from a non-runtime evosuite
					class. this could happen if source is an instrumentation done
					during search which is not applied to runtime
				 */

            //from class EvoAssertions
            result += "   verifyException(\"" + sourceClass + "\", e);" + NEWLINE;
        }

        result += "}" + NEWLINE;// closing the catch block
        return result;
    }

    private String getSourceClassName(Throwable exception) {
        if (exception.getStackTrace().length == 0) {
            return null;
        }
        return exception.getStackTrace()[0].getClassName();
    }

    private boolean isValidSource(String sourceClass) {
        return (!sourceClass.startsWith(PackageInfo.getEvoSuitePackage() + ".") ||
                sourceClass.startsWith(PackageInfo.getEvoSuitePackage() + ".runtime.")) &&
                !sourceClass.equals(URLClassLoader.class.getName()) && // Classloaders may differ, e.g. when running with ant
                !sourceClass.startsWith(RegExp.class.getPackage().getName()) &&
                !sourceClass.startsWith("java.lang.System") &&
                !sourceClass.startsWith("java.lang.String") &&
                !sourceClass.startsWith("java.lang.Class") &&
                !sourceClass.startsWith("sun.") &&
                !sourceClass.startsWith("com.sun.") &&
                !sourceClass.startsWith("jdk.internal.") &&
                !sourceClass.startsWith("<evosuite>");
    }

    private final List<Class<?>> invalidExceptions = Arrays.asList(new Class<?>[]{
            StackOverflowError.class, // Might be thrown at different places
            AssertionError.class}     // Depends whether assertions are enabled or not
    );

    private boolean isExceptionToAssertThrownBy(Class<?> exceptionClass) {
        return !invalidExceptions.contains(exceptionClass);
    }

    private Class<?> getExceptionClassToUse(Throwable exception) {
        /*
            we can only catch a public class.
            for "readability" of tests, it shouldn't be a mock one either
          */
        Class<?> ex = exception.getClass();
        while (!Modifier.isPublic(ex.getModifiers()) || EvoSuiteMock.class.isAssignableFrom(ex) ||
                ex.getCanonicalName().startsWith("com.sun.")) {
            ex = ex.getSuperclass();
        }
        return ex;
    }

    private String getSimpleTypeName(Type type) {
        String typeName = getTypeName(type);
        int dotIndex = typeName.lastIndexOf(".");
        if (dotIndex >= 0 && (dotIndex + 1) < typeName.length()) {
            typeName = typeName.substring(dotIndex + 1);
        }

        return typeName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.testcase.TestVisitor#visitConstructorStatement(org.evosuite.testcase.ConstructorStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitConstructorStatement(ConstructorStatement statement) {
        String result = "";
        GenericConstructor constructor = statement.getConstructor();
        VariableReference retval = statement.getReturnValue();
        Throwable exception = getException(statement);
        boolean isGenericConstructor = constructor.hasTypeParameters();
        boolean isNonStaticMemberClass = constructor.getConstructor().getDeclaringClass().isMemberClass()
                && !constructor.isStatic()
                && !Modifier.isStatic(constructor.getConstructor().getDeclaringClass().getModifiers());

        List<VariableReference> parameters = statement.getParameterReferences();
        int startPos = 0;
        if (isNonStaticMemberClass) {
            startPos = 1;
        }
        Type[] parameterTypes = constructor.getParameterTypes();
        String parameterString = getParameterString(parameterTypes, parameters,
                isGenericConstructor,
                constructor.isOverloaded(parameters),
                startPos);

        if (shouldUseTryCatch(exception, statement.isDeclaredException(exception))) {
            String className = getClassName(retval);

            // FIXXME: Workaround for primitives:
            // But really, this can't really add any coverage, so we shouldn't be printing this in the first place!
            if (retval.isPrimitive()) {
                className = retval.getGenericClass().getUnboxedType().getSimpleName();
            }

            result = className + " " + getVariableName(retval) + " = null;" + NEWLINE;
            result += "try {" + NEWLINE + "  ";
        } else {
            result += getClassName(retval) + " ";
        }

        if (isNonStaticMemberClass) {
            result += getVariableName(retval) + " = "
                    + getVariableName(parameters.get(0))
                    + ".new "
                    + getSimpleTypeName(constructor.getOwnerType()) + "("
                    + parameterString + ");";
        } else {
            result += getVariableName(retval) + " = new "
                    + getTypeName(constructor.getOwnerType())
                    + "(" + parameterString + ");";
        }

        if (shouldUseTryCatch(exception, statement.isDeclaredException(exception))) {
            if (Properties.ASSERTIONS) {
                result += generateFailAssertion(statement, exception);
            }

            result += NEWLINE + "}";// end try block

            result += generateCatchBlock(statement, exception);
        }

        testCode += result + NEWLINE;
        addAssertions(statement);
    }

    private boolean shouldUseTryCatch(Throwable t, boolean isDeclared) {
        return t != null
                && !(t instanceof OutOfMemoryError)
                && !(t instanceof TooManyResourcesException)
                && !test.isFailing()
                && (Properties.CATCH_UNDECLARED_EXCEPTIONS || isDeclared);
    }

    /**
     * Generates a fail assertion for being inserted after a statement
     * generating an exception. Parameter "statement" is not used in the default
     * implementation but may be used in future extensions.
     **/
    public String generateFailAssertion(AbstractStatement statement, Throwable exception) {
        Class<?> ex = getExceptionClassToUse(exception);

        // boolean isExpected = getDeclaredExceptions().contains(ex);
        // if (isExpected)

        String stmt = " fail(\"Expecting exception: " + getClassName(ex) + "\");" + NEWLINE;

        if (isTestUnstable()) {
            /*
             * if the current test is unstable, then comment out all of its assertions.
             */
            stmt = "// " + stmt + getUnstableTestComment();
        }

        return NEWLINE + " " + stmt;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.testcase.TestVisitor#visitArrayStatement(org.evosuite.testcase.ArrayStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitArrayStatement(ArrayStatement statement) {
        VariableReference retval = statement.getReturnValue();
        List<Integer> lengths = statement.getLengths();

        String type = getClassName(retval);
        String multiDimensions = "";
        if (lengths.size() == 1) {
            type = type.replaceFirst("\\[\\]", "");
            multiDimensions = "[" + lengths.get(0) + "]";
            while (type.contains("[]")) {
                multiDimensions += "[]";
                type = type.replaceFirst("\\[\\]", "");
            }
        } else {
            type = type.replaceAll("\\[\\]", "");
            for (int length : lengths) {
                multiDimensions += "[" + length + "]";
            }
        }

        if (retval.getGenericClass().isGenericArray()) {
            if (lengths.size() > 1) {
                multiDimensions = "new int[] {" + lengths.get(0);
                for (int i = 1; i < lengths.size(); i++)
                    multiDimensions += ", " + lengths.get(i);
                multiDimensions += "}";
            } else {
                multiDimensions = "" + lengths.get(0);
            }

            testCode += getClassName(retval) + " " + getVariableName(retval) + " = ("
                    + getClassName(retval) + ") " + getClassName(Array.class)
                    + ".newInstance("
                    + getClassName(retval.getComponentClass()).replaceAll("\\[\\]", "")
                    + ".class, " + multiDimensions + ");" + NEWLINE;

        } else {
            testCode += getClassName(retval) + " " + getVariableName(retval) + " = new "
                    + type + multiDimensions + ";" + NEWLINE;
        }
        addAssertions(statement);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.testcase.TestVisitor#visitAssignmentStatement(org.evosuite.testcase.AssignmentStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitAssignmentStatement(AssignmentStatement statement) {
        String cast = "";
        VariableReference retval = statement.getReturnValue();
        VariableReference parameter = statement.getValue();

        if (!retval.getVariableClass().equals(parameter.getVariableClass())) {
            if (retval.isWrapperType() && parameter.isPrimitive()) {
                cast = "(" + getTypeName(retval.getType()) + ") ";
                if (!ClassUtils.primitiveToWrapper(parameter.getVariableClass()).equals(retval.getVariableClass())) {
                    cast += "(" + ClassUtils.wrapperToPrimitive(retval.getVariableClass()) + ")";
                }

            } else if (retval.isPrimitive()
                    && parameter.isWrapperType()) {
                cast = "(" + getTypeName(retval.getType()) + ") ";
                if (!ClassUtils.primitiveToWrapper(retval.getVariableClass()).equals(parameter.getVariableClass())) {
                    cast += "(" + ClassUtils.wrapperToPrimitive(parameter.getVariableClass()) + ")";
                }
            } else if (retval.isWrapperType()
                    && parameter.isWrapperType()) {
                cast = "(" + getTypeName(retval.getType()) + ") ";
                // Unbox first to make cast work
                if (!ClassUtils.primitiveToWrapper(parameter.getVariableClass()).equals(retval.getVariableClass())) {
                    cast += "(" + ClassUtils.wrapperToPrimitive(retval.getVariableClass()) + ")";
                }
            } else {
                cast = "(" + getClassName(retval) + ") ";
            }
        }

        testCode += getVariableName(retval) + " = " + cast + getVariableName(parameter)
                + ";" + NEWLINE;
        addAssertions(statement);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.testcase.TestVisitor#visitNullStatement(org.evosuite.testcase.NullStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitNullStatement(NullStatement statement) {
        VariableReference retval = statement.getReturnValue();

        testCode += getClassName(retval) + " " + getVariableName(retval) + " = null;" + NEWLINE;
    }

    @Override
    public void visitStatement(Statement statement) {
        if (!statement.getComment().isEmpty()) {
            String comment = statement.getComment();
            for (String line : comment.split("\n")) {
                testCode += "// " + line + NEWLINE;
            }
        }
        super.visitStatement(statement);
    }


}
