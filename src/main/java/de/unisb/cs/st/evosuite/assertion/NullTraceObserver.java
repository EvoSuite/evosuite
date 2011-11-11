/*
 * Copyright (C) 2010 Saarland University
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
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.assertion;

import de.unisb.cs.st.evosuite.testcase.CodeUnderTestException;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class NullTraceObserver extends AssertionTraceObserver<NullTraceEntry> {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.AssertionTraceObserver#visit(de.unisb.cs.st.evosuite.testcase.StatementInterface, de.unisb.cs.st.evosuite.testcase.Scope, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	protected void visit(StatementInterface statement, Scope scope, VariableReference var) {
		logger.debug("Checking for null of " + var);
		try {
			if (var == null || var.isPrimitive())
				return;

			Object object = var.getObject(scope);
			trace.addEntry(statement.getPosition(), var, new NullTraceEntry(var,
			        object == null));
		} catch (CodeUnderTestException e) {
			throw new UnsupportedOperationException();
		}
	}
}
