package org.evosuite.symbolic.solver.smt;

public abstract class SmtExpr {

	public abstract <K, V> K accept(SmtExprVisitor<K, V> v, V arg);

	public abstract boolean isSymbolic();
}
