package org.evosuite.symbolic.solver.smt;

public interface SmtExprVisitor<K, V> {

	public K visit(SmtBooleanConstant n, V arg);

	public K visit(SmtIntConstant n, V arg);

	public K visit(SmtRealConstant n, V arg);

	public K visit(SmtStringConstant n, V arg);

	public K visit(SmtIntVariable n, V arg);

	public K visit(SmtRealVariable n, V arg);

	public K visit(SmtStringVariable n, V arg);

	public K visit(SmtOperation n, V arg);

}
