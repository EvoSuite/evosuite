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
 */
/**
 * 
 */
package org.evosuite.assertion;

import org.evosuite.testcase.ExecutionObserver;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Gordon Fraser
 * 
 */
public abstract class AssertionTraceObserver<T extends OutputTraceEntry> extends
        ExecutionObserver {

	protected static Logger logger = LoggerFactory.getLogger(AssertionTraceObserver.class);

	protected OutputTrace<T> trace = new OutputTrace<T>();

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#output(int, java.lang.String)
	 */
	@Override
	public void output(int position, String output) {
		// Default behavior is to ignore console output

	}

	protected void visitDependencies(StatementInterface statement, Scope scope) {
		for (VariableReference var : currentTest.getDependencies(statement.getReturnValue())) {
			if (!var.isVoid()) {
				visit(statement, scope, var);
			}
		}
	}

	protected void visitReturnValue(StatementInterface statement, Scope scope) {
		if (!statement.getReturnClass().equals(void.class))
			visit(statement, scope, statement.getReturnValue());
	}

	protected abstract void visit(StatementInterface statement, Scope scope,
	        VariableReference var);

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#statement(de.unisb.cs.st.evosuite.testcase.StatementInterface, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		//visitReturnValue(statement, scope);
		visitDependencies(statement, scope);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#clear()
	 */
	@Override
	public void clear() {
		trace.clear();
	}

	public OutputTrace<T> getTrace() {
		return trace.clone();
	}

}
