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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * Statement that accesses an instance/class field
 * @author Gordon Fraser
 *
 */
public class FieldStatement extends Statement {

	transient Field field;
	VariableReference source;
	//VariableReference ret_val;
	
	String className;
	String fieldName;
	
	private Object readResolve() {
		try {
			Class<?> clazz = Class.forName(className);
			this.field = clazz.getField(fieldName);
		} 
		catch(ClassNotFoundException e) {} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this;
	}
	
	public FieldStatement(Field field, VariableReference source, VariableReference ret_val) {
		this.field = field;
		this.className = field.getDeclaringClass().getName();
		this.fieldName = field.getName();
		this.source = source;
		this.retval = ret_val;
	}

	public VariableReference getSource() {
		return source;
	}
	
	public void setSource(VariableReference source) {
		this.source = source;
	}
	
	@Override
	public boolean isValid() {
		return retval.isAssignableFrom(field.getType());
	}

	@Override
	public String getCode() {
		String cast_str = "";
		if(field == null)
			logger.warn("Field is null: "+className+"."+fieldName);
		if(!retval.getVariableClass().isAssignableFrom(field.getType())) {
			cast_str = "(" + retval.getSimpleClassName()+ ")";
		}

		if(!Modifier.isStatic(field.getModifiers()))
			return retval.getSimpleClassName() +" "+ retval.getName() + " = " + cast_str + source.getName() + "." + field.getName()+";";
		else
			return retval.getSimpleClassName() +" "+ retval.getName() + " = " + cast_str + field.getDeclaringClass().getSimpleName()+"." + field.getName()+";";
	}


	@Override
	public String getCode(Throwable exception) {
		String cast_str = "  ";
		if(!retval.getVariableClass().isAssignableFrom(field.getType())) {
			cast_str += "(" + retval.getSimpleClassName()+ ")";
		}

		
		String result = retval.getSimpleClassName() +" "+ retval.getName() + " = null;\n";
		result += "try {\n";
		if(!Modifier.isStatic(field.getModifiers()))
			result += cast_str + source.getName() + "." + field.getName()+";\n";
		else
			result += cast_str + field.getDeclaringClass().getSimpleName()+"." + field.getName()+";\n";
		result += "} catch("+exception.getClass().getSimpleName()+" e) {}";
		
		return result;
	}
	
	@Override
	public Statement clone() {
		if(Modifier.isStatic(field.getModifiers()))
			return new FieldStatement(field, null, retval.clone());
		else
			return new FieldStatement(field, source.clone(), retval.clone());
	}

	@Override
	public Throwable execute(Scope scope, PrintStream out)
			throws InvocationTargetException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		Object source_object = null;
		if(!Modifier.isStatic(field.getModifiers())) {
			source_object = scope.get(source);
			if(source_object == null) {
		        scope.set(retval, null);
		        return exceptionThrown;
			}
				
		}
		Object ret = field.get(source_object);
        scope.set(retval, ret);
        return exceptionThrown;
	}

	@Override
	public void adjustVariableReferences(int position, int delta) {		
		if(!Modifier.isStatic(field.getModifiers()))
			source.adjust(delta, position);
		retval.adjust(delta, position);
		adjustAssertions(position, delta);
	}

	@Override
	public boolean references(VariableReference var) {
		if(!Modifier.isStatic(field.getModifiers())) {
			if(source.equals(var))
				return true;
			if(source.isArrayIndex() && source.array.equals(var))
				return true;
		}
		return false;
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new HashSet<VariableReference>();
		references.add(retval);
		if(!Modifier.isStatic(field.getModifiers()))
			references.add(source);
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
				
		FieldStatement fs = (FieldStatement)s;
		if(!Modifier.isStatic(field.getModifiers()))
			return source.equals(fs.source) && retval.equals(fs.retval) && field.equals(fs.field);
		else
			return retval.equals(fs.retval) && field.equals(fs.field);
	}

	@Override
	public int hashCode() {
		final int prime = 51;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}
	
	public Field getField() {
		return field;
	}

	@Override
	public void replace(VariableReference oldVar, VariableReference newVar) {
		if(retval.equals(oldVar))
			retval = newVar;
		if(source != null && source.equals(oldVar))
			source = newVar;
		
	}
	
}
