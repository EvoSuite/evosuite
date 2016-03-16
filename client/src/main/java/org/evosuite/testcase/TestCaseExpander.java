/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
/**
 * 
 */
package org.evosuite.testcase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericClass;

public class TestCaseExpander {

	private final Set<VariableReference> usedVariables = new HashSet<VariableReference>();

	public Map<Integer, Set<VariableReference>> variableMapping = new HashMap<Integer, Set<VariableReference>>();

	private int currentPosition = 0;

	public TestCase expandTestCase(TestCase test) {
		TestCase expandedTest = test.clone();
		// Deactivated for now - only needed in NL branch
		// createConcretePrimitives(expandedTest);
		while (currentPosition < expandedTest.size()) {
			Statement statement = expandedTest.getStatement(currentPosition);
			if (statement instanceof MethodStatement) {
				visitMethodStatement(expandedTest, (MethodStatement) statement);
			} else if (statement instanceof ConstructorStatement) {
				visitConstructorStatement(expandedTest, (ConstructorStatement) statement);
			} else if (statement instanceof ArrayStatement) {
				visitArrayStatement(expandedTest, ((ArrayStatement) statement));
			} else if (statement instanceof AssignmentStatement) {
				visitAssignmentStatement(expandedTest, ((AssignmentStatement) statement));
			}
			currentPosition++;
		}
		return expandedTest;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	private void createConcretePrimitives(TestCase test) {

		// Execute test to collect concrete values
		TestCaseExecutor executor = TestCaseExecutor.getInstance();
		ConcreteValueObserver observer = new ConcreteValueObserver();
		executor.addObserver(observer);
		executor.execute(test);
		executor.removeObserver(observer);

		// Now replace references to concrete values with new primitive statements
		Map<Integer, Object> concreteValues = observer.getConcreteValues();
		List<Integer> positions = new ArrayList<Integer>(concreteValues.keySet());
		Collections.sort(positions, Collections.reverseOrder());

		for (Integer position : positions) {
			Object value = concreteValues.get(position);
			Statement statement = test.getStatement(position);

			PrimitiveStatement primitive = PrimitiveStatement.getPrimitiveStatement(test,
			                                                                        new GenericClass(
			                                                                                value.getClass()));
			primitive.setValue(value);
			VariableReference replacement = test.addStatement(primitive, position);
			test.replace(statement.getReturnValue(), replacement);
		}
	}

	private VariableReference duplicateStatement(TestCase test, VariableReference owner) {
		Statement statement = test.getStatement(owner.getStPosition());
		currentPosition++;
		VariableReference copy = test.addStatement(statement.clone(test),
		                                           owner.getStPosition() + 1);
		if (!variableMapping.containsKey(owner.getStPosition())) {
			variableMapping.put(owner.getStPosition(), new HashSet<VariableReference>());
			// variableMapping.get(owner.getStPosition()).add(owner);
		}
		variableMapping.get(owner.getStPosition()).add(copy);
		return copy;
	}

	private void addUnchangedMapping(TestCase test, VariableReference var) {
		VariableReference copy = test.getStatement(var.getStPosition()).getReturnValue();
		if (!variableMapping.containsKey(var.getStPosition())) {
			variableMapping.put(var.getStPosition(), new HashSet<VariableReference>());
			variableMapping.get(var.getStPosition()).add(var);
		}
		variableMapping.get(var.getStPosition()).add(copy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.testcase.TestVisitor#visitMethodStatement(org.evosuite
	 * .testcase.MethodStatement)
	 */
	public void visitMethodStatement(TestCase test, MethodStatement statement) {
		// The problem is that at this point in the test case the parameters
		// might have already changed

		int i = 0;
		for (VariableReference var : statement.getParameterReferences()) {
			if (var.isPrimitive() || var.isString()) {
				if (usedVariables.contains(var)
				        && test.getStatement(var.getStPosition()) instanceof PrimitiveStatement<?>) {
					// Duplicate and replace
					VariableReference varCopy = duplicateStatement(test, var);
					statement.replaceParameterReference(varCopy, i);
					usedVariables.add(varCopy);
				}
				usedVariables.add(var);
			}
			i++;
		}
		addUnchangedMapping(test, statement.getReturnValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.testcase.TestVisitor#visitConstructorStatement(org.evosuite
	 * .testcase.ConstructorStatement)
	 */
	public void visitConstructorStatement(TestCase test, ConstructorStatement statement) {
		int i = 0;
		for (VariableReference var : statement.getParameterReferences()) {
			if (var.isPrimitive() || var.isString()) {
				if (usedVariables.contains(var)
				        && test.getStatement(var.getStPosition()) instanceof PrimitiveStatement<?>) {
					// Duplicate and replace
					VariableReference varCopy = duplicateStatement(test, var);
					statement.replaceParameterReference(varCopy, i);
					usedVariables.add(varCopy);
				}
				usedVariables.add(var);
			}
			i++;
		}
		addUnchangedMapping(test, statement.getReturnValue());

	}

	public void visitArrayStatement(TestCase test, ArrayStatement statement) {
		ArrayReference arrRef = (ArrayReference) statement.getReturnValue();

		Set<Integer> assignments = new HashSet<Integer>();
		int position = statement.getPosition() + 1;

		while (position < test.size()) {
			Statement st = test.getStatement(position);
			if (st instanceof AssignmentStatement) {
				if (st.getReturnValue() instanceof ArrayIndex) {
					ArrayIndex arrayIndex = (ArrayIndex) st.getReturnValue();
					if (arrayIndex.getArray().equals(arrRef)) {
						assignments.add(arrayIndex.getArrayIndex());
					}
				}
			} else if (st instanceof PrimitiveStatement) {
				// OK, ignore
			} else {
				break;
			}
			position++;
		}

		position = statement.getPosition() + 1;

		for (int i = 0; i < statement.size(); i++) {
			if (assignments.contains(i))
				continue;

			ArrayIndex index = new ArrayIndex(test, arrRef, i);
			VariableReference retVal = null;
			if (index.isPrimitive()) {
				PrimitiveStatement<?> primitive = PrimitiveStatement.getPrimitiveStatement(test,
				                                                                           index.getGenericClass());
				retVal = test.addStatement(primitive, position++);
			} else {
				NullStatement nullStatement = new NullStatement(test, index.getType());
				retVal = test.addStatement(nullStatement, position++);
			}
			AssignmentStatement assignment = new AssignmentStatement(test, index, retVal);
			test.addStatement(assignment, position++);

		}
	}

	public void visitAssignmentStatement(TestCase test, AssignmentStatement statement) {

		VariableReference var = statement.getValue();
		if (var.isPrimitive() || var.isString()) {
			if (usedVariables.contains(var)
			        && test.getStatement(var.getStPosition()) instanceof PrimitiveStatement<?>) {
				// Duplicate and replace
				VariableReference varCopy = duplicateStatement(test, var);
				statement.replace(var, varCopy);
				usedVariables.add(varCopy);
			}
			usedVariables.add(var);
		}
		addUnchangedMapping(test, statement.getReturnValue());

	}

}