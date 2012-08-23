/**
 * 
 */
package org.evosuite.testsuite;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.testcase.ArrayIndex;
import org.evosuite.testcase.ArrayReference;
import org.evosuite.testcase.ArrayStatement;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.VariableReference;

public class TestCaseExpander {

	private final Set<VariableReference> usedVariables = new HashSet<VariableReference>();

	private int currentPosition = 0;

	public TestCase expandTestCase(TestCase test) {
		TestCase expandedTest = test.clone();
		while (currentPosition < expandedTest.size()) {
			StatementInterface statement = expandedTest.getStatement(currentPosition);
			if (statement instanceof MethodStatement) {
				visitMethodStatement(expandedTest, (MethodStatement) statement);
			} else if (statement instanceof ConstructorStatement) {
				visitConstructorStatement(expandedTest, (ConstructorStatement) statement);
			} else if (statement instanceof ArrayStatement) {
				visitArrayStatement(expandedTest, ((ArrayStatement) statement));
			}
			currentPosition++;
		}
		return expandedTest;
	}

	private VariableReference duplicateStatement(TestCase test, VariableReference owner) {
		StatementInterface statement = test.getStatement(owner.getStPosition());
		currentPosition++;
		return test.addStatement(statement.clone(test), owner.getStPosition() + 1);
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
	}

	public void visitArrayStatement(TestCase test, ArrayStatement statement) {
		ArrayReference arrRef = (ArrayReference) statement.getReturnValue();

		Set<Integer> assignments = new HashSet<Integer>();
		int position = statement.getPosition() + 1;

		while (position < test.size()) {
			StatementInterface st = test.getStatement(position);
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
			PrimitiveStatement<?> primitive = PrimitiveStatement.getPrimitiveStatement(test,
			                                                                           index.getVariableClass());
			VariableReference retVal = test.addStatement(primitive, position++);
			AssignmentStatement assignment = new AssignmentStatement(test, index, retVal);
			test.addStatement(assignment, position++);

		}
	}

}