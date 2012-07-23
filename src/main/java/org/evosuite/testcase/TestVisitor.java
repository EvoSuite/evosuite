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
 * <p>TestVisitor interface.</p>
 *
 * @author fraser
 */
public interface TestVisitor {

	/**
	 * <p>visitTestCase</p>
	 *
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 */
	public void visitTestCase(TestCase test);

	/**
	 * <p>visitPrimitiveStatement</p>
	 *
	 * @param statement a {@link org.evosuite.testcase.PrimitiveStatement} object.
	 */
	public void visitPrimitiveStatement(PrimitiveStatement<?> statement);

	/**
	 * <p>visitFieldStatement</p>
	 *
	 * @param statement a {@link org.evosuite.testcase.FieldStatement} object.
	 */
	public void visitFieldStatement(FieldStatement statement);

	/**
	 * <p>visitMethodStatement</p>
	 *
	 * @param statement a {@link org.evosuite.testcase.MethodStatement} object.
	 */
	public void visitMethodStatement(MethodStatement statement);

	/**
	 * <p>visitConstructorStatement</p>
	 *
	 * @param statement a {@link org.evosuite.testcase.ConstructorStatement} object.
	 */
	public void visitConstructorStatement(ConstructorStatement statement);

	/**
	 * <p>visitArrayStatement</p>
	 *
	 * @param statement a {@link org.evosuite.testcase.ArrayStatement} object.
	 */
	public void visitArrayStatement(ArrayStatement statement);

	/**
	 * <p>visitAssignmentStatement</p>
	 *
	 * @param statement a {@link org.evosuite.testcase.AssignmentStatement} object.
	 */
	public void visitAssignmentStatement(AssignmentStatement statement);

	/**
	 * <p>visitNullStatement</p>
	 *
	 * @param statement a {@link org.evosuite.testcase.NullStatement} object.
	 */
	public void visitNullStatement(NullStatement statement);

	/**
	 * <p>visitPrimitiveExpression</p>
	 *
	 * @param primitiveExpression a {@link org.evosuite.testcase.PrimitiveExpression} object.
	 */
	public void visitPrimitiveExpression(PrimitiveExpression primitiveExpression);

}
