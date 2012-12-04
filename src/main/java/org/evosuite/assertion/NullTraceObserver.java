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
 * 
 * @author Gordon Fraser
 */
package org.evosuite.assertion;

import org.evosuite.testcase.CodeUnderTestException;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.VariableReference;

public class NullTraceObserver extends AssertionTraceObserver<NullTraceEntry> {

	/** {@inheritDoc} */
	@Override
	public void afterStatement(StatementInterface statement, Scope scope,
	        Throwable exception) {
		visitReturnValue(statement, scope);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.AssertionTraceObserver#visit(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	protected void visit(StatementInterface statement, Scope scope, VariableReference var) {
		logger.debug("Checking for null of " + var);
		try {
			if (var == null
			        || var.isPrimitive()
			        || var.isEnum()
			        || currentTest.getStatement(var.getStPosition()) instanceof PrimitiveStatement)
				return;

			Object object = var.getObject(scope);
			trace.addEntry(statement.getPosition(), var, new NullTraceEntry(var,
			        object == null));
		} catch (CodeUnderTestException e) {
			logger.debug("", e);
			//throw new UnsupportedOperationException();
		}
	}
}
