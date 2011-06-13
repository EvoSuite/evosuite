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
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * An assignment statement assigns a variable to another variable. This is only
 * used to assign to array indices
 * 
 * @author Gordon Fraser
 * 
 */
public class AssignmentStatement extends AbstractStatement {

	private static final long serialVersionUID = 2051431241124468349L;

	public VariableReference parameter;

	public AssignmentStatement(TestCase tc, ArrayReference array, int array_index,
	        VariableReference value) {
		super(tc, new ArrayIndex(tc, array, array_index));
		this.parameter = value;
	}

	public void setArray(ArrayReference array) {
		this.retval = array;
	}

	public ArrayIndex getArrayIndexRef() {
		if (this.retval instanceof ArrayIndex) {
			return (ArrayIndex) super.retval;
		} else {
			throw new AssertionError(
			        "The array reference of an assignment statement must be an array");
		}
	}

	@Override
	public StatementInterface clone(TestCase newTestCase) {
		VariableReference newParam = newTestCase.getStatement(parameter.getStPosition()).getReturnValue(); //must be set as we only use this to clone whole testcases
		VariableReference newArray = newTestCase.getStatement(getArrayIndexRef().getArray().getStPosition()).getReturnValue();
		if (!(newArray instanceof ArrayReference)) {
			throw new AssertionError(
			        "Can't clone this assignment statement in new TestCase. As on position: "
			                + getArrayIndexRef().getArray().getStPosition()
			                + " of the new TestCase no Array is created");
		}
		assert (newParam != null);
		AssignmentStatement copy = new AssignmentStatement(newTestCase,
		        (ArrayReference) newArray, getArrayIndexRef().getArrayIndex(), newParam);
		return copy;
	}

	@Override
	public Throwable execute(Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {

		try {
			Object value = scope.get(parameter);
			ArrayIndex index = getArrayIndexRef();
			Object array = scope.get(index.getArray());
			Array.set(array, index.getArrayIndex(), value);
		} catch (AssertionError ae) { //could be thrown in getArrayIndex
			throw ae;
		} catch (Throwable t) {
			exceptionThrown = t;
		}
		// scope.set(retval, value);
		return exceptionThrown;
	}

	@Override
	public String getCode(Throwable exception) {
		return retval.getName() + " = " + parameter.getName() + ";";
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> vars = new HashSet<VariableReference>();
		vars.add(retval);
		vars.add(parameter);
		vars.add(getArrayIndexRef().getArray());
		if (parameter instanceof ArrayIndex)
			vars.add(((ArrayIndex) parameter).getArray());
		return vars;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#replace(de.unisb.cs.st.evosuite.testcase.VariableReference, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replace(VariableReference var1, VariableReference var2) {
		if (parameter.equals(var1))
			parameter = var2;
		// TODO: ArrayIndex
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + retval.hashCode()
		        + +((parameter == null) ? 0 : parameter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssignmentStatement other = (AssignmentStatement) obj;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.equals(other.parameter))
			return false;
		if (retval == null) {
			if (other.retval != null)
				return false;
		} else if (!retval.equals(other.retval))
			return false;
		return true;
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
		getArrayIndexRef().getArray().loadBytecode(mg, locals);
		mg.push(getArrayIndexRef().getArrayIndex());
		parameter.loadBytecode(mg, locals);
		Class<?> clazz = parameter.getVariableClass();
		if (!clazz.equals(retval.getVariableClass())) {
			mg.cast(org.objectweb.asm.Type.getType(clazz),
			        org.objectweb.asm.Type.getType(retval.getVariableClass()));
		}

		mg.arrayStore(Type.getType(retval.getVariableClass()));
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
		assert (super.isValid());
		parameter.getStPosition();
		return true;
	}

	@Override
	public boolean same(StatementInterface s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		AssignmentStatement other = (AssignmentStatement) s;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.same(other.parameter))
			return false;
		if (retval == null) {
			if (other.retval != null)
				return false;
		} else if (!retval.same(other.retval))
			return false;
		return true;
	}

}
