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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.objectweb.asm.Type;


public class MethodStatement extends Statement {

	private final Method method;
	VariableReference callee;
	public List<VariableReference> parameters;
	
	public MethodStatement(Method method, VariableReference callee, VariableReference retval, List<VariableReference> parameters) {
		this.method = method;
		this.callee = callee;
		this.retval = retval;
		this.parameters = parameters;
	}
	
	public boolean isValid() {

		Class<?>[] param_types = method.getParameterTypes();
		
		if(parameters.size() != param_types.length)
			return false;
		
		// Check callee
		if(method.getClass().isAssignableFrom((Class<?>) callee.getType()))
			return false;
		
		// Check parameters
		for(int i=0; i<parameters.size(); i++) {
			if(!param_types[i].isInstance(parameters.get(i)))
				return false;
		}
		
		return true;
	}
	
	public Method getMethod() {
		return method;
	}
	
	public VariableReference getCallee() {
		return callee;
	}

	public void setCallee(VariableReference callee) {
		this.callee = callee;
	}
	
	public boolean isStatic() {
		return Modifier.isStatic(method.getModifiers());
	}
	
	private boolean isInstanceMethod() {
		return ! Modifier.isStatic(method.getModifiers());
	}
	
	@Override
	public Throwable execute(Scope scope, PrintStream out) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException {
		logger.trace("Executing method "+method.getName());
        exceptionThrown = null;
        Object[] inputs = new Object[parameters.size()];
		try {
		for(int i=0; i<parameters.size(); i++) {
			inputs[i] = scope.get(parameters.get(i));
			if(inputs[i] == null)
				logger.debug("Null input as parameter "+i+" of method call "+method.getName()+"!");
			//else {
			//	logger.debug("Parameter "+i+": "+inputs[i].getClass()+" - "+inputs[i]);
			//}
			//else {
		}
			Object callee_object = null;
			if(!Modifier.isStatic(method.getModifiers())) {
				callee_object = scope.get(callee);
				if(callee_object == null && logger.isDebugEnabled()) {
					logger.debug("Callee is null in statement "+getCode());
					//logger.debug("Callee: "+callee);
					for(Entry<VariableReference, Object> entry : scope.pool.entrySet()) {
						logger.debug("Pool: "+entry.getKey().statement+", "+entry.getKey().getType()+" : "+entry.getValue());
					}
					
				//} else {
				//	logger.debug("Callee: "+callee_object);
				}
			}
						
			PrintStream old_out = System.out;
			System.setOut(out);
	        Object ret = this.method.invoke(callee_object, inputs);
			System.setOut(old_out);
	        scope.set(retval, ret);
			

		} catch (Throwable e) {
	          if (e instanceof java.lang.reflect.InvocationTargetException) {
	              e = e.getCause();
	          } 
	          logger.debug("Exception thrown in method: "+e);
        	  exceptionThrown = e;
	      }
	      return exceptionThrown;
	}

	@Override
	public String getCode() {
		
		String result = "";
		if(method.getExceptionTypes().length > 0) {
			if(retval.getType() != Void.TYPE) {
				result = retval.getSimpleClassName() +" "+retval.getName() + " = null;\n";	
			}
			result += "try {\n";
		} else {
			if(retval.getType() != Void.TYPE) {
				result = retval.getSimpleClassName() +" "+retval.getName() + " = ";				
			}
		}
		
		String parameter_string = "";
		if(!parameters.isEmpty()) {
			parameter_string += parameters.get(0).getName();
			for(int i=1; i<parameters.size(); i++) {
				parameter_string += ", " + parameters.get(i).getName();
			}
		}
		
		String callee_str = "";
		if(!retval.getVariableClass().isAssignableFrom(method.getReturnType())) {
			callee_str = "(" + retval.getSimpleClassName()+ ")";
		}
		
		if(Modifier.isStatic(method.getModifiers())) {
			callee_str += method.getDeclaringClass().getName();
		} else {
			callee_str += callee.getName();
		}

		if(method.getExceptionTypes().length > 0)
			result += "  ";
		result += callee_str + "." + method.getName() + "(" + parameter_string + ");";

		if(method.getExceptionTypes().length > 0)
			result += "\n}";

		for(Class<?> exception : method.getExceptionTypes()) {
			result += "\ncatch("+exception.getSimpleName()+" e) {} // Declared exception";
		}
		
		return result;

	}
	
