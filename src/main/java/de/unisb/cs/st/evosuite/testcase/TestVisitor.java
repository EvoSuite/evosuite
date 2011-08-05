/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;


/**
 * @author fraser
 * 
 */
public interface TestVisitor {

	public abstract void visitPrimitiveStatement(PrimitiveStatement<?> statement);

	public abstract void visitFieldStatement(FieldStatement statement);

	public abstract void visitMethodStatement(MethodStatement statement);

	public abstract void visitConstructorStatement(ConstructorStatement statement);

	public abstract void visitArrayStatement(ArrayStatement statement);

	public abstract void visitAssignmentStatement(AssignmentStatement statement);

}
