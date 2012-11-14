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

public class ComparisonTraceObserver extends AssertionTraceObserver<ComparisonTraceEntry> {

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.AssertionTraceObserver#visit(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	protected void visit(StatementInterface statement, Scope scope, VariableReference var) {
		try {
			Object object = var.getObject(scope);
			if (object == null)
				return;

			ComparisonTraceEntry entry = new ComparisonTraceEntry(var);

			for (VariableReference other : scope.getElements(var.getType())) {
				Object otherObject = other.getObject(scope);
				// TODO: Create a matrix of object comparisons?
				if (otherObject == null)
					continue; // TODO: Don't do this?

				if (object == otherObject)
					continue; // Don't compare with self?

				if (statement instanceof PrimitiveStatement
				        && currentTest.getStatement(other.getStPosition()) instanceof PrimitiveStatement)
					continue; // Don't compare two primitives

				try {
					logger.debug("Comparison of " + var + " with " + other + " is: "
					        + object.equals(otherObject));
					entry.addEntry(other, object.equals(otherObject));
				} catch (Throwable t) {
					logger.debug("Exception during equals: " + t);
					// ignore?
				}
				if (object instanceof Comparable<?>) {
					// TODO
				}
			}

			trace.addEntry(statement.getPosition(), var, entry);
		} catch (CodeUnderTestException e) {
			logger.debug("", e);
		}

	}

}
