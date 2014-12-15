package org.evosuite.symbolic.solver.smt;

import java.util.HashSet;
import java.util.Set;

public final class SmtConstantCollector implements SmtExprVisitor<Void, Void> {

	@Override
	public Void visit(SmtIntConstant n, Void arg) {
		smtConstants.add(n);
		return null;
	}

	@Override
	public Void visit(SmtRealConstant n, Void arg) {
		smtConstants.add(n);
		return null;
	}

	@Override
	public Void visit(SmtStringConstant n, Void arg) {
		smtConstants.add(n);
		return null;
	}

	private final Set<SmtConstant> smtConstants = new HashSet<SmtConstant>();

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

	@Override
	public Void visit(SmtOperation n, Void arg) {
		for (SmtExpr expr : n.getArguments()) {
			expr.accept(this, null);
		}
		return null;
	}

	public Set<SmtConstant> getSmtConstants() {
		return smtConstants;
	}
}
