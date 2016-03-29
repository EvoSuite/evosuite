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
package org.evosuite.testcase.statements;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.utils.generic.GenericAccessibleObject;

// TODO-JRO Implement methods of PrimitiveExpression as needed
public class PrimitiveExpression extends AbstractStatement {

	public static enum Operator {
		TIMES("*"), //
		DIVIDE("/"), //
		REMAINDER("%"), //
		PLUS("+"), //
		MINUS("-"), //
		LEFT_SHIFT("<<"), //
		RIGHT_SHIFT_SIGNED(">>"), //
		RIGHT_SHIFT_UNSIGNED(">>>"), //
		LESS("<"), //
		GREATER(">"), //
		LESS_EQUALS("<="), //
		GREATER_EQUALS(">="), //
		EQUALS("=="), //
		NOT_EQUALS("!="), //
		XOR("^"), //
		AND("&"), //
		OR("|"), //
		CONDITIONAL_AND("&&"), //
		CONDITIONAL_OR("||");

		public static Operator toOperator(String code) {
			for (Operator operator : values()) {
				if (operator.code.equals(code)) {
					return operator;
				}
			}
			throw new RuntimeException("No operator for " + code);
		}

		private final String code;

		private Operator(String code) {
			this.code = code;
		}

		public String toCode() {
			return code;
		}
	}

	private static final long serialVersionUID = 1L;

	private VariableReference leftOperand;
	private final Operator operator;
	private VariableReference rightOperand;

	/**
	 * <p>
	 * Constructor for PrimitiveExpression.
	 * </p>
	 * 
	 * @param testCase
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param reference
	 *            a {@link org.evosuite.testcase.variable.VariableReference} object.
	 * @param leftOperand
	 *            a {@link org.evosuite.testcase.variable.VariableReference} object.
	 * @param operator
	 *            a {@link org.evosuite.testcase.statements.PrimitiveExpression.Operator}
	 *            object.
	 * @param rightOperand
	 *            a {@link org.evosuite.testcase.variable.VariableReference} object.
	 */
	public PrimitiveExpression(TestCase testCase, VariableReference reference,
	        VariableReference leftOperand, Operator operator,
	        VariableReference rightOperand) {
		super(testCase, reference);
		this.leftOperand = leftOperand;
		this.operator = operator;
		this.rightOperand = rightOperand;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.testcase.StatementInterface#changeClassLoader
	 * (java.lang.ClassLoader)
	 */
	/** {@inheritDoc} */
	@Override
	public void changeClassLoader(ClassLoader loader) {
		// No-op
	}

	/** {@inheritDoc} */
	@Override
	public Statement copy(TestCase newTestCase, int offset) {
		VariableReference newRetVal = new VariableReferenceImpl(newTestCase,
		        retval.getType());
		VariableReference newLeftOperand = newTestCase.getStatement(leftOperand.getStPosition()).getReturnValue();
		VariableReference newRightOperand = newTestCase.getStatement(rightOperand.getStPosition()).getReturnValue();
		return new PrimitiveExpression(newTestCase, newRetVal, newLeftOperand, operator,
		        newRightOperand);
		//		return new PrimitiveExpression(newTestCase, retval, leftOperand, operator, rightOperand);
	}

	/** {@inheritDoc} */
	@Override
	public Throwable execute(Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {
		try {
			Object o1 = leftOperand.getObject(scope);
			Object o2 = rightOperand.getObject(scope);
			switch (operator) {
				case EQUALS:
					if (o1 == o2) {
						scope.setObject(retval, true);
					} else {
						scope.setObject(retval, true);
					}
					break;
				default:
					throw new UnsupportedOperationException("Method execute not implemented!");
			}
		} catch(CodeUnderTestException e) {
			return e;
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public GenericAccessibleObject<?> getAccessibleObject() {
		throw new UnsupportedOperationException(
		        "Method getAccessibleObject not implemented!");
	}

	/** {@inheritDoc} */
	@Override
	public String getCode() {
		String code = ((Class<?>) retval.getType()).getSimpleName() + " "
		        + retval.getName() + " = " + leftOperand.getName() + " "
		        + operator.toCode() + " " + rightOperand.getName() + ";";
		return code;
	}

	/**
	 * <p>
	 * Getter for the field <code>leftOperand</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
	 */
	public VariableReference getLeftOperand() {
		return leftOperand;
	}

	/**
	 * <p>
	 * Getter for the field <code>operator</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.statements.PrimitiveExpression.Operator}
	 *         object.
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * <p>
	 * Getter for the field <code>rightOperand</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
	 */
	public VariableReference getRightOperand() {
		return rightOperand;
	}

	/** {@inheritDoc} */
	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		throw new UnsupportedOperationException(
		        "Method getUniqueVariableReferences not implemented!");
	}

	/** {@inheritDoc} */
	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> result = new HashSet<VariableReference>();
		result.add(retval);
		result.add(leftOperand);
		result.add(rightOperand);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssignmentStatement() {
		throw new UnsupportedOperationException(
		        "Method isAssignmentStatement not implemented!");
	}

	/** {@inheritDoc} */
	@Override
	public void replace(VariableReference oldVar, VariableReference newVar) {
		if (leftOperand.equals(oldVar)) {
			leftOperand = newVar;
		}
		if (rightOperand.equals(oldVar)) {
			rightOperand = newVar;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean same(Statement s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		PrimitiveExpression ps = (PrimitiveExpression) s;

		return operator.equals(ps.operator) && leftOperand.same(ps.leftOperand)
		        && rightOperand.same(ps.rightOperand);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getCode();
	}
}
