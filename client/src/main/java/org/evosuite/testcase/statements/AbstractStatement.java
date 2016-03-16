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
package org.evosuite.testcase.statements;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.evosuite.assertion.Assertion;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.utils.generic.GenericClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass of test case statements
 * 
 * @author Gordon Fraser
 */
public abstract class AbstractStatement implements Statement, Serializable {

	/**
	 * An interface to enable the concrete statements to use the executer/1
	 * method.
	 * 
	 **/
	protected abstract class Executer {
		/**
		 * The execute statement should, when called only execute exactly one
		 * statement. For example executing java.reflect.Field.get()/1 could be
		 * the responsibility of the execute method. Execute SHOULD NOT catch
		 * any exceptions. Exception handling SHOULD be done by
		 * AbstractStatement.executer()/1.
		 */
		public abstract void execute() throws InvocationTargetException,
		        IllegalArgumentException, IllegalAccessException, InstantiationException,
		        CodeUnderTestException;

		/**
		 * A call to this method should return a set of throwables.
		 * AbstractStatement.executer()/1 will catch all exceptions thrown by
		 * Executer.execute()/1. All exception in the returned set will be
		 * thrown to a higher layer. If the others are thrown or returned by
		 * AbstractStatement.executer()/1 is to be defined by executer()/1.
		 * 
		 * @return
		 */
		public Set<Class<? extends Throwable>> throwableExceptions() {
			return new HashSet<Class<? extends Throwable>>();
		}
	}

	private static final long serialVersionUID = 8993506743384548704L;

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(AbstractStatement.class);

	/**
	 * The return value of this statement. Should never be null.
	 */
	protected VariableReference retval;
	
	/**
	 * Reference of the test case this statement belongs to. Should never be null.
	 */
	protected final TestCase tc;

	protected Set<Assertion> assertions = new LinkedHashSet<Assertion>();

	protected String comment = "";
	
	/**
	 * <p>
	 * Constructor for AbstractStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param retval
	 *            a {@link org.evosuite.testcase.variable.VariableReference} object.
	 */
	protected AbstractStatement(TestCase tc, VariableReference retval) throws IllegalArgumentException{
		if(tc==null){
			throw new IllegalArgumentException("tc cannot be null");
		}
		if(retval==null){
			throw new IllegalArgumentException("retval cannot be null");
		}
		this.retval = retval;
		this.tc = tc;
	}

	/**
	 * <p>
	 * Constructor for AbstractStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param type
	 *            a {@link java.lang.reflect.Type} object.
	 */
	protected AbstractStatement(TestCase tc, Type type) throws IllegalArgumentException{
		if(tc==null){
			throw new IllegalArgumentException("tc cannot be null");
		}
		if(type==null){
			throw new IllegalArgumentException("type cannot be null");
		}
		
		GenericClass c = new GenericClass(type);
		if (c.isArray()) {
			this.retval = new ArrayReference(tc, c, 0);
		} else {
			this.retval = new VariableReferenceImpl(tc, type);
		}
		this.tc = tc;
	}

	/**
	 * This method abstracts the exception handling away from the concrete
	 * statements. Thereby hopefully enabling us to have a more consistent
	 * approach to exceptions.
	 * 
	 * @param code
	 *            a {@link org.evosuite.testcase.statements.AbstractStatement.Executer}
	 *            object.
	 * @throws java.lang.reflect.InvocationTargetException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @throws java.lang.InstantiationException
	 *             if any.
	 * @return a {@link java.lang.Throwable} object.
	 */
	protected Throwable exceptionHandler(Executer code) throws InvocationTargetException,
	        IllegalArgumentException, IllegalAccessException, InstantiationException {
		try {
			code.execute();
			// } catch (CodeUnderTestException e) {
			// throw CodeUnderTestException.throwException(e);
			//}
		} catch (CodeUnderTestException e) {
			return e;
		} catch (EvosuiteError e) {
			/*
			 * Signal an error in evosuite code and are therefore always thrown
			 */
			throw e;
		} catch (Error e) {
			if (isAssignableFrom(e, code.throwableExceptions()))
				throw e;
			else
				return e;
		} catch (RuntimeException e) {
			if (isAssignableFrom(e, code.throwableExceptions()))
				throw e;
			else
				return e;
		} catch (InvocationTargetException e) {
			if (isAssignableFrom(e, code.throwableExceptions()))
				throw e;
			else
				return e;
		} catch (IllegalAccessException e) {
			if (isAssignableFrom(e, code.throwableExceptions()))
				throw e;
			else
				return e;
		} catch (InstantiationException e) {
			if (isAssignableFrom(e, code.throwableExceptions()))
				throw e;
			else
				return e;
		}

		return null;
	}

