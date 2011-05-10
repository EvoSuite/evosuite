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

	public VariableReference parameter;

	public AssignmentStatement(VariableReference variable, VariableReference value) {
		this.retval = variable;
		this.parameter = value;
	}

	@Override
	public void adjustVariableReferences(int position, int delta) {
		retval.adjust(delta, position);
		parameter.adjust(delta, position);
		adjustAssertions(position, delta);
	}

	public void setArray(VariableReference array) {
		this.retval = array;
	}

	@Override
	public StatementInterface clone() {
		AssignmentStatement copy = new AssignmentStatement(retval.clone(),
		        parameter.clone());
		return copy;
	}

	@Override
	public Throwable execute(Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {

		try {
			Object value = scope.get(parameter);
			if (retval.array == null) {
				logger.warn("Assigning outside of array");
			}
			Object array = scope.get(retval.array);
			Array.set(array, retval.array_index, value);
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
		if (retval.isArrayIndex())
			vars.add(retval.array);
		if (parameter.isArrayIndex())
			vars.add(parameter.array);
		return vars;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + retval.hashCode()
		        + +((parameter == null) ? 0 : parameter.hashCode());
		return result;
	}

	@Override
	public void replace(VariableReference oldVar, VariableReference newVar) {
		if (retval.equals(oldVar))
			retval = newVar;
		if (parameter.equals(oldVar))
			parameter = newVar;
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
		retval.array.loadBytecode(mg, locals);
		mg.push(retval.array_index);
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
		if (retval.array == old_var)
			retval.array = new_var;
		if (parameter == old_var)
			parameter = new_var;
		if (parameter.array == old_var)
			parameter.array = new_var;
	}
}
