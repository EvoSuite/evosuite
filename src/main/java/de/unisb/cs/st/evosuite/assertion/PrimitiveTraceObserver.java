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
package de.unisb.cs.st.evosuite.assertion;

import java.lang.reflect.Modifier;

import de.unisb.cs.st.evosuite.testcase.CodeUnderTestException;
import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class PrimitiveTraceObserver extends AssertionTraceObserver<PrimitiveTraceEntry> {

	@Override
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		visitReturnValue(statement, scope);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.AssertionTraceObserver#visit(de.unisb.cs.st.evosuite.testcase.StatementInterface, de.unisb.cs.st.evosuite.testcase.Scope, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	protected void visit(StatementInterface statement, Scope scope, VariableReference var) {
		logger.debug("Checking primitive " + var);
		try {
			// Need only legal values
			if (var == null)
				return;

			// We don't need assertions on constant values
			if (statement instanceof PrimitiveStatement<?>)
				return;

			Object object = var.getObject(scope);

			// We don't need to compare to null
			if (object == null)
				return;

			// We can't check private member enums
			if (object.getClass().isEnum()
			        && !Modifier.isPublic(object.getClass().getModifiers()))
				return;

			if (object.getClass().isPrimitive() || object.getClass().isEnum()
			        || isWrapperType(object.getClass()) || object instanceof String) {
				logger.debug("Observed value " + object + " for statement "
				        + statement.getCode());
				trace.addEntry(statement.getPosition(), var, new PrimitiveTraceEntry(var,
				        object));

			}
		} catch (CodeUnderTestException e) {
			logger.error("", e);
			//e.printStackTrace();
			//System.exit(0);
			//throw new UnsupportedOperationException();
		}
	}
}
