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

import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class PrimitiveOutputTraceObserver extends ExecutionObserver {

	private final PrimitiveOutputTrace trace = new PrimitiveOutputTrace();

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		VariableReference retval = statement.getReturnValue();

		if (retval == null)
			return;

		Object object = scope.get(retval);
		if (object == null || object.getClass().isPrimitive()
		        || object.getClass().isEnum() || isWrapperType(object.getClass())
		        || object instanceof String) {
			trace.trace.put(statement.getPosition(), object);
			/*
			if(object == null)
				logger.info("Adding null (Type: "+retval.type.getName()+")");
			else
				logger.info("Adding object of type "+object.getClass().getName());
				*/
		}
		//else
		//	logger.info("Not adding object of type "+object.getClass().getName());
	}

	@Override
	public void clear() {
		trace.trace.clear();
	}

	public PrimitiveOutputTrace getTrace() {
		return trace.clone();
	}

}