	/**
	 * Tests if concreteThrowable.getClass is assignable to any of the classes
	 * in throwableClasses
	 * 
	 * @param concreteThrowable
	 *            true if concreteThrowable is assignable
	 * @param throwableClasses
	 * @return
	 */
	private boolean isAssignableFrom(Throwable concreteThrowable,
	        Set<Class<? extends Throwable>> throwableClasses) {
		for (Class<? extends Throwable> t : throwableClasses) {
			if (t.isAssignableFrom(concreteThrowable.getClass())) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#references(org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean references(VariableReference var) {
		return getVariableReferences().contains(var);
	}

	/**
	 * <p>
	 * getAssertionReferences
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	protected Set<VariableReference> getAssertionReferences() {
		Set<VariableReference> variables = new LinkedHashSet<VariableReference>();
		for (Assertion assertion : assertions) {
			variables.addAll(assertion.getReferencedVariables());
		}
		return variables;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#SetRetval(org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void setRetval(VariableReference newRetVal) throws IllegalArgumentException {
		if(newRetVal==null){
			throw new IllegalArgumentException("newRetVal cannot be null");
		}
		this.retval = newRetVal;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getCode()
	 */
	/** {@inheritDoc} */
	@Override
	public String getCode() {
		return getCode(null);
	}

	/** {@inheritDoc} */
	@Override
	public String getCode(Throwable exception) {
		TestCodeVisitor visitor = new TestCodeVisitor();
		visitor.setException(this, exception);
		visitor.visitStatement(this);
		String code = visitor.getCode();
		return code.substring(0, code.length() - 2);
	}

	@Override
	public void addComment(String comment) {
		this.comment += comment;
	}
	
	public String getComment() {
		return comment;
	}
	
	/** {@inheritDoc} */
	@Override
	public final Statement clone() {
		throw new UnsupportedOperationException("Use statementInterface.clone(TestCase)");
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getReturnType()
	 */
	/** {@inheritDoc} */
	@Override
	public Type getReturnType() {
		return retval.getType();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getReturnClass()
	 */
	/** {@inheritDoc} */
	@Override
	public Class<?> getReturnClass() {
		return retval.getVariableClass();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getReturnValue()
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference getReturnValue() {
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getNumParameters()
	 */
	@Override
	public int getNumParameters() {
		return 0;
	}
	
	@Override
	public TestCase getTestCase() {
		return tc;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Create copies of all attached assertions
	 */
	@Override
	public Set<Assertion> copyAssertions(TestCase newTestCase, int offset) {
		Set<Assertion> copy = new LinkedHashSet<Assertion>();
		for (Assertion a : assertions) {
			if (a == null) {
				logger.info("Assertion is null!");
				logger.info("Statement has assertions: " + assertions.size());
			} else
				copy.add(a.copy(newTestCase, offset));
		}
		return copy;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#hasAssertions()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasAssertions() {
		return !assertions.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#addAssertion(org.evosuite.assertion.Assertion)
	 */
	/** {@inheritDoc} */
	@Override
	public void addAssertion(Assertion assertion) {
		if (assertion == null) {
			logger.warn("Trying to add null assertion!");
		} else {
			logger.debug("Adding assertion " + assertion.getCode());
			assert (assertion.isValid()) : "Invalid assertion detected: "
			        + assertion.getCode() + ", " + assertion.getSource() + ", "
			        + assertion.getValue();
			assertion.setStatement(this);
			assertions.add(assertion);
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#setAssertions(java.util.Set)
	 */
	/** {@inheritDoc} */
	@Override
	public void setAssertions(Set<Assertion> assertions) {
		for (Assertion assertion : assertions)
			assertion.setStatement(this);

		this.assertions = assertions;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getAssertionCode()
	 */
	/** {@inheritDoc} */
	@Override
	public String getAssertionCode() {
		String ret_val = "";
		for (Assertion a : assertions) {
			if (a != null)
				ret_val += a.getCode() + "\n";
		}
		return ret_val;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#removeAssertions()
	 */
	/** {@inheritDoc} */
	@Override
	public void removeAssertions() {
		assertions.clear();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#removeAssertion(org.evosuite.assertion.Assertion)
	 */
	/** {@inheritDoc} */
	@Override
	public void removeAssertion(Assertion assertion) {
		assertions.remove(assertion);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getAssertions()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<Assertion> getAssertions() {
		return assertions;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getDeclaredExceptions()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<Class<?>> getDeclaredExceptions() {
		Set<Class<?>> ex = new HashSet<Class<?>>();
		return ex;
	}

	/**
	 * <p>
	 * getExceptionClass
	 * </p>
	 * 
	 * @param t
	 *            a {@link java.lang.Throwable} object.
	 * @return a {@link java.lang.Class} object.
	 */
	public static Class<?> getExceptionClass(Throwable t) {
		Class<?> clazz = t.getClass();
		while (!Modifier.isPublic(clazz.getModifiers())) {
			clazz = clazz.getSuperclass();
		}
		return clazz;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getPosition()
	 */
	/** {@inheritDoc} */
	@Override
	public int getPosition() {
		return retval.getStPosition();
	}

	@Override
	public boolean isAccessible() {
		for(VariableReference var : getVariableReferences()) {
			if(!var.isAccessible()) 
				return false;
		}
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isValid() {
		retval.getStPosition();
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDeclaredException(Throwable t) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#mutate(org.evosuite.testcase.TestCase)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean mutate(TestCase test, TestFactory factory) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#clone(org.evosuite.testcase.TestCase)
	 */
	/** {@inheritDoc} */
	@Override
	public Statement clone(TestCase newTestCase) {
		Statement result = copy(newTestCase, 0);
		result.getReturnValue().setOriginalCode(retval.getOriginalCode());
		result.addComment(getComment());
		return result;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#changeClassLoader(java.lang.ClassLoader)
	 */
	/** {@inheritDoc} */
	@Override
	public void changeClassLoader(ClassLoader loader) {
		for (VariableReference var : getVariableReferences()) {
			var.changeClassLoader(loader);
		}
		for(Assertion assertion : assertions) {
			assertion.changeClassLoader(loader);
		}
	}

	/**
	 * <p>
	 * negate
	 * </p>
	 */
	public void negate() {
	}
	
	@Override
	public boolean isReflectionStatement() {
		return false;
	}
}