	@Override
	public String getCode(Throwable exception) {
		
		String result = "";
		if(retval.getType() != Void.TYPE) {
			result = retval.getSimpleClassName() +" "+retval.getName() + " = null;\n";	
		}
		result += "try {\n";
		
		String parameter_string = "";
		if(!parameters.isEmpty()) {
			parameter_string += parameters.get(0).getName();
			for(int i=1; i<parameters.size(); i++) {
				parameter_string += ", " + parameters.get(i).getName();
			}
		}
		
		String callee_str = "";
		if(!retval.getVariableClass().isAssignableFrom(method.getReturnType())) {
			callee_str = "(" + retval.getSimpleClassName()+ ")";
		}
		
		if(Modifier.isStatic(method.getModifiers())) {
			callee_str += method.getDeclaringClass().getName();
		} else {
			callee_str += callee.getName();
		}
		
		if(retval.getType() == Void.TYPE) {
			result += "  "+callee_str + "." + method.getName() + "(" + parameter_string + ");\n";
		} else {
			result += "  "+retval.getName() + " = " + callee_str + "." + method.getName() + "(" + parameter_string + ");\n";
		}
		
		result += "} catch("+exception.getClass().getSimpleName()+" e) {} // Raised exception";
		
		for(Class<?> ex : method.getExceptionTypes()) {
			if(!ex.equals(exception.getClass()))
				result += "catch("+ex.getSimpleName()+" e) {} // Declared exception";
		}

		return result;
	}

	@Override
	public Statement clone() {
		ArrayList<VariableReference> new_params = new ArrayList<VariableReference>();
		for(VariableReference r : parameters) {
			new_params.add(r.clone());
		}
		
		MethodStatement m;
		if(Modifier.isStatic(method.getModifiers()))
			m = new MethodStatement(method,
					null,
					new VariableReference(retval.getType(), retval.statement),
					new_params);
		else
			m = new MethodStatement(method,
					new VariableReference(callee.getType(), callee.statement),
					new VariableReference(retval.getType(), retval.statement),
					new_params);

		m.assertions = cloneAssertions();
		
		return m;
	}

	@Override
	public void adjustVariableReferences(int position, int delta) {
		if(isInstanceMethod())
			callee.adjust(delta, position);
		retval.adjust(delta, position);
		for(VariableReference var : parameters) {
			var.adjust(delta, position);
		}
		adjustAssertions(position, delta);
	}

	@Override
	public boolean references(VariableReference var) {
		if(isInstanceMethod()) {
			if(callee.equals(var))
				return true;
			if(callee.isArrayIndex() && callee.array.equals(var))
				return true;
		}

		for(VariableReference param : parameters) {
			if(param.equals(var))
				return true;
			if(param.isArrayIndex() && param.array.equals(var))
				return true;
		}
		if(retval.equals(var))
			return true;
		return false;
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new HashSet<VariableReference>();
		references.add(retval);
		if(isInstanceMethod())
			references.add(callee);
		references.addAll(parameters);
		return references;

	}
	
	public String toString() {
		return method.getName()+Type.getMethodDescriptor(method);
	}

	@Override
	public boolean equals(Statement s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;
				
		MethodStatement ms = (MethodStatement)s;
		if(ms.parameters.size() != parameters.size())
			return false;
		
		for(int i=0; i<parameters.size(); i++) {
			if(!parameters.get(i).equals(ms.parameters.get(i)))
				return false;
		}
		
		if(!retval.equals(ms.retval))
			return false;
		
		if((callee == null && ms.callee != null) || (callee != null && ms.callee == null)) {
			return false;		
		} else {
			if(callee == null)
				return true;
			else 
				return (callee.equals(ms.callee));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callee == null) ? 0 : callee.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		if(callee != null && callee.equals(oldVar))
			callee = newVar;	
	}

}
