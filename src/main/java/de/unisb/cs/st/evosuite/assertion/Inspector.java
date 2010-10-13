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


package de.unisb.cs.st.evosuite.assertion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Inspector {

	Class<?> clazz;
	Method method;
	
	public Inspector(Class<?> clazz, Method m) {
		this.clazz= clazz;
		method = m;
	}
	
	public Object getValue(Object object) {
		try {
			Object ret = this.method.invoke(object);
			return ret;
			
		} catch (IllegalArgumentException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}
	}
	
	public String getMethodCall() {
		return method.getName();
	}
	
	public String getClassName() {
		return clazz.getName();
	}
	
	public Class<?> getReturnType() {
		return method.getReturnType();
	}
	
}
