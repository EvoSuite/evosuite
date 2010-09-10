/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Gordon Fraser
 *
 */
public class AssignmentStatement extends Statement {

	VariableReference parameter;

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

	@Override
	public Statement clone() {
		AssignmentStatement copy = new AssignmentStatement(retval.clone(), parameter.clone());
		return copy;
	}


	@Override
	public Throwable execute(Scope scope, PrintStream out)
			throws InvocationTargetException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {

		try {
			Object value = scope.get(parameter);
			if(retval.array == null) {
				logger.warn("Assigning outside of array"); 
			}  
			Object array = scope.get(retval.array);
			Array.set(array, retval.array_index, value);
		}
		catch(Throwable t) {
			exceptionThrown = t;
		}
		//scope.set(retval, value);
		return exceptionThrown;
	}

	@Override
	public String getCode() {
		return retval.getName() + " = " + parameter.getName();
	}

	@Override
	public String getCode(Throwable exception) {
		// TODO
		return null;
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> vars = new HashSet<VariableReference>();
		vars.add(retval);
		vars.add(parameter);
		if(retval.isArrayIndex())
			vars.add(retval.array);
		if(parameter.isArrayIndex())
			vars.add(parameter.array);
		return vars;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + retval.hashCode() +
				+ ((parameter == null) ? 0 : parameter.hashCode());
		return result;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean references(VariableReference var) {
		if(retval.equals(var) || parameter.equals(var))
			return true;	
		if(var.isArray()) {
			if(retval.isArrayIndex()) {
				if(retval.array.equals(var))
					return true;
			}
			if(parameter.isArrayIndex()) {
				if(parameter.array.equals(var))
					return true;
			}
		}
		return false;
	}

	@Override
	public void replace(VariableReference oldVar, VariableReference newVar) {
		if(retval.equals(oldVar))
			retval = newVar;
		if(parameter.equals(oldVar))
			parameter = newVar;
	}

	@Override
	public boolean equals(Statement obj) {
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

}
