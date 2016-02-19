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
package org.evosuite.assertion;

import org.evosuite.Properties;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;

/**
 * <p>
 * SameTraceObserver class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class SameTraceObserver extends AssertionTraceObserver<SameTraceEntry> {

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.AssertionTraceObserver#visit(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	protected void visit(Statement statement, Scope scope, VariableReference var) {
		// TODO: Only MethodStatement?
		if(statement.isAssignmentStatement())
			return;
		if(statement instanceof PrimitiveStatement<?>)
			return;
		if(statement instanceof ArrayStatement)
			return;
		if(statement instanceof ConstructorStatement)
			return;
		
		try {
			Object object = var.getObject(scope);
			if (object == null)
				return;
			if(var.isPrimitive())
				return;
			if(var.isString() && Properties.INLINE)
				return; // After inlining the value of assertions would be different

			SameTraceEntry entry = new SameTraceEntry(var);

			for (VariableReference other : scope.getElements(var.getType())) {
				if (other == var)
					continue;
				if(other.isPrimitive())
					continue;
                if(other.isWrapperType())
                    continue; // Issues with inlining resulting in unstable assertions

				Object otherObject = other.getObject(scope);
				if (otherObject == null)
					continue;
				if(otherObject.getClass() != object.getClass())
					continue;
				
				try {
					logger.debug("Comparison of {} with {}", var, other);
					entry.addEntry(other, object == otherObject);
				} catch (Throwable t) {
					logger.debug("Exception during equals: " + t);
					// ignore?
				}
			}

			trace.addEntry(statement.getPosition(), var, entry);
		} catch (CodeUnderTestException e) {
			logger.debug("", e);
		}
	}

	@Override
	public void testExecutionFinished(ExecutionResult r, Scope s) {
		// do nothing
	}
}