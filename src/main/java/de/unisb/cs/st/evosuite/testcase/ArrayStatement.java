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

import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * An array statement creates a new array
 * 
 * @author Gordon Fraser
 * 
 */
public class ArrayStatement extends AbstractStatement {

	private static final long serialVersionUID = -2858236370873914156L;

	private int length = 0;

	public ArrayStatement(TestCase tc, java.lang.reflect.Type type) {
		this(tc, type, Randomness.nextInt(Properties.MAX_ARRAY) + 1);
		logger.debug("Chosen length: " + this.length + "/" + Properties.MAX_ARRAY);
	}

	public ArrayStatement(TestCase tc, java.lang.reflect.Type type, int length) {
		super(tc, new ArrayReference(tc, new GenericClass(type), length));
		this.length = length;
	}

	public int size() {
		return length;
	}

	public void setSize(int size) {
		this.length = size;
	}

	@Override
	public StatementInterface copy(TestCase newTestCase, int offset) {
		ArrayStatement copy = new ArrayStatement(newTestCase, retval.getType(), length);
		// copy.assertions = copyAssertions(newTestCase, offset);
		return copy;
	}

	@Override
	public boolean equals(Object s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		ArrayStatement as = (ArrayStatement) s;
		if (length != as.length)
			return false;
		if (retval.equals(as.retval)) {
			return true;
		} else {
			return false;
		}

		// if (!Arrays.equals(variables, other.variables))
		// return false;

	}

	@Override
	public Throwable execute(Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {
		// Add array variable to pool
		try {
			retval.setObject(scope,
			                 Array.newInstance((Class<?>) retval.getComponentType(),
			                                   length));
		} catch (CodeUnderTestException e) {
			exceptionThrown = e.getCause();
		}
		return exceptionThrown;

	}

	@Override
	public String getCode(Throwable exception) {
		String type = retval.getSimpleClassName().replaceFirst("\\[\\]", "");
		String multiDimensions = "";
		while (type.contains("[]")) {
			multiDimensions += "[]";
			type = type.replaceFirst("\\[\\]", "");
		}
		return retval.getSimpleClassName() + " " + retval.getName() + " = new " + type
		        + "[" + length + "]" + multiDimensions + ";";
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new HashSet<VariableReference>();
		references.add(retval);
		return references;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#replace(de.unisb.cs.st.evosuite.testcase.VariableReference, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replace(VariableReference var1, VariableReference var2) {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = retval.hashCode();
		result = prime * result + length;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#getBytecode(org.objectweb.
	 * asm.commons.GeneratorAdapter)
	 */
	@Override
	public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
	        Throwable exception) {
		mg.push(length);
		mg.newArray(Type.getType((Class<?>) retval.getComponentType()));
		retval.storeBytecode(mg, locals);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#getUniqueVariableReferences()
	 */
	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		return new ArrayList<VariableReference>(getVariableReferences());
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#isValid()
	 */
	@Override
	public boolean isValid() {
		return super.isValid();
	}

	@Override
	public boolean same(StatementInterface s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		ArrayStatement as = (ArrayStatement) s;
		if (length != as.length)
			return false;
		if (retval.same(as.retval)) {
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.AbstractStatement#mutate(de.unisb.cs.st.evosuite.testcase.TestCase, de.unisb.cs.st.evosuite.testcase.AbstractTestFactory)
	 */
	@Override
	public boolean mutate(TestCase test, AbstractTestFactory factory) {
		return true; // TODO: Possibly increase length?
	}

	@Override
	public AccessibleObject getAccessibleObject() {
		return null;
	}

	@Override
	public boolean isAssignmentStatement() {
		return false;
	}
}
