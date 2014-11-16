package org.evosuite.symbolic.expr;

public interface ConstraintVisitor<K,V> {

	public K visit(IntegerConstraint n, V arg);
	
	public K visit(RealConstraint n, V arg);
	
	public K visit(StringConstraint n, V arg);
}
