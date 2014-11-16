/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.testcase;


/**
 * <p>
 * TestVisitor interface.
 * </p>
 * 
 * @author fraser
 */
public abstract class TestVisitor {

	/**
	 * <p>
	 * visitTestCase
	 * </p>
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 */
	public abstract void visitTestCase(TestCase test);

	/**
	 * <p>
	 * visitPrimitiveStatement
	 * </p>
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.PrimitiveStatement} object.
	 */
	public abstract void visitPrimitiveStatement(PrimitiveStatement<?> statement);

	/**
	 * <p>
	 * visitFieldStatement
	 * </p>
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.FieldStatement} object.
	 */
	public abstract void visitFieldStatement(FieldStatement statement);

	/**
	 * <p>
	 * visitMethodStatement
	 * </p>
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.MethodStatement} object.
	 */
	public abstract void visitMethodStatement(MethodStatement statement);

	/**
	 * <p>
	 * visitConstructorStatement
	 * </p>
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.ConstructorStatement} object.
	 */
	public abstract void visitConstructorStatement(ConstructorStatement statement);

	/**
	 * <p>
	 * visitArrayStatement
	 * </p>
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.ArrayStatement} object.
	 */
	public abstract void visitArrayStatement(ArrayStatement statement);

	/**
	 * <p>
	 * visitAssignmentStatement
	 * </p>
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.AssignmentStatement} object.
	 */
	public abstract void visitAssignmentStatement(AssignmentStatement statement);

	/**
	 * <p>
	 * visitNullStatement
	 * </p>
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.NullStatement} object.
	 */
	public abstract void visitNullStatement(NullStatement statement);

	/**
	 * <p>
	 * visitPrimitiveExpression
	 * </p>
	 * 
	 * @param primitiveExpression
	 *            a {@link org.evosuite.testcase.PrimitiveExpression} object.
	 */
	public abstract void visitPrimitiveExpression(PrimitiveExpression primitiveExpression);

	/**
	 * <p>
	 * visitStatement
	 * </p>
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.StatementInterface} object.
	 */
	public void visitStatement(StatementInterface statement) {

		if (statement instanceof PrimitiveStatement<?>)
			visitPrimitiveStatement((PrimitiveStatement<?>) statement);
		else if (statement instanceof FieldStatement)
			visitFieldStatement((FieldStatement) statement);
		else if (statement instanceof ConstructorStatement)
			visitConstructorStatement((ConstructorStatement) statement);
		else if (statement instanceof MethodStatement)
			visitMethodStatement((MethodStatement) statement);
		else if (statement instanceof AssignmentStatement)
			visitAssignmentStatement((AssignmentStatement) statement);
		else if (statement instanceof ArrayStatement)
			visitArrayStatement((ArrayStatement) statement);
		else if (statement instanceof NullStatement)
			visitNullStatement((NullStatement) statement);
		else if (statement instanceof PrimitiveExpression)
			visitPrimitiveExpression((PrimitiveExpression) statement);
		else
			throw new RuntimeException("Unknown statement type: " + statement);
	}
}
