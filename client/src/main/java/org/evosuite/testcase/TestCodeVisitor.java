/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.assertion.ArrayEqualsAssertion;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.CompareAssertion;
import org.evosuite.assertion.EqualsAssertion;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.assertion.NullAssertion;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.assertion.PrimitiveFieldAssertion;
import org.evosuite.assertion.SameAssertion;
import org.evosuite.classpath.ResourceList;
import org.evosuite.parameterize.InputVariable;
import org.evosuite.runtime.EvoSuiteFile;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericField;
import org.evosuite.utils.GenericMethod;
import org.evosuite.utils.NumberFormatter;

import com.googlecode.gentyref.CaptureType;
import com.googlecode.gentyref.GenericTypeReflector;

/**
 * The TestCodeVisitor is a visitor that produces a String representation of a
 * test case. This is the preferred way to produce executable code from EvoSuite
 * tests.
 * 
 * @author Gordon Fraser
 */
public class TestCodeVisitor extends TestVisitor {

	protected String testCode = "";

	protected final Map<Integer, Throwable> exceptions = new HashMap<Integer, Throwable>();

	protected TestCase test = null;

	protected final Map<VariableReference, String> variableNames = new HashMap<VariableReference, String>();

	protected final Map<Class<?>, String> classNames = new HashMap<Class<?>, String>();

	protected final Map<String, Integer> nextIndices = new HashMap<String, Integer>();

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
		Set<Class<?>> imports = new HashSet<Class<?>>();
		for (Class<?> clazz : classNames.keySet()) {
			String name = classNames.get(clazz);
			// If there's a dot in the name, then we assume this is the
			// fully qualified name and we don't need to import
			if (!name.contains(".")) {
				imports.add(clazz);
			}
		}
		return imports;
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
	 * @param exceptions
	 *            a {@link java.util.Map} object.
	 */
	public void setExceptions(Map<Integer, Throwable> exceptions) {
		this.exceptions.putAll(exceptions);
	}

	/**
	 * <p>
	 * setException
	 * </p>
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.StatementInterface} object.
	 * @param exception
	 *            a {@link java.lang.Throwable} object.
	 */
	public void setException(StatementInterface statement, Throwable exception) {
		exceptions.put(statement.getPosition(), exception);
	}

	/**
	 * <p>
	 * getException
	 * </p>
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.StatementInterface} object.
	 * @return a {@link java.lang.Throwable} object.
	 */
	protected Throwable getException(StatementInterface statement) {
		if (exceptions != null && exceptions.containsKey(statement.getPosition()))
			return exceptions.get(statement.getPosition());

		return null;
	}

