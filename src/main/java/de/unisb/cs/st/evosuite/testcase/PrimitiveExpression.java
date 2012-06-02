/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
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
package de.unisb.cs.st.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.commons.GeneratorAdapter;

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

	private final VariableReference leftOperand;
	private final Operator operator;
	private final VariableReference rightOperand;

	public PrimitiveExpression(TestCase testCase, VariableReference reference,
	        VariableReference leftOperand, Operator operator,
	        VariableReference rightOperand) {
		super(testCase, reference);
		this.leftOperand = leftOperand;
		this.operator = operator;
		this.rightOperand = rightOperand;
	}

	@Override
	public StatementInterface copy(TestCase newTestCase, int offset) {
		throw new UnsupportedOperationException("Method clone not implemented!");
	}

	@Override
	public Throwable execute(Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {
		throw new UnsupportedOperationException("Method execute not implemented!");
	}

	@Override
	public AccessibleObject getAccessibleObject() {
		throw new UnsupportedOperationException(
		        "Method getAccessibleObject not implemented!");
	}

	@Override
	public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
	        Throwable exception) {
		throw new UnsupportedOperationException("Method getBytecode not implemented!");
	}

	@Override
	public String getCode() {
		String code = ((Class<?>) retval.getType()).getSimpleName() + " "
		        + retval.getName() + " = " + leftOperand.getName() + " "
		        + operator.toCode() + " " + rightOperand.getName() + ";";
		return code;
	}

	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		throw new UnsupportedOperationException(
		        "Method getUniqueVariableReferences not implemented!");
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		throw new UnsupportedOperationException(
		        "Method getVariableReferences not implemented!");
	}

	@Override
	public boolean isAssignmentStatement() {
		throw new UnsupportedOperationException(
		        "Method isAssignmentStatement not implemented!");
	}

	@Override
	public void replace(VariableReference old_var, VariableReference new_var) {
		throw new UnsupportedOperationException("Method replace not implemented!");
	}

	@Override
	public boolean same(StatementInterface s) {
		throw new UnsupportedOperationException("Method same not implemented!");
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#changeClassLoader(java.lang.ClassLoader)
	 */
	@Override
	public void changeClassLoader(ClassLoader loader) {
		// No-op
	}
}
