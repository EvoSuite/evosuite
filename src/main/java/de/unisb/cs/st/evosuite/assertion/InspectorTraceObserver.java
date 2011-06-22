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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class InspectorTraceObserver extends ExecutionObserver {

	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(InspectorTraceObserver.class);

	private final InspectorManager manager = InspectorManager.getInstance();

	private final InspectorTrace trace = new InspectorTrace();

	@Override
	public void clear() {
		trace.clear();
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		VariableReference retval = statement.getReturnValue();

		if (retval == null)
			return;

		// Add inspector calls on return value
		List<Inspector> inspectors = manager.getInspectors(retval.getVariableClass());
		if (retval.getObject(scope) != null && !inspectors.isEmpty()) {
			List<Object> result = new ArrayList<Object>();
			for (Inspector i : inspectors) {
				try {
					Object value = i.getValue(retval.getObject(scope));
					result.add(value);
					// TODO: Need to keep reference to inspector if exception is thrown!
				} catch (IllegalArgumentException e) {
					logger.info("Exception during call to inspector: " + e);
					continue;
				} catch (IllegalAccessException e) {
					logger.info("Exception during call to inspector: " + e);
					continue;
				} catch (InvocationTargetException e) {
					logger.info("Exception during call to inspector: " + e.getCause());
					continue;
				}
			}

			trace.inspector_results.put(statement.getPosition(), result);
			trace.return_values.put(statement.getPosition(), retval);
		}

		// Add inspector calls on callee
		if (statement instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) statement;
			if (!ms.isStatic()) {
				inspectors = manager.getInspectors(ms.getMethod().getDeclaringClass());
				if (!inspectors.isEmpty()) {
					trace.calleeMap.put(statement.getPosition(),
					                    new HashMap<Inspector, Object>());

					VariableReference callee = ms.getCallee();
					if (callee.getObject(scope) == null)
						return;
					for (Inspector i : inspectors) {
						try {
							Object value = i.getValue(callee.getObject(scope));
							trace.calleeMap.get(statement.getPosition()).put(i, value);
						} catch (Exception e) {
							logger.info("Exception during call to inspector: " + e);
						}
					}
				}
			}
		}

	}

	public InspectorTrace getTrace() {
		return trace.clone();
	}
}
