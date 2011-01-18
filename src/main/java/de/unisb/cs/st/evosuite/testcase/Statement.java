/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.assertion.Assertion;

/**
 * Abstract superclass of test case statements
 * 
 * @author Gordon Fraser
 *
 */
public abstract class Statement {

	protected static Logger logger = Logger.getLogger(Statement.class);

	VariableReference retval = null;
	
	protected Set<Assertion> assertions = new HashSet<Assertion>();
	
	protected Throwable exceptionThrown = null;

	/**
	 * Adjust all variables up to position by delta
	 * @param position
	 * @param delta
	 */
	public abstract void adjustVariableReferences(int position, int delta);

	/**
	 * Check if the statement makes use of var
	 * @param var
	 *   Variable we are checking for
	 * @return
	 *   True if var is referenced
	 */
	public boolean references(VariableReference var) {
		return getVariableReferences().contains(var);
	}

	public abstract Throwable execute(Scope scope, PrintStream out) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, InstantiationException;
	
	/**
	 * Get Java representation of statement
	 * @return
	 */
	public String getCode() {
		return getCode(null);
	}
	
	/**
	 * Get Java representation of statement
	 * @return
	 */
	public abstract String getCode(Throwable exception);
	
	/**
	 * Generate bytecode by calling method generator
	 * @param mg
	 */
	public abstract void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals, Throwable exception);
	
	/**
	 * 
	 * @return
	 *   Generic type of return value
	 */
	public Type getReturnType() {
		return retval.getType();
	}

	/**
	 * 
	 * @return
	 *   Raw class of return value
	 */
	public Class<?> getReturnClass() {
		return (Class<?>)retval.getType();
	}

	/**
	 * Equality check
	 * @param s
	 *   Other statement
	 * @return
	 *   True if equals
	 */
	public abstract boolean equals(Statement s);
	
	/**
	 * Generate hash code
	 */
	public abstract int hashCode();
	
	
	/**
	 * @return
	 *   Variable representing return value
	 */
	public VariableReference getReturnValue() {
		return retval;
	}

	public abstract Set<VariableReference> getVariableReferences();

	public abstract void replace(VariableReference old_var, VariableReference new_var);
	
	/**
	 * Create copies of all attached assertions
	 * 
	 * @return
	 *   List of the assertion copies
	 */
	protected Set<Assertion> cloneAssertions() {
		Set<Assertion> copy = new HashSet<Assertion>();
		for(Assertion a: assertions) {
			if(a == null) {
				logger.info("Assertion is null!");
				logger.info("Statement has assertions: "+assertions.size());
			} else
			copy.add(a.clone());
		}
		return copy;
	}
	
	/**
	 * Create deep copy of statement
	 */
	public abstract Statement clone();
	
	/**
	 * Check if there are assertions
	 * 
	 * @return
	 *   True if there are assertions
	 */
	public boolean hasAssertions() {
		return !assertions.isEmpty();
	}
	
	/**
	 * Add a new assertion to statement
	 * 
	 * @param assertion
	 *   Assertion to be added
	 */
	public void addAssertion(Assertion assertion) {
		if(assertion == null) {
			logger.warn("Trying to add null assertion!");
		} else {
			logger.debug("Adding assertion");
			assertions.add(assertion);
		}
	}
	
	/**
	 * Get Java code representation of assertions
	 * 
	 * @return
	 *   String representing all assertions attached to this statement
	 */
	public String getAssertionCode() {
		String ret_val = "";
		for(Assertion a : assertions) {
			if(a != null)
				ret_val += a.getCode() +"\n";
		}
		return ret_val;
	}
	
	/**
	 * Fix variable references in assertions
	 * 
	 * @param position
	 * @param delta
	 */
	public void adjustAssertions(int position, int delta) {
		for(Assertion a : assertions) {
			if(a != null)
				a.getSource().adjust(delta, position);
		}
	}	
	
	/**
	 * Delete all assertions attached to this statement
	 */
	public void removeAssertions() {
		assertions.clear();
	}
	
	/**
	 * Return list of assertions
	 */
	public Set<Assertion> getAssertions() {
		return assertions;
	}
	
	public Set<Class<?>> getDeclaredExceptions() {
		Set<Class<?>> ex = new HashSet<Class<?>>();
		return ex;
	}
	
	public static Class<?> getExceptionClass(Throwable t) {
		Class<?> clazz = t.getClass();
		while(!Modifier.isPublic(clazz.getModifiers())) {
			clazz = clazz.getSuperclass();
		}
		return clazz;
	}
}
