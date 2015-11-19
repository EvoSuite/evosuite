package org.evosuite.symbolic.solver.search;

import java.util.Collection;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.SolverTimeoutException;

abstract class VariableAVM {

	protected final Collection<Constraint<?>> cnstr;
	private final long start_time;
	private final long timeout;

	public VariableAVM(Collection<Constraint<?>> cnstr, long startTimeMillis, long timeout) {
		this.cnstr = cnstr;
		this.start_time = startTimeMillis;
		this.timeout = timeout;
	}

	protected boolean isFinished() {
		long current_time = System.currentTimeMillis();
		return (current_time - start_time) > timeout;
	}
	
	public abstract boolean applyAVM() throws SolverTimeoutException;
}
