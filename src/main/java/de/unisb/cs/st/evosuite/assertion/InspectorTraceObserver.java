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

import java.util.List;
import java.util.concurrent.TimeoutException;

import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class InspectorTraceObserver extends AssertionTraceObserver<InspectorTraceEntry> {

	private final InspectorManager manager = InspectorManager.getInstance();

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.AssertionTraceObserver#visit(de.unisb.cs.st.evosuite.testcase.StatementInterface, de.unisb.cs.st.evosuite.testcase.Scope, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	protected void visit(StatementInterface statement, Scope scope, VariableReference var) {
		// TODO: Check the variable class is complex?

		// We don't want inspector checks on string constants
		if (statement instanceof PrimitiveStatement<?>)
			return;

		logger.debug("Checking for inspectors of " + var + " at statement "
		        + statement.getPosition());
		List<Inspector> inspectors = manager.getInspectors(var.getVariableClass());

		InspectorTraceEntry entry = new InspectorTraceEntry(var);

		for (Inspector i : inspectors) {

			// No inspectors from java.lang.Object
			if (i.getMethod().getDeclaringClass().equals(Object.class))
				continue;

			try {
				Object target = var.getObject(scope);
				if (target != null) {
					Object value = i.getValue(target);
					logger.debug("Inspector " + i.getMethodCall() + " is: " + value);

					// We need no assertions that include the memory location
					if (i.getMethodCall().equals("toString")) {
						if (!value.toString().matches("@[abcdef\\d]+"))
							continue;
					}

					entry.addValue(i, value);
				}
			} catch (Exception e) {
				if (e instanceof TimeoutException) {
					logger.info("Timeout during inspector call - deactivating inspector "
					        + i.getMethodCall());
					manager.removeInspector(var.getVariableClass(), i);
				}
				logger.debug("Exception " + e + " / " + e.getCause());
				if (e.getCause() != null
				        && !e.getCause().getClass().equals(NullPointerException.class)) {
					logger.debug("Exception during call to inspector: " + e + " - "
					        + e.getCause());
				}
			}
		}
		logger.debug("Found " + entry.size() + " inspectors for " + var
		        + " at statement " + statement.getPosition());

		trace.addEntry(statement.getPosition(), var, entry);

	}
}
