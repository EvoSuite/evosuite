package org.evosuite.symbolic.solver.smt;

import java.util.HashSet;
import java.util.Set;

public final class SmtVarCollector implements SmtExprVisitor<Void, Void> {

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

	private final Set<SmtVariable> variableNames = new HashSet<SmtVariable>();

	@Override
	public Void visit(SmtIntVariable n, Void arg) {
		variableNames.add(n);
		return null;
	}

	@Override
	public Void visit(SmtRealVariable n, Void arg) {
		variableNames.add(n);
		return null;
	}

	@Override
	public Void visit(SmtStringVariable n, Void arg) {
		variableNames.add(n);
		return null;
	}

	@Override
	public Void visit(SmtOperation n, Void arg) {
		for (SmtExpr expr : n.getArguments()) {
			expr.accept(this, null);
		}
		return null;
	}

	public Set<SmtVariable> getVariableNames() {
		return variableNames;
	}
}
