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

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Randomness;

/**
 * An array statement creates a new array
 * 
 * @author Gordon Fraser
 * 
 */
public class ArrayStatement extends AbstractStatement {

	private final Randomness randomness = Randomness.getInstance();

	private int length = 0;

	public ArrayStatement(VariableReference ret_val) {
		this.retval = ret_val;
		this.length = randomness.nextInt(Properties.MAX_ARRAY) + 1;
		this.retval.array_length = this.length;
	}

	public ArrayStatement(VariableReference ret_val, int length) {
		this.retval = ret_val;
		this.length = length;
		this.retval.array_length = this.length;
	}

	public int size() {
		return length;
	}

	@Override
	public void adjustVariableReferences(int position, int delta) {
		retval.adjust(delta, position);
		adjustAssertions(position, delta);
	}

	@Override
	public StatementInterface clone() {
		ArrayStatement copy = new ArrayStatement(retval.clone(), length);
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
		scope.set(retval, Array.newInstance((Class<?>) retval.getComponentType(), length));
		return exceptionThrown;

	}

	@Override
	public String getCode(Throwable exception) {
		return retval.getComponentName() + "[] " + retval.getName() + " = new "
		        + retval.getComponentName() + "[" + length + "];";
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new HashSet<VariableReference>();
		references.add(retval);
		return references;
	}

	@Override
	public void replace(VariableReference oldVar, VariableReference newVar) {
		if (retval.equals(oldVar))
			retval = newVar;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#replaceUnique(de.unisb.cs.
	 * st.evosuite.testcase.VariableReference,
	 * de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replaceUnique(VariableReference old_var, VariableReference new_var) {
		if (retval == old_var)
			retval = new_var;
	}
}
