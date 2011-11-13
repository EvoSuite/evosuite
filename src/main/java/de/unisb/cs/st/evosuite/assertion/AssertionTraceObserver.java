/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

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
