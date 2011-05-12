/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.assertion.Assertion;

/**
 * Abstract superclass of test case statements
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class AbstractStatement implements StatementInterface {

	protected static Logger logger = Logger.getLogger(AbstractStatement.class);

	protected VariableReference retval;
	protected final TestCase tc;
	
	protected Set<Assertion> assertions = new HashSet<Assertion>();

	protected Throwable exceptionThrown = null;

	protected AbstractStatement(TestCase tc, VariableReference retval){
		assert(retval!=null);
		this.retval=retval;
		this.tc=tc;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#references(de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public boolean references(VariableReference var) {
		return getVariableReferences().contains(var);
	}
	
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getCode()
	 */
	@Override
	public String getCode() {
		return getCode(null);
	}

	@Override
	public final StatementInterface clone(){
		throw new UnsupportedOperationException("Use statementInterface.clone(TestCase)");
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getReturnType()
	 */
	@Override
	public Type getReturnType() {
		return retval.getType();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getReturnClass()
	 */
	@Override
	public Class<?> getReturnClass() {
		return (Class<?>) retval.getType();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getReturnValue()
	 */
	@Override
	public VariableReference getReturnValue() {
		return retval;
	}

	/**
	 * Create copies of all attached assertions
	 * 
	 * @return List of the assertion copies
	 */
	protected Set<Assertion> cloneAssertions(TestCase newTestCase) {
		Set<Assertion> copy = new HashSet<Assertion>();
		for (Assertion a : assertions) {
			if (a == null) {
				logger.info("Assertion is null!");
				logger.info("Statement has assertions: " + assertions.size());
			} else
				copy.add(a.clone(newTestCase));
		}
		return copy;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#hasAssertions()
	 */
	@Override
	public boolean hasAssertions() {
		return !assertions.isEmpty();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#addAssertion(de.unisb.cs.st.evosuite.assertion.Assertion)
	 */
	@Override
	public void addAssertion(Assertion assertion) {
		if (assertion == null) {
			logger.warn("Trying to add null assertion!");
		} else {
			logger.debug("Adding assertion");
			assertions.add(assertion);
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getAssertionCode()
	 */
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
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#removeAssertions()
	 */
	@Override
	public void removeAssertions() {
		assertions.clear();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#removeAssertion(de.unisb.cs.st.evosuite.assertion.Assertion)
	 */
	@Override
	public void removeAssertion(Assertion assertion) {
		assertions.remove(assertion);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getAssertions()
	 */
	@Override
	public Set<Assertion> getAssertions() {
		return assertions;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getDeclaredExceptions()
	 */
	@Override
	public Set<Class<?>> getDeclaredExceptions() {
		Set<Class<?>> ex = new HashSet<Class<?>>();
		return ex;
	}

	public static Class<?> getExceptionClass(Throwable t) {
		Class<?> clazz = t.getClass();
		while (!Modifier.isPublic(clazz.getModifiers())) {
			clazz = clazz.getSuperclass();
		}
		return clazz;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getPosition()
	 */
	@Override
	public int getPosition() {
		return retval.getStPosition();
	}
}
