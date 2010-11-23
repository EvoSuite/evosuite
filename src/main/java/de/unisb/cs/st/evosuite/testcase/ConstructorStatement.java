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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import de.unisb.cs.st.evosuite.Properties;

/**
 * This statement represents a constructor call
 * 
 * @author Gordon Fraser
 *
 */
public class ConstructorStatement extends Statement {

	Constructor<?> constructor;

	public List<VariableReference> parameters;	
	
	public ConstructorStatement(Constructor<?> constructor, VariableReference retval, List<VariableReference> parameters) {
		this.constructor = constructor;
		//this.return_type = constructor.getDeclaringClass();
		this.parameters = parameters;
		this.retval = retval;
	}
	
	public Constructor<?> getConstructor() {
		return constructor;
	}
	
	// TODO: Handle inner classes (need instance parameter for newInstance)
	@Override
	public Throwable execute(Scope scope, PrintStream out) throws InvocationTargetException, IllegalArgumentException, InstantiationException, IllegalAccessException {
		PrintStream old_out = System.out;
		PrintStream old_err = System.err;
		System.setOut(out);
		System.setErr(out);
		try {
	        logger.trace("Executing constructor "+constructor.toString());
	        exceptionThrown = null;
			Object[] inputs = new Object[parameters.size()];
			for(int i=0; i<parameters.size(); i++) {
				inputs[i] = scope.get(parameters.get(i));
			}

			Object ret = this.constructor.newInstance(inputs);
			scope.set(retval, ret);

		} catch (Throwable e) {
	          if (e instanceof java.lang.reflect.InvocationTargetException) {
	              e = e.getCause();
		    	  logger.debug("Exception thrown in constructor: "+e);
	          } else
		    	  logger.debug("Exception thrown in constructor: "+e);
        	  exceptionThrown = e;

		} finally {
			System.setOut(old_out);
			System.setErr(old_err);			
		}
		return exceptionThrown;
	}
	
	@Override
	public String getCode(Throwable exception) {
		String parameter_string = "";
		String result = "";
		if(!parameters.isEmpty()) {
			parameter_string += parameters.get(0).getName();
			for(int i=1; i<parameters.size(); i++) {
				parameter_string += ", "+parameters.get(i).getName();
			}
		}
//		String result = ((Class<?>) retval.getType()).getSimpleName() +" "+retval.getName()+ " = null;\n";
		if(exception != null) {
			result = retval.getSimpleClassName() +" "+retval.getName()+ " = null;\n";
			result += "try {\n  ";
		} else {
			result += retval.getSimpleClassName() +" ";
		}
		result += retval.getName()+ " = new " + ClassUtils.getShortClassName(constructor.getDeclaringClass()) + "(" + parameter_string + ");";
		if(exception != null) {
			Class<?> ex = exception.getClass();
			while(!Modifier.isPublic(ex.getModifiers()))
				ex = ex.getSuperclass();
			result += "\n} catch("+ClassUtils.getShortClassName(ex)+" e) {}";
		}
		
		return result;
	}

	@Override
	public Statement clone() {
		ArrayList<VariableReference> new_params = new ArrayList<VariableReference>();
		for(VariableReference r : parameters) {
			new_params.add(r.clone());
		}
		Statement copy = new ConstructorStatement(constructor,
				new VariableReference(retval.getType(), retval.statement),
				new_params);
		copy.assertions = cloneAssertions();
		return copy;
	}

	@Override
	public void adjustVariableReferences(int position, int delta) {
		retval.adjust(delta, position);
		for(VariableReference var : parameters) {
			var.adjust(delta, position);
		}
		adjustAssertions(position, delta);
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new HashSet<VariableReference>();
		references.add(retval);
		references.addAll(parameters);
		for(VariableReference param : parameters) {
			if(param.isArrayIndex())
				references.add(param.array);
		}
		return references;
	}

	@Override
	public boolean equals(Statement s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;
				
		ConstructorStatement ms = (ConstructorStatement)s;
		if(ms.parameters.size() != parameters.size())
			return false;
		
		for(int i=0; i<parameters.size(); i++) {
			if(!parameters.get(i).equals(ms.parameters.get(i)))
				return false;
		}
		
		return retval.equals(ms.retval);
	}

	@Override
	public int hashCode() {
		final int prime = 41;
		int result = 1;
		result = prime * result
				+ ((constructor == null) ? 0 : constructor.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}

	@Override
	public void replace(VariableReference oldVar, VariableReference newVar) {
		if(retval.equals(oldVar))
			retval = newVar;
		for(int i = 0; i<parameters.size(); i++) {
			if(parameters.get(i).equals(oldVar))
				parameters.set(i, newVar);
		}
	}	
	
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.Statement#getBytecode(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void getBytecode(GeneratorAdapter mg) {
		for(VariableReference parameter : parameters) {
			parameter.loadBytecode(mg);
		}
		mg.invokeConstructor(Type.getType(retval.getVariableClass()), Method.getMethod(constructor));
		retval.storeBytecode(mg);
	}
	
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.Statement#getDeclaredExceptions()
	 */
	@Override
	public Set<Class<?>> getDeclaredExceptions() {
		Set<Class<?>> ex = super.getDeclaredExceptions();
		for(Class<?> t : constructor.getExceptionTypes())
			ex.add(t);
		return ex;
	}
}
