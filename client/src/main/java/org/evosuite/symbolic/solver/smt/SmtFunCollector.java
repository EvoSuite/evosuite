package org.evosuite.symbolic.solver.smt;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.symbolic.solver.smt.SmtOperation.Operator;

public class SmtFunCollector implements SmtExprVisitor<Void, Void> {

	@Override
	public Void visit(SmtIntConstant n, Void arg) {
		return null;
	}

	@Override
	public Void visit(SmtRealConstant n, Void arg) {
		return null;
	}

	@Override
	public Void visit(SmtStringConstant n, Void arg) {
		return null;
	}

	@Override
	public Void visit(SmtIntVariable n, Void arg) {
		return null;
	}

	@Override
	public Void visit(SmtRealVariable n, Void arg) {
		return null;
	}

	@Override
	public Void visit(SmtStringVariable n, Void arg) {
		return null;
	}

	private final Set<Operator> operators = new HashSet<Operator>();

	@Override
	public Void visit(SmtOperation n, Void arg) {
		operators.add(n.getOperator());
		for (SmtExpr argument : n.getArguments()) {
			argument.accept(this, null);
		}
		return null;
	}

	public Set<Operator> getOperators() {
		return operators;
	}
}
