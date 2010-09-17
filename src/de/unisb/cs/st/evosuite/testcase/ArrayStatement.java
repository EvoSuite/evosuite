/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.ga.Randomness;

/**
 * @author Gordon Fraser
 *
 */
public class ArrayStatement extends Statement {

	private final static int MAX_ARRAY = Properties.getPropertyOrDefault("max.array", 20);

	private Randomness randomness = Randomness.getInstance();

	private int length = 0;
	
	public ArrayStatement(VariableReference ret_val) {
		this.retval = ret_val;
		this.length = randomness.nextInt(MAX_ARRAY);
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
	public Statement clone() {
		ArrayStatement copy = new ArrayStatement(retval.clone(), length);
		return copy;
	}

	@Override
	public boolean equals(Statement s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;
				
		ArrayStatement as = (ArrayStatement)s;
		if (length != as.length)
			return false;
		if (retval.equals(as.retval)) {
			return true;
		} else {
			return false;
		}
		
//		if (!Arrays.equals(variables, other.variables))
//			return false;
		
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
	public String getCode() {
		return retval.getComponentName() + "[] " +retval.getName() + " = new " + retval.getComponentName() + "["+length+"]";	
	}

	@Override
	public String getCode(Throwable exception) {
		// This should not be possible?
		return null;
	}
	
	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new HashSet<VariableReference>();
		references.add(retval);
		return references;
	}


	// TODO: Remove this method alltogether from Statement?
	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean references(VariableReference var) {
		return false;
	}

	@Override
	public void replace(VariableReference oldVar, VariableReference newVar) {
		if(retval.equals(oldVar))
			retval = newVar;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = retval.hashCode();
		result = prime * result + length;
		return result;
	}


}
