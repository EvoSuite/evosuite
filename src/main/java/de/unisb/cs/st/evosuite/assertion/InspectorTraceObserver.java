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
				result.add(i.getValue(retval.getObject(scope)));
				//logger.info("New inspector result for variable of type "+retval.getClassName()+"/" + retval.getVariableClass().getName()+": "+i.getClassName()+"."+i.getMethodCall()+" -> "+i.getValue(scope.get(retval)));
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
						trace.calleeMap.get(statement.getPosition()).put(i,
						                                                 i.getValue(callee.getObject(scope)));
					}
				}
			}
		}

	}

	public InspectorTrace getTrace() {
		return trace.clone();
	}
}
