/*
 * Copyright (C) 2009 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Javalanche is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This statement represents a constructor call
 * 
 * @author Gordon Fraser
 *
 */
public class ConstructorStatement extends Statement {

	Constructor<?> constructor;
	//Object[] parameters;
	public List<VariableReference> parameters;
	//private VariableReference retval;
	
	/*
	public ConstructorStatement(Gene gene, Scope scope) {
		super(scope);

		assert(gene.receiver_id == 0);
		
		constructor = scope.getConstructor(gene.method_id);
		int num_parameters = constructor.getParameterTypes().length;
		parameters = new Object[num_parameters];
		
		for(int i=0; i<num_parameters; i++) {
			parameters[i] = scope.getElement(gene.parameters[i]);
		}
		
		assert(isValid()); // TODO: Only while developing
	}
	*/
	
	public ConstructorStatement(Constructor<?> constructor, VariableReference retval, List<VariableReference> parameters) {
		this.constructor = constructor;
		//this.return_type = constructor.getDeclaringClass();
		this.parameters = parameters;
		this.retval = retval;
	}
	
	public Constructor<?> getConstructor() {
		return constructor;
	}
	
	public boolean isValid() {

		Class<?>[] param_types = constructor.getParameterTypes();
		
		if(parameters.size() != param_types.length)
			return false;
		
		// Check parameters
		for(int i=0; i<parameters.size(); i++) {
			if(!param_types[i].isInstance(parameters.get(i).getType()))
				return false;
		}
		
		return true;
	}
	
	// TODO: Handle inner classes (need instance parameter for newInstance)
	@Override
	public Throwable execute(Scope scope, PrintStream out) throws InvocationTargetException, IllegalArgumentException, InstantiationException, IllegalAccessException {
		try {
	        logger.trace("Executing constructor "+constructor.toString());
	        exceptionThrown = null;
			Object[] inputs = new Object[parameters.size()];
			for(int i=0; i<parameters.size(); i++) {
				inputs[i] = scope.get(parameters.get(i));
				//System.out.println("Adding parameter of type "+parameters.get(i).getType());
			}
			//System.out.println("TG: Got "+inputs.length+" parameters");
			//for(Object o : inputs) {
			//	System.out.println(o.getClass()+": "+o);
			//}
			PrintStream old_out = System.out;
			System.setOut(out);
			Object ret = this.constructor.newInstance(inputs);
			System.setOut(old_out);
			if(ret == null) {
				logger.warn("Constructor returned null: "+this.constructor);
			}
			//System.out.println("TG: Constructed object");
			scope.set(retval, ret);
			//System.out.println("TG: Added to scope");
			//for(ExecutionObserver obs : observers) {
			//	obs.constructorExecuted(constructor, inputs, ret);
			//}
	      } catch (Throwable e) {
	          if (e instanceof java.lang.reflect.InvocationTargetException) {
	              e = e.getCause();
		    	  logger.debug("Exception thrown in constructor: "+e);
	          } else
		    	  logger.debug("Exception thrown in constructor: "+e);
        	  exceptionThrown = e;

/*
	          Class<?>[] valid_exceptions = constructor.getExceptionTypes();
	          boolean declared = false;
	          for(Class<?> ex : valid_exceptions) {
	        	  if(ex.equals(e.getClass())) {
	        		  //System.out.println("Declared exception thrown in constructor "+this.constructor.getName()+": "+e.getCause());
	        		  //System.exit(1);
	        		  declared = true;
	        		  break;
	        	  }
	          }
	          if(!declared) {
	        	  exceptionThrown = e;
	        	  //System.err.println(e.toString() + " in constructor "+this.constructor.toString());
	          } else {
	        	  logger.debug("Ignoring declared exception "+e.toString() + " in constructor "+this.constructor.toString());	        	  
	          }
	          */
	          //e.printStackTrace();
	          //System.exit(1);

	      }
	      /*
		} catch (InvocationTargetException e) {
			System.out.println("InvocationTargetException in Constructor: "+e.getCause());
			this.exceptionThrown = e.getCause();
			e.printStackTrace();
			System.exit(1);
			//throw e;
		} catch(Throwable e) {
			System.out.println("Exception thrown in constructor: "+e);
			e.printStackTrace();
			System.exit(1);
			*/
		// } finally {
		      //if (retval != null && exceptionThrown != null)
		      //  throw new NotCaughtIllegalStateException("cannot have both retval and exception not null");
		    //}
	      return exceptionThrown;
	}

	@Override
	public String getCode() {
		String parameter_string = new String("");
		if(!parameters.isEmpty()) {
			parameter_string += parameters.get(0).getName();
			for(int i=1; i<parameters.size(); i++) {
				parameter_string += ", "+parameters.get(i).getName();
			}
		}
//		return ((Class<?>) retval.getType()).getSimpleName() +" "+retval.getName()+ " = new " + constructor.getName() + "(" + parameter_string + ")";
		return retval.getSimpleClassName() +" "+retval.getName()+ " = new " + constructor.getName() + "(" + parameter_string + ")";

	}
	
	@Override
	public String getCode(Throwable exception) {
		String parameter_string = new String("");
		if(!parameters.isEmpty()) {
			parameter_string += parameters.get(0).getName();
			for(int i=1; i<parameters.size(); i++) {
				parameter_string += ", "+parameters.get(i).getName();
			}
		}
//		String result = ((Class<?>) retval.getType()).getSimpleName() +" "+retval.getName()+ " = null;\n";
		String result = retval.getSimpleClassName() +" "+retval.getName()+ " = null;\n";
		result += "try {\n";
		result += "  "+ retval.getName()+ " = new " + constructor.getName() + "(" + parameter_string + ");\n";
		result += "} catch("+exception.getClass().getSimpleName()+" e) {}";
		
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
	public boolean references(VariableReference var) {
		for(VariableReference param : parameters) {
			if(param.equals(var))
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
		references.addAll(parameters);
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
	
}
