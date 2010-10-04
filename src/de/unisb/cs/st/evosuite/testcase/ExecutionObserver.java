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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class of all execution observers
 * @author Gordon Fraser
 *
 */
public abstract class ExecutionObserver {

	@SuppressWarnings("unchecked")
	protected static final Set<Class> WRAPPER_TYPES = new HashSet<Class>(Arrays.asList(
		    Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class));
	
	protected static boolean isWrapperType(Class<?> clazz) {
		return WRAPPER_TYPES.contains(clazz);
	}
	
	//protected Scope current_scope;
	
	//public abstract void constructorExecuted(Constructor constructor, Object[] parameters, Object ret_val);

	//public abstract void methodExecuted(Method method, Object callee, Object[] parameters, Object ret_val);

	//public abstract void returnValue(int position, Object object);

	public abstract void output(int position, String output);
	
	//public void setScope(Scope scope){
	//	this.current_scope = scope;
	//}

	public abstract void statement(int position, Scope scope, VariableReference retval);
	
	public abstract void clear();
}