	/**
	 * <p>
	 * getClassName
	 * </p>
	 * 
	 * @param var
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName(VariableReference var) {
		return getTypeName(var.getType());
	}

	private String getTypeName(ParameterizedType type) {
		String name = getClassName((Class<?>) type.getRawType());
		Type[] types = type.getActualTypeArguments();
		boolean isDefined = false;
		for(Type parameterType : types) {
			if(parameterType instanceof Class<?> ||
					parameterType instanceof ParameterizedType ||
					parameterType instanceof WildcardType ||
					parameterType instanceof GenericArrayType) {
				isDefined = true;
				break;
			}
		}
		if(isDefined) {
			if (types.length > 0) {
				name += "<";
				for (int i = 0; i < types.length; i++) {
					if (i != 0)
						name += ", ";

					name += getTypeName(types[i]);
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
			String ret = "?";
			boolean first = true;
			for (Type bound : ((WildcardType) type).getLowerBounds()) {
				// If there are lower bounds we need to state them, even if Object
				if (bound == null) // || GenericTypeReflector.erase(bound).equals(Object.class))
					continue;

				if (!first)
					ret += ", ";
				ret += " super " + getTypeName(bound);
				first = false;
			}
			for (Type bound : ((WildcardType) type).getUpperBounds()) {
				if (bound == null
				        || (!(bound instanceof CaptureType) && GenericTypeReflector.erase(bound).equals(Object.class)))
					continue;

				if (!first)
					ret += ", ";
				ret += " extends " + getTypeName(bound);
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

		GenericClass clazz = var.getGenericClass();
		return getTypeName(clazz.getType());
	}

	/**
	 * <p>
	 * getClassName
	 * </p>
	 * 
	 * @param clazz
	 *            a {@link java.lang.Class} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName(Class<?> clazz) {
		if (classNames.containsKey(clazz))
			return classNames.get(clazz);

		if (clazz.isArray()) {
			return getClassName(clazz.getComponentType()) + "[]";
		}

		GenericClass c = new GenericClass(clazz);
		String name = c.getSimpleName();
		if (classNames.values().contains(name)) {
			name = clazz.getCanonicalName();
		} else {
			/*
			 * If e.g. there is a foo.bar.IllegalStateException with
			 * foo.bar being the SUT package, then we need to use the
			 * full package name for java.lang.IllegalStateException
			 */
			String fullName = Properties.CLASS_PREFIX +"."+name;
			if(!fullName.equals(clazz.getCanonicalName())) {
				try {
					if(ResourceList.hasClass(fullName)) {
						name = clazz.getCanonicalName();
					}
				} catch(IllegalArgumentException e) {
					// If the classpath is not correct, then we just don't check 
					// because that cannot happen in regular EvoSuite use, only
					// from test cases
				}
			}
		}

		// Ensure outer classes are imported as well
		Class<?> outerClass = clazz.getEnclosingClass();
		if(outerClass != null) {
			String enclosingName = getClassName(outerClass);
			String simpleOuterName = outerClass.getSimpleName();
			if(simpleOuterName.equals(enclosingName)) {
				name = enclosingName + name.substring(simpleOuterName.length());
			}
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
	 * @param var
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getVariableName(VariableReference var) {
		if (var instanceof ConstantValue) {
			return var.getName();
		} else if (var instanceof InputVariable) {
			return var.getName();
		} else if (var instanceof FieldReference) {
			VariableReference source = ((FieldReference) var).getSource();
			GenericField field = ((FieldReference) var).getField();
			if (source != null)
				return getVariableName(source) + "." + field.getName();
			else
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
			variableName = variableName.replace(".", "_").replace("[]", "");
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
	/** {@inheritDoc} */
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
	 * @param assertion
	 *            a {@link org.evosuite.assertion.PrimitiveAssertion} object.
	 */
	protected void visitPrimitiveAssertion(PrimitiveAssertion assertion) {
		VariableReference source = assertion.getSource();
		Object value = assertion.getValue();

		String stmt = "";
		
		if (value == null) {
			stmt += "assertNull(" + getVariableName(source) + ");";
		} else if (source.getVariableClass().equals(float.class)) {
			stmt += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + ", 0.01F);";
		} else if (source.getVariableClass().equals(double.class)) {
			stmt += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + ", 0.01D);";
		} else if (value.getClass().isEnum()) {
			stmt += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + ");";
			// Make sure the enum is imported in the JUnit test
			getClassName(value.getClass());

		} else if (source.isWrapperType()) {
			if (source.getVariableClass().equals(Float.class)) {
				stmt += "assertEquals(" + NumberFormatter.getNumberString(value)
				        + ", (float)" + getVariableName(source) + ", 0.01F);";
			} else if (source.getVariableClass().equals(Double.class)) {
				stmt += "assertEquals(" + NumberFormatter.getNumberString(value)
				        + ", (double)" + getVariableName(source) + ", 0.01D);";
			} else if (value.getClass().isEnum()) {
				stmt += "assertEquals(" + NumberFormatter.getNumberString(value)
				        + ", " + getVariableName(source) + ");";
			} else
				stmt += "assertEquals(" + NumberFormatter.getNumberString(value)
				        + ", (" + NumberFormatter.getBoxedClassName(value) + ")"
				        + getVariableName(source) + ");";
		} else {
			stmt += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + ");";
		}
						
		testCode += stmt; 
	}


	
	protected void visitArrayEqualsAssertion(ArrayEqualsAssertion assertion) {
		VariableReference source = assertion.getSource();
		Object[] value = (Object[]) assertion.getValue();

		String stmt = "";
		
		if(source.getComponentClass().equals(Boolean.class) || source.getComponentClass().equals(boolean.class)) {
			stmt += "assertTrue(Arrays.equals(";
			// Make sure that the Arrays class is imported
			getClassName(Arrays.class);
		} else {
			stmt += "assertArrayEquals(";
		}
		stmt += "new "+getTypeName(source.getComponentType()) + "[] {";
		boolean first = true;
		for (Object o : value) {
			if (!first)
				stmt += ", ";
			else
				first = false;

			stmt += NumberFormatter.getNumberString(o);

		}
		stmt += "}" + ", " + getVariableName(source);
		if(source.getComponentClass().equals(Float.class) || source.getComponentClass().equals(float.class))
			stmt += ", 0.01F);";
		else if(source.getComponentClass().equals(Double.class) || source.getComponentClass().equals(double.class))
			stmt += ", 0.01);";
		else if(source.getComponentClass().equals(Boolean.class) || source.getComponentClass().equals(boolean.class))
			stmt += "));";
		else
			stmt += ");";
		
		testCode += stmt;
	}

	/**
	 * <p>
	 * visitPrimitiveFieldAssertion
	 * </p>
	 * 
	 * @param assertion
	 *            a {@link org.evosuite.assertion.PrimitiveFieldAssertion}
	 *            object.
	 */
	protected void visitPrimitiveFieldAssertion(PrimitiveFieldAssertion assertion) {
		VariableReference source = assertion.getSource();
		Object value = assertion.getValue();
		Field field = assertion.getField();

		if (value == null) {
			testCode += "assertNull(" + getVariableName(source) + "." + field.getName()
			        + ");";
		} else if (value.getClass().equals(Long.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ");";
		} else if (value.getClass().equals(Float.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ", 0.01F);";
		} else if (value.getClass().equals(Double.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ", 0.01D);";
		} else if (value.getClass().equals(Character.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ");";
		} else if (value.getClass().equals(String.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ");";
		} else if (value.getClass().isEnum()) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ");";
			// Make sure the enum is imported in the JUnit test
			getClassName(value.getClass());

		} else
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ");";
	}

	/**
	 * <p>
	 * visitInspectorAssertion
	 * </p>
	 * 
	 * @param assertion
	 *            a {@link org.evosuite.assertion.InspectorAssertion} object.
	 */
	protected void visitInspectorAssertion(InspectorAssertion assertion) {
		VariableReference source = assertion.getSource();
		Object value = assertion.getValue();
		Inspector inspector = assertion.getInspector();

		if (value == null) {
			testCode += "assertNull(" + getVariableName(source) + "."
			        + inspector.getMethodCall() + "());";
		} else if (value.getClass().equals(Long.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + inspector.getMethodCall() + "());";
		} else if (value.getClass().equals(Float.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + inspector.getMethodCall()
			        + "(), 0.01F);";
		} else if (value.getClass().equals(Double.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + inspector.getMethodCall()
			        + "(), 0.01D);";
		} else if (value.getClass().equals(Character.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + inspector.getMethodCall() + "());";
		} else if (value.getClass().equals(String.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + inspector.getMethodCall() + "());";
		} else if (value.getClass().isEnum() || value instanceof Enum) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + inspector.getMethodCall() + "());";
			// Make sure the enum is imported in the JUnit test
			getClassName(value.getClass());

		} else
			testCode += "assertEquals(" + value + ", " + getVariableName(source) + "."
			        + inspector.getMethodCall() + "());";
	}

	/**
	 * <p>
	 * visitNullAssertion
	 * </p>
	 * 
	 * @param assertion
	 *            a {@link org.evosuite.assertion.NullAssertion} object.
	 */
	protected void visitNullAssertion(NullAssertion assertion) {
		VariableReference source = assertion.getSource();
		Boolean value = (Boolean) assertion.getValue();
		if (value.booleanValue()) {
			testCode += "assertNull(" + getVariableName(source) + ");";
		} else
			testCode += "assertNotNull(" + getVariableName(source) + ");";
	}

	/**
	 * <p>
	 * visitCompareAssertion
	 * </p>
	 * 
	 * @param assertion
	 *            a {@link org.evosuite.assertion.CompareAssertion} object.
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
	 * @param assertion
	 *            a {@link org.evosuite.assertion.EqualsAssertion} object.
	 */
	protected void visitEqualsAssertion(EqualsAssertion assertion) {
		VariableReference source = assertion.getSource();
		VariableReference dest = assertion.getDest();
		Object value = assertion.getValue();

		if (source.isPrimitive() && dest.isPrimitive()) {
			if (((Boolean) value).booleanValue())
				testCode += "assertTrue(" + getVariableName(source) + " == "
				        + getVariableName(dest) + ");";
			else
				testCode += "assertFalse(" + getVariableName(source) + " == "
				        + getVariableName(dest) + ");";
		} else {
			if (((Boolean) value).booleanValue())
				testCode += "assertTrue(" + getVariableName(source) + ".equals((" + this.getClassName(Object.class) +")"
				        + getVariableName(dest) + "));";
			else
				testCode += "assertFalse(" + getVariableName(source) + ".equals((" + this.getClassName(Object.class) +")"
				        + getVariableName(dest) + "));";
		}
	}

	/**
	 * <p>
	 * visitSameAssertion
	 * </p>
	 * 
	 * @param assertion
	 *            a {@link org.evosuite.assertion.SameAssertion} object.
	 */
	protected void visitSameAssertion(SameAssertion assertion) {
		VariableReference source = assertion.getSource();
		VariableReference dest = assertion.getDest();
		Object value = assertion.getValue();

		if (((Boolean) value).booleanValue())
			testCode += "assertSame(" + getVariableName(source) + ", "
			        + getVariableName(dest) + ");";
		else
			testCode += "assertNotSame(" + getVariableName(source) + ", "
			        + getVariableName(dest) + ");";
	}

	private String getUnstableTestComment(){
		return " // Unstable assertion";
	}
	
	private boolean isTestUnstable() {
		return test!=null && test.isUnstable();
	}

		
	protected void visitAssertion(Assertion assertion) {
		
		if(isTestUnstable()){
			/*
			 * if the current test is unstable, then comment out all of its assertions.		
			 */
			testCode  += "// "+getUnstableTestComment()+": ";
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
		} else {
			throw new RuntimeException("Unknown assertion type: " + assertion);
		}
	}

	private void addAssertions(StatementInterface statement) {
		boolean assertionAdded = false;
		if (getException(statement) != null) {
			// Assumption: The statement that throws an exception is the last statement of a test.
			VariableReference returnValue = statement.getReturnValue();
			for (Assertion assertion : statement.getAssertions()) {
				if (assertion != null
				        && !assertion.getReferencedVariables().contains(returnValue)) {
					visitAssertion(assertion);
					testCode += "\n";
					assertionAdded = true;
				}
			}
		} else {
			for (Assertion assertion : statement.getAssertions()) {
				if (assertion != null) {
					visitAssertion(assertion);
					testCode += "\n";
					assertionAdded = true;
				}
			}
		}
		if (assertionAdded)
			testCode += "\n";
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

	private String getEscapedString(String original) {
		char[] charArray = StringEscapeUtils.escapeJava((String) original).toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < charArray.length; ++i) {
			char a = charArray[i];
			if (a > 255) {
				sb.append("\\u");
				sb.append(Integer.toHexString(a));
			} else {
				sb.append(a);
			}
		}
		return sb.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.testcase.TestVisitor#visitPrimitiveStatement(org.evosuite.testcase.PrimitiveStatement)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitPrimitiveStatement(PrimitiveStatement<?> statement) {
		VariableReference retval = statement.getReturnValue();
		Object value = statement.getValue();

		if (statement instanceof StringPrimitiveStatement) {
			if(value == null) {
				testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
				        + getVariableName(retval) + " = null;\n";

			} else {
				String escapedString = getEscapedString((String)value);
				testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
						+ getVariableName(retval) + " = \"" + escapedString + "\";\n";
			}
			// testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
			// + getVariableName(retval) + " = \""
			// + StringEscapeUtils.escapeJava((String) value) + "\";\n";
		} else if (statement instanceof FileNamePrimitiveStatement) {
			// changed by Daniel
			if (value != null) {
				String escapedPath = getEscapedString(((EvoSuiteFile) value).getPath());
				testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
				        + getVariableName(retval) + " = new "
				        + ((Class<?>) retval.getType()).getSimpleName() + "(\""
				        + escapedPath + "\");\n";
			} else {
				testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
				        + getVariableName(retval) + " = null;\n";
			}
		} else if (statement instanceof ClassPrimitiveStatement) {
			StringBuilder builder = new StringBuilder();
			String className = getClassName(retval);
			className = className.replaceAll("Class<(.*)(<.*>)>", "Class<$1>");
			builder.append(className);
			builder.append(" ");
			builder.append(getVariableName(retval));
			builder.append(" = ");
			builder.append(getClassName(((Class<?>) value)));
			builder.append(".class;\n");
			testCode += builder.toString();
		} else {
			testCode += getClassName(retval) + " " + getVariableName(retval) + " = "
			        + NumberFormatter.getNumberString(value) + ";\n";
		}
		addAssertions(statement);
	}

	/** {@inheritDoc} */
	@Override
	public void visitPrimitiveExpression(PrimitiveExpression statement) {
		VariableReference retval = statement.getReturnValue();
		String expression = ((Class<?>) retval.getType()).getSimpleName() + " "
		        + getVariableName(retval) + " = ";
		expression += getVariableName(statement.getLeftOperand()) + " "
		        + statement.getOperator().toCode() + " "
		        + getVariableName(statement.getRightOperand());
		testCode += expression + ";\n";
		addAssertions(statement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.testcase.TestVisitor#visitFieldStatement(org.evosuite.testcase.FieldStatement)
	 */
	/** {@inheritDoc} */
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
			builder.append(" = null;\n");
			builder.append("try {\n  ");
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
			builder.append(".");
			builder.append(field.getName());
			builder.append(";");
		} else {
			builder.append(getVariableName(retval));
			builder.append(" = ");
			builder.append(cast_str);
			builder.append(getClassName(field.getField().getDeclaringClass()));
			builder.append(".");
			builder.append(field.getName());
			builder.append(";");
		}
		if (exception != null) {
			Class<?> ex = exception.getClass();
			while (!Modifier.isPublic(ex.getModifiers()))
				ex = ex.getSuperclass();
			builder.append("\n} catch(");
			builder.append(getClassName(ex));
			builder.append(" e) {}");
		}
		builder.append("\n");

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
			Class<?> rawParamClass = GenericTypeReflector.erase(declaredParamType);
			if (rawParamClass.isPrimitive() && name.equals("null")) {
				parameterString += getPrimitiveNullCast(rawParamClass);
			} else if (isGenericMethod) {
				if (!declaredParamType.equals(actualParamType) || name.equals("null")) {
					parameterString += "(" + getTypeName(declaredParamType) + ") ";
					if (name.contains("(short"))
						name = name.replace("(short)", "");
					if (name.contains("(byte"))
						name = name.replace("(byte)", "");

				}
			} else if (name.equals("null")) {
				parameterString += "(" + getTypeName(declaredParamType) + ") ";
			} else if (!GenericClass.isAssignable(declaredParamType, actualParamType)) {

				if (TypeUtils.isArrayType(declaredParamType)
				        && TypeUtils.isArrayType(actualParamType)) {
					Class<?> componentClass = GenericTypeReflector.erase(declaredParamType).getComponentType();
					if (componentClass.equals(Object.class)) {
						GenericClass genericComponentClass = new GenericClass(
						        componentClass);
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
				GenericClass parameterClass = new GenericClass(declaredParamType);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.testcase.TestVisitor#visitMethodStatement(org.evosuite.testcase.MethodStatement)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitMethodStatement(MethodStatement statement) {
		String result = "";
		VariableReference retval = statement.getReturnValue();
		GenericMethod method = statement.getMethod();
		Throwable exception = getException(statement);
		List<VariableReference> parameters = statement.getParameterReferences();
		boolean isGenericMethod = method.hasTypeParameters();

		if (exception != null && !statement.isDeclaredException(exception)) {
			result += "// Undeclared exception!\n";
		}

		boolean lastStatement = statement.getPosition() == statement.tc.size() - 1;
		boolean unused = !Properties.ASSERTIONS ? exception != null : test != null
		        && !test.hasReferences(retval);

		if (!retval.isVoid() && retval.getAdditionalVariableReference() == null
		        && !unused) {
			if (exception != null) {
				if (!lastStatement || statement.hasAssertions())
					result += getClassName(retval) + " " + getVariableName(retval)
					        + " = " + retval.getDefaultValueString() + ";\n";
			} else {
				result += getClassName(retval) + " ";
			}
		}
		if (exception != null && !test.isFailing())
			result += "try {\n  ";

		String parameter_string = getParameterString(method.getParameterTypes(),
		                                             parameters, isGenericMethod,
		                                             method.isOverloaded(parameters), 0);

		String callee_str = "";
		if (!retval.isAssignableFrom(method.getReturnType())
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
				callee_str += getVariableName(callee);
			}
		}

		if (retval.isVoid()) {
			result += callee_str + "." + method.getName() + "(" + parameter_string + ");";
		} else {
			// if (exception == null || !lastStatement)
			if (!unused)
				result += getVariableName(retval) + " = ";
			else
				result += getClassName(retval) + " " + getVariableName(retval) + " = ";

			result += callee_str + "." + method.getName() + "(" + parameter_string + ");";
		}

		if (exception != null && !test.isFailing()) {
			if (Properties.ASSERTIONS) {
				result += generateFailAssertion(statement, exception);
			}

			result += "\n}";// end try block

			result += generateCatchBlock(statement, exception);
		}

		testCode += result + "\n";
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

		// we can only catch a public class
		Class<?> ex = exception.getClass();
		while (!Modifier.isPublic(ex.getModifiers()))
			ex = ex.getSuperclass();

		// preparing the catch block
		result += " catch(" + getClassName(ex) + " e) {\n";

		// adding the message of the exception
		String exceptionMessage = "";
		if (exception.getMessage() != null) {
			exceptionMessage = exception.getMessage().replace("*/", "*_/");
		} else {
			exceptionMessage = "no message in exception (getMessage() returned null)";
		}

		result += "   //\n";
		for (String msg : exceptionMessage.split("\n")) {
			result += "   // " + StringEscapeUtils.escapeJava(msg) + "\n";
		}
		result += "   //\n";

		result += "}\n";// closing the catch block
		return result;
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
	/** {@inheritDoc} */
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

		// String result = ((Class<?>) retval.getType()).getSimpleName()
		// +" "+getVariableName(retval)+ " = null;\n";
		if (exception != null) {
			String className = getClassName(retval);

			// FIXXME: Workaround for primitives:
			// But really, this can't really add any coverage, so we shouldn't be printing this in the first place!
			if (retval.isPrimitive()) {
				className = retval.getGenericClass().getUnboxedType().getSimpleName();
			}

			result = className + " " + getVariableName(retval) + " = null;\n";
			result += "try {\n  ";
		} else {
			result += getClassName(retval) + " ";
		}
		if (isNonStaticMemberClass) {

			result += getVariableName(retval) + " = "
			        + getVariableName(parameters.get(0))
			        // + new GenericClass(
			        // constructor.getDeclaringClass().getEnclosingClass()).getSimpleName()
			        + ".new "
			        // + ConstructorStatement.getReturnType(constructor.getDeclaringClass())
			        // + getTypeName(constructor.getOwnerType()) + "("
			        + getSimpleTypeName(constructor.getOwnerType()) + "("
			        // + getClassName(constructor.getDeclaringClass()) + "("
			        + parameterString + ");";

		} else {

			result += getVariableName(retval) + " = new "
			        + getTypeName(constructor.getOwnerType())
			        // + ConstructorStatement.getReturnType(constructor.getDeclaringClass())
			        + "(" + parameterString + ");";
		}

		if (exception != null) {
			if (Properties.ASSERTIONS) {
				result += generateFailAssertion(statement, exception);
			}

			result += "\n}";// end try block

			result += generateCatchBlock(statement, exception);
		}

		testCode += result + "\n";
		addAssertions(statement);
	}

	/**
	 * Generates a fail assertion for being inserted after a statement
	 * generating an exception. Parameter "statement" is not used in the default
	 * implementation but may be used in future extensions.
	 **/
	public String generateFailAssertion(AbstractStatement statement, Throwable exception) {
		Class<?> ex = exception.getClass();
		// boolean isExpected = getDeclaredExceptions().contains(ex);
		while (!Modifier.isPublic(ex.getModifiers()))
			ex = ex.getSuperclass();
		// if (isExpected)      
		String stmt =  " fail(\"Expecting exception: " + getClassName(ex) + "\");\n";
		
		if(isTestUnstable()){
			/*
			 * if the current test is unstable, then comment out all of its assertions.		
			 */
			stmt = "// "+stmt +getUnstableTestComment();
		}
		
		return "\n "+stmt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.testcase.TestVisitor#visitArrayStatement(org.evosuite.testcase.ArrayStatement)
	 */
	/** {@inheritDoc} */
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
			        + ".class, " + multiDimensions + ");\n";

		} else {
			testCode += getClassName(retval) + " " + getVariableName(retval) + " = new "
			        + type + multiDimensions + ";\n";
		}
		addAssertions(statement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.testcase.TestVisitor#visitAssignmentStatement(org.evosuite.testcase.AssignmentStatement)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitAssignmentStatement(AssignmentStatement statement) {
		String cast = "";
		VariableReference retval = statement.getReturnValue();
		VariableReference parameter = statement.parameter;

		if (!retval.getVariableClass().equals(parameter.getVariableClass()))
			cast = "(" + getClassName(retval) + ") ";

		testCode += getVariableName(retval) + " = " + cast + getVariableName(parameter)
		        + ";\n";
		addAssertions(statement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.testcase.TestVisitor#visitNullStatement(org.evosuite.testcase.NullStatement)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitNullStatement(NullStatement statement) {
		VariableReference retval = statement.getReturnValue();

		testCode += getClassName(retval) + " " + getVariableName(retval) + " = null;\n";
	}

	@Override
	public void visitStatement(StatementInterface statement) {
		if (!statement.getComment().isEmpty()) {
			String comment = statement.getComment();
			for (String line : comment.split("\n")) {
				testCode += "// " + line + "\n";
			}
		}
		super.visitStatement(statement);
	}
}
