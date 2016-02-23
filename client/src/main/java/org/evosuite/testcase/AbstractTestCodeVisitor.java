/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase;

import org.evosuite.PackageInfo;
import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.FieldStatement;
import org.evosuite.testcase.statements.FunctionalMockStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.NullStatement;
import org.evosuite.testcase.statements.PrimitiveExpression;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * The TestCodeVisitor is a visitor that produces a String representation of a
 * test case. This is the preferred way to produce executable code from EvoSuite
 * tests.
 * 
 * @author Gordon Fraser
 */
public class AbstractTestCodeVisitor extends TestVisitor {

	protected final Map<Integer, Throwable> exceptions = new HashMap<Integer, Throwable>();

	protected TestCase test = null;

	protected AbstractTestCodeVisitor tcv;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.evosuite.testcase.TestVisitor#visitTestCase(org.evosuite.testcase.TestCase)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitTestCase(TestCase test) {
		if(this.tcv != null) {
			this.tcv.visitTestCase(test);
		}
		this.test = test;
	}

	@Override
	public void visitPrimitiveStatement(PrimitiveStatement<?> statement) {
		if(this.tcv != null) {
			this.tcv.visitPrimitiveStatement(statement);
		}
	}

	@Override
	public void visitFieldStatement(FieldStatement statement) {
		if(this.tcv != null) {
			this.tcv.visitFieldStatement(statement);
		}
	}

	@Override
	public void visitMethodStatement(MethodStatement statement) {
		if(this.tcv != null) {
			this.tcv.visitMethodStatement(statement);
		}
	}

	@Override
	public void visitConstructorStatement(ConstructorStatement statement) {
		if(this.tcv != null) {
			this.tcv.visitConstructorStatement(statement);
		}
	}

	@Override
	public void visitArrayStatement(ArrayStatement statement) {
		if(this.tcv != null) {
			this.tcv.visitArrayStatement(statement);
		}
	}

	@Override
	public void visitAssignmentStatement(AssignmentStatement statement) {
		if(this.tcv != null) {
			this.tcv.visitAssignmentStatement(statement);
		}
	}

	@Override
	public void visitNullStatement(NullStatement statement) {
		if(this.tcv != null) {
			this.tcv.visitNullStatement(statement);
		}
	}

	@Override
	public void visitPrimitiveExpression(PrimitiveExpression primitiveExpression) {
		if(this.tcv != null) {
			this.tcv.visitPrimitiveExpression(primitiveExpression);
		}
	}

	@Override
	public void visitFunctionalMockStatement(FunctionalMockStatement functionalMockStatement) {
		if(this.tcv != null) {
			this.tcv.visitFunctionalMockStatement(functionalMockStatement);
		}
	}

	/**
	 * <p>
	 * clearExceptions
	 * </p>
	 */
	public void clearExceptions() {
		if(this.tcv != null) {
			this.tcv.clearExceptions();
		}
		this.exceptions.clear();
	}

	/**
	 * <p>
	 * Setter for the field <code>exceptions</code>.
	 * </p>
	 *
	 * @param exceptions
	 *            a {@link Map} object.
	 */
	public void setExceptions(Map<Integer, Throwable> exceptions) {
		if(this.tcv != null) {
			this.tcv.setExceptions(exceptions);
		}
		this.exceptions.putAll(exceptions);
	}

	/**
	 * <p>
	 * setException
	 * </p>
	 *
	 * @param statement
	 *            a {@link Statement} object.
	 * @param exception
	 *            a {@link Throwable} object.
	 */
	public void setException(Statement statement, Throwable exception) {
		if(this.tcv != null) {
			this.tcv.setException(statement, exception);
		}
		exceptions.put(statement.getPosition(), exception);
	}

	/**
	 * <p>
	 * getException
	 * </p>
	 *
	 * @param statement
	 *            a {@link Statement} object.
	 * @return a {@link Throwable} object.
	 */
	public Throwable getException(Statement statement) {
		if (exceptions != null && exceptions.containsKey(statement.getPosition()))
			return exceptions.get(statement.getPosition());

		return null;
	}

	protected boolean isTestUnstable() {
		return test!=null && test.isUnstable();
	}

	protected String getSourceClassName(Throwable exception){
		if(exception.getStackTrace().length == 0){
			return null;
		}
		return exception.getStackTrace()[0].getClassName();
	}

	protected boolean isValidSource(String sourceClass){
		return ! sourceClass.startsWith(PackageInfo.getEvoSuitePackage()+".") ||
				sourceClass.startsWith(PackageInfo.getEvoSuitePackage()+".runtime.");
	}

    protected Class<?> getExceptionClassToUse(Throwable exception){
        /*
            we can only catch a public class.
            for "readability" of tests, it shouldn't be a mock one either
          */
        Class<?> ex = exception.getClass();
        while (!Modifier.isPublic(ex.getModifiers()) || EvoSuiteMock.class.isAssignableFrom(ex) ||
				ex.getCanonicalName().startsWith("com.sun.")) {
            ex = ex.getSuperclass();
        }
        return ex;
    }
}
