/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

/**
 * @author fraser
 * 
 */
public interface TestVisitor {

	public void visitTestCase(TestCase test);

	public void visitPrimitiveStatement(PrimitiveStatement<?> statement);

	public void visitFieldStatement(FieldStatement statement);

	public void visitMethodStatement(MethodStatement statement);

	public void visitConstructorStatement(ConstructorStatement statement);

	public void visitArrayStatement(ArrayStatement statement);

	public void visitAssignmentStatement(AssignmentStatement statement);

	public void visitNullStatement(NullStatement statement);

}
