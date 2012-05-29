/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.repair;

import java.lang.reflect.Field;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.assertion.CompareAssertion;
import de.unisb.cs.st.evosuite.assertion.EqualsAssertion;
import de.unisb.cs.st.evosuite.assertion.Inspector;
import de.unisb.cs.st.evosuite.assertion.InspectorAssertion;
import de.unisb.cs.st.evosuite.assertion.NullAssertion;
import de.unisb.cs.st.evosuite.assertion.PrimitiveAssertion;
import de.unisb.cs.st.evosuite.assertion.PrimitiveFieldAssertion;
import de.unisb.cs.st.evosuite.testcase.TestCodeVisitor;
import de.unisb.cs.st.evosuite.testcase.VariableReference;
import de.unisb.cs.st.evosuite.utils.NumberFormatter;

/**
 * @author Gordon Fraser
 * 
 */
public class AssertionLogTestVisitor extends TestCodeVisitor {

	private final int testId;

	private int numAssertion = 0;

	public AssertionLogTestVisitor(int id) {
		testId = id;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCodeVisitor#visitCompareAssertion(de.unisb.cs.st.evosuite.assertion.CompareAssertion)
	 */
	@Override
	protected void visitCompareAssertion(CompareAssertion assertion) {
		VariableReference source = assertion.getSource();
		VariableReference dest = assertion.getDest();
		Object value = assertion.getValue();

		if (source.getType().equals(Integer.class)) {
			if ((Integer) value == 0)
				testCode += "assertTrue(\"" + Properties.TARGET_CLASS + "\", " + testId
				        + ", " + numAssertion + ", " + getVariableName(source) + " == "
				        + getVariableName(dest) + ");";
			else if ((Integer) value < 0)
				testCode += "assertTrue(\"" + Properties.TARGET_CLASS + "\", " + testId
				        + ", " + numAssertion + ", " + getVariableName(source) + " < "
				        + getVariableName(dest) + ");";
			else
				testCode += "assertTrue(\"" + Properties.TARGET_CLASS + "\", " + testId
				        + ", " + numAssertion + ", " + getVariableName(source) + " > "
				        + getVariableName(dest) + ");";

		} else {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + getVariableName(source)
			        + ".compareTo(" + getVariableName(dest) + "), " + value + ");";
		}
		numAssertion++;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCodeVisitor#visitEqualsAssertion(de.unisb.cs.st.evosuite.assertion.EqualsAssertion)
	 */
	@Override
	protected void visitEqualsAssertion(EqualsAssertion assertion) {
		VariableReference source = assertion.getSource();
		VariableReference dest = assertion.getDest();
		Object value = assertion.getValue();

		if (source.isPrimitive() && dest.isPrimitive()) {
			if (((Boolean) value).booleanValue())
				testCode += "assertTrue(\"" + Properties.TARGET_CLASS + "\", " + testId
				        + ", " + numAssertion + ", " + getVariableName(source) + " == "
				        + getVariableName(dest) + ");";
			else
				testCode += "assertFalse(\"" + Properties.TARGET_CLASS + "\", " + testId
				        + ", " + numAssertion + ", " + getVariableName(source) + " == "
				        + getVariableName(dest) + ");";
		} else {
			if (((Boolean) value).booleanValue())
				testCode += "assertTrue(\"" + Properties.TARGET_CLASS + "\", " + testId
				        + ", " + numAssertion + ", " + getVariableName(source)
				        + ".equals(" + getVariableName(dest) + "));";
			else
				testCode += "assertFalse(\"" + Properties.TARGET_CLASS + "\", " + testId
				        + ", " + numAssertion + ", " + getVariableName(source)
				        + ".equals(" + getVariableName(dest) + "));";
		}
		numAssertion++;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCodeVisitor#visitInspectorAssertion(de.unisb.cs.st.evosuite.assertion.InspectorAssertion)
	 */
	@Override
	protected void visitInspectorAssertion(InspectorAssertion assertion) {
		VariableReference source = assertion.getSource();
		Object value = assertion.getValue();
		Inspector inspector = assertion.getInspector();

		if (value == null) {
			testCode += "assertNull(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + getVariableName(source) + "."
			        + inspector.getMethodCall() + "());";
		} else if (value.getClass().equals(Long.class)) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + "." + inspector.getMethodCall()
			        + "());";
		} else if (value.getClass().equals(Float.class)) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + "." + inspector.getMethodCall()
			        + "(), 0.01F);";
		} else if (value.getClass().equals(Double.class)) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + "." + inspector.getMethodCall()
			        + "(), 0.01D);";
		} else if (value.getClass().equals(Character.class)) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + "." + inspector.getMethodCall()
			        + "());";
		} else if (value.getClass().equals(String.class)) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + "." + inspector.getMethodCall()
			        + "());";
		} else if (value.getClass().isEnum()) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + "." + inspector.getMethodCall()
			        + "());";

		} else
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + value + ", " + getVariableName(source)
			        + "." + inspector.getMethodCall() + "());";
		numAssertion++;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCodeVisitor#visitNullAssertion(de.unisb.cs.st.evosuite.assertion.NullAssertion)
	 */
	@Override
	protected void visitNullAssertion(NullAssertion assertion) {
		VariableReference source = assertion.getSource();
		Boolean value = (Boolean) assertion.getValue();
		if (value.booleanValue()) {
			testCode += "assertNull(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + getVariableName(source) + ");";
		} else
			testCode += "assertNotNull(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + getVariableName(source) + ");";
		numAssertion++;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCodeVisitor#visitPrimitiveAssertion(de.unisb.cs.st.evosuite.assertion.PrimitiveAssertion)
	 */
	@Override
	protected void visitPrimitiveAssertion(PrimitiveAssertion assertion) {
		VariableReference source = assertion.getSource();
		Object value = assertion.getValue();

		if (value == null) {
			testCode += "assertNull(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + getVariableName(source) + ");";
		} else if (source.getVariableClass().equals(float.class)) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + ", 0.01F);";
		} else if (source.getVariableClass().equals(double.class)) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + ", 0.01D);";
		} else if (value.getClass().isEnum()) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + ");";
		} else if (source.isWrapperType()) {
			if (source.getVariableClass().equals(Float.class)) {
				testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
				        + ", " + numAssertion + ", "
				        + NumberFormatter.getNumberString(value) + ", " + "(float)"
				        + getVariableName(source) + ", 0.01F);";
			} else if (source.getVariableClass().equals(Double.class)) {
				testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
				        + ", " + numAssertion + ", "
				        + NumberFormatter.getNumberString(value) + ", " + "(double)"
				        + getVariableName(source) + ", 0.01D);";
			} else if (value.getClass().isEnum()) {
				testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
				        + ", " + numAssertion + ", "
				        + NumberFormatter.getNumberString(value) + ", "
				        + getVariableName(source) + ");";
			} else
				testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
				        + ", " + numAssertion + ", "
				        + NumberFormatter.getNumberString(value) + ", ("
				        + NumberFormatter.getBoxedClassName(value) + ")"
				        + getVariableName(source) + ");";
		} else
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + ");";
		numAssertion++;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCodeVisitor#visitPrimitiveFieldAssertion(de.unisb.cs.st.evosuite.assertion.PrimitiveFieldAssertion)
	 */
	@Override
	protected void visitPrimitiveFieldAssertion(PrimitiveFieldAssertion assertion) {
		VariableReference source = assertion.getSource();
		Object value = assertion.getValue();
		Field field = assertion.getField();

		if (value == null) {
			testCode += "assertNull(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + getVariableName(source) + "."
			        + field.getName() + ");";
		} else if (value.getClass().equals(Long.class)) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + "." + field.getName() + ");";
		} else if (value.getClass().equals(Float.class)) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + "." + field.getName()
			        + ", 0.01F);";
		} else if (value.getClass().equals(Double.class)) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + "." + field.getName()
			        + ", 0.01D);";
		} else if (value.getClass().equals(Character.class)) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + "." + field.getName() + ");";
		} else if (value.getClass().equals(String.class)) {
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + "." + field.getName() + ");";
		} else
			testCode += "assertEquals(\"" + Properties.TARGET_CLASS + "\", " + testId
			        + ", " + numAssertion + ", " + NumberFormatter.getNumberString(value)
			        + ", " + getVariableName(source) + "." + field.getName() + ");";
		numAssertion++;
	}
}
